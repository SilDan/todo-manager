package org.sildan.todomanager.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class TodoProcessingSession {

    @Id
    @GeneratedValue
    private Long id;


    // this means that each processing session is associated with exactly one
    // todo item, and that association is mandatory (i.e., a processing session cannot
    //  exist without being linked to a todo item).
    @ManyToOne(optional = false)
    @JoinColumn(name = "todo_id")
    private Todo todo;

    private Instant beginTime;
    private Instant endTime;

    protected TodoProcessingSession() {
    }

    public TodoProcessingSession(Todo todo, Instant beginTime) {
        this.todo = todo;
        this.beginTime = beginTime;
    }

    public Long getId() {
        return id;
    }

    public Todo getTodo() {
        return todo;
    }

    public void setTodo(Todo todo) {
        this.todo = todo;
    }

    public Instant getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Instant beginTime) {
        this.beginTime = beginTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public void safe() {
    }
}
