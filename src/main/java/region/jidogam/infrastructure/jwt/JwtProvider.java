package region.jidogam.infrastructure.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.entity.User.Role;
import region.jidogam.infrastructure.jwt.exception.TokenException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.access-token-expiration}")
  private Duration accessTokenExpiration;

  @Value("${jwt.refresh-token-expiration}")
  private Duration refreshTokenExpiration;

  private static final String ISSUER = "region-jidogam";

  public String generateAccessToken(User user) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + accessTokenExpiration.toMillis());

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(user.getId().toString())
        .issuer(ISSUER)
        .issueTime(now)
        .expirationTime(exp)
        .claim("nickname", user.getNickname())
        .claim("email", user.getEmail())
        .claim("role", user.getRole())
        .claim("type", "access")
        .build();

    return signToken(claimsSet);
  }

  public String generateRefreshToken(User user) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + refreshTokenExpiration.toMillis());

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(user.getId().toString())
        .issuer(ISSUER)
        .issueTime(now)
        .expirationTime(exp)
        .claim("type", "refresh")
        .build();

    return signToken(claimsSet);
  }

  private String signToken(JWTClaimsSet claimsSet){
    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
    try {
      signedJWT.sign(new MACSigner(jwtSecret));
    } catch (JOSEException e) {
      throw new RuntimeException("Jwt signing error", e);
    }
    return signedJWT.serialize();
  }

  // 만료 시간만 추출
  public LocalDateTime extractExpirationTime(String token) {
    JWTClaimsSet claims = extractClaims(token);
    Date expirationTime = claims.getExpirationTime();
    return expirationTime.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
  }

  public String extractUserEmail(String token) {
    JWTClaimsSet claims = extractClaims(token);
    return claims.getClaim("email").toString();
  }

  public Role extractUserRole(String token) {
    JWTClaimsSet claims = extractClaims(token);
    return Role.valueOf(claims.getClaim("role").toString());
  }

  public UUID extractUserId(String token) {
    JWTClaimsSet claims = extractClaims(token);
    return UUID.fromString(claims.getSubject());
  }

  // 검증
  public boolean validateToken(String token) {
    try {
      extractClaims(token); // 내부에서 모든 검증(파싱, 서명, 만료시간, 발행자) 수행
      return true;
    } catch (Exception e) {
      log.warn("토큰 검증 실패: {}", e.getMessage());
      return false;
    }
  }

  // claims 추출 및 서명 검증
  private JWTClaimsSet extractClaims(String token) {
    try {
      SignedJWT jwt = SignedJWT.parse(token);
      JWSVerifier verifier = new MACVerifier(jwtSecret);

      if (!jwt.verify(verifier)) {
        throw new IllegalArgumentException("토큰 서명이 유효하지 않습니다");
      }

      JWTClaimsSet claims = jwt.getJWTClaimsSet();
      validateClaims(claims); // 발행자, 만료시간 검증
      return claims;

    } catch (Exception e) {
      // todo - 추후에 인가 과정에서 예외처리할지, 전역 예외처리 할지 고민해보기
      log.error("토큰 검증 및 파싱 에러");
      throw TokenException.withMessage(e.getMessage());
    }
  }

  // 발행자, 만료시간 검증
  private void validateClaims(JWTClaimsSet claims) {
    // issuer 검증
    String issuer = claims.getIssuer();
    if (issuer == null || !issuer.equals(ISSUER)) {
      throw new IllegalArgumentException("유효하지 않은 issuer: " + issuer);
    }

    // 만료 시간 검증
    Date expiration = claims.getExpirationTime();
    if (expiration == null) {
      throw new IllegalArgumentException("토큰에 만료시간이 없습니다");
    }

    if (expiration.before(new Date())) {
      throw new IllegalArgumentException("토큰이 만료되었습니다");
    }
  }
}
