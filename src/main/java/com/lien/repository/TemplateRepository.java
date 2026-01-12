package com.lien.repository;

import com.lien.entity.Template;
import com.lien.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ==============================================================================
 * Template Repository (템플릿 레포지토리)
 * ==============================================================================
 * 
 * 역할:
 * - Template 엔티티의 데이터베이스 접근 계층
 * - 복잡한 조인 쿼리 및 페이징 처리
 * - N+1 문제 해결을 위한 Fetch Join 쿼리
 * 
 * 이점:
 * 1. Fetch Join으로 성능 최적화
 * 2. 페이징으로 대용량 데이터 처리
 * 3. 사용자별 템플릿 조회
 * 4. Lazy Loading 문제 해결
 * 
 * N+1 문제 예시:
 * <pre>
 * // 문제 있는 코드 (N+1 발생)
 * List<Template> templates = templateRepository.findAll();
 * for (Template t : templates) {
 *     t.getDaySchedules().size();  // SELECT 쿼리 N번 추가 실행
 * }
 * 
 * // 해결: Fetch Join 사용
 * @Query("SELECT t FROM Template t LEFT JOIN FETCH t.daySchedules")
 * List<Template> findAllWithDaySchedules();
 * // SELECT 쿼리 1번만 실행
 * </pre>
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @Service
 * public class TemplateService {
 *     @Autowired
 *     private TemplateRepository templateRepository;
 *     
 *     // 사용자별 템플릿 목록 (페이징)
 *     public Page<Template> getMyTemplates(User user, int page, int size) {
 *         Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
 *         return templateRepository.findByUser(user, pageable);
 *     }
 *     
 *     // 템플릿 상세 조회 (체크리스트 포함)
 *     public Template getTemplateWithChecklist(Long templateId, User user) {
 *         Template template = templateRepository
 *             .findByIdAndUserWithChecklistSections(templateId, user);
 *         
 *         if (template == null) {
 *             throw new EntityNotFoundException("Template not found");
 *         }
 *         return template;
 *     }
 * }
 * }
 * </pre>
 * 
 * @see Template
 * @see JpaRepository
 */
public interface TemplateRepository extends JpaRepository<Template, Long> {
    
    /**
     * 사용자별 템플릿 목록 조회 (페이징)
     * 
     * 역할:
     * - 사용자의 모든 템플릿 조회
     * - 페이징 처리로 대용량 데이터 효율적 처리
     * - 정렬 지원 (최신순, 날짜순 등)
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM templates WHERE user_id = ?
     * ORDER BY created_at DESC
     * LIMIT 10 OFFSET 0;
     * </pre>
     * 
     * 성능:
     * - idx_template_user_id 인덱스 사용
     * - 페이징으로 메모리 효율적 사용
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 첫 페이지 조회 (10개씩, 최신순)
     * Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
     * Page<Template> page = templateRepository.findByUser(user, pageable);
     * 
     * System.out.println("총 개수: " + page.getTotalElements());
     * System.out.println("총 페이지: " + page.getTotalPages());
     * System.out.println("현재 페이지: " + page.getNumber());
     * 
     * List<Template> templates = page.getContent();
     * for (Template template : templates) {
     *     System.out.println(template.getTitle());
     * }
     * 
     * // 다음 페이지 조회
     * if (page.hasNext()) {
     *     Page<Template> nextPage = templateRepository.findByUser(user, page.nextPageable());
     * }
     * }
     * </pre>
     * 
     * @param user 조회할 사용자
     * @param pageable 페이징 정보 (페이지 번호, 크기, 정렬)
     * @return Page<Template> 템플릿 페이지 (내용 + 메타데이터)
     */
    Page<Template> findByUser(User user, Pageable pageable);
    
    /**
     * 템플릿 및 체크리스트 조회 (Fetch Join)
     * 
     * 역할:
     * - 템플릿과 체크리스트를 한 번의 쿼리로 조회
     * - N+1 문제 해결
     * - Lazy Loading 예외 방지
     * 
     * Fetch Join 설명:
     * - LEFT JOIN FETCH: 외래 키가 null이어도 조회
     * - INNER JOIN FETCH: 외래 키가 있는 경우만 조회
     * 
     * 실행 SQL:
     * <pre>
     * SELECT t.*, cs.*, ci.*
     * FROM templates t
     * LEFT JOIN checklist_sections cs ON t.id = cs.template_id
     * LEFT JOIN checklist_items ci ON cs.id = ci.section_id
     * WHERE t.id = ? AND t.user_id = ?;
     * </pre>
     * 
     * 성능 비교:
     * <pre>
     * // Lazy Loading (N+1 문제)
     * Template template = templateRepository.findById(id);  // 1번
     * template.getChecklistSections().size();               // 1번
     * for (ChecklistSection section : template.getChecklistSections()) {
     *     section.getItems().size();  // N번 (섹션 개수만큼)
     * }
     * // 총 쿼리: 1 + 1 + N = N+2번
     * 
     * // Fetch Join (최적화)
     * Template template = templateRepository
     *     .findByIdAndUserWithChecklistSections(id, user);  // 1번
     * // 총 쿼리: 1번
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 템플릿 상세 페이지 - 체크리스트 표시
     * Template template = templateRepository
     *     .findByIdAndUserWithChecklistSections(templateId, user);
     * 
     * if (template == null) {
     *     throw new EntityNotFoundException("Template not found or access denied");
     * }
     * 
     * // 체크리스트 진행률 계산
     * for (ChecklistSection section : template.getChecklistSections()) {
     *     int total = section.getItems().size();
     *     int checked = (int) section.getItems().stream()
     *         .filter(ChecklistItem::getChecked)
     *         .count();
     *     System.out.println(section.getTitle() + ": " + checked + "/" + total);
     * }
     * }
     * </pre>
     * 
     * @param templateId 조회할 템플릿 ID
     * @param user 소유자 확인용 사용자
     * @return Template 템플릿 (체크리스트 포함), 없으면 null
     */
    @Query("SELECT t FROM Template t LEFT JOIN FETCH t.checklistSections cs " +
           "LEFT JOIN FETCH cs.items WHERE t.id = :templateId AND t.user = :user")
    Template findByIdAndUserWithChecklistSections(@Param("templateId") Long templateId, 
                                                   @Param("user") User user);
    
