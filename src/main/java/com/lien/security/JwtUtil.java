package com.lien.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * ==============================================================================
 * JWT (JSON Web Token) 유틸리티 클래스
 * ==============================================================================
 * 
 * 역할:
 * - JWT 토큰 생성 (로그인 시)
 * - JWT 토큰 검증 (API 요청 시)
 * - JWT 토큰에서 사용자 정보 추출
 * 
 * JWT 구조:
 * <pre>
 * eyJhbGciOiJIUzI1NiJ9                      ← Header (알고리즘)
 * .eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0     ← Payload (사용자 정보)
 * .SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ← Signature (서명)
 * </pre>
 * 
 * JWT 인증 흐름:
 * <pre>
 * 1. 로그인 (POST /api/auth/login)
 *    → 이메일, 비밀번호 검증
 *    → JwtUtil.generateToken(email) 호출
 *    → JWT 토큰 생성 및 반환
 * 
 * 2. 인증 필요한 API 호출
 *    → Authorization 헤더에 "Bearer {token}" 포함
 *    → CurrentUserArgumentResolver에서 JwtUtil.validateToken() 호출
 *    → JwtUtil.getEmailFromToken()로 이메일 추출
 *    → UserRepository에서 User 조회
 *    → Controller의 @CurrentUser 파라미터에 User 객체 주입
 * </pre>
 * 
 * 보안:
 * - HMAC SHA-256 알고리즘 사용
 * - 비밀 키(Secret Key)로 서명
 * - 토큰 만료 시간 설정 (기본 1시간)
 * - 토큰 변조 시 검증 실패
 * 
 * 설정:
 * <pre>
 * # application.properties
 * jwt.secret=${JWT_SECRET_KEY}           # 환경 변수로 설정 (보안)
 * jwt.expiration=${JWT_EXPIRATION_MS:3600000}  # 1시간 (밀리초)
 * 
 * # .env 파일
 * JWT_SECRET_KEY=your-256-bit-secret-key-here-must-be-long-enough
 * JWT_EXPIRATION_MS=3600000
 * </pre>
 * 
 * 주의사항:
 * 1. Secret Key는 최소 256비트(32자) 이상이어야 함
 * 2. Secret Key는 환경 변수로 관리 (코드에 하드코딩 금지)
 * 3. 프로덕션에서는 HTTPS 사용 필수
 * 4. Refresh Token 구현 권장 (현재는 Access Token만 구현)
 * 
 * 사용 예시:
 * <pre>
 * // 1. 로그인 시 토큰 생성
 * String token = JwtUtil.generateToken("user@example.com");
 * // 결과: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0..."
 * 
 * // 2. 토큰 검증
 * boolean isValid = JwtUtil.validateToken(token);
 * // 결과: true (유효) / false (만료 또는 변조)
 * 
 * // 3. 토큰에서 이메일 추출
 * String email = JwtUtil.getEmailFromToken(token);
 * // 결과: "user@example.com"
 * </pre>
 * 
 * @see io.jsonwebtoken.Jwts
 * @see com.lien.security.CurrentUserArgumentResolver
 * @see com.lien.controller.AuthController
 */
@Component
public class JwtUtil {
    
    /**
     * JWT 서명에 사용되는 비밀 키
     * 
     * application.properties의 jwt.secret 값을 주입받습니다.
     * 환경 변수(JWT_SECRET_KEY)로 설정하여 보안을 유지합니다.
     * 
     * 요구사항:
     * - 최소 256비트(32자) 이상
     * - 랜덤한 문자열 사용
     * - 절대 코드에 하드코딩하지 말 것
     * 
     * 생성 방법 (OpenSSL):
     * <pre>
     * openssl rand -base64 32
     * </pre>
     */
    private static String secretKey;
    
    /**
     * JWT 토큰 만료 시간 (밀리초)
     * 
     * application.properties의 jwt.expiration 값을 주입받습니다.
     * 기본값: 3600000 (1시간)
     * 
     * 권장 설정:
     * - Access Token: 15분 ~ 1시간
     * - Refresh Token: 7일 ~ 30일 (현재 미구현)
     */
    private static long expirationMs;

    /**
     * Secret Key 설정 (Setter Injection)
     * 
     * @Value 어노테이션을 통해 application.properties의 jwt.secret 값을 주입받습니다.
     * static 필드를 초기화하기 위해 setter 메서드를 사용합니다.
     * 
     * @param secret JWT 서명에 사용할 비밀 키
     */
    @Value("${jwt.secret}")
    public void setSecretKey(String secret) {
        secretKey = secret;
    }

    /**
     * Expiration Time 설정 (Setter Injection)
     * 
     * @Value 어노테이션을 통해 application.properties의 jwt.expiration 값을 주입받습니다.
     * static 필드를 초기화하기 위해 setter 메서드를 사용합니다.
     * 
     * @param expiration JWT 토큰 만료 시간 (밀리초)
     */
    @Value("${jwt.expiration}")
    public void setExpirationMs(long expiration) {
        expirationMs = expiration;
    }

