package org.sildan.todomanager.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "todos")
public class Todo {

    @Id
    private String id;

    private String title;
    private String description;
    private String status;

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoProcessingSession> processingSessions = new ArrayList<>();

    public Todo() {
    }

    public Todo(String id, String title, String description, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TodoProcessingSession> getProcessingSessions() {
        return processingSessions;
    }

    public void setProcessingSessions(List<TodoProcessingSession> processingSessions) {
        this.processingSessions = processingSessions;
    }
}