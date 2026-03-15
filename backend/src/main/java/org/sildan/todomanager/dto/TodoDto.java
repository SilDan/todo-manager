package org.sildan.todomanager.dto;

import org.sildan.todomanager.model.Todo;
import org.sildan.todomanager.model.TodoProcessingSession;

import java.time.Instant;
import java.util.Comparator;

public record TodoDto(
        String id,
        String title,
        String status,
        Instant activeSessionBeginTime
) {

    public static TodoDto from(Todo todo) {
        Instant beginTime = todo.getProcessingSessions().stream().filter(
                session -> session.getEndTime() == null).max(
                Comparator.comparing(
                        TodoProcessingSession::getBeginTime)).map(TodoProcessingSession::getBeginTime).orElse(null);

        return new TodoDto(
                todo.getId(),
                todo.getTitle(),
                todo.getStatus(),
                beginTime
        );

    }

    public static TodoDto from(Todo todo, Instant beginTime) {

        return new TodoDto(
                todo.getId(),
                todo.getTitle(),
                todo.getStatus(),
                beginTime
        );
    }
}