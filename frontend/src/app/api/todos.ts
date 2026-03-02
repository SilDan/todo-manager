import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type Status = 'TODO' | 'IN_PROGRESS' | 'DONE';

export type Task = {
  id: string;
  title: string;
  description?: string;
  status: Status;
}

@Injectable({
  providedIn: 'root',
})

export class TodoService  {

  private readonly baseUrl = '/api/todos';

  constructor(private readonly http: HttpClient) {}
  
  getTodos(): Observable<Task[]> {
    return this.http.get<Task[]>(this.baseUrl);
  }
  
  createTodo(title: string): Observable<Task> {
    return this.http.post<Task>(this.baseUrl, { title });
  }   
  updateTodo(id: string, status: Status): Observable<Task> {
    return this.http.patch<Task>(`${this.baseUrl}/${id}`, { status });
  } 


}
