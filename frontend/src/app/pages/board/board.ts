import {ChangeDetectorRef, Component, OnInit } from '@angular/core';
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

  /** Flat list from backend (optional but useful for debugging / future features). */
  tasks: Task[] = [];

  /** Stable arrays for CDK Drag&Drop (must NOT be getters that create new arrays). */
  todoTasks: Task[] = [];
  inProgressTasks: Task[] = [];
  doneTasks: Task[] = [];
  trashData: Task[] = []; 

  constructor(private readonly todoService: TodoService, private readonly cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.reload();
  }

  private reload(): void {
    this.todoService.getTodos().subscribe(todos => {
      // Backend liefert kein description -> default setzen
      const mapped = todos.map(t => ({ ...t, description: (t as any).description ?? '' }));

      this.tasks = mapped;

      // build stable lists for DnD
      this.todoTasks = mapped.filter(t => t.status === 'TODO');
      this.inProgressTasks = mapped.filter(t => t.status === 'IN_PROGRESS');
      this.doneTasks = mapped.filter(t => t.status === 'DONE');

      this.cdr.detectChanges(); // Force update to avoid ExpressionChangedAfterItHasBeenCheckedError after async update
    });
  }

  add(): void {
    const title = this.newTitle.trim();
    if (!title) return;

    this.todoService.createTodo(title).subscribe(() => this.reload());
    this.newTitle = '';
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

    // IMPORTANT: status must become the target column's status
    this.move(movedTask.id, targetStatus);
  }

  trackById(_: number, t: Task): string {
    return t.id;
  }

  // Only allow drops if there is actual drag data
canDropToTrash = (drag: CdkDrag<Task>, _drop: any) => {
  return !!drag.data?.id;
};

trashDropped(event: CdkDragDrop<Task[]>): void {
  const task = event.item.data as Task | undefined;
  if (!task?.id) return;

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
}

