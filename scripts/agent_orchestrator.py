#!/usr/bin/env python3
"""
Agent Orchestrator (Pattern A)

- Fetches schema from MCP (`GET /api/mcp/mcp-schema-tasks`)
- Either (mode="faker") calls server-side generator (`POST /api/mcp/generate?count=N`) or (mode="llm")
  requests an LLM (OpenAI) to generate batches of tasks, validates them and POSTs to
  `POST /api/mcp/mcp-tasks` in batches.

Usage examples:
# Use server-side faker (no OpenAI key required)
MCP_BASE=http://localhost:8080/api/mcp python3 scripts/agent_orchestrator.py --mode faker --count 1000

# Use OpenAI (set OPENAI_API_KEY)
OPENAI_API_KEY=sk-... MCP_BASE=http://localhost:8080/api/mcp MCP_TOKEN=dev-secret \
  python3 scripts/agent_orchestrator.py --mode llm --count 1000 --batch 200

Requirements (install locally):
python3 -m pip install requests openai python-dateutil
"""

import os
import json
import math
import time
import argparse
import random
from urllib.parse import urlencode

import requests
from dateutil.parser import parse as parse_date

try:
    import openai
except Exception:
    openai = None

MCP_BASE = os.environ.get('MCP_BASE', 'http://localhost:8080/api/mcp')
MCP_TOKEN = os.environ.get('MCP_TOKEN')
OPENAI_API_KEY = os.environ.get('OPENAI_API_KEY')

HEADERS = {'Content-Type': 'application/json'}
if MCP_TOKEN:
    HEADERS['X-MCP-Token'] = MCP_TOKEN

DEFAULT_MODEL = 'gpt-4o-mini'  # change if needed


def get_schema():
    r = requests.get(f"{MCP_BASE}/mcp-schema-tasks", headers=HEADERS, timeout=30)
    r.raise_for_status()
    return r.json()


def server_side_generate(count):
    # Calls the server-side convenience endpoint which uses javafaker
    r = requests.post(f"{MCP_BASE}/generate?count={count}", headers=HEADERS, timeout=300)
    r.raise_for_status()
    return r.json()


def validate_task(obj, schema):
    props = schema.get('properties', {})
    status_enum = props.get('status', {}).get('enum', ['TODO', 'IN_PROGRESS', 'DONE'])
    title_max = props.get('title', {}).get('maxLength', 100)
    desc_max = props.get('description', {}).get('maxLength', 500)

    if 'title' not in obj or not isinstance(obj['title'], str) or len(obj['title'].strip()) == 0:
        return False, 'title missing or invalid'
    if len(obj['title']) > title_max:
        return False, 'title too long'
    if 'description' in obj and obj['description'] is not None:
        if not isinstance(obj['description'], str) or len(obj['description']) > desc_max:
            return False, 'description invalid or too long'
    if 'status' in obj:
        if obj['status'] not in status_enum:
            return False, f"invalid status: {obj['status']}"
    else:
        obj['status'] = 'TODO'
    if 'dueDate' in obj and obj['dueDate'] is not None:
        try:
            # ensure parseable
            parse_date(obj['dueDate'])
        except Exception:
            return False, 'dueDate invalid'
    return True, None


def call_openai_generate(schema, n, model=DEFAULT_MODEL, system_msg=None):
    if openai is None:
        raise RuntimeError('openai package not installed; pip install openai')
    if OPENAI_API_KEY is None:
        raise RuntimeError('OPENAI_API_KEY not set in environment')
    # Prepare messages
    if system_msg is None:
        system_msg = 'You are a JSON-only generator. Output only valid JSON arrays.'

    user_msg = (
        f"Schema: {json.dumps(schema)}\n"
        f"Produce {n} Task objects as a compact JSON array matching the schema.\n"
        "Each task must have: title (<=100 chars), description (<=500 chars, optional),\n"
        "status (one of TODO, IN_PROGRESS, DONE), dueDate optional YYYY-MM-DD.\n"
        "Return ONLY the JSON array (no surrounding explanation)."
    )

    # Prefer the new openai>=1.0.0 client when available.
    if hasattr(openai, 'OpenAI'):
        try:
            client = openai.OpenAI(api_key=OPENAI_API_KEY)
            resp = client.chat.completions.create(
                model=model,
                messages=[{'role': 'system', 'content': system_msg}, {'role': 'user', 'content': user_msg}],
                temperature=0.2,
                max_tokens=20000,
            )
            # resp may be an object with attribute access or a dict-like
            try:
                text = resp.choices[0].message.content
            except Exception:
                try:
                    text = resp['choices'][0]['message']['content']
                except Exception:
                    # Last resort: stringify the response
                    text = str(resp)
            return text
        except Exception as e:
            # Surface the error for clarity rather than falling back to the removed API shim.
            raise RuntimeError(f'OpenAI client (new API) request failed: {e}')

    # Legacy client (openai<1.0.0) — only use if OpenAI() is not available.
    if hasattr(openai, 'ChatCompletion'):
        openai.api_key = OPENAI_API_KEY
        resp = openai.ChatCompletion.create(
            model=model,
            messages=[{'role': 'system', 'content': system_msg}, {'role': 'user', 'content': user_msg}],
            temperature=0.2,
            max_tokens=20000,
        )
        text = resp['choices'][0]['message']['content']
        return text

    raise RuntimeError('OpenAI client does not expose a usable ChatCompletion API. Install an appropriate openai package version.')


