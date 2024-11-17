package ntou.cs.project.Util;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Date;
import java.util.UUID;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Claims;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final int EXPIRATION_TIME = 86400;
    SecretKey secretKey = Keys.hmacShaKeyFor(
            "6bee699621687e6092e0de24fd24a2d6cca91ca8dc610d7a8ec0ca794493262d".getBytes(StandardCharsets.UTF_8));

    public String generateToken(
            Map<String, String> claims,
            String subject,
            String jti) {
        int expireSecond = EXPIRATION_TIME * 1000;
        Date expireDate = new Date(System.currentTimeMillis() + expireSecond);
        if (jti == null) {
            UUID uuid = UUID.randomUUID();
            jti = uuid.toString();
        }
        return Jwts.builder().setClaims(claims).setSubject(subject)
                .setId(jti)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expireDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    public Boolean validateToken(String token) {
        final Date expDate = getClaimFromToken(token, Claims::getExpiration);
        return expDate.before(new Date()) == false;
    }

    public String getSubjectFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
