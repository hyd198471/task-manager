import axios from 'axios';
import { Task, TaskRequest } from './types';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

const client = axios.create({ baseURL: API_BASE });

export async function fetchTasks(): Promise<Task[]> {
  const res = await client.get<Task[]>('/tasks');
  return res.data;
}

export async function createTask(req: TaskRequest): Promise<Task> {
  const res = await client.post<Task>('/tasks', req);
  return res.data;
}

export async function updateTask(id: number, req: TaskRequest): Promise<Task> {
  const res = await client.put<Task>(`/tasks/${id}`, req);
  return res.data;
}

export async function deleteTask(id: number): Promise<void> {
  await client.delete(`/tasks/${id}`);
}

export function formatError(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const data = e.response?.data;
    if (data?.fieldErrors) {
      return Object.entries(data.fieldErrors).map(([f, m]) => `${f}: ${m}`).join('\n');
    }
    return data?.error || e.message;
  }
  return (e as any)?.message || 'Unknown error';
}

