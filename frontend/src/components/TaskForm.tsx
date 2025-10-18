import React, { useState } from 'react';
import { TaskStatus, TaskRequest } from '../types';

interface Props {
  onSubmit: (req: TaskRequest) => void;
}

const statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

export const TaskForm: React.FC<Props> = ({ onSubmit }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<TaskStatus>('TODO');
  const [dueDate, setDueDate] = useState('');
  const [errors, setErrors] = useState<string[]>([]);

  function validate(): boolean {
    const errs: string[] = [];
    if (!title.trim()) errs.push('Title is required');
    if (title.length > 100) errs.push('Title must be <= 100 characters');
    if (description.length > 500) errs.push('Description must be <= 500 characters');
    setErrors(errs);
    return errs.length === 0;
  }

  function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    onSubmit({ title: title.trim(), description: description.trim() || undefined, status, dueDate: dueDate || undefined });
    setTitle(''); setDescription(''); setStatus('TODO'); setDueDate(''); setErrors([]);
  }

  return (
    <form onSubmit={submit} className="task-form">
      <h3>Create Task</h3>
      {errors.length > 0 && <div className="errors">{errors.map(e => <div key={e}>{e}</div>)}</div>}
      <div>
        <label>Title</label>
        <input value={title} onChange={e => setTitle(e.target.value)} required maxLength={100} />
      </div>
      <div>
        <label>Description</label>
        <textarea value={description} onChange={e => setDescription(e.target.value)} maxLength={500} />
      </div>
      <div>
        <label>Status</label>
        <select value={status} onChange={e => setStatus(e.target.value as TaskStatus)}>
          {statuses.map(s => <option key={s}>{s}</option>)}
        </select>
      </div>
      <div>
        <label>Due Date</label>
        <input type="date" value={dueDate} onChange={e => setDueDate(e.target.value)} />
      </div>
      <button type="submit">Add</button>
    </form>
  );
};