def parse_json_safe(text):
    try:
        return json.loads(text)
    except Exception:
        a = text.find('[')
        b = text.rfind(']')
        if a != -1 and b != -1 and b > a:
            try:
                return json.loads(text[a:b+1])
            except Exception:
                pass
    raise ValueError('Could not parse LLM output as JSON array')


def post_batch(batch):
    r = requests.post(f"{MCP_BASE}/mcp-tasks", headers=HEADERS, json=batch, timeout=300)
    r.raise_for_status()
    return r.json()


def orchestrate_llm(schema, total, batch_size, model):
    batches = math.ceil(total / batch_size)
    inserted_total = 0
    for i in range(batches):
        to_generate = batch_size if i < batches - 1 else (total - i*batch_size)
        print(f'Generating batch {i+1}/{batches} size {to_generate}')
        # Try generating with the LLM, with a few retries for transient rate limits.
        raw = None
        max_retries = 3
        for attempt in range(1, max_retries + 1):
            try:
                raw = call_openai_generate(schema, to_generate, model=model)
                break
            except Exception as e:
                msg = str(e)
                print(f'LLM batch generation failed (attempt {attempt}/{max_retries}):', msg)
                # If it's a quota/insufficient_quota error, don't retry — fall back.
                if 'quota' in msg.lower() or 'insufficient_quota' in msg.lower() or 'rate limit' in msg.lower():
                    print('Detected quota or rate-limit issue; switching to server-side faker for remaining tasks.')
                    remaining = total - inserted_total
                    if remaining > 0:
                        inserted = orchestrate_faker(remaining)
                        inserted_total += inserted
                    return inserted_total
                # transient backoff
                if attempt < max_retries:
                    backoff = 2 ** attempt
                    print(f'Waiting {backoff}s before retrying...')
                    time.sleep(backoff)
                else:
                    print('Max retries reached. Switching to server-side faker for remaining tasks.')
                    remaining = total - inserted_total
                    if remaining > 0:
                        inserted = orchestrate_faker(remaining)
                        inserted_total += inserted
                    return inserted_total
        try:
            items = parse_json_safe(raw)
        except Exception as e:
            print('Failed to parse LLM output as JSON:', e)
            print('LLM output excerpt:', raw[:1000])
            raise
        # validate
        valid_items = []
        for obj in items:
            ok, err = validate_task(obj, schema)
            if not ok:
                # attempt mild fix (truncate title/description)
                if 'title' in obj and isinstance(obj['title'], str) and len(obj['title']) > schema.get('properties', {}).get('title', {}).get('maxLength', 100):
                    obj['title'] = obj['title'][:schema.get('properties', {}).get('title', {}).get('maxLength', 100)]
                    ok, err = validate_task(obj, schema)
                if not ok:
                    print('Skipping invalid item:', err)
                    continue
            valid_items.append(obj)
        print(f'Posting {len(valid_items)} validated items')
        resp = post_batch(valid_items)
        inserted_total += len(resp)
        print('Inserted this batch:', len(resp), 'Inserted total:', inserted_total)
        time.sleep(0.2)
    return inserted_total


def orchestrate_faker(total):
    # server-side convenience endpoint generates and inserts
    print('Requesting server-side generation of', total, 'tasks')
    resp = server_side_generate(total)
    print('Server inserted:', len(resp))
    return len(resp)


def get_summary():
    r = requests.get(f"{MCP_BASE}/mcp-tasks-summary", headers=HEADERS, timeout=30)
    r.raise_for_status()
    return r.json()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--mode', choices=['llm', 'faker'], default='faker', help='generation mode')
    ap.add_argument('--count', type=int, default=1000)
    ap.add_argument('--batch', type=int, default=200)
    ap.add_argument('--model', type=str, default=DEFAULT_MODEL, help='OpenAI model to use (LLM mode)')
    args = ap.parse_args()

    print('Fetching schema...')
    schema = get_schema()
    print('Schema keys:', list(schema.get('properties', {}).keys()))

    if args.mode == 'faker':
        inserted = orchestrate_faker(args.count)
    else:
        # pass model through to generation calls
        inserted = orchestrate_llm(schema, args.count, args.batch, args.model)

    print('Fetching summary...')
    summary = get_summary()
    print('Summary:', json.dumps(summary, indent=2))
    print('Done. Inserted (reported):', inserted)


if __name__ == '__main__':
    main()
