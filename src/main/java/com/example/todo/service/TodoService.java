package com.example.todo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import com.example.todo.entity.AppUser;
import com.example.todo.entity.Priority;
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

  public Page<Todo> findPageForUserOrAll(String keyword, Long categoryId, Priority priority, AppUser user, Pageable pageable) {
    String q = keyword == null ? null : keyword.trim();
    if (isAdmin(user)) {
      return todoRepository.searchAll(q, categoryId, priority, pageable);
    }
    return todoRepository.search(q, categoryId, priority, user, pageable);
  }

  public Optional<Todo> findByIdForUser(Long id, AppUser user) {
    return todoRepository.findByIdAndUser(id, user);
  }

  public Optional<Todo> findByIdForUserOrAll(Long id, AppUser user) {
    if (isAdmin(user)) {
      return todoRepository.findWithCategoryById(id);
    }
    return todoRepository.findByIdAndUser(id, user);
  }

  public List<Todo> findAllByUser(AppUser user) {
    return todoRepository.findAllByUser(user, Sort.by("createdAt").descending());
  }

  public List<Todo> findAllByUserOrAll(AppUser user) {
    if (isAdmin(user)) {
      return todoRepository.findAllBy(Sort.by("createdAt").descending());
    }
    return todoRepository.findAllByUser(user, Sort.by("createdAt").descending());
  }

  public List<Todo> findAllByIdsForUser(List<Long> ids, AppUser user) {
    return todoRepository.findAllByIdInAndUser(ids, user);
  }

  public List<Todo> findAllByIdsForUserOrAll(List<Long> ids, AppUser user) {
    if (isAdmin(user)) {
      return todoRepository.findAllByIdIn(ids);
    }
    return todoRepository.findAllByIdInAndUser(ids, user);
  }

  public List<Todo> findOverdue(LocalDate date) {
    return todoRepository.findByDeadlineBefore(date);
  }

  @Transactional
  public Todo saveForUser(Todo todo, AppUser user) {
    todo.setUser(user);
    if (todo.getId() != null) {
      todoRepository.findByIdAndUser(todo.getId(), user).ifPresentOrElse(existing -> {
        todo.setCreatedAt(existing.getCreatedAt());
      }, () -> {
        throw new AccessDeniedException("Not allowed");
      });
    }
    if (todo.getPriority() == null) {
      todo.setPriority(Priority.NONE);
    }
    normalizeCategory(todo);
    return todoRepository.save(todo);
  }

  @Transactional
  public void toggleCompleted(Long id, AppUser user) {
    Optional<Todo> target = isAdmin(user)
        ? todoRepository.findById(id)
        : todoRepository.findByIdAndUser(id, user);
    target.ifPresent(todo -> {
      todo.setCompleted(!todo.isCompleted());
      todoRepository.save(todo);
    });
  }

  @Transactional
  public void delete(Long id, AppUser user) {
    if (isAdmin(user)) {
      todoRepository.findById(id).ifPresent(todoRepository::delete);
      return;
    }
    todoRepository.findByIdAndUser(id, user).ifPresent(todoRepository::delete);
  }

  @Transactional
  public void deleteAllByIds(List<Long> ids, AppUser user) {
    if (isAdmin(user)) {
      List<Todo> all = todoRepository.findAllByIdIn(ids);
      todoRepository.deleteAllInBatch(all);
      return;
    }
    List<Todo> owned = todoRepository.findAllByIdInAndUser(ids, user);
    todoRepository.deleteAllInBatch(owned);
  }

  private boolean isAdmin(AppUser user) {
    return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
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

