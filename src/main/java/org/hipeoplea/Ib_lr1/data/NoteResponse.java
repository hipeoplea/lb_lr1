package org.hipeoplea.Ib_lr1.data;

import java.time.Instant;

public record NoteResponse(long id, String title, String content, Instant createdAt) {
}
