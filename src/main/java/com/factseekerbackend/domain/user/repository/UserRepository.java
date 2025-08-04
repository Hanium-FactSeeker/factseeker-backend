package com.factseekerbackend.domain.user.repository;

import com.factseekerbackend.domain.user.entity.AuthProvider;
import com.factseekerbackend.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByLoginId(String loginId);

  boolean existsByEmail(String email);

  Optional<User> findByLoginId(String loginId);

  void deleteByLoginId(String loginId);

  Optional<User> findByEmail(String email);

  Optional<User> findByLoginIdAndEmail(String loginId, String email);

  Optional<User> findBySocialIdAndAuthProvider(String socialId, AuthProvider authProvider);

}
