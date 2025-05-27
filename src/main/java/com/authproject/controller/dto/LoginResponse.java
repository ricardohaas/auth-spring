package com.authproject.controller.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
}
