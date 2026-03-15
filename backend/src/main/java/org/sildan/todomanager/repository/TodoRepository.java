package org.sildan.todomanager.repository;

import org.sildan.todomanager.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, String> {

    @Query("""
            select distinct t
            from Todo t
            left join fetch t.processingSessions
            """)
    List<Todo> findAllWithProcessingSessions();

    @Query("""
            select t
            from Todo t
            left join fetch t.processingSessions
            where t.id = :id
            """)
    Optional<Todo> findByIdWithProcessingSessions(String id);
}
