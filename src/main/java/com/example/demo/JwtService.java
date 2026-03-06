package com.example.demo;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.*;

import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final KeyStoreService keyStoreService;

    public JwtService(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    public String generateAccessToken(String userId) throws Exception {

        PrivateKey privateKey = keyStoreService.getPrivateKey();

        JWSSigner signer = new RSASSASigner(privateKey);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId)
                .issuer("qr-login-server")
                .issueTime(new Date())
                .expirationTime(
                        Date.from(Instant.now().plusSeconds(3600))
                )
                .claim("scope", "user")
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader(JWSAlgorithm.RS256),
                claims
        );

        jwt.sign(signer);

        return jwt.serialize();
    }
}