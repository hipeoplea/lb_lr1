package org.hipeoplea.Ib_lr1.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 32)
        @Pattern(regexp = "[A-Za-z0-9._-]+", message = "логин может содержать латинские буквы, цифры, точку, дефис и подчеркивание")
        String username,
        @NotBlank @Size(min = 12, max = 72) String password) {
}
