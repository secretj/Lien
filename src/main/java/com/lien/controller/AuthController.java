package com.lien.controller;

import com.lien.entity.User;
import com.lien.service.UserService;
import com.lien.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ==============================================================================
 * Auth Controller (인증 컨트롤러)
 * ==============================================================================
 * 
 * 역할:
 * - 사용자 인증 관련 REST API 엔드포인트 제공
 * - 회원가입 및 로그인 처리
 * - JWT 토큰 발급
 * 
 * Base URL: /api/auth
 * 
 * API 목록:
 * - POST /api/auth/register  : 회원가입
 * - POST /api/auth/login     : 로그인
 * 
 * 응답 형식: JSON
 * 인증 필요: 없음 (Public API)
 * 
 * 전체 인증 흐름:
 * <pre>
 * 1. 회원가입: POST /api/auth/register
 *    → 이메일, 비밀번호, 이름 전송
 *    → 서버에서 비밀번호 암호화 (BCrypt)
 *    → 사용자 정보 반환
 * 
 * 2. 로그인: POST /api/auth/login
 *    → 이메일, 비밀번호 전송
 *    → 서버에서 비밀번호 검증
 *    → JWT 토큰 발급 및 반환
 * 
 * 3. 인증 필요한 API 호출:
 *    → Authorization 헤더에 JWT 토큰 포함
 *    → Bearer {token}
 * </pre>
 * 
 * 보안:
 * - 비밀번호는 BCrypt로 암호화하여 저장
 * - JWT 토큰은 HMAC SHA-256으로 서명
 * - 토큰 만료 시간: 기본 1시간 (설정 가능)
 * 
 * @see UserService
 * @see JwtUtil
 */
