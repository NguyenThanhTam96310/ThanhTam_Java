package com.nguyentam.example05.service;

import com.nguyentam.example05.payloads.UserDTO;
import com.nguyentam.example05.payloads.UserResponse;

public interface UserService {
  UserDTO registerUser(UserDTO userDTO);

  UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

  UserDTO getUserById(Long userId);

  UserDTO updateUser(Long userId, UserDTO userDTO);

  UserDTO getUserByEmail(String email);

  String deleteUser(Long userId);
}