package org.hipeoplea.Ib_lr1.auth;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, String username) {
}
