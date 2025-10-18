import React from 'react';
import { TaskForm } from './components/TaskForm';
import { TaskList } from './components/TaskList';
import { useTasks } from './hooks/useTasks';
import { TaskRequest, Task } from './types';

const App: React.FC = () => {
  const { tasks, add, update, remove, error, sort, setError } = useTasks();

  function handleCreate(req: TaskRequest) {
    add(req);
  }

  // Accept partial updates (e.g., status only) and expand to full TaskRequest for backend validation.
  function handleUpdate(id: number, partial: Partial<Task>) {
    const existing = tasks.find(t => t.id === id);
    if (!existing) return;
    const request: TaskRequest = {
      title: partial.title !== undefined ? partial.title : existing.title,
      description: partial.description !== undefined ? partial.description : existing.description,
      status: partial.status !== undefined ? partial.status : existing.status,
      dueDate: partial.dueDate !== undefined ? partial.dueDate : existing.dueDate
    };
    update(id, request);
  }

  return (
    <div className="container">
      <h1>Task Manager</h1>
      {error && <div className="global-error" onClick={() => setError(null)}>{error}</div>}
      <TaskForm onSubmit={handleCreate} />
      <div className="toolbar">
        <button onClick={() => sort('status')}>Sort by Status</button>
        <button onClick={() => sort('dueDate')}>Sort by Due Date</button>
      </div>
      <TaskList tasks={tasks} onDelete={remove} onUpdate={handleUpdate} onEdit={update} />
    </div>
  );
};

export default App;
