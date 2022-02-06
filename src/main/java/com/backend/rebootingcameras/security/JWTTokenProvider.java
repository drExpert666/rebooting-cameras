package com.backend.rebootingcameras.security;

import com.backend.rebootingcameras.entity.User;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** класс по созданию и проверке токенов  */
@Component
public class JWTTokenProvider {

    /* логгер по этому классу */
    //todo посмотреть, надо ли менять реализацию,
    // так как у меня уже подключен собственный логгер у всего приложения (RebootingCamerasApplication.class)
    public static final Logger LOG = LoggerFactory.getLogger(JWTTokenProvider.class);

    public String generateToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Date now = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(now.getTime() + SecurityConstants.EXPIRATION_TIME); // вычисляем время, когда закончится действие токена

        String userId = Long.toString(user.getId());

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("id", userId);
        claimsMap.put("username", user.getUsername());

        // Jwts прописали отельно в зависимости
        return Jwts.builder()
                .setSubject(userId)
                .addClaims(claimsMap)
                .setIssuedAt(now) // когда сгенерирован токен
                .setExpiration(expiryDate) // когда закончится действие токена
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET) // алгоритмы шифрования
                .compact();
    }

    /* проверяем пришедший токен */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(SecurityConstants.SECRET) // декодируем блягодаря нашему секретному слову
                    .parseClaimsJws(token); // берём все клэймсы, записанные в токен при передаче
            return true;
        } catch (SignatureException |
                MalformedJwtException |
                ExpiredJwtException |
                UnsupportedJwtException |
                IllegalArgumentException exception) {
            LOG.error(exception.getMessage());
            return false;
        }
    }

    /* берем id из токена */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SecurityConstants.SECRET)
                .parseClaimsJws(token)
                .getBody();
        String id = (String) claims.get("id");
        return Long.parseLong(id);
    }

}
