package com.authproject.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
}
