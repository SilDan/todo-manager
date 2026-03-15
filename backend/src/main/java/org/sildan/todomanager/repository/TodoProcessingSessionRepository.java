package org.sildan.todomanager.repository;

import org.sildan.todomanager.model.Todo;
import org.sildan.todomanager.model.TodoProcessingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface  TodoProcessingSessionRepository extends JpaRepository<TodoProcessingSession, Long> {
    Optional<TodoProcessingSession> findFirstByTodoAndEndTimeIsNullOrderByBeginTimeDesc(Todo todo);
}
