package com.example.todo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {
  @EntityGraph(attributePaths = "category")
  @Query("""
      select t from Todo t
      left join t.category c
      where (:keyword is null or :keyword = ''
          or lower(t.title) like lower(concat('%', :keyword, '%'))
          or lower(t.author) like lower(concat('%', :keyword, '%')))
        and (:categoryId is null or c.id = :categoryId)
        and (:priority is null or t.priority = :priority)
      """)
  List<Todo> search(
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId,
      @Param("priority") Priority priority,
      Sort sort);

  @EntityGraph(attributePaths = "category")
  Optional<Todo> findWithCategoryById(Long id);
}

