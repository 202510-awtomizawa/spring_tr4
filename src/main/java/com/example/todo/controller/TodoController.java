package com.example.todo.controller;

import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

import com.example.todo.entity.Category;
import com.example.todo.entity.AppUser;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;
import com.example.todo.service.CsvExportService;
import com.example.todo.service.CategoryService;
import com.example.todo.service.TodoService;
import com.example.todo.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/todos")
public class TodoController {
  private final TodoService todoService;
  private final CategoryService categoryService;
  private final CsvExportService csvExportService;
  private final UserRepository userRepository;

  public TodoController(
      TodoService todoService,
      CategoryService categoryService,
      CsvExportService csvExportService,
      UserRepository userRepository) {
    this.todoService = todoService;
    this.categoryService = categoryService;
    this.csvExportService = csvExportService;
    this.userRepository = userRepository;
  }

  @ModelAttribute("categories")
  public List<Category> categories() {
    return categoryService.findAllOrderById();
  }

  @ModelAttribute("priorities")
  public Priority[] priorities() {
    return Priority.values();
  }

  @GetMapping
  public String index(
      @RequestParam(name = "q", required = false) String keyword,
      @RequestParam(name = "categoryId", required = false) Long categoryId,
      @RequestParam(name = "priority", required = false) Priority priority,
      @RequestParam(name = "sort", defaultValue = "createdAt") String sortField,
      @RequestParam(name = "dir", defaultValue = "desc") String direction,
      @RequestParam(name = "bulk", defaultValue = "false") boolean bulk,
      @PageableDefault(size = 10) Pageable pageable,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    AppUser user = getCurrentUser(userDetails);
    Sort sort = Sort.by("completed").ascending()
        .and(Sort.by(Sort.Direction.fromString(direction), sortField));
    Pageable effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    Page<Todo> page = todoService.findPageForUserOrAll(keyword, categoryId, priority, user, effectivePageable);
    model.addAttribute("todos", page.getContent());
    model.addAttribute("page", page);
    model.addAttribute("q", keyword == null ? "" : keyword);
    model.addAttribute("categoryId", categoryId);
    model.addAttribute("priority", priority);
    model.addAttribute("sort", sortField);
    model.addAttribute("dir", direction);
    model.addAttribute("bulk", bulk);
    long total = page.getTotalElements();
    int start = total == 0 ? 0 : page.getNumber() * page.getSize() + 1;
    int end = total == 0 ? 0 : start + page.getNumberOfElements() - 1;
    model.addAttribute("rangeStart", start);
    model.addAttribute("rangeEnd", end);
    return "index";
  }

  @GetMapping("/new")
  public String createForm(Model model) {
    Todo todo = new Todo();
    if (todo.getCategory() == null) {
      todo.setCategory(new Category());
    }
    model.addAttribute("todo", todo);
    return "create";
  }

  @PostMapping("/new")
  public String backToCreate(@ModelAttribute("todo") Todo todo) {
    return "create";
  }

  @PostMapping("/confirm")
  public String confirm(@Valid @ModelAttribute("todo") Todo todo, BindingResult result) {
    if (todo.getCategory() == null) {
      todo.setCategory(new Category());
    }
    if (result.hasErrors()) {
      return "create";
    }
    return "confirm";
  }

  @PostMapping
  public String save(@ModelAttribute("todo") Todo todo,
      @RequestParam(name = "categoryId", required = false) Long categoryId,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal UserDetails userDetails) {
    AppUser user = getCurrentUser(userDetails);
    if (categoryId != null) {
      categoryService.findById(categoryId).ifPresent(todo::setCategory);
    } else {
      todo.setCategory(null);
    }
    Todo saved = todoService.saveForUser(todo, user);
    redirectAttributes.addAttribute("id", saved.getId());
    return "redirect:/todos/complete";
  }

  @GetMapping("/complete")
  public String complete(@RequestParam("id") Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
    AppUser user = getCurrentUser(userDetails);
    Optional<Todo> todoOpt = todoService.findByIdForUserOrAll(id, user);
    if (todoOpt.isEmpty()) {
      throw new AccessDeniedException("Not allowed");
    }
    model.addAttribute("todo", todoOpt.get());
    return "complete";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable("id") Long id,
      Model model,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal UserDetails userDetails) {
    AppUser user = getCurrentUser(userDetails);
    Optional<Todo> todoOpt = todoService.findByIdForUserOrAll(id, user);
    if (todoOpt.isEmpty()) {
      throw new AccessDeniedException("Not allowed");
    }
    model.addAttribute("todo", todoOpt.get());
    return "create";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
    AppUser user = getCurrentUser(userDetails);
    Optional<Todo> todoOpt = todoService.findByIdForUserOrAll(id, user);
    if (todoOpt.isEmpty()) {
      throw new AccessDeniedException("Not allowed");
    }
    todoService.delete(id, user);
    return "redirect:/todos";
  }

  @PostMapping("/{id}/toggle")
  public String toggle(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
    AppUser user = getCurrentUser(userDetails);
    Optional<Todo> todoOpt = todoService.findByIdForUserOrAll(id, user);
    if (todoOpt.isEmpty()) {
      throw new AccessDeniedException("Not allowed");
    }
    todoService.toggleCompleted(id, user);
    return "redirect:/todos";
  }

  @GetMapping("/{id}/toggle")
  public String toggleGet(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
    AppUser user = getCurrentUser(userDetails);
    Optional<Todo> todoOpt = todoService.findByIdForUserOrAll(id, user);
    if (todoOpt.isEmpty()) {
      throw new AccessDeniedException("Not allowed");
    }
    todoService.toggleCompleted(id, user);
    return "redirect:/todos";
  }

  @PostMapping("/bulk/confirm")
  public String bulkConfirm(@RequestParam(name = "ids", required = false) List<Long> ids,
      Model model,
      @AuthenticationPrincipal UserDetails userDetails) {
    if (ids == null || ids.isEmpty()) {
      return "redirect:/todos?bulk=true";
    }
    AppUser user = getCurrentUser(userDetails);
    List<Todo> todos = todoService.findAllByIdsForUserOrAll(ids, user);
    if (todos.size() != ids.size()) {
      throw new AccessDeniedException("Not allowed");
    }
    model.addAttribute("todos", todos);
    model.addAttribute("ids", ids);
    return "bulk_confirm";
  }

  @PostMapping("/bulk/delete")
  public String bulkDelete(@RequestParam(name = "ids", required = false) List<Long> ids,
      @AuthenticationPrincipal UserDetails userDetails) {
    if (ids != null && !ids.isEmpty()) {
      AppUser user = getCurrentUser(userDetails);
      List<Todo> todos = todoService.findAllByIdsForUserOrAll(ids, user);
      if (todos.size() != ids.size()) {
        throw new AccessDeniedException("Not allowed");
      }
      todoService.deleteAllByIds(ids, user);
    }
    return "redirect:/todos";
  }

  @GetMapping("/export/csv")
  public void exportCsv(HttpServletResponse response, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
    AppUser user = getCurrentUser(userDetails);
    response.setContentType("text/csv; charset=UTF-8");

    String filename = "todo_export_"
        + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        + ".csv";
    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

    try (PrintWriter writer = response.getWriter()) {
      writer.write('\uFEFF');
      csvExportService.writeCsv(writer, todoService.findAllByUserOrAll(user));
    }
  }

  private AppUser getCurrentUser(UserDetails userDetails) {
    if (userDetails == null) {
      throw new AccessDeniedException("User not authenticated");
    }
    return userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new AccessDeniedException("User not found"));
  }
}