    /**
     * 템플릿 및 일정 조회 (Fetch Join)
     * 
     * 역할:
     * - 템플릿, 일정, 활동, 위치를 한 번의 쿼리로 조회
     * - 3단계 조인으로 복잡한 데이터 구조 최적화
     * - 일정 표시 화면에 필요한 모든 데이터 로드
     * 
     * 실행 SQL:
     * <pre>
     * SELECT t.*, ds.*, a.*, l.*
     * FROM templates t
     * LEFT JOIN day_schedules ds ON t.id = ds.template_id
     * LEFT JOIN activities a ON ds.id = a.day_schedule_id
     * LEFT JOIN locations l ON a.location_id = l.id
     * WHERE t.id = ? AND t.user_id = ?
     * ORDER BY ds.day_number, a.order_index;
     * </pre>
     * 
     * 성능 비교:
     * <pre>
     * // Lazy Loading (최악의 경우)
     * Template template = templateRepository.findById(id);      // 1번
     * for (DaySchedule day : template.getDaySchedules()) {      // 1번
     *     for (Activity activity : day.getActivities()) {       // N번
     *         activity.getLocation().getName();                 // M번
     *     }
     * }
     * // 총 쿼리: 1 + 1 + N + (N*M) = 매우 많음
     * 
     * // Fetch Join
     * Template template = templateRepository
     *     .findByIdAndUserWithDaySchedules(id, user);           // 1번
     * // 총 쿼리: 1번
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 일정 표시 페이지
     * Template template = templateRepository
     *     .findByIdAndUserWithDaySchedules(templateId, user);
     * 
     * // 일정 출력
     * for (DaySchedule day : template.getDaySchedules()) {
     *     System.out.println("Day " + day.getDayNumber() + ": " + day.getTitle());
     *     
     *     for (Activity activity : day.getActivities()) {
     *         System.out.println("  " + activity.getTime() + " - " + 
     *                            activity.getDescription() + " at " + 
     *                            activity.getLocation().getName());
     *     }
     * }
     * 
     * // 출력 예시:
     * // Day 1: 첫째 날 - 제주 도착
     * //   14:00 - 공항 도착 at 제주국제공항
     * //   16:00 - 호텔 체크인 at 제주 신라호텔
     * //   18:00 - 저녁 식사 at 올레국수
     * }
     * </pre>
     * 
     * 주의사항:
     * - 복잡한 조인으로 데이터 중복 발생 가능 (정규화 필요)
     * - 활동이 많은 경우 데이터 크기 증가
     * - DISTINCT 사용 고려
     * 
     * @param templateId 조회할 템플릿 ID
     * @param user 소유자 확인용 사용자
     * @return Template 템플릿 (일정 포함), 없으면 null
     */
    @Query("SELECT t FROM Template t LEFT JOIN FETCH t.daySchedules ds " +
           "LEFT JOIN FETCH ds.activities a LEFT JOIN FETCH a.location " +
           "WHERE t.id = :templateId AND t.user = :user")
    Template findByIdAndUserWithDaySchedules(@Param("templateId") Long templateId, 
                                             @Param("user") User user);
    
    /**
     * 사용자 및 ID로 템플릿 조회
     * 
     * 역할:
     * - 템플릿 소유자 확인
     * - 권한 검증
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM templates WHERE id = ? AND user_id = ?;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 템플릿 삭제 시 소유자 확인
     * Optional<Template> template = templateRepository.findByIdAndUser(templateId, user);
     * if (!template.isPresent()) {
     *     throw new AccessDeniedException("템플릿이 없거나 접근 권한이 없습니다");
     * }
     * templateRepository.delete(template.get());
     * }
     * </pre>
     * 
     * @param id 템플릿 ID
     * @param user 사용자
     * @return Optional<Template> 템플릿 (없으면 Optional.empty())
     */
    Optional<Template> findByIdAndUser(Long id, User user);
    
    /**
     * 날짜 범위로 템플릿 조회
     * 
     * 역할:
     * - 특정 기간의 여행 계획 검색
     * - 캘린더 뷰 데이터 제공
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM templates 
     * WHERE user_id = ? 
     * AND start_date >= ? 
     * AND end_date <= ?
     * ORDER BY start_date;
     * </pre>
     * 
     * 성능:
     * - idx_template_dates 인덱스 사용
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 2024년 2월 여행 계획 조회
     * LocalDate start = LocalDate.of(2024, 2, 1);
     * LocalDate end = LocalDate.of(2024, 2, 29);
     * List<Template> templates = templateRepository
     *     .findByUserAndStartDateBetween(user, start, end);
     * }
     * </pre>
     * 
     * @param user 사용자
     * @param startDate 시작일 (이상)
     * @param endDate 종료일 (이하)
     * @return List<Template> 해당 기간의 템플릿 목록
     */
    List<Template> findByUserAndStartDateBetween(User user, LocalDate startDate, LocalDate endDate);
}