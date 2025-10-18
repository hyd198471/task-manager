import React, { useState } from 'react';
import { Task, TaskStatus } from '../types';
import { TaskEditModal } from './TaskEditModal';

interface Props {
  tasks: Task[];
  onDelete: (id: number) => void;
  onUpdate: (id: number, req: Partial<Task>) => void;
  onEdit: (id: number, req: any) => void;
}

export const TaskList: React.FC<Props> = ({ tasks, onDelete, onUpdate, onEdit }) => {
  const [editing, setEditing] = useState<Task | null>(null);

  function changeStatus(task: Task, status: TaskStatus) {
    onUpdate(task.id, { status });
  }

  return (
    <div className="task-list">
      <h3>Tasks ({tasks.length})</h3>
      {tasks.length === 0 && <div>No tasks yet.</div>}
      <ul>
        {tasks.map(t => (
          <li key={t.id} className={`task status-${t.status.toLowerCase()}`}>
            <div className="row">
              <strong>{t.title}</strong> <small>#{t.id}</small>
            </div>
            {t.description && <div className="desc">{t.description}</div>}
            <div className="meta">
              <label>Status: </label>
              <select value={t.status} onChange={e => changeStatus(t, e.target.value as TaskStatus)}>
                <option>TODO</option>
                <option>IN_PROGRESS</option>
                <option>DONE</option>
              </select>
              {t.dueDate && <span className="due">Due: {t.dueDate}</span>}
            </div>
            <div className="actions">
              <button onClick={() => setEditing(t)}>Edit</button>
              <button onClick={() => onDelete(t.id)}>Delete</button>
            </div>
          </li>
        ))}
      </ul>
      {editing && <TaskEditModal task={editing} onClose={() => setEditing(null)} onSave={(id, req) => onEdit(id, req)} />}
    </div>
  );
};

