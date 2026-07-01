package com.catail.backend.user.inbound.oauth;

public interface OAuthUserInfo {
    String getProviderId();
    String getEmail();
}
