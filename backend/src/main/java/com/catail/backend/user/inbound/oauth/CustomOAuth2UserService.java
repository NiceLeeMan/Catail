package com.catail.backend.user.inbound.oauth;

import com.catail.backend.global.BusinessException;
import com.catail.backend.user.DB.User;
import com.catail.backend.user.application.UserErrorCode;
import com.catail.backend.user.application.UserService;
import com.catail.backend.user.outbound.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthUserInfo userInfo = switch (registrationId) {
            case "google" -> new GoogleUserInfo(attributes);
            default -> throw new BusinessException(UserErrorCode.UNSUPPORTED_PROVIDER);
        };

        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());
        User user = userService.loginOrRegister(provider, userInfo.getProviderId(), userInfo.getEmail());

        return new CustomOAuth2User(user.getId(), attributes);
    }
}
