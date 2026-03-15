package org.sildan.todomanager.controller;

import org.sildan.todomanager.dto.TodoDto;
import org.sildan.todomanager.dto.UpdateStatusRequest;
import org.sildan.todomanager.dto.UpdateTitleRequest;
import org.sildan.todomanager.model.Todo;
import org.sildan.todomanager.model.TodoProcessingSession;
import org.sildan.todomanager.repository.TodoProcessingSessionRepository;
import org.sildan.todomanager.repository.TodoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private static final String INITIAL_STATE = "TODO";
    private final TodoRepository repo;
    private final TodoProcessingSessionRepository sessionRepo;

    public TodoController(TodoRepository repo, TodoProcessingSessionRepository sessionRepo) {
        this.repo = repo;
        this.sessionRepo = sessionRepo;
    }

    /**
     * Retrieves all todo items. This endpoint returns a list of all existing todo items in the system, each represented
     * as a JSON object containing its ID, title, and status.
     *
     * <p><strong>Example response:</strong></p>
     *
     * <pre>{@code
     * [
     *   {
     *     "id": "123e4567-e89b-12d3-a456-426614174000",
     *     "title": "Buy groceries",
     *     "status": "TODO"
     *   },
     *   {
     *     "id": "123e4567-e89b-12d3-a456-426614174001",
     *     "title": "Finish project report",
     *     "status": "IN_PROGRESS"
     *   }
     * ]
     * }</pre>
     *
     * @return a collection of {@link TodoDto} objects representing all todo items
     */
    @GetMapping
    public Collection<TodoDto> getAll() {
        return repo.findAllWithProcessingSessions().stream().map(TodoDto::from).toList();
    }

    /**
     * Create a new todo item. The request body should contain a JSON object with a "title" field. The server will
     * generate a unique ID and set the initial status to "TODO". The created todo item will be returned in the
     * response. Example request body: { "title": "Buy groceries" }
     *
     * @param todo The todo item to create, containing at least a "title" field.
     * @return The created {@link TodoDto} item with a generated ID and initial status "TODO".
     */
    @PostMapping
    public TodoDto create(@RequestBody Todo todo) {
        String id = UUID.randomUUID().toString();
        Todo newTodo = new Todo(id, todo.getTitle(), INITIAL_STATE);
        return TodoDto.from(repo.save(newTodo));
    }

    /**
     * Update the status of a todo item.
     * <p>This endpoint allows you to change the status of a specific todo item by
     * providing its ID in the URL path and the new status in the request body.</p>
     * <p>
     * <p>
     * The request body should be a JSON object containing a "status" field with the new status value. The server will
     * validate the new status and update the todo item accordingly.
     * </p>
     * If the new status is the same as the current status, no changes will be made. If the new status is "IN_PROGRESS",
     * a new processing session will be started for the todo item.
     * <p>
     * If the current status is "IN_PROGRESS" and the new status is different, the existing processing session will be
     * stopped. The updated todo item will be returned in the response.
     * </p>
     * <p><strong>Example request:</strong></p>
     * <pre>{@code
     * PATCH /api/todos/123e4567-e89b-12d3-a456-426614174000
     * Content-Type: application/json
     *
     *      {
     *          "status": "IN_PROGRESS"
     *      }
     * }</pre>
     */
    @PatchMapping("/{id}")
    public TodoDto updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest body) {

        Todo givenTodo = repo.findById(id).orElseThrow();

        String oldStatus = givenTodo.getStatus();
        String newStatus = body.status();

        if (newStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is null");
        }
        if (oldStatus == null) {
            throw new IllegalStateException("status is null");
        }

        if (newStatus.equals(oldStatus)) {
            Todo todoWithSessions = repo.findByIdWithProcessingSessions(id).orElseThrow();
            return TodoDto.from(todoWithSessions);
        }

        Instant activeSessionBeginTime = null;

        if (newStatus.equals("IN_PROGRESS") && !oldStatus.equals("IN_PROGRESS")) {
            activeSessionBeginTime = startProcessingSession(givenTodo).getBeginTime();
        }

        if (oldStatus.equals("IN_PROGRESS") && !newStatus.equals("IN_PROGRESS")) {
            stopProcessingSession(givenTodo);

        }

        givenTodo.setStatus(newStatus);
        repo.save(givenTodo);

        Todo todoWithSessions = repo.findByIdWithProcessingSessions(id).orElseThrow();

        if (newStatus.equals("IN_PROGRESS") && !oldStatus.equals("IN_PROGRESS")) {
            return TodoDto.from(givenTodo, activeSessionBeginTime, 0);
        }

        return TodoDto.from(todoWithSessions);
    }

    /**
     * Update the title of a todo item. This endpoint allows you to change the title of a specific todo item by
     * providing its ID in the URL path and the new title in the request body. The request
     *
     * @param id                 The ID of the todo item to update.
     * @param updateTitleRequest The request body containing the new title for the todo item.
     * @return The updated {@link TodoDto} item with the new title. If the item with the given ID does not exist, an
     * error will be thrown.
     */
    @PatchMapping("/{id}/title")
    public TodoDto updateTitle(@PathVariable String id, @RequestBody UpdateTitleRequest updateTitleRequest) {

        Todo existing = repo.findById(id).orElseThrow();
        existing.setTitle(updateTitleRequest.title());
        return TodoDto.from(repo.save(existing));
    }

    /**
     * Delete a todo item by its ID. This endpoint removes the specified todo item from the system. If the item with the
     * given ID does not exist, an error will be thrown.
     *
     * @param id The ID of the todo item to delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    private TodoProcessingSession startProcessingSession(Todo todo) {
        boolean openSessionExists = sessionRepo.findFirstByTodoAndEndTimeIsNullOrderByBeginTimeDesc(todo).isPresent();
        if (openSessionExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is already open");
        }
        return sessionRepo.save(new TodoProcessingSession(todo, Instant.now()));
    }

    private void stopProcessingSession(Todo todo) {
        TodoProcessingSession session = sessionRepo.findFirstByTodoAndEndTimeIsNullOrderByBeginTimeDesc(
                todo).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No open processing session found for todo " + todo.getId()
                ));
        session.setEndTime(Instant.now());
        sessionRepo.save(session);
    }

}