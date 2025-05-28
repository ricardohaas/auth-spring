package com.authproject.controller.dto;

public record LoginResponseDto(String accessToken, Long expiresIn) {
}
