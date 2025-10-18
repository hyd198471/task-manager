export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface Task {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus;
  dueDate?: string; // ISO date (yyyy-MM-dd)
}

export interface TaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  dueDate?: string;
}

