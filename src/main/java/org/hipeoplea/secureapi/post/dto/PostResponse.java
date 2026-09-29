package org.hipeoplea.secureapi.post.dto;

import java.time.Instant;

public class PostResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final Instant createdAt;

    public PostResponse(Long id, String title, String content, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
