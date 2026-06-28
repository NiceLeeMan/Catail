package com.catail.backend.user.DB;

import com.catail.backend.user.outbound.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}