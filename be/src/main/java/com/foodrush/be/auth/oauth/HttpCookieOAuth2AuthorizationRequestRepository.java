package com.foodrush.be.auth.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Cookie-based authorization request repository for stateless OAuth2 login.
 * Solves authorization_request_not_found caused by stateless sessions or cross-domain/reverse-proxy cookie issues.
 */
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        log.info("[OAuth2 CookieRepo] loadAuthorizationRequest for URI: {}", request.getRequestURI());
        logCookies(request);

        Optional<Cookie> cookieOpt = getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        if (cookieOpt.isEmpty()) {
            log.warn("[OAuth2 CookieRepo] Cookie '{}' not found in incoming request", OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return null;
        }

        OAuth2AuthorizationRequest authRequest = deserialize(cookieOpt.get());
        if (authRequest == null) {
            log.error("[OAuth2 CookieRepo] Failed to deserialize cookie value");
        } else {
            log.info("[OAuth2 CookieRepo] Successfully loaded authorization request. state={}, redirectUri={}",
                    authRequest.getState(), authRequest.getRedirectUri());
        }
        return authRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        log.info("[OAuth2 CookieRepo] saveAuthorizationRequest called for URI: {}", request.getRequestURI());
        if (authorizationRequest == null) {
            log.info("[OAuth2 CookieRepo] authorizationRequest is null, deleting cookie");
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return;
        }

        String serialized = serialize(authorizationRequest);
        boolean isHttps = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));

        ResponseCookie responseCookie = ResponseCookie.from(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized)
                .path("/")
                .httpOnly(true)
                .maxAge(COOKIE_EXPIRE_SECONDS)
                .secure(isHttps)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        log.info("[OAuth2 CookieRepo] Set-Cookie header added: {}. isHttps={}, state={}, length={}",
                responseCookie, isHttps, authorizationRequest.getState(), serialized.length());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        log.info("[OAuth2 CookieRepo] removeAuthorizationRequest(request, response) called for URI: {}", request.getRequestURI());
        OAuth2AuthorizationRequest originalRequest = loadAuthorizationRequest(request);
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        return originalRequest;
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    private void logCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            log.info("[OAuth2 CookieRepo] Request has NO cookies attached. Headers: Host={}, X-Forwarded-Proto={}, X-Forwarded-For={}",
                    request.getHeader("Host"), request.getHeader("X-Forwarded-Proto"), request.getHeader("X-Forwarded-For"));
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Cookie c : cookies) {
            sb.append(c.getName()).append("=").append(c.getName().equals(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME) ? "[PRESENT]" : c.getValue()).append("; ");
        }
        log.info("[OAuth2 CookieRepo] Request cookies: {}", sb.toString());
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        boolean isHttps = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        ResponseCookie responseCookie = ResponseCookie.from(name, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(isHttps)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    private String serialize(OAuth2AuthorizationRequest object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
            return Base64.getUrlEncoder().encodeToString(baos.toByteArray());
        } catch (Exception ex) {
            log.error("[OAuth2 CookieRepo] Failed to serialize OAuth2AuthorizationRequest: {}", ex.getMessage());
            throw new IllegalArgumentException("Failed to serialize OAuth2AuthorizationRequest", ex);
        }
    }

    private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (OAuth2AuthorizationRequest) ois.readObject();
        } catch (Exception ex) {
            log.error("[OAuth2 CookieRepo] Failed to deserialize cookie: {}", ex.getMessage());
            return null;
        }
    }
}
