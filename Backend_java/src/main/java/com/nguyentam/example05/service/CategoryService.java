package com.nguyentam.example05.service;

import com.nguyentam.example05.entity.Category;
import com.nguyentam.example05.payloads.CategoryDTO;
import com.nguyentam.example05.payloads.CategoryResponse;

public interface CategoryService {

  CategoryDTO createCategory(Category category);

  CategoryDTO getCategoryById(Long categoryId);

  CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

  CategoryDTO updateCategory(Category category, Long categoryId);

  String deleteCategory(Long categoryId);
}