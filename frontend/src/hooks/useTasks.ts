import { useEffect, useState } from 'react';
import { Task, TaskRequest, TaskStatus } from '../types';
import { fetchTasks, createTask, updateTask, deleteTask, formatError } from '../api';

export function useTasks() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchTasks();
      setTasks(data);
    } catch (e) {
      setError(formatError(e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function add(req: TaskRequest) {
    try {
      const created = await createTask(req);
      setTasks(t => [...t, created]);
      return created;
    } catch (e) {
      setError(formatError(e));
      throw e;
    }
  }

  async function update(id: number, req: TaskRequest) {
    try {
      const updated = await updateTask(id, req);
      setTasks(t => t.map(x => x.id === id ? updated : x));
      return updated;
    } catch (e) {
      setError(formatError(e));
      throw e;
    }
  }

  async function remove(id: number) {
    try {
      await deleteTask(id);
      setTasks(t => t.filter(x => x.id !== id));
    } catch (e) { setError(formatError(e)); }
  }

  function sort(by: 'status' | 'dueDate') {
    setTasks(t => {
      const copy = [...t];
      if (by === 'status') {
        const order: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];
        copy.sort((a,b) => order.indexOf(a.status) - order.indexOf(b.status));
      } else if (by === 'dueDate') {
        copy.sort((a,b) => (a.dueDate || '').localeCompare(b.dueDate || ''));
      }
      return copy;
    });
  }

  return { tasks, loading, error, add, update, remove, reload: load, sort, setError };
}

