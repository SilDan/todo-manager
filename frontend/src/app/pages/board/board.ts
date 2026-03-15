import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  DragDropModule,
  CdkDrag,
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem,
} from '@angular/cdk/drag-drop';

import { TodoService, Task, Status } from '../../api/todos';

@Component({
  selector: 'app-board',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule],
  templateUrl: './board.html',
  styleUrls: ['./board.scss'],
})
export class Board implements OnInit {

  newTitle = '';
  newDescription = '';

  /** Flat list from backend (optional but useful for debugging / future features). */
  tasks: Task[] = [];

  /** Stable arrays for CDK Drag&Drop (must NOT be getters that create new arrays). */
  todoTasks: Task[] = [];
  inProgressTasks: Task[] = [];
  doneTasks: Task[] = [];
  editTasks: Task[] = [];
  trashData: Task[] = [];

  constructor(private readonly todoService: TodoService, private readonly cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.reload();
  }

  private reload(): void {
    this.todoService.getTodos().subscribe(todos => {
      const mapped = todos.map(t => ({ ...t, description: t.description ?? '' }));

      this.tasks = mapped;
      this.todoTasks = mapped.filter(t => t.status === 'TODO');
      this.inProgressTasks = mapped.filter(t => t.status === 'IN_PROGRESS');
      this.doneTasks = mapped.filter(t => t.status === 'DONE');
      this.trashData = [];
      this.editTasks = [];
      this.cdr.detectChanges();
    });
  }

  add(): void {
    const title = this.newTitle.trim();
    const description = this.newDescription.trim();

    if (!title) {
      return;
    }

    this.todoService.createTodo(title, description).subscribe(() => this.reload());
    this.newTitle = '';
    this.newDescription = '';
  }

  move(id: string, status: Status): void {
    this.todoService.updateTodo(id, status).subscribe({
      next: () => this.reload(),
      error: (err) => {
        console.error('Update status failed', err);
        this.reload();
      }
    });
  }

  /**
   * Handle drag & drop:
   * - same container: reorder only
   * - different container: move item + update status in backend
   */
  drop(event: CdkDragDrop<Task[]>, targetStatus: Status): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex
    );

    const movedTask = event.container.data[event.currentIndex];
    this.move(movedTask.id, targetStatus);
  }

  trackById(_: number, t: Task): string {
    return t.id;
  }

  canDropToTrash = (drag: CdkDrag<Task>, _drop: unknown): boolean => {
    return !!drag.data?.id;
  };

  trashDropped(event: CdkDragDrop<Task[]>): void {
    const task = event.item.data as Task | undefined;
    if (!task?.id) {
      return;
    }

    const ok = confirm(`"${task.title}" really delete?`);
    if (!ok) {
      this.reload();
      return;
    }

    this.removeFromColumns(task.id);

    this.todoService.deleteTodo(task.id).subscribe({
      next: () => this.reload(),
      error: (err) => {
        console.error('Delete failed', err);
        this.reload();
        alert('Delete failed - item was restored to board');
      },
    });
  }

  private removeFromColumns(id: string): void {
    this.todoTasks = this.todoTasks.filter(t => t.id !== id);
    this.inProgressTasks = this.inProgressTasks.filter(t => t.id !== id);
    this.doneTasks = this.doneTasks.filter(t => t.id !== id);
  }

  dropToEditArea(event: CdkDragDrop<Task[]>): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex
    );
  }

  saveEditTaskTitle(task: Task, event: Event): void {
    const element = event.target as HTMLElement;
    const newTitle = (element.textContent ?? '').trim();

    if (!newTitle) {
      element.textContent = task.title;
      return;
    }

    if (newTitle === task.title) {
      return;
    }

    this.todoService.updateTitle(task.id, newTitle).subscribe({
      next: updatedTask => {
        this.updateTaskInAllLists(updatedTask);
        element.textContent = updatedTask.title;
      },
      error: () => {
        element.textContent = task.title;
        this.reload();
      }
    });
  }

  saveEditTaskDescription(task: Task, event: Event): void {
    const element = event.target as HTMLElement;
    const newDescription = (element.textContent ?? '').trim();
    const currentDescription = task.description ?? '';

    if (newDescription === currentDescription) {
      element.textContent = currentDescription;
      return;
    }

    this.todoService.updateDescription(task.id, newDescription).subscribe({
      next: updatedTask => {
        this.updateTaskInAllLists(updatedTask);
        element.textContent = updatedTask.description ?? '';
      },
      error: () => {
        element.textContent = currentDescription;
        this.reload();
      }
    });
  }

  startEditingTitle(event: Event): void {
    event.stopPropagation();
  }

  finishEdit(event: Event, task: Task): void {
    event.preventDefault();
    this.saveEditTaskTitle(task, event);
    (event.target as HTMLElement).blur();
  }

  finishDescriptionEdit(event: Event, task: Task): void {
    event.preventDefault();
    this.saveEditTaskDescription(task, event);
    (event.target as HTMLElement).blur();
  }

  formatProcessingTime(totalSeconds: number): string {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }

    if (minutes > 0) {
      return `${minutes}m ${seconds}s`;
    }

    return `${seconds}s`;
  }

  private updateTaskInAllLists(updatedTask: Task): void {
    const allLists = [
      this.todoTasks,
      this.inProgressTasks,
      this.doneTasks,
      this.editTasks
    ];

    for (const list of allLists) {
      const existingTask = list.find(task => task.id === updatedTask.id);
      if (existingTask) {
        existingTask.title = updatedTask.title;
        existingTask.description = updatedTask.description ?? '';
        existingTask.status = updatedTask.status;
        existingTask.activeSessionBeginTime = updatedTask.activeSessionBeginTime;
        existingTask.totalProcessingTimeSeconds = updatedTask.totalProcessingTimeSeconds;
      }
    }
  }
}
