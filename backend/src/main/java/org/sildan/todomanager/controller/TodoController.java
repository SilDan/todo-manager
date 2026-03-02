package org.sildan.todomanager.controller;

import org.sildan.todomanager.model.Todo;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private static final String INITIAL_STATE = "TODO";
    private final Map<String, Todo> todos = new ConcurrentHashMap<>();

    @GetMapping
    public Collection<Todo> getAll() {
        return todos.values();
    }

    /**
     * Create a new todo item. The request body should contain a JSON object with a "title" field.
     * The server will generate a unique ID and set the initial status to "TODO". The created todo item will be returned in the response.
     * Example request body:
     * {
     *  "title": "Buy groceries"
     *  }
     *
     *
     * @param todo The todo item to create, containing at least a "title" field.
     * @return The created {@link Todo} item with a generated ID and initial status "TODO".
     */
    @PostMapping
    public Todo create(@RequestBody Todo todo) {
        String id = UUID.randomUUID().toString();
        Todo newTodo = new Todo(id, todo.getTitle(), INITIAL_STATE);
        todos.put(id, newTodo);
        return newTodo;
    }


    /**
     * Update the status of an existing todo item. The request body should contain a JSON object with a "status" field.
     * The server will update the status of the specified todo item and return the updated item in the response. If the todo item with the given ID does not exist, an error will be thrown.
     * Example request body:
     * {
     *  "status": "IN_PROGRESS"
     * }
     * @param id The ID of the todo item to update.
     * @param body A map containing the new status for the todo item, with a "status" key.
     * @return The updated {@link Todo} item with the new status. If the item does not exist, an error will be thrown.
     */
    @PatchMapping("/{id}")
    public Todo updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        Todo existing = todos.get(id);
        if (existing == null) {
            throw new RuntimeException("Todo not found");
        }

        existing.setStatus(body.get("status"));
        return existing;
    }

}