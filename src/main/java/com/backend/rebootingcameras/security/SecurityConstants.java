package com.backend.rebootingcameras.security;

public class SecurityConstants {

    public static final String SIGN_UP_URLS = "/api/auth/**";
    public static final String GET_USERS_URLS = "/users/find/**";

    /* поля, необходимые для генерации веб-токена */
    public static final String SECRET = "SecretKeyGenJWT";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String CONTENT_TYPE = "application/json";
    public static final long EXPIRATION_TIME = 60_000_000; // время истечения срока токена (1000 мин)


}
