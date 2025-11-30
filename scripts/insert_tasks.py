#!/usr/bin/env python3
"""
Generate N tasks and POST to /api/mcp/mcp-tasks
Usage: python3 scripts/insert_tasks.py [count]
"""
import sys
import json
import random
import datetime
from urllib import request

COUNT = int(sys.argv[1]) if len(sys.argv) > 1 else 1000
URL = 'http://localhost:8080/api/mcp/mcp-tasks'

statuses = ['TODO', 'IN_PROGRESS', 'DONE']

def rand_title():
    words = ["alpha","beta","gamma","task","do","build","review","update","fix","test","deploy"]
    return ' '.join(random.choices(words, k=random.randint(2,5)))[:100]

def rand_description():
    words = ["lorem","ipsum","dolor","sit","amet","consectetur","adipiscing","elit","feature","backend","frontend","refactor","optimize"]
    return ' '.join(random.choices(words, k=random.randint(10,50)))[:500]

def rand_due_date():
    if random.random() < 0.2:
        return None
    days = random.randint(1,90)
    d = datetime.date.today() + datetime.timedelta(days=days)
    return d.isoformat()

batch = []
for i in range(COUNT):
    obj = {
        'title': rand_title().capitalize(),
        'description': rand_description(),
        'status': random.choice(statuses),
    }
    dd = rand_due_date()
    if dd:
        obj['dueDate'] = dd
    batch.append(obj)

data = json.dumps(batch).encode('utf-8')
req = request.Request(URL, data=data, headers={'Content-Type':'application/json'})
print(f"Posting {COUNT} tasks to {URL}...")
with request.urlopen(req, timeout=300) as resp:
    body = resp.read().decode('utf-8')
    print('Response received. Length:', len(body))
    # try to parse JSON
    try:
        parsed = json.loads(body)
        print('Inserted:', len(parsed))
    except Exception as e:
        print('Could not parse response as JSON:', e)
        print(body)
