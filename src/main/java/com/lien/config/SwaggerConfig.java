package com.lien.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ==============================================================================
 * Swagger/OpenAPI 설정
 * ==============================================================================
 * 
 * 역할:
 * - REST API 문서 자동 생성
 * - Swagger UI 제공 (대화형 API 테스트 도구)
 * - JWT 인증 설정
 * - API 정보 및 서버 정보 설정
 * 
 * 접속 URL:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 * 
 * 이점:
 * 1. API 문서 자동 생성 (수동 문서 작성 불필요)
 * 2. API 테스트 간편화 (Postman 대체)
 * 3. 프론트엔드 개발자와 협업 시 명확한 API 명세 제공
 * 4. OpenAPI 표준 준수로 다양한 도구 연동 가능
 * 
 * JWT 인증 사용법:
 * <pre>
 * 1. Swagger UI 접속: http://localhost:8080/swagger-ui.html
 * 2. 우측 상단 "Authorize" 버튼 클릭
 * 3. Bearer {token} 형식으로 JWT 토큰 입력
 *    예: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...
 * 4. "Authorize" 버튼 클릭하여 인증
 * 5. 이후 모든 API 요청에 자동으로 JWT 토큰 포함
 * </pre>
 * 
 * 예시:
 * <pre>
 * # 1. 회원가입 (POST /api/auth/register)
 * curl -X POST http://localhost:8080/api/auth/register \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "email": "user@example.com",
 *     "password": "password123",
 *     "name": "홍길동"
 *   }'
 * 
 * # 2. 로그인 (POST /api/auth/login)
 * curl -X POST http://localhost:8080/api/auth/login \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "email": "user@example.com",
 *     "password": "password123"
 *   }'
 * 
 * # 응답:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "email": "user@example.com",
 *   "name": "홍길동"
 * }
 * 
 * # 3. 인증 필요한 API 호출 (GET /api/templates)
 * curl -X GET http://localhost:8080/api/templates \
 *   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
 * </pre>
 * 
 * 프로덕션 환경 비활성화:
 * <pre>
 * # application-prod.properties
 * springdoc.swagger-ui.enabled=false
 * springdoc.api-docs.enabled=false
 * </pre>
 * 
 * @see io.swagger.v3.oas.annotations.Operation
 * @see io.swagger.v3.oas.annotations.tags.Tag
 */
@Configuration
public class SwaggerConfig {
    
    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * OpenAPI 설정
     * 
     * 역할:
     * - API 문서의 기본 정보 설정
     * - JWT Bearer 인증 스키마 정의
     * - 서버 정보 설정
     * 
     * 구성:
     * - Info: API 제목, 설명, 버전, 연락처, 라이선스
     * - SecurityScheme: JWT Bearer 인증 방식 정의
     * - SecurityRequirement: 전역 보안 요구사항 설정
     * - Servers: API 서버 URL 목록
     * 
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 스키마 이름
        String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
                // API 기본 정보
                .info(new Info()
                        .title("Lien API Documentation")
                        .description("""
                                여행 계획 관리 시스템 REST API
                                
                                ## 주요 기능
                                - **회원가입/로그인**: JWT 기반 인증
                                - **여행 템플릿**: 여행 계획 생성, 조회, 수정, 삭제
                                - **체크리스트**: 여행 준비물 관리
                                - **일정 관리**: 일별 일정 및 활동 관리
                                - **위치 관리**: 여행지, 숙소, 음식점 등 위치 정보 관리
                                
                                ## 인증 방법
                                1. POST /api/auth/login으로 로그인
                                2. 응답으로 받은 JWT 토큰을 복사
                                3. 우측 상단 "Authorize" 버튼 클릭
                                4. "Bearer {token}" 형식으로 입력
                                
                                ## 기술 스택
                                - Spring Boot 3.5.3
                                - Java 21
                                - MySQL 8.0
                                - Redis
                                - JPA/Hibernate
                                - JWT
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Lien Development Team")
                                .email("support@lien.com")
                                .url("https://github.com/lien-project"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                
                // JWT 보안 스키마 정의
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)  // HTTP 인증 방식
                                .scheme("bearer")                 // Bearer 토큰 사용
                                .bearerFormat("JWT")              // JWT 형식
                                .description("""
                                        JWT 토큰을 입력하세요.
                                        
                                        1. POST /api/auth/login으로 로그인
                                        2. 응답의 "token" 필드 값을 복사
                                        3. "Bearer {token}" 형식으로 입력
                                        
                                        예시: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...
                                        """)))
                
                // 전역 보안 요구사항 (모든 API에 JWT 필요)
                // 단, @SecurityRequirement(name = "") 어노테이션으로 개별 API는 제외 가능
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                
                // 서버 정보
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.lien.com")
                                .description("프로덕션 서버")
                ));
    }
}
