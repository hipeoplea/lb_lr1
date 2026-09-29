package org.hipeoplea.secureapi.post;

import java.util.List;
import org.hipeoplea.secureapi.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findTop100ByOwnerOrderByIdDesc(User owner);
}
