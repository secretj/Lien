package com.lien.config;

import com.lien.security.CurrentUserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ==============================================================================
 * Spring MVC 설정
 * ==============================================================================
 * 
 * 역할:
 * - Spring MVC의 추가 설정을 정의
 * - HandlerMethodArgumentResolver 등록
 * - CORS, Interceptor, MessageConverter 등 설정 가능
 * 
 * 이점:
 * 1. 커스텀 ArgumentResolver 등록
 * 2. 커스텀 Interceptor 등록
 * 3. 정적 리소스 매핑
 * 4. 메시지 변환기 설정
 * 
 * HandlerMethodArgumentResolver:
 * - Controller 메서드의 파라미터에 특별한 처리가 필요할 때 사용
 * - Spring MVC가 Controller 메서드를 호출하기 전에 파라미터 값을 처리
 * 
 * 현재 등록된 ArgumentResolver:
 * - CurrentUserArgumentResolver: {@literal @}CurrentUser 어노테이션으로 현재 사용자 자동 주입
 * 
 * 동작 과정:
 * <pre>
 * 1. 클라이언트 요청
 *    → GET /api/templates
 *    → Authorization: Bearer {JWT_TOKEN}
 * 
 * 2. DispatcherServlet이 Controller 메서드 찾기
 *    → TemplateController.getTemplates({@literal @}CurrentUser User user)
 * 
 * 3. ArgumentResolver 체인 실행
 *    → CurrentUserArgumentResolver.supportsParameter() 호출
 *    → true 반환 (지원하는 파라미터)
 *    → CurrentUserArgumentResolver.resolveArgument() 호출
 *    → User 객체 생성 및 반환
 * 
 * 4. Controller 메서드 실행
 *    → user 파라미터에 User 객체 주입
 * </pre>
 * 
 * 확장 가능한 설정:
 * <pre>
 * // Interceptor 추가
 * {@literal @}Override
 * public void addInterceptors(InterceptorRegistry registry) {
 *     registry.addInterceptor(new LoggingInterceptor())
 *             .addPathPatterns("/api/**")
 *             .excludePathPatterns("/api/auth/**");
 * }
 * 
 * // CORS 설정 (SecurityConfig에서 설정 중)
 * {@literal @}Override
 * public void addCorsMappings(CorsRegistry registry) {
 *     registry.addMapping("/api/**")
 *             .allowedOrigins("http://localhost:3000")
 *             .allowedMethods("GET", "POST", "PUT", "DELETE");
 * }
 * 
 * // 정적 리소스 매핑
 * {@literal @}Override
 * public void addResourceHandlers(ResourceHandlerRegistry registry) {
 *     registry.addResourceHandler("/static/**")
 *             .addResourceLocations("classpath:/static/");
 * }
 * </pre>
 * 
 * @see com.lien.security.CurrentUserArgumentResolver
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * {@literal @}CurrentUser 어노테이션 처리를 위한 ArgumentResolver
     * 
     * JWT 토큰에서 사용자 정보를 추출하여 User 객체로 변환합니다.
     */
    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    /**
     * 커스텀 ArgumentResolver 등록
     * 
     * Spring MVC가 Controller 메서드를 호출하기 전에 파라미터를 처리할
     * ArgumentResolver를 등록합니다.
     * 
     * 등록된 ArgumentResolver:
     * - CurrentUserArgumentResolver: {@literal @}CurrentUser 어노테이션 처리
     * 
     * 동작 순서:
     * 1. Controller 메서드에 {@literal @}CurrentUser User user 파라미터 발견
     * 2. Spring MVC가 등록된 ArgumentResolver 목록 확인
     * 3. CurrentUserArgumentResolver.supportsParameter() 호출
     * 4. true 반환 시 CurrentUserArgumentResolver.resolveArgument() 호출
     * 5. 반환된 User 객체를 파라미터에 주입
     * 
     * 사용 예시:
     * <pre>
     * // Controller 메서드
     * {@literal @}GetMapping("/api/templates")
     * public ResponseEntity<List<TemplateResponse>> getTemplates(
     *         {@literal @}CurrentUser User user) {  ← CurrentUserArgumentResolver가 처리
     *     
     *     return ResponseEntity.ok(templateService.getTemplates(user));
     * }
     * </pre>
     * 
     * 추가 ArgumentResolver 등록 예시:
     * <pre>
     * {@literal @}Override
     * public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
     *     resolvers.add(currentUserArgumentResolver);  // 현재 사용자 정보 주입
     *     resolvers.add(new PagingArgumentResolver());  // 페이징 처리
     *     resolvers.add(new SortArgumentResolver());    // 정렬 처리
     * }
     * </pre>
     * 
     * @param resolvers ArgumentResolver 목록
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // CurrentUserArgumentResolver 등록
        // {@literal @}CurrentUser 어노테이션이 붙은 파라미터에 User 객체 자동 주입
        resolvers.add(currentUserArgumentResolver);
    }
}

