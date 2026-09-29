package org.hipeoplea.Ib_lr1.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Repository;

@Repository
public class ApiDataRepository {

    private static final RowMapper<ApiDataItem> DATA_MAPPER = (result, rowNumber) ->
            new ApiDataItem(result.getLong("id"), result.getString("title"), result.getString("content"));

    private static final RowMapper<NoteRecord> NOTE_MAPPER = ApiDataRepository::mapNote;

    private final JdbcTemplate jdbcTemplate;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "JdbcTemplate is a thread-safe Spring-managed DAO dependency.")
    public ApiDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ApiDataItem> findData() {
        return jdbcTemplate.query(
                "SELECT id, title, content FROM api_data ORDER BY id",
                DATA_MAPPER);
    }

    public NoteRecord createNote(String owner, String title, String content) {
        Long generatedId = jdbcTemplate.execute((ConnectionCallback<Long>) connection -> {
            try (var statement = connection.prepareStatement(
                    "INSERT INTO notes (owner, title, content) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, owner);
                statement.setString(2, title);
                statement.setString(3, content);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        return null;
                    }
                    return keys.getLong(1);
                }
            }
        });

        if (generatedId == null) {
            throw new IllegalStateException("Database did not return a note id");
        }
        return jdbcTemplate.queryForObject(
                "SELECT id, owner, title, content, created_at FROM notes WHERE id = ? AND owner = ?",
                NOTE_MAPPER,
                generatedId,
                owner);
    }

    public List<NoteRecord> findNotesByOwner(String owner) {
        return jdbcTemplate.query(
                "SELECT id, owner, title, content, created_at FROM notes "
                        + "WHERE owner = ? ORDER BY id DESC LIMIT 100",
                NOTE_MAPPER,
                owner);
    }

    private static NoteRecord mapNote(ResultSet result, int rowNumber) throws SQLException {
        Timestamp timestamp = result.getTimestamp("created_at");
        Instant createdAt = timestamp == null ? Instant.EPOCH : timestamp.toInstant();
        return new NoteRecord(
                result.getLong("id"),
                result.getString("owner"),
                result.getString("title"),
                result.getString("content"),
                createdAt);
    }
}
