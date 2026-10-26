package com.example.Demo.dto;

import com.example.Demo.entity.Role;

public record UserResponse(Long id, String username, Role role) {
}
