package com.example.todo.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.todo.service.CategoryService;

@Component
public class CategoryInitializer implements ApplicationRunner {
  private final CategoryService categoryService;

  public CategoryInitializer(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @Override
  public void run(ApplicationArguments args) {
    categoryService.seedDefaults();
  }
}
