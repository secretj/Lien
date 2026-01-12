package com.lien.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * ==============================================================================
 * Spring Security 설정
 * ==============================================================================
 * 
 * 역할:
 * - Spring Security의 보안 설정 정의
 * - CORS(Cross-Origin Resource Sharing) 설정
 * - CSRF(Cross-Site Request Forgery) 설정
 * - API 엔드포인트별 권한 설정
 * 
 * 현재 설정:
 * - 모든 요청 허용 (anyRequest().permitAll())
 * - CSRF 비활성화 (REST API용)
 * - CORS 활성화 (프론트엔드 연동)
 * 
 * 보안 고려사항:
 * 1. JWT 기반 인증 사용 (Stateless)
 * 2. 세션 사용하지 않음
 * 3. 프론트엔드와 백엔드가 분리된 구조
 * 4. 현재는 모든 요청 허용 (개발 단계)
 * 
 * 프로덕션 강화 권장사항:
 * <pre>
 * // JWT 필터 추가
 * .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
 * 
 * // 엔드포인트별 권한 설정
 * .authorizeHttpRequests(auth -> auth
 *     .requestMatchers("/api/auth/**").permitAll()  // 인증 API는 모두 허용
 *     .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger 허용
 *     .anyRequest().authenticated()  // 나머지는 인증 필요
 * )
 * 
 * // 세션 정책 설정
 * .sessionManagement(session -> 
 *     session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
 * )
 * </pre>
 * 
 * CORS 설정:
 * - 프론트엔드(React, Vue 등)와 백엔드가 다른 포트에서 실행될 때 필요
 * - 개발: localhost:3000, localhost:5173 등
 * - 프로덕션: 실제 도메인으로 변경 필요
 * 
 * CSRF 설정:
 * - REST API에서는 일반적으로 비활성화
 * - JWT 토큰을 사용하므로 CSRF 공격에 덜 취약
 * - 단, XSS(Cross-Site Scripting) 공격에는 여전히 취약할 수 있음
 * 
 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration
 * @see com.lien.security.JwtUtil
 * @see com.lien.security.CurrentUserArgumentResolver
 */
@Configuration
public class SecurityConfig {
    
