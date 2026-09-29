package org.hipeoplea.Ib_lr1.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000) String content) {
}
