import React from 'react';
import { describe, it, expect, vi } from 'vitest';
// Prevent tests from making real network requests or starting external services by mocking the API layer.
vi.mock('../api', () => ({
  fetchTasks: vi.fn().mockResolvedValue([]),
  createTask: vi.fn().mockImplementation((req) => Promise.resolve({ id: Date.now(), ...req })),
  updateTask: vi.fn().mockImplementation((id, req) => Promise.resolve({ id, ...req })),
  deleteTask: vi.fn().mockResolvedValue(undefined),
  formatError: (e: any) => (e?.response?.data?.error ?? String(e))
}));
import { render, screen, fireEvent, cleanup, waitFor } from '@testing-library/react';
import { afterEach } from 'vitest';
import '@testing-library/jest-dom/vitest';
import App from '../App';
import { TaskForm } from '../components/TaskForm';
import { TaskList } from '../components/TaskList';
import { TaskEditModal } from '../components/TaskEditModal';

afterEach(() => cleanup());

describe('App basic render', () => {
  it('renders heading', () => {
    render(React.createElement(App));
    expect(screen.getByText(/Task Manager/i)).toBeInTheDocument();
  });
});

describe('TaskForm validation', () => {
  it('shows required title error when submitting empty', async () => {
    const onSubmit = vi.fn();
    const { container } = render(React.createElement(TaskForm, { onSubmit }));

    const addBtn = screen.getByRole('button', { name: /add/i });
    fireEvent.click(addBtn);

    // handler should not be called and an error UI node should appear
    expect(onSubmit).not.toHaveBeenCalled();
    await waitFor(() => expect(container.querySelector('.field-error') || container.querySelector('.errors')).toBeTruthy());
  });

  it('shows length error for long title', async () => {
    const onSubmit = vi.fn();
    const { container } = render(React.createElement(TaskForm, { onSubmit }));

    const titleInput = screen.getByLabelText(/Title/i) as HTMLInputElement;
    const addBtn = screen.getByRole('button', { name: /add/i });

    const long = 'x'.repeat(120);
    fireEvent.change(titleInput, { target: { value: long } });
    fireEvent.click(addBtn);

    expect(onSubmit).not.toHaveBeenCalled();
    await waitFor(() => expect(container.querySelector('.field-error') || container.querySelector('.errors')).toBeTruthy());
  });
});

describe('TaskList and TaskEditModal behavior', () => {
  const sampleTask = { id: 1, title: 'T1', description: 'D1', status: 'TODO', dueDate: '2025-12-01' };

  it('renders tasks and respects loading state', () => {
    const del = vi.fn();
    const upd = vi.fn();
    const edit = vi.fn();
    render(React.createElement(TaskList, { tasks: [sampleTask], onDelete: del, onUpdate: upd, onEdit: edit, loading: true }));

    // heading and count
    expect(screen.getByText(/Tasks \(1\)/i)).toBeInTheDocument();
    // select and buttons should be disabled when loading
    const select = screen.getByDisplayValue(/TODO/i) as HTMLSelectElement;
    expect(select).toBeDisabled();
    expect(screen.getByRole('button', { name: /edit/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /delete/i })).toBeDisabled();
  });

  it('opens edit modal and closes on cancel', async () => {
    const del = vi.fn();
    const upd = vi.fn();
    const edit = vi.fn();
    const { getByRole, findByText, queryByText } = render(React.createElement(TaskList, { tasks: [sampleTask], onDelete: del, onUpdate: upd, onEdit: edit }));

    const editBtn = getByRole('button', { name: /edit/i });
    fireEvent.click(editBtn);

    // Modal should appear
    expect(await findByText(/Edit Task/i)).toBeInTheDocument();

    // Click cancel to close
    const cancel = getByRole('button', { name: /cancel/i });
    fireEvent.click(cancel);

    // modal should be removed
    expect(queryByText(/Edit Task/i)).not.toBeInTheDocument();
  });

  it('validates TaskEditModal title required', async () => {
    const onClose = vi.fn();
    const onSave = vi.fn();
    const { container } = render(React.createElement(TaskEditModal, { task: sampleTask, onClose, onSave }));

    const titleInput = screen.getByLabelText(/Title/i) as HTMLInputElement;
    const saveBtn = screen.getByRole('button', { name: /save/i });

    fireEvent.change(titleInput, { target: { value: '' } });
    fireEvent.click(saveBtn);

    expect(onSave).not.toHaveBeenCalled();
    await waitFor(() => expect(container.querySelector('.field-error') || container.querySelector('.errors')).toBeTruthy());
  });
});
