package org.hipeoplea.secureapi.auth.dto;

public class TokenResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresInSeconds;

    public TokenResponse(String accessToken, long expiresInSeconds) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
