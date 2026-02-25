import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type Status = 'TODO' | 'IN_PROGRESS' | 'DONE';

type Task = {
  id: string;
  title: string;
  description: string;
  status: Status;
};

@Component({
  selector: 'app-board',
  imports: [CommonModule, FormsModule],
  templateUrl: './board.html',
  styleUrls: ['./board.scss'],
})
export class Board {

  newTitle= '';

  tasks: Task[] = [
    { id: crypto.randomUUID(), title: 'Task 1', description: 'Description for Task 1', status: 'TODO' }
  ];
  
  get todo() {
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

    this.tasks = [
      ...this.tasks,
      { id: crypto.randomUUID(), title, description: '', status: 'TODO' }
    ];
    
    this.newTitle = '';
  }

  move(id: string, status: Status): void {
  this.tasks = this.tasks.map((task) =>
    task.id === id ? { ...task, status } : task
  );
}

}
