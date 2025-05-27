package com.authproject.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;

    @Value("${jwt.validity:3600000}")
    private long validityInMilliseconds; // 1 hora por padrão

    // Método para criar um token
    public String createToken(String username, List<String> roles, List<String> scopes) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("scopes", scopes)
                .issuedAt(now)
                .expiration(validity)
                .signWith(this.privateKey)
                .compact();
    }

    // Método para extrair claims
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Método para extrair o nome de usuário
    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return claims.getSubject();
    }

    // Método para obter autoridades, garantindo ROLE_ADMIN e ADMIN
    public List<GrantedAuthority> getAuthorities(Claims claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        try {
            // Tenta extrair roles como lista
            Object rolesObj = claims.get("roles");
            if (rolesObj != null) {
                if (rolesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) rolesObj;
                    for (String role : roles) {
                        // Adiciona tanto a versão com prefixo ROLE_ quanto sem
                        if (!role.startsWith("ROLE_")) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                        }
                        authorities.add(new SimpleGrantedAuthority(role));
                        logger.debug("Added authority from roles: {}", role);
                    }
                } else if (rolesObj instanceof String) {
                    // Se for uma string única
                    String roleStr = (String) rolesObj;
                    // Adiciona tanto a versão com prefixo ROLE_ quanto sem
                    if (!roleStr.startsWith("ROLE_")) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr));
                    }
                    authorities.add(new SimpleGrantedAuthority(roleStr));
                    logger.debug("Added authority from role string: {}", roleStr);
                }
            }

            // Tenta extrair scope/scopes (comuns em tokens OAuth2)
            // Primeiro tenta como string única (formato comum: "scope1 scope2 scope3")
            Object scopeObj = claims.get("scope");
            if (scopeObj instanceof String) {
                String scopeStr = (String) scopeObj;
                String[] scopes = scopeStr.split(" ");
                for (String scope : scopes) {
                    if (!scope.trim().isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority(scope.trim()));
                        logger.debug("Added authority from scope string: {}", scope.trim());
                    }
                }
            }

            // Depois tenta como lista de scopes
            Object scopesObj = claims.get("scopes");
            if (scopesObj != null) {
                if (scopesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> scopes = (List<String>) scopesObj;
                    for (String scope : scopes) {
                        authorities.add(new SimpleGrantedAuthority(scope));
                        logger.debug("Added authority from scopes list: {}", scope);
                    }
                } else if (scopesObj instanceof String) {
                    String scopeStr = (String) scopesObj;
                    authorities.add(new SimpleGrantedAuthority(scopeStr));
                    logger.debug("Added authority from scopes string: {}", scopeStr);
                }
            }

            // Imprime todas as autoridades concedidas para depuração
            logger.debug("All granted authorities: {}", authorities);

        } catch (Exception e) {
            logger.error("Error extracting authorities from JWT: {}", e.getMessage(), e);
        }

        return authorities;
    }
}