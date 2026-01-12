package com.lien.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ==============================================================================
 * 현재 로그인한 사용자 정보 주입 어노테이션
 * ==============================================================================
 * 
 * 역할:
 * - Controller 메서드 파라미터에 사용하여 현재 로그인한 사용자 정보를 자동으로 주입
 * - JWT 토큰에서 사용자 정보를 추출하여 User 엔티티로 변환
 * - CurrentUserArgumentResolver와 함께 동작
 * 
 * 이점:
 * 1. 코드 중복 제거: 모든 Controller에서 JWT 토큰을 직접 파싱할 필요 없음
 * 2. 가독성 향상: 메서드 시그니처에서 명확하게 인증된 사용자 표시
 * 3. 일관성 유지: 사용자 정보 추출 로직이 한 곳에 집중
 * 4. 유지보수 용이성: 인증 로직 변경 시 ArgumentResolver만 수정
 * 
 * 사용 예시:
 * <pre>
 * // Before (어노테이션 사용 전)
 * {@literal @}GetMapping("/api/templates")
 * public ResponseEntity<List<TemplateResponse>> getTemplates(
 *         {@literal @}RequestHeader("Authorization") String authHeader) {
 *     
 *     // JWT 토큰 추출
 *     String token = authHeader.substring(7);
 *     
 *     // 토큰 검증
 *     if (!JwtUtil.validateToken(token)) {
 *         throw new IllegalArgumentException("유효하지 않은 토큰입니다");
 *     }
 *     
 *     // 이메일 추출
 *     String email = JwtUtil.getEmailFromToken(token);
 *     
 *     // 사용자 조회
 *     User user = userRepository.findByEmail(email)
 *         .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
 *     
 *     // 비즈니스 로직
 *     return ResponseEntity.ok(templateService.getTemplates(user));
 * }
 * 
 * // After (어노테이션 사용 후)
 * {@literal @}GetMapping("/api/templates")
 * public ResponseEntity<List<TemplateResponse>> getTemplates(
 *         {@literal @}CurrentUser User user) {  ← 자동으로 User 객체 주입!
 *     
 *     // 바로 비즈니스 로직 실행
 *     return ResponseEntity.ok(templateService.getTemplates(user));
 * }
 * </pre>
 * 
 * 동작 과정:
 * <pre>
 * 1. 클라이언트 요청
 *    → GET /api/templates
 *    → Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 * 
 * 2. Spring MVC가 CurrentUserArgumentResolver 호출
 *    → supportsParameter() 메서드로 {@literal @}CurrentUser 어노테이션 확인
 * 
 * 3. CurrentUserArgumentResolver.resolveArgument() 실행
 *    → Authorization 헤더에서 JWT 토큰 추출
 *    → JwtUtil.validateToken()로 토큰 검증
 *    → JwtUtil.getEmailFromToken()로 이메일 추출
 *    → UserRepository에서 User 조회
 * 
 * 4. Controller 메서드 실행
 *    → {@literal @}CurrentUser User user 파라미터에 User 객체 주입
 *    → 비즈니스 로직 실행
 * </pre>
 * 
 * 실제 사용 예시 (TemplateController):
 * <pre>
 * {@literal @}RestController
 * {@literal @}RequestMapping("/api/templates")
 * public class TemplateController {
 *     
 *     {@literal @}PostMapping
 *     public ResponseEntity<TemplateResponse> createTemplate(
 *             {@literal @}CurrentUser User user,  ← 로그인한 사용자 정보 자동 주입
 *             {@literal @}Valid {@literal @}RequestBody TemplateCreateRequest request) {
 *         
 *         // user는 이미 JWT 토큰으로부터 추출된 User 객체
 *         return ResponseEntity.status(HttpStatus.CREATED)
 *                 .body(templateService.createTemplate(user, request));
 *     }
 *     
 *     {@literal @}GetMapping
 *     public ResponseEntity<Page<TemplateResponse>> getTemplates(
 *             {@literal @}CurrentUser User user,  ← 로그인한 사용자 정보 자동 주입
 *             Pageable pageable) {
 *         
 *         // user.getId()로 사용자별 템플릿 조회
 *         return ResponseEntity.ok(templateService.getTemplates(user, pageable));
 *     }
 *     
 *     {@literal @}DeleteMapping("/{templateId}")
 *     public ResponseEntity<Void> deleteTemplate(
 *             {@literal @}CurrentUser User user,  ← 권한 검증에 사용
 *             {@literal @}PathVariable Long templateId) {
 *         
 *         // 템플릿 소유자인지 확인 후 삭제
 *         templateService.deleteTemplate(user, templateId);
 *         return ResponseEntity.noContent().build();
 *     }
 * }
 * </pre>
 * 
 * 어노테이션 메타데이터:
 * - {@code @Target(ElementType.PARAMETER)}: 메서드 파라미터에만 사용 가능
 * - {@code @Retention(RetentionPolicy.RUNTIME)}: 런타임에 어노테이션 정보 유지
 * - {@code @Documented}: JavaDoc에 어노테이션 정보 포함
 * 
 * 참고:
 * - CurrentUserArgumentResolver에서 실제 User 객체 추출 로직 구현
 * - WebMvcConfig에서 ArgumentResolver 등록 필요
 * - JWT 토큰이 없거나 유효하지 않으면 IllegalArgumentException 발생
 * 
 * @see com.lien.security.CurrentUserArgumentResolver
 * @see com.lien.config.WebMvcConfig
 * @see com.lien.security.JwtUtil
 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver
 */
@Target(ElementType.PARAMETER)  // 메서드 파라미터에만 사용 가능
@Retention(RetentionPolicy.RUNTIME)  // 런타임에 어노테이션 정보 유지
@Documented  // JavaDoc에 포함
public @interface CurrentUser {
}