    /**
     * Spring Security 필터 체인 설정
     * 
     * Spring Security의 핵심 설정을 정의합니다.
     * HTTP 요청에 대한 보안 정책을 설정합니다.
     * 
     * 설정 항목:
     * 1. CORS: Cross-Origin Resource Sharing 활성화
     * 2. CSRF: Cross-Site Request Forgery 비활성화 (REST API용)
     * 3. 권한 설정: 모든 요청 허용 (개발 단계)
     * 
     * CORS 설정:
     * - 프론트엔드와 백엔드가 다른 origin(도메인/포트)에서 실행될 때 필요
     * - 브라우저의 Same-Origin Policy를 우회하기 위해 사용
     * - corsConfigurationSource() 메서드에서 상세 설정
     * 
     * CSRF 설정:
     * - REST API에서는 일반적으로 비활성화
     * - JWT 토큰을 사용하므로 세션 기반 CSRF 공격에 안전
     * - 대신 XSS 공격 방지에 주의 필요 (Content-Security-Policy 헤더 등)
     * 
     * 권한 설정 (현재):
     * - anyRequest().permitAll(): 모든 요청 허용
     * - 개발 단계에서는 편리하나, 프로덕션에서는 보안 강화 필요
     * 
     * 프로덕션 권장 설정:
     * <pre>
     * .authorizeHttpRequests(auth -> auth
     *     // 인증 API는 모두 허용
     *     .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
     *     
     *     // Swagger 문서는 허용 (또는 프로덕션에서는 제한)
     *     .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
     *     
     *     // Actuator 엔드포인트는 관리자만
     *     .requestMatchers("/actuator/**").hasRole("ADMIN")
     *     
     *     // 나머지는 모두 인증 필요
     *     .anyRequest().authenticated()
     * )
     * </pre>
     * 
     * @param http HttpSecurity 설정 객체
     * @return SecurityFilterChain Spring Security 필터 체인
     * @throws Exception 설정 오류 시 발생
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정 활성화
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF 비활성화 (REST API는 CSRF 토큰 불필요)
            .csrf(csrf -> csrf.disable())
            
            // 모든 요청 허용 (개발 단계)
            // 프로덕션에서는 엔드포인트별로 권한 설정 필요
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        
        return http.build();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정
     * 
     * 프론트엔드와 백엔드가 다른 origin(도메인/포트)에서 실행될 때 필요한 설정입니다.
     * 브라우저의 Same-Origin Policy를 우회하여 서로 다른 origin 간 통신을 허용합니다.
     * 
     * CORS 동작 과정:
     * <pre>
     * 1. 프론트엔드(http://localhost:3000)에서 백엔드(http://localhost:8080) API 호출
     * 2. 브라우저가 Preflight Request(OPTIONS) 전송
     * 3. 백엔드가 CORS 헤더 응답 (Access-Control-Allow-Origin 등)
     * 4. 브라우저가 실제 요청 전송 (GET, POST 등)
     * 5. 백엔드가 응답
     * </pre>
     * 
     * 설정 항목:
     * 
     * 1. Allowed Origins (허용된 출처)
     *    - localhost:3000: Create React App 기본 포트
     *    - localhost:5173: Vite 기본 포트
     *    - localhost:8081: Nginx 클라이언트 포트
     *    - 127.0.0.1:8081: Nginx 클라이언트 IP 포트
     *    
     *    프로덕션 설정 예시:
     *    <pre>
     *    configuration.setAllowedOrigins(Arrays.asList(
     *        "https://yourdomain.com",
     *        "https://www.yourdomain.com"
     *    ));
     *    
     *    // 또는 패턴 사용
     *    configuration.setAllowedOriginPatterns(Arrays.asList("https://*.yourdomain.com"));
     *    </pre>
     * 
     * 2. Allowed Methods (허용된 HTTP 메서드)
     *    - GET: 조회
     *    - POST: 생성
     *    - PUT: 수정
     *    - DELETE: 삭제
     *    - OPTIONS: Preflight 요청
     * 
     * 3. Allowed Headers (허용된 헤더)
     *    - "*": 모든 헤더 허용
     *    - Authorization: JWT 토큰
     *    - Content-Type: 요청 본문 형식
     *    
     *    프로덕션 설정 예시:
     *    <pre>
     *    configuration.setAllowedHeaders(Arrays.asList(
     *        "Authorization",
     *        "Content-Type",
     *        "Accept",
     *        "X-Requested-With"
     *    ));
     *    </pre>
     * 
     * 4. Allow Credentials (인증 정보 포함 허용)
     *    - true: 쿠키, Authorization 헤더 포함 가능
     *    - false: 인증 정보 불포함
     *    
     *    주의사항:
     *    - allowCredentials(true) 설정 시 allowedOrigins에 "*" 사용 불가
     *    - 반드시 구체적인 origin 지정 필요
     * 
     * 보안 고려사항:
     * - 프로덕션에서는 실제 프론트엔드 도메인만 허용
     * - "*"(와일드카드) 사용 지양
     * - HTTPS 사용 필수
     * 
     * @return CorsConfigurationSource CORS 설정 객체
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 origin(출처) 설정
        // 개발 환경: React, Vite, Nginx 등 다양한 개발 서버
        // 프로덕션: 실제 도메인으로 변경 필요
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",      // Create React App
            "http://localhost:5173",      // Vite
            "http://localhost:8081",      // Nginx Client
            "http://127.0.0.1:8081"       // Nginx Client (IP)
        ));
        
        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList(
            "GET",      // 조회
            "POST",     // 생성
            "PUT",      // 수정
            "DELETE",   // 삭제
            "OPTIONS"   // Preflight 요청
        ));
        
        // 허용할 헤더 설정
        // "*": 모든 헤더 허용 (개발 단계)
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 인증 정보 포함 허용
        // true: 쿠키, Authorization 헤더 포함 가능
        configuration.setAllowCredentials(true);
        
        // URL 패턴별 CORS 설정 등록
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 대해 CORS 설정 적용
        
        return source;
    }
}