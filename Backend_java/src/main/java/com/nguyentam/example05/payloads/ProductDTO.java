package com.nguyentam.example05.payloads;

import java.util.Locale.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
  private Long productId;
  private String productName;
  private String image;
  private String description;
  // private String categoryName;
  private Integer quantity;
  private double price;
  private double discount;
  private double specialPrice;
  private CategoryDTO category;
}