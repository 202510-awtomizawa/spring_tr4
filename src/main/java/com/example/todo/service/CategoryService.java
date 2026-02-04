package com.example.todo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.entity.Category;
import com.example.todo.mapper.CategoryMapper;

@Service
public class CategoryService {
  private final CategoryMapper categoryMapper;

  public CategoryService(CategoryMapper categoryMapper) {
    this.categoryMapper = categoryMapper;
  }

  public List<Category> findAllOrderById() {
    return categoryMapper.findAllOrderById();
  }

  public Optional<Category> findById(Long id) {
    if (id == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(categoryMapper.findById(id));
  }

  @Transactional
  public void updateAll(List<Category> categories) {
    if (categories == null) {
      return;
    }
    for (Category category : categories) {
      if (category == null || category.getId() == null) {
        continue;
      }
      categoryMapper.update(category);
    }
  }

  @Transactional
  public void seedDefaults() {
    List<Category> defaults = new ArrayList<>();
    defaults.add(build(1L, "未分類1", "#9CA3AF"));
    defaults.add(build(2L, "未分類2", "#60A5FA"));
    defaults.add(build(3L, "未分類3", "#34D399"));
    defaults.add(build(4L, "未分類4", "#FBBF24"));
    defaults.add(build(5L, "未分類5", "#F87171"));

    for (Category category : defaults) {
      if (categoryMapper.countById(category.getId()) == 0) {
        categoryMapper.insert(category);
      }
    }
  }

  private Category build(Long id, String name, String color) {
    Category category = new Category();
    category.setId(id);
    category.setName(name);
    category.setColor(color);
    return category;
  }
}
