package org.sildan.todomanager.controller;

import org.sildan.todomanager.model.Todo;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final Map<String, Todo> todos = new ConcurrentHashMap<>();

    @GetMapping
    public Collection<Todo> getAll() {
        return todos.values();
    }

    @PostMapping
    public Todo create(@RequestBody Todo todo) {
        String id = UUID.randomUUID().toString();
        Todo newTodo = new Todo(id, todo.getTitle(), "TODO");
        todos.put(id, newTodo);
        return newTodo;
    }

    @PutMapping("/{id}/status")
    public Todo updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Todo existing = todos.get(id);
        if (existing == null) {
            throw new RuntimeException("Todo not found");
        }

        existing.setStatus(body.get("status"));
        return existing;
    }

}