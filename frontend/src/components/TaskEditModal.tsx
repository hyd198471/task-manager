import React, { useState } from 'react';
import { Task, TaskRequest, TaskStatus } from '../types';

interface Props {
  task: Task;
  onClose: () => void;
  onSave: (id: number, req: TaskRequest) => void;
  loading?: boolean;
}

const statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

export const TaskEditModal: React.FC<Props> = ({ task, onClose, onSave, loading = false }) => {
  const [title, setTitle] = useState(task.title);
  const [description, setDescription] = useState(task.description || '');
  const [status, setStatus] = useState<TaskStatus>(task.status);
  const [dueDate, setDueDate] = useState(task.dueDate || '');
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
      await onSave(task.id, { title: title.trim(), description: description.trim() || undefined, status, dueDate: dueDate || undefined });
      onClose();
    } catch (err: any) {
      const fieldErrors = err?.response?.data?.fieldErrors;
      if (fieldErrors && typeof fieldErrors === 'object') {
        setErrors(fieldErrors);
      } else {
        setErrors({ global: err?.message || 'Unknown error' });
      }
    }
  }

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h3>Edit Task</h3>
        {Object.keys(errors).length > 0 && (
          <div className="errors">
            {errors.global && <div key="global">{errors.global}</div>}
            {errors.title && <div key="title">Title: {errors.title}</div>}
            {errors.description && <div key="desc">Description: {errors.description}</div>}
          </div>
        )}
        <form onSubmit={submit} noValidate>
            <div>
              <label htmlFor="edit-title">Title</label>
              <input id="edit-title" value={title} onChange={e => setTitle(e.target.value)} required maxLength={100} disabled={loading} />
              {errors.title && <div className="field-error">{errors.title}</div>}
            </div>
            <div>
              <label htmlFor="edit-desc">Description</label>
              <textarea id="edit-desc" value={description} onChange={e => setDescription(e.target.value)} maxLength={500} disabled={loading} />
              {errors.description && <div className="field-error">{errors.description}</div>}
            </div>
            <div>
              <label htmlFor="edit-status">Status</label>
              <select id="edit-status" value={status} onChange={e => setStatus(e.target.value as TaskStatus)}>
                {statuses.map(s => <option key={s}>{s}</option>)}
              </select>
            </div>
            <div>
              <label htmlFor="edit-due">Due Date</label>
              <input id="edit-due" type="date" value={dueDate} onChange={e => setDueDate(e.target.value)} />
            </div>
          <button type="submit" disabled={loading}>Save</button>
          <button type="button" onClick={onClose} disabled={loading}>Cancel</button>
        </form>
      </div>
    </div>
  );
};