    /**
     * JWT 서명에 사용할 SecretKey 객체 생성
     * 
     * HMAC SHA-256 알고리즘에 적합한 키를 생성합니다.
     * Secret Key 문자열을 UTF-8 바이트 배열로 변환한 후 SecretKey 객체로 래핑합니다.
     * 
     * 역할:
     * - JWT 토큰 생성 시 서명에 사용
     * - JWT 토큰 검증 시 서명 확인에 사용
     * 
     * @return SecretKey HMAC SHA-256 서명 키
     */
    private static SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰 생성
     * 
     * 사용자 이메일을 subject로 하는 JWT 토큰을 생성합니다.
     * 로그인 성공 시 호출되어 클라이언트에게 반환됩니다.
     * 
     * JWT Payload 구조:
     * <pre>
     * {
     *   "sub": "user@example.com",  ← 사용자 이메일
     *   "iat": 1705324800,           ← 발급 시간 (Issued At)
     *   "exp": 1705328400            ← 만료 시간 (Expiration)
     * }
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * // AuthController.login()에서 호출
     * User user = userService.authenticate(email, password);
     * String token = JwtUtil.generateToken(user.getEmail());
     * 
     * return ResponseEntity.ok(Map.of(
     *     "token", token,
     *     "email", user.getEmail(),
     *     "name", user.getName()
     * ));
     * </pre>
     * 
     * @param email 사용자 이메일 (JWT의 subject로 사용)
     * @return String JWT 토큰 문자열
     */
    public static String generateToken(String email) {
        return Jwts.builder()
                .subject(email)  // 사용자 이메일을 subject로 설정
                .issuedAt(new Date())  // 현재 시간을 발급 시간으로 설정
                .expiration(new Date(System.currentTimeMillis() + expirationMs))  // 만료 시간 설정
                .signWith(getSigningKey())  // HMAC SHA-256으로 서명
                .compact();  // 토큰 문자열로 변환
    }

    /**
     * JWT 토큰에서 사용자 이메일 추출
     * 
     * JWT 토큰을 파싱하여 payload의 subject(이메일)를 추출합니다.
     * CurrentUserArgumentResolver에서 호출되어 사용자 정보를 조회하는 데 사용됩니다.
     * 
     * 동작 과정:
     * 1. JWT 토큰 파싱
     * 2. 서명 검증 (getSigningKey()로 검증)
     * 3. Payload(Claims) 추출
     * 4. Subject(이메일) 반환
     * 
     * 사용 예시:
     * <pre>
     * // CurrentUserArgumentResolver.resolveArgument()에서 호출
     * String token = authHeader.substring(7);  // "Bearer " 제거
     * String email = JwtUtil.getEmailFromToken(token);
     * User user = userRepository.findByEmail(email).orElseThrow();
     * </pre>
     * 
     * 예외 처리:
     * - 토큰이 변조된 경우: SignatureException
     * - 토큰이 만료된 경우: ExpiredJwtException
     * - 토큰 형식이 잘못된 경우: MalformedJwtException
     * 
     * @param token JWT 토큰 문자열
     * @return String 사용자 이메일
     * @throws io.jsonwebtoken.JwtException 토큰이 유효하지 않은 경우
     */
    public static String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())  // 서명 검증
                .build()
                .parseSignedClaims(token)  // 토큰 파싱
                .getPayload();  // Payload(Claims) 추출
        return claims.getSubject();  // Subject(이메일) 반환
    }

    /**
     * JWT 토큰 유효성 검증
     * 
     * JWT 토큰이 유효한지 검증합니다.
     * 서명, 만료 시간, 형식을 모두 확인합니다.
     * 
     * 검증 항목:
     * 1. 서명 검증: 토큰이 변조되지 않았는지 확인
     * 2. 만료 시간 검증: 토큰이 만료되지 않았는지 확인
     * 3. 형식 검증: JWT 형식이 올바른지 확인
     * 
     * 사용 예시:
     * <pre>
     * // CurrentUserArgumentResolver.resolveArgument()에서 호출
     * String token = authHeader.substring(7);
     * 
     * if (JwtUtil.validateToken(token)) {
     *     String email = JwtUtil.getEmailFromToken(token);
     *     return userRepository.findByEmail(email).orElseThrow();
     * }
     * 
     * throw new IllegalArgumentException("유효하지 않은 토큰입니다");
     * </pre>
     * 
     * @param token JWT 토큰 문자열
     * @return boolean true: 유효한 토큰, false: 유효하지 않은 토큰
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())  // 서명 검증
                    .build()
                    .parseSignedClaims(token);  // 토큰 파싱 (만료 시간 등 검증)
            return true;
        } catch (Exception e) {
            // 모든 JWT 예외를 catch하여 false 반환
            // - SignatureException: 서명 불일치
            // - ExpiredJwtException: 토큰 만료
            // - MalformedJwtException: 잘못된 형식
            // - UnsupportedJwtException: 지원하지 않는 JWT
            // - IllegalArgumentException: null 또는 빈 문자열
            return false;
        }
    }
} 