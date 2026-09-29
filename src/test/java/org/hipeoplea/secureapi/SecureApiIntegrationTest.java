package org.hipeoplea.secureapi;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hipeoplea.secureapi.user.User;
import org.hipeoplea.secureapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SecureApiIntegrationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");
    private static final String PASSWORD = "correct-horse-battery-42";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository users;

    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret-base64", () -> Base64.getEncoder().encodeToString(
                "test-only-jwt-signing-secret-at-least-32-bytes".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void protectedEndpointRejectsMissingAndInvalidTokens() throws Exception {
        mockMvc.perform(get("/api/data"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/data").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationLoginAndDataFlowProtectUserContent() throws Exception {
        String username = uniqueUsername();
        register(username);

        User savedUser = users.findByUsername(username).orElseThrow();
        assertNotEquals(PASSWORD, savedUser.getPasswordHash());
        assertTrue(savedUser.getPasswordHash().startsWith("$2"));

        String token = login(username);
        String postBody = "{\"title\":\"<script>alert(1)</script>\","
                + "\"content\":\"<img src=x onerror=alert(1)>\"}";

        mockMvc.perform(post("/api/data")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(containsString("&lt;script&gt;")));

        mockMvc.perform(get("/api/data").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(containsString("&lt;img")));

        String otherUsername = uniqueUsername();
        register(otherUsername);
        String otherToken = login(otherUsername);
        mockMvc.perform(get("/api/data").header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"' OR 1=1 --\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    private void register(String username) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
    }

    private String login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
        Matcher matcher = TOKEN_PATTERN.matcher(result.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new AssertionError("Login response has no JWT");
        }
        return matcher.group(1);
    }

    private String uniqueUsername() {
        return "student_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
