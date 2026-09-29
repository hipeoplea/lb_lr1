package org.hipeoplea.Ib_lr1.auth;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.hipeoplea.Ib_lr1.security.TokenService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final String dummyPasswordHash;
    private final long tokenLifetimeMinutes;

    public AuthService(
            AccountRepository accounts,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            @Value("${app.jwt.lifetime-minutes:15}") long tokenLifetimeMinutes) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.tokenLifetimeMinutes = tokenLifetimeMinutes;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-used-for-timing-only");
    }

    public void register(RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароль слишком длинный");
        }
        try {
            accounts.create(request.username(), passwordEncoder.encode(request.password()));
        } catch (DuplicateKeyException exception) {
            throw new UsernameAlreadyExistsException();
        }
    }

    public AuthResponse login(LoginRequest request) {
        boolean passwordFitsBcrypt = request.password().getBytes(StandardCharsets.UTF_8).length <= 72;
        Account account = accounts.findByUsername(request.username()).orElse(null);
        String passwordHash = account == null ? dummyPasswordHash : account.passwordHash();
        boolean passwordMatches = passwordEncoder.matches(
                passwordFitsBcrypt ? request.password() : "invalid-overlong-password",
                passwordHash);
        if (account == null || !passwordFitsBcrypt || !passwordMatches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        return new AuthResponse(
                tokenService.issue(account.username()),
                "Bearer",
                tokenLifetimeMinutes * 60,
                account.username());
    }
}