@Tag(name = "1. 인증", description = "회원가입 및 로그인 API (인증 불필요)")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 회원가입
     * 
     * HTTP Method: POST
     * URL: /api/auth/register
     * 인증: 불필요
     * 
     * 역할:
     * - 새로운 사용자 등록
     * - 이메일 중복 체크
     * - 비밀번호 BCrypt 암호화
     * 
     * 요청 본문:
     * <pre>
     * {
     *   "email": "user@example.com",
     *   "password": "password123",
     *   "name": "홍길동"
     * }
     * </pre>
     * 
     * 성공 응답 (200 OK):
     * <pre>
     * {
     *   "id": 1,
     *   "email": "user@example.com",
     *   "name": "홍길동"
     * }
     * </pre>
     * 
     * 에러 응답 (400 Bad Request):
     * <pre>
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "이미 가입된 이메일입니다."
     * }
     * </pre>
     * 
     * curl 예시:
     * <pre>
     * curl -X POST http://localhost:8080/api/auth/register \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "email": "user@example.com",
     *     "password": "password123",
     *     "name": "홍길동"
     *   }'
     * </pre>
     * 
     * JavaScript 예시:
     * <pre>
     * {@code
     * fetch('http://localhost:8080/api/auth/register', {
     *   method: 'POST',
     *   headers: {
     *     'Content-Type': 'application/json'
     *   },
     *   body: JSON.stringify({
     *     email: 'user@example.com',
     *     password: 'password123',
     *     name: '홍길동'
     *   })
     * })
     * .then(res => res.json())
     * .then(data => {
     *   console.log('회원가입 성공:', data);
     *   // 로그인 페이지로 이동
     *   window.location.href = '/login';
     * })
     * .catch(err => {
     *   console.error('회원가입 실패:', err);
     *   alert('이미 가입된 이메일입니다.');
     * });
     * }
     * </pre>
     * 
     * @param req 요청 데이터 (email, password, name)
     * @return ResponseEntity 사용자 정보 (id, email, name)
     */
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다. 이메일 중복 시 400 에러 발생"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "id": 1,
                          "email": "user@example.com",
                          "name": "홍길동"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "이미 가입된 이메일",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2024-01-15T10:30:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "이미 가입된 이메일입니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "")  // 인증 불필요
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "회원가입 요청 데이터",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                            {
                              "email": "user@example.com",
                              "password": "password123",
                              "name": "홍길동"
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        String name = req.get("name");
        
        User user = userService.register(email, password, name);
        
        return ResponseEntity.ok(Map.of(
            "id", user.getId(), 
            "email", user.getEmail(), 
            "name", user.getName()
        ));
    }

    /**
     * 로그인
     * 
     * HTTP Method: POST
     * URL: /api/auth/login
     * 
     * 역할:
     * - 사용자 인증
     * - JWT 토큰 발급
     * - 사용자 정보 반환
     * 
     * 요청 본문:
     * <pre>
     * {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     * </pre>
     * 
     * 성공 응답 (200 OK):
     * <pre>
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...",
     *   "email": "user@example.com",
     *   "name": "홍길동"
     * }
     * </pre>
     * 
     * JWT 토큰 구조:
     * <pre>
     * eyJhbGciOiJIUzI1NiJ9          # Header (알고리즘)
     * .eyJzdWIiOiJ1c2VyQGV4YW1...   # Payload (사용자 정보)
     * .SflKxwRJSMeKKF2QT4fwpM...   # Signature (서명)
     * </pre>
     * 
     * 에러 응답 (400 Bad Request):
     * <pre>
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "이메일 또는 비밀번호가 올바르지 않습니다."
     * }
     * </pre>
     * 
     * curl 예시:
     * <pre>
     * curl -X POST http://localhost:8080/api/auth/login \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "email": "user@example.com",
     *     "password": "password123"
     *   }'
     * </pre>
     * 
     * JavaScript 예시:
     * <pre>
     * {@code
     * fetch('http://localhost:8080/api/auth/login', {
     *   method: 'POST',
     *   headers: {
     *     'Content-Type': 'application/json'
     *   },
     *   body: JSON.stringify({
     *     email: 'user@example.com',
     *     password: 'password123'
     *   })
     * })
     * .then(res => res.json())
     * .then(data => {
     *   console.log('로그인 성공:', data);
     *   
     *   // JWT 토큰 저장 (LocalStorage)
     *   localStorage.setItem('token', data.token);
     *   localStorage.setItem('email', data.email);
     *   localStorage.setItem('name', data.name);
     *   
     *   // 메인 페이지로 이동
     *   window.location.href = '/home';
     * })
     * .catch(err => {
     *   console.error('로그인 실패:', err);
     *   alert('이메일 또는 비밀번호가 올바르지 않습니다.');
     * });
     * }
     * </pre>
     * 
     * 인증 필요한 API 호출 예시:
     * <pre>
     * {@code
     * // JWT 토큰으로 인증된 API 호출
     * const token = localStorage.getItem('token');
     * 
     * fetch('http://localhost:8080/api/templates', {
     *   method: 'GET',
     *   headers: {
     *     'Authorization': `Bearer ${token}`,  // JWT 토큰 포함
     *     'Content-Type': 'application/json'
     *   }
     * })
     * .then(res => res.json())
     * .then(data => {
     *   console.log('템플릿 목록:', data);
     * });
     * }
     * </pre>
     * 
     * @param req 요청 데이터 (email, password)
     * @return ResponseEntity JWT 토큰 및 사용자 정보
     */
    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공 (JWT 토큰 발급)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjM5NTM2MDAwLCJleHAiOjE2Mzk1Mzk2MDB9.abcdef123456",
                          "email": "user@example.com",
                          "name": "홍길동"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "이메일 또는 비밀번호가 올바르지 않음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2024-01-15T10:30:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "이메일 또는 비밀번호가 올바르지 않습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "")  // 인증 불필요
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "로그인 요청 데이터",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                            {
                              "email": "user@example.com",
                              "password": "password123"
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        
        // 사용자 인증
        User user = userService.authenticate(email, password);
        
        // JWT 토큰 생성
        String token = JwtUtil.generateToken(user.getEmail());
        
        return ResponseEntity.ok(Map.of(
            "token", token, 
            "email", user.getEmail(), 
            "name", user.getName()
        ));
    }
} 