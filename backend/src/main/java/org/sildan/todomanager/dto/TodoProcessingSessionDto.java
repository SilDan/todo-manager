package org.sildan.todomanager.dto;

import org.sildan.todomanager.model.TodoProcessingSession;

import java.time.Instant;

public record TodoProcessingSessionDto(
        Long id,
        Instant beginTime,
        Instant endTime
) {
    public static TodoProcessingSessionDto from(TodoProcessingSession ts) {
        return new TodoProcessingSessionDto(
                ts.getId(),
                ts.getBeginTime(),
                ts.getEndTime()
        );
    }
}