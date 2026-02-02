package com.example.todo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoRepository;

@Service
public class TodoService {
  private final TodoRepository todoRepository;
  private final CategoryService categoryService;

  public TodoService(TodoRepository todoRepository, CategoryService categoryService) {
    this.todoRepository = todoRepository;
    this.categoryService = categoryService;
  }

  public List<Todo> findAll(String keyword, Long categoryId, Sort sort) {
    String q = keyword == null ? null : keyword.trim();
    return todoRepository.search(q, categoryId, sort);
  }

  public Optional<Todo> findById(Long id) {
    return todoRepository.findWithCategoryById(id);
  }

  public List<Todo> findAllByIds(List<Long> ids) {
    return todoRepository.findAllById(ids);
  }

  @Transactional
  public Todo save(Todo todo) {
    if (todo.getId() != null) {
      todoRepository.findById(todo.getId()).ifPresent(existing -> {
        todo.setCreatedAt(existing.getCreatedAt());
      });
    }
    normalizeCategory(todo);
    return todoRepository.save(todo);
  }

  @Transactional
  public void toggleCompleted(Long id) {
    todoRepository.findById(id).ifPresent(todo -> {
      todo.setCompleted(!todo.isCompleted());
      todoRepository.save(todo);
    });
  }

  @Transactional
  public void delete(Long id) {
    todoRepository.deleteById(id);
  }

  @Transactional
  public void deleteAllByIds(List<Long> ids) {
    todoRepository.deleteAllByIdInBatch(ids);
  }

  private void normalizeCategory(Todo todo) {
    if (todo.getCategory() == null || todo.getCategory().getId() == null) {
      todo.setCategory(null);
      return;
    }
    Long categoryId = todo.getCategory().getId();
    todo.setCategory(categoryService.findById(categoryId).orElse(null));
  }
}

