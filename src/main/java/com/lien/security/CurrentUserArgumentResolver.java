package com.lien.security;

import com.lien.entity.User;
import com.lien.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * ==============================================================================
 * 현재 로그인한 사용자 정보 주입 Argument Resolver
 * ==============================================================================
 * 
 * 역할:
 * - {@literal @}CurrentUser 어노테이션이 붙은 Controller 파라미터에 User 객체 자동 주입
 * - JWT 토큰에서 사용자 정보를 추출하여 User 엔티티로 변환
 * - Spring MVC의 HandlerMethodArgumentResolver 인터페이스 구현
 * 
 * 이점:
 * 1. 코드 중복 제거: Controller마다 JWT 파싱 로직 불필요
 * 2. 일관된 인증 처리: 모든 Controller에서 동일한 방식으로 사용자 정보 추출
 * 3. 테스트 용이성: ArgumentResolver만 Mocking하면 됨
 * 4. 유지보수 용이성: 인증 로직 변경 시 이 클래스만 수정
 * 
 * 동작 과정:
 * <pre>
 * 1. 클라이언트 요청
 *    GET /api/templates
 *    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...
 * 
 * 2. Spring MVC가 Controller 메서드 호출 전 ArgumentResolver 체크
 *    → supportsParameter() 메서드 호출
 *    → {@literal @}CurrentUser 어노테이션 확인
 *    → 파라미터 타입이 User인지 확인
 * 
 * 3. supportsParameter() == true이면 resolveArgument() 호출
 *    → Authorization 헤더에서 JWT 토큰 추출
 *    → "Bearer " 접두사 제거
 *    → JwtUtil.validateToken()로 토큰 검증
 *    → JwtUtil.getEmailFromToken()로 이메일 추출
 *    → UserRepository에서 User 조회
 *    → User 객체 반환
 * 
 * 4. Controller 메서드 실행
 *    → {@literal @}CurrentUser User user 파라미터에 User 객체 주입
 * </pre>
 * 
 * 사용 예시:
 * <pre>
 * // Controller 메서드에서 {@literal @}CurrentUser 사용
 * {@literal @}GetMapping("/api/templates")
 * public ResponseEntity<List<TemplateResponse>> getTemplates(
 *         {@literal @}CurrentUser User user) {  ← 이 부분이 자동으로 처리됨
 *     
 *     // user 객체는 이미 JWT 토큰으로부터 추출된 상태
 *     return ResponseEntity.ok(templateService.getTemplates(user));
 * }
 * </pre>
 * 
 * JWT 토큰 형식:
 * <pre>
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...
 *                ^^^^^^ ← 7자 (접두사)
 *                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ ← JWT 토큰
 * </pre>
 * 
 * 에러 처리:
 * 1. Authorization 헤더 없음 → "인증이 필요합니다"
 * 2. Bearer 접두사 없음 → "인증이 필요합니다"
 * 3. JWT 토큰 검증 실패 (만료, 변조 등) → "인증이 필요합니다"
 * 4. 이메일에 해당하는 사용자 없음 → "사용자를 찾을 수 없습니다"
 * 
 * 등록:
 * - WebMvcConfig에서 addArgumentResolvers()로 등록 필요
 * 
 * @see com.lien.security.CurrentUser
 * @see com.lien.config.WebMvcConfig
 * @see com.lien.security.JwtUtil
 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver
 */
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 사용자 정보 조회를 위한 UserRepository
     * 
     * JWT 토큰에서 추출한 이메일로 사용자를 조회합니다.
     */
    private final UserRepository userRepository;

    /**
     * 이 ArgumentResolver가 지원하는 파라미터인지 확인
     * 
     * Spring MVC가 Controller 메서드 호출 전에 이 메서드를 호출하여
     * 이 ArgumentResolver가 해당 파라미터를 처리할 수 있는지 확인합니다.
     * 
     * 지원 조건:
     * 1. 파라미터에 {@literal @}CurrentUser 어노테이션이 있어야 함
     * 2. 파라미터 타입이 User 클래스여야 함
     * 
     * 사용 예시:
     * <pre>
     * // 지원하는 파라미터 (true 반환)
     * public void method({@literal @}CurrentUser User user) { }
     * 
     * // 지원하지 않는 파라미터 (false 반환)
     * public void method({@literal @}CurrentUser String email) { }  // 타입이 User가 아님
     * public void method(User user) { }  // {@literal @}CurrentUser 어노테이션 없음
     * </pre>
     * 
     * @param parameter Controller 메서드의 파라미터 정보
     * @return boolean true: 지원함, false: 지원하지 않음
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)  // {@literal @}CurrentUser 어노테이션 확인
                && parameter.getParameterType().equals(User.class);  // 파라미터 타입이 User인지 확인
    }

    /**
     * 파라미터에 주입할 객체(User) 생성
     * 
     * JWT 토큰에서 사용자 정보를 추출하여 User 객체를 반환합니다.
     * supportsParameter()가 true를 반환한 경우에만 호출됩니다.
     * 
     * 처리 과정:
     * 1. HTTP 요청에서 Authorization 헤더 추출
     * 2. "Bearer " 접두사 제거하여 JWT 토큰 추출
     * 3. JwtUtil.validateToken()로 토큰 검증
     * 4. JwtUtil.getEmailFromToken()로 이메일 추출
     * 5. UserRepository에서 이메일로 User 조회
     * 6. User 객체 반환
     * 
     * Authorization 헤더 형식:
     * <pre>
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...
     * </pre>
     * 
     * 에러 케이스:
     * <pre>
     * Case 1: Authorization 헤더 없음
     * → authHeader == null
     * → IllegalArgumentException("인증이 필요합니다")
     * 
     * Case 2: Bearer 접두사 없음
     * → authHeader.startsWith("Bearer ") == false
     * → IllegalArgumentException("인증이 필요합니다")
     * 
     * Case 3: JWT 토큰 검증 실패
     * → JwtUtil.validateToken(token) == false
     * → IllegalArgumentException("인증이 필요합니다")
     * 
     * Case 4: 사용자 조회 실패
     * → userRepository.findByEmail(email) == Optional.empty()
     * → IllegalArgumentException("사용자를 찾을 수 없습니다")
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * // Controller에서 자동으로 호출됨
     * {@literal @}GetMapping("/api/templates")
     * public ResponseEntity<List<TemplateResponse>> getTemplates(
     *         {@literal @}CurrentUser User user) {
     *     
     *     // 이 메서드가 호출되기 전에 resolveArgument()가 실행되어
     *     // user 파라미터에 User 객체가 주입됨
     *     
     *     return ResponseEntity.ok(templateService.getTemplates(user));
     * }
     * </pre>
     * 
     * @param parameter Controller 메서드의 파라미터 정보
     * @param mavContainer ModelAndViewContainer (사용하지 않음)
     * @param webRequest 현재 HTTP 요청 정보
     * @param binderFactory WebDataBinderFactory (사용하지 않음)
     * @return Object User 객체 (Controller 파라미터에 주입됨)
     * @throws IllegalArgumentException 인증 실패 또는 사용자 조회 실패 시
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        
        // 1. HTTP 요청 객체 추출
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        
        // 2. Authorization 헤더 추출
        String authHeader = request.getHeader("Authorization");

        // 3. Authorization 헤더가 존재하고 "Bearer "로 시작하는지 확인
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            
            // 4. JWT 토큰 추출 ("Bearer " 접두사 제거)
            String token = authHeader.substring(7);  // "Bearer " = 7자
            
            // 5. JWT 토큰 검증 (서명, 만료 시간, 형식 확인)
            if (JwtUtil.validateToken(token)) {
                
                // 6. JWT 토큰에서 이메일 추출
                String email = JwtUtil.getEmailFromToken(token);
                
                // 7. 이메일로 사용자 조회
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
            }
        }

        // 8. Authorization 헤더가 없거나 JWT 토큰이 유효하지 않은 경우
        throw new IllegalArgumentException("인증이 필요합니다");
    }
}

