package org.hipeoplea.secureapi.auth;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.hipeoplea.secureapi.auth.dto.LoginRequest;
import org.hipeoplea.secureapi.auth.dto.RegisterRequest;
import org.hipeoplea.secureapi.auth.dto.TokenResponse;
import org.hipeoplea.secureapi.user.User;
import org.hipeoplea.secureapi.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String dummyPasswordHash;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-unknown-user");
    }

    public void register(RegisterRequest request) {
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароль длиннее 72 байт UTF-8");
        }

        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()));
        try {
            users.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Логин уже занят");
        }
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Optional<User> user = users.findByUsername(request.getUsername());
        String hash = user.map(User::getPasswordHash).orElse(dummyPasswordHash);
        boolean passwordLengthValid = request.getPassword().getBytes(StandardCharsets.UTF_8).length <= 72;
        String passwordToCheck = passwordLengthValid ? request.getPassword() : "invalid-overlong-password";
        boolean passwordMatches = passwordEncoder.matches(passwordToCheck, hash);

        if (user.isEmpty() || !passwordLengthValid || !passwordMatches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        String token = jwtService.createToken(user.get().getUsername());
        return new TokenResponse(token, JwtService.TOKEN_LIFETIME_SECONDS);
    }
}
