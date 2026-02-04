package com.example.todo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.todo.entity.Category;

@Mapper
public interface CategoryMapper {
  List<Category> findAllOrderById();

  Category findById(@Param("id") Long id);

  int update(Category category);

  int insert(Category category);

  int countById(@Param("id") Long id);
}
