package org.hipeoplea.secureapi.post;

import java.util.List;
import org.hipeoplea.secureapi.post.dto.CreatePostRequest;
import org.hipeoplea.secureapi.post.dto.PostResponse;
import org.hipeoplea.secureapi.user.User;
import org.hipeoplea.secureapi.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

@Service
public class DataService {

    private final UserRepository users;
    private final PostRepository posts;

    public DataService(UserRepository users, PostRepository posts) {
        this.users = users;
        this.posts = posts;
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts(String username) {
        User owner = findUser(username);
        return posts.findTop100ByOwnerOrderByIdDesc(owner).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PostResponse createPost(String username, CreatePostRequest request) {
        User owner = findUser(username);
        Post post = posts.save(new Post(owner, request.getTitle(), request.getContent()));
        return toResponse(post);
    }

    private User findUser(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                HtmlUtils.htmlEscape(post.getTitle()),
                HtmlUtils.htmlEscape(post.getContent()),
                post.getCreatedAt());
    }
}
