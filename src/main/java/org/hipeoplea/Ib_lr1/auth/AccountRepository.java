package org.hipeoplea.Ib_lr1.auth;

import java.util.Optional;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "JdbcTemplate is a thread-safe Spring-managed DAO dependency.")
    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Account> findByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT id, username, password_hash FROM users WHERE username = ?",
                (result, rowNumber) -> new Account(
                        result.getLong("id"),
                        result.getString("username"),
                        result.getString("password_hash")),
                username).stream().findFirst();
    }

    public void create(String username, String passwordHash) throws DuplicateKeyException {
        jdbcTemplate.update(
                "INSERT INTO users (username, password_hash) VALUES (?, ?)",
                username,
                passwordHash);
    }
}
