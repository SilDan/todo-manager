package org.sildan.todomanager.repository;

import org.sildan.todomanager.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, String> {
}
