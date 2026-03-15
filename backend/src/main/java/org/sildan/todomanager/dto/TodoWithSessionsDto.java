package org.sildan.todomanager.dto;

import org.sildan.todomanager.model.Todo;

import java.util.List;

public record TodoWithSessionsDto(
        String id,
        String title,
        String status,
        List<TodoProcessingSessionDto> processingSessions
) {
    public static TodoWithSessionsDto from(Todo todo) {
        List<TodoProcessingSessionDto> sessions = todo.getProcessingSessions().stream()
                .map(TodoProcessingSessionDto::from)
                .toList();
        return new TodoWithSessionsDto(
                todo.getId(),
                todo.getTitle(),
                todo.getStatus(),
                sessions
        );
    }
}