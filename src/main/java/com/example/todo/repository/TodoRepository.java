package com.example.todo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import com.example.todo.entity.AppUser;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {
  @EntityGraph(attributePaths = "category")
  @Query(value = "select t from Todo t " +
      "left join t.category c " +
      "where (:keyword is null or :keyword = '' " +
      "  or lower(t.title) like lower(concat('%', :keyword, '%')) " +
      "  or lower(t.author) like lower(concat('%', :keyword, '%'))) " +
      "and (:categoryId is null or c.id = :categoryId) " +
      "and (:priority is null or t.priority = :priority) " +
      "and (t.user = :user)",
      countQuery = "select count(t) from Todo t " +
      "left join t.category c " +
      "where (:keyword is null or :keyword = '' " +
      "  or lower(t.title) like lower(concat('%', :keyword, '%')) " +
      "  or lower(t.author) like lower(concat('%', :keyword, '%'))) " +
      "and (:categoryId is null or c.id = :categoryId) " +
      "and (:priority is null or t.priority = :priority) " +
      "and (t.user = :user)")
  Page<Todo> search(
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId,
      @Param("priority") Priority priority,
      @Param("user") AppUser user,
      Pageable pageable);

  @EntityGraph(attributePaths = "category")
  @Query(value = "select t from Todo t " +
      "left join t.category c " +
      "where (:keyword is null or :keyword = '' " +
      "  or lower(t.title) like lower(concat('%', :keyword, '%')) " +
      "  or lower(t.author) like lower(concat('%', :keyword, '%'))) " +
      "and (:categoryId is null or c.id = :categoryId) " +
      "and (:priority is null or t.priority = :priority)",
      countQuery = "select count(t) from Todo t " +
      "left join t.category c " +
      "where (:keyword is null or :keyword = '' " +
      "  or lower(t.title) like lower(concat('%', :keyword, '%')) " +
      "  or lower(t.author) like lower(concat('%', :keyword, '%'))) " +
      "and (:categoryId is null or c.id = :categoryId) " +
      "and (:priority is null or t.priority = :priority)")
  Page<Todo> searchAll(
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId,
      @Param("priority") Priority priority,
      Pageable pageable);

  @EntityGraph(attributePaths = "category")
  Optional<Todo> findWithCategoryById(Long id);

  @EntityGraph(attributePaths = "category")
  Optional<Todo> findByIdAndUser(Long id, AppUser user);

  @EntityGraph(attributePaths = "category")
  List<Todo> findAllByIdInAndUser(List<Long> ids, AppUser user);

  @EntityGraph(attributePaths = "category")
  List<Todo> findAllByIdIn(List<Long> ids);

  @EntityGraph(attributePaths = "category")
  List<Todo> findAllByUser(AppUser user, Sort sort);

  @EntityGraph(attributePaths = "category")
  Page<Todo> findAllByUser(AppUser user, Pageable pageable);

  @EntityGraph(attributePaths = "category")
  List<Todo> findAllBy(Sort sort);

  @EntityGraph(attributePaths = "category")
  Page<Todo> findAllBy(Pageable pageable);

  @EntityGraph(attributePaths = "category")
  List<Todo> findByDeadlineBefore(LocalDate date);
}

