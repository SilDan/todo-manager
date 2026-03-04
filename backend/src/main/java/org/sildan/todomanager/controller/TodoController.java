package org.sildan.todomanager.controller;

import org.sildan.todomanager.model.Todo;
import org.sildan.todomanager.repository.TodoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private static final String INITIAL_STATE = "TODO";
    private final TodoRepository repo;

    public TodoController(TodoRepository repo) {
        this.repo = repo;
    }

    /**
     * Get all todo items. This endpoint returns a list of all existing todo items in the system. Each item includes its ID, title, and current status.
     * <strong>Example response:</strong>
     * <pre>
     * {code
     * [
     *      {
     *          "id": "123e4567-e89b-12d3-a456-426614174000",
     *          "title": "Always smile at work :)",
     *          "status": "TODO"
     *      },
     *      {
     *          "id": "123e4567-e89b-12d3-a456-426614174001",
     *          "title": "Finish project and find some friends ;)",
     *          "status": "IN_PROGRESS"
     *      }
     * ]
     * }
     * </pre>
     * @return A collection of all {@link Todo} items currently stored in the system, each with its ID, title, and status.
     */
    @GetMapping
    public Collection<Todo> getAll() {
        return repo.findAll();
    }

    /**
     * Create a new todo item. The request body should contain a JSON object with a "title" field.
     * The server will generate a unique ID and set the initial status to "TODO". The created todo item will be returned in the response.
     * Example request body:
     * {
     *  "title": "Buy groceries"
     * }
     *
     *
     * @param todo The todo item to create, containing at least a "title" field.
     * @return The created {@link Todo} item with a generated ID and initial status "TODO".
     */
    @PostMapping
    public Todo create(@RequestBody Todo todo) {
        String id = UUID.randomUUID().toString();
        Todo newTodo = new Todo(id, todo.getTitle(), INITIAL_STATE);
        return repo.save(newTodo);
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

        Todo existing = repo.findById(id).orElseThrow();
        existing.setStatus(body.get("status"));
        return repo.save(existing);
    }

    /**
     * Delete a todo item by its ID. This endpoint removes the specified todo item from the system. If the item with the given ID does not exist, an error will be thrown.
     * @param id The ID of the todo item to delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if(!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}