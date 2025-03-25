package com.example.addressBook.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final String SECRET_KEY = "Lock"; // Move this to application.properties

    // Generate JWT Token
    public String createToken(Long userId) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withClaim("user_id", userId)
                .sign(algorithm);
    }

    // Verify Token and get User ID
    public Long verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT.getClaim("user_id").asLong();
    }
}
