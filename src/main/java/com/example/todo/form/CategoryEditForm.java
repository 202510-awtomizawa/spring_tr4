package com.example.todo.form;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import com.example.todo.entity.Category;

public class CategoryEditForm {
  @Valid
  private List<Category> categories = new ArrayList<>();

  public List<Category> getCategories() {
    return categories;
  }

  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }
}
