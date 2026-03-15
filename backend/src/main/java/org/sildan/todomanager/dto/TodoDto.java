package org.sildan.todomanager.dto;

import org.sildan.todomanager.model.Todo;
import org.sildan.todomanager.model.TodoProcessingSession;

import java.time.Instant;
import java.util.Comparator;

public record TodoDto(
        String id,
        String title,
        String description,
        String status,
        Instant activeSessionBeginTime,
        long totalProcessingTimeSeconds
) {

    public static TodoDto from(Todo todo) {
        Instant beginTime = todo.getProcessingSessions().stream().filter(
                session -> session.getEndTime() == null).max(
                Comparator.comparing(
                        TodoProcessingSession::getBeginTime)).map(TodoProcessingSession::getBeginTime).orElse(null);
        long totalProcessingTimeSeconds = todo.getProcessingSessions().stream()
                .filter(session -> session.getEndTime() != null)
                .mapToLong(session ->
                        (session.getEndTime().toEpochMilli() - session.getBeginTime().toEpochMilli()) / 1000
                )
                .sum();
        return new TodoDto(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getStatus(),
                beginTime,
                totalProcessingTimeSeconds
        );

    }

    public static TodoDto from(Todo todo, Instant beginTime, long totalProcessingTimeSeconds) {

        return new TodoDto(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getStatus(),
                beginTime,
                totalProcessingTimeSeconds
        );
    }
}