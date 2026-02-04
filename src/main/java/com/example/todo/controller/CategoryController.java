package com.example.todo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import com.example.todo.entity.Category;
import com.example.todo.form.CategoryEditForm;
import com.example.todo.service.CategoryService;

@Controller
@RequestMapping("/categories")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping
  public String index(Model model) {
    List<Category> categories = categoryService.findAllOrderById();
    CategoryEditForm form = new CategoryEditForm();
    form.setCategories(categories);
    model.addAttribute("form", form);
    return "categories";
  }

  @PostMapping("/update")
  public String update(
      @Valid @ModelAttribute("form") CategoryEditForm form,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {
    validateDuplicates(form.getCategories(), result);
    if (result.hasErrors()) {
      model.addAttribute("form", form);
      return "categories";
    }
    categoryService.updateAll(form.getCategories());
    redirectAttributes.addFlashAttribute("message", "カテゴリを更新しました。");
    return "redirect:/categories";
  }

  private void validateDuplicates(List<Category> categories, BindingResult result) {
    Map<String, Integer> seen = new HashMap<>();
    for (int i = 0; i < categories.size(); i++) {
      Category category = categories.get(i);
      if (category == null || category.getName() == null) {
        continue;
      }
      String key = category.getName().trim().toLowerCase();
      if (key.isEmpty()) {
        continue;
      }
      if (seen.containsKey(key)) {
        result.rejectValue("categories[" + i + "].name", "duplicate", "カテゴリ名が重複しています。");
      } else {
        seen.put(key, i);
      }
    }
  }
}
