import React, { useState } from 'react';
import { TaskStatus, TaskRequest } from '../types';

interface Props {
  onSubmit: (req: TaskRequest) => void;
  loading?: boolean;
}

const statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

export const TaskForm: React.FC<Props> = ({ onSubmit, loading = false }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<TaskStatus>('TODO');
  const [dueDate, setDueDate] = useState('');
  const [errors, setErrors] = useState<Record<string,string>>({});

  function validate(): boolean {
    const errs: Record<string,string> = {};
    if (!title.trim()) errs.title = 'Title is required';
    if (title.length > 100) errs.title = 'Title must be <= 100 characters';
    if (description.length > 500) errs.description = 'Description must be <= 500 characters';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErrors({});
    if (!validate()) return;
    try {
      await onSubmit({ title: title.trim(), description: description.trim() || undefined, status, dueDate: dueDate || undefined });
      setTitle(''); setDescription(''); setStatus('TODO'); setDueDate(''); setErrors({});
    } catch (err: any) {
      // Try to extract fieldErrors from backend response
      const fieldErrors = err?.response?.data?.fieldErrors;
      if (fieldErrors && typeof fieldErrors === 'object') {
        setErrors(fieldErrors);
      } else {
        setErrors({ global: err?.message || 'Unknown error' });
      }
    }
  }

  return (
    <form onSubmit={submit} className="task-form" noValidate>
      <h3>Create Task</h3>
      {Object.keys(errors).length > 0 && (
        <div className="errors">
          {errors.global && <div key="global">{errors.global}</div>}
          {errors.title && <div key="title">Title: {errors.title}</div>}
          {errors.description && <div key="desc">Description: {errors.description}</div>}
        </div>
      )}
      <div>
        <label htmlFor="task-title">Title</label>
        <input id="task-title" value={title} onChange={e => setTitle(e.target.value)} required maxLength={100} disabled={loading} />
        {errors.title && <div className="field-error">{errors.title}</div>}
      </div>
      <div>
        <label htmlFor="task-desc">Description</label>
        <textarea id="task-desc" value={description} onChange={e => setDescription(e.target.value)} maxLength={500} disabled={loading} />
        {errors.description && <div className="field-error">{errors.description}</div>}
      </div>
      <div>
        <label htmlFor="task-status">Status</label>
        <select id="task-status" value={status} onChange={e => setStatus(e.target.value as TaskStatus)}>
          {statuses.map(s => <option key={s}>{s}</option>)}
        </select>
      </div>
      <div>
        <label htmlFor="task-due">Due Date</label>
        <input id="task-due" type="date" value={dueDate} onChange={e => setDueDate(e.target.value)} />
      </div>
      <button type="submit" disabled={loading}>Add</button>
    </form>
  );
};

