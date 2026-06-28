package com.catail.backend.user.application;

import com.catail.backend.user.DB.User;
import com.catail.backend.user.DB.UserRepository;
import com.catail.backend.user.outbound.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // UC-1: OAuth 로그인/회원가입
    public User loginOrRegister(OAuthProvider provider, String providerId, String email) {
        throw new UnsupportedOperationException();
    }

    // UC-2: 프로필 조회
    public User getProfile(Long userId) {
        throw new UnsupportedOperationException();
    }

    // UC-3: 로그아웃
    @Transactional
    public void logout(Long userId) {
        throw new UnsupportedOperationException();
    }

    // UC-4: 회원 탈퇴
    @Transactional
    public void deleteAccount(Long userId) {
        throw new UnsupportedOperationException();
    }
}