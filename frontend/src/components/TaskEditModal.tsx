import React, { useState } from 'react';
import { Task, TaskRequest, TaskStatus } from '../types';

interface Props {
  task: Task;
  onClose: () => void;
  onSave: (id: number, req: TaskRequest) => void;
}

const statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

export const TaskEditModal: React.FC<Props> = ({ task, onClose, onSave }) => {
  const [title, setTitle] = useState(task.title);
  const [description, setDescription] = useState(task.description || '');
  const [status, setStatus] = useState<TaskStatus>(task.status);
  const [dueDate, setDueDate] = useState(task.dueDate || '');
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
    onSave(task.id, { title: title.trim(), description: description.trim() || undefined, status, dueDate: dueDate || undefined });
    onClose();
  }

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h3>Edit Task</h3>
        {errors.length > 0 && <div className="errors">{errors.map(e => <div key={e}>{e}</div>)}</div>}
        <form onSubmit={submit}>
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
          <button type="submit">Save</button>
          <button type="button" onClick={onClose}>Cancel</button>
        </form>
      </div>
    </div>
  );
};

