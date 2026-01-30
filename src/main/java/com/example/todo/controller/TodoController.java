package com.example.todo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.todo.entity.Todo;
import com.example.todo.service.TodoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/todos")
public class TodoController {
  private final TodoService todoService;

  public TodoController(TodoService todoService) {
    this.todoService = todoService;
  }

  @GetMapping
  public String index(
      @RequestParam(name = "q", required = false) String keyword,
      @RequestParam(name = "sort", defaultValue = "createdAt") String sortField,
      @RequestParam(name = "dir", defaultValue = "desc") String direction,
      Model model) {
    Sort sort = Sort.by("completed").ascending()
        .and(Sort.by(Sort.Direction.fromString(direction), sortField));
    List<Todo> todos = todoService.findAll(keyword, sort);
    model.addAttribute("todos", todos);
    model.addAttribute("q", keyword == null ? "" : keyword);
    model.addAttribute("sort", sortField);
    model.addAttribute("dir", direction);
    return "index";
  }

  @GetMapping("/new")
  public String createForm(Model model) {
    model.addAttribute("todo", new Todo());
    return "create";
  }

  @PostMapping("/new")
  public String backToCreate(@ModelAttribute("todo") Todo todo) {
    return "create";
  }

  @PostMapping("/confirm")
  public String confirm(@Valid @ModelAttribute("todo") Todo todo, BindingResult result) {
    if (result.hasErrors()) {
      return "create";
    }
    return "confirm";
  }

  @PostMapping
  public String save(@ModelAttribute("todo") Todo todo, RedirectAttributes redirectAttributes) {
    Todo saved = todoService.save(todo);
    redirectAttributes.addAttribute("id", saved.getId());
    return "redirect:/todos/complete";
  }

  @GetMapping("/complete")
  public String complete(@RequestParam("id") Long id, Model model) {
    Optional<Todo> todoOpt = todoService.findById(id);
    if (todoOpt.isEmpty()) {
      return "redirect:/todos";
    }
    model.addAttribute("todo", todoOpt.get());
    return "complete";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
    Optional<Todo> todoOpt = todoService.findById(id);
    if (todoOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "???ToDo???????????");
      return "redirect:/todos";
    }
    model.addAttribute("todo", todoOpt.get());
    return "create";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable("id") Long id) {
    todoService.delete(id);
    return "redirect:/todos";
  }

  @PostMapping("/{id}/toggle")
  public String toggle(@PathVariable("id") Long id) {
    todoService.toggleCompleted(id);
    return "redirect:/todos";
  }

  @GetMapping("/{id}/toggle")
  public String toggleGet(@PathVariable("id") Long id) {
    todoService.toggleCompleted(id);
    return "redirect:/todos";
  }
}

