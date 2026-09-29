package org.hipeoplea.Ib_lr1.data;

import java.time.Instant;

public record NoteRecord(long id, String owner, String title, String content, Instant createdAt) {
}
