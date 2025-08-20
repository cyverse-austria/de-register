package com.cyverse.api.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cyverse.api.config.AuthUserConfig;
import com.cyverse.api.exceptions.ApiTokenExpiredException;
import com.cyverse.api.exceptions.UnauthorizedAccessException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Authentication service based on JWTs.
 */
public class AuthService {

    private final Map<String, AuthUserConfig> users;
    private static final String TOKEN_ISSUER = "http://api-service.cyverse.at";
    private Algorithm algorithm;
    private static final String AUTH_SECRET = "AUTH_SECRET";

    public static final Long EXPIRES_IN_MS = 3600 * 1000L;

    public AuthService(Map<String, AuthUserConfig> users) {
        this.users = users;
    }

    public void init() {
        String secret = System.getenv(AUTH_SECRET);
        algorithm = Algorithm.HMAC256(secret);
    }

    /**
     * Generate a new JWT token with a default expiration time, based on
     * the known user that tries to log in.
     */
    public String generateToken(String mail, String password)
            throws UnauthorizedAccessException {
        List<Map.Entry<String, AuthUserConfig>> matchUsers = users.entrySet().stream()
                .filter(u -> u.getValue().getMail().equals(mail)
                        && u.getValue().getPassword().equals(password))
                .toList();
        if (matchUsers.isEmpty()) {
            throw new UnauthorizedAccessException("User not found");
        }

        Date now = new Date();
        Date expires = new Date(now.getTime() + EXPIRES_IN_MS);
        return JWT.create()
                .withIssuer(TOKEN_ISSUER)
                .withSubject(matchUsers.get(0).getKey()) // username
                .withIssuedAt(now)
                .withExpiresAt(expires)
                .sign(algorithm);
    }

    /**
     * Basit JWT Token check by sub and issuer.
     */
    public void verifyToken(String token)
            throws UnauthorizedAccessException, JWTVerificationException,
            ApiTokenExpiredException {
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedAccessException("Missing JWT token");
        }

        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        if (!(users.containsKey(decodedJWT.getSubject())
                && Objects.equals(decodedJWT.getIssuer(), TOKEN_ISSUER))) {
            throw new UnauthorizedAccessException("Unknown sub/issuer");
        }

        Date now = new Date();
        if (decodedJWT.getExpiresAt().before(now)) {
            throw new ApiTokenExpiredException();
        }
    }
}
