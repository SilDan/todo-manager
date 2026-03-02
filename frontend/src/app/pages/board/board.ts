import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TodoService, Task , Status } from '../../api/todos';

@Component({
  selector: 'app-board',
  imports: [CommonModule, FormsModule],
  templateUrl: './board.html',
  styleUrls: ['./board.scss'],
})
export class Board implements OnInit {

  newTitle= '';

  tasks: Task[] = [
   //{ id: crypto.randomUUID(), title: 'Task 1', description: 'Description for Task 1', status: 'TODO' }
  ];

  constructor(private readonly todoService: TodoService) {}
  
   ngOnInit(): void {
    this.reload();
  }
  

  private reload(): void {
    this.todoService.getTodos().subscribe(todos => {
      // Backend liefert kein description -> default setzen
      this.tasks = todos.map(t => ({ ...t, description: (t as any).description ?? '' }));
    });
  }

  get task() {
    return this.tasks.filter(task => task.status === 'TODO');
  }

  get inProgress() {
    return this.tasks.filter(task => task.status === 'IN_PROGRESS');
  }

  get done() {
    return this.tasks.filter(task => task.status === 'DONE');
  }

  add(): void {
    const title = this.newTitle.trim();
    if (!title) return;

    this.todoService.createTodo(title).subscribe(() => {
      this.reload();
    });
    
    this.newTitle = '';
  }


  move(id: string, status: Status): void {
    this.todoService.updateTodo(id, status).subscribe(updated => {
      this.tasks = this.tasks.map(t => t.id === id ? { ...t, ...updated } : t);
    });
  }
}

