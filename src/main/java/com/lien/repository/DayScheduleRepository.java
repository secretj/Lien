package com.lien.repository;

import com.lien.entity.DaySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ==============================================================================
 * DaySchedule Repository (일정 레포지토리)
 * ==============================================================================
 * 
 * 역할:
 * - DaySchedule 엔티티의 데이터베이스 접근 계층
 * - 템플릿별 일정 조회
 * - 일차별 정렬 보장
 * 
 * 이점:
 * 1. JpaRepository 기본 CRUD
 * 2. Template을 통한 Cascade 관리
 * 3. 별도 쿼리 불필요 (Template에서 함께 조회)
 * 
 * 주의사항:
 * - 대부분의 경우 TemplateRepository의 Fetch Join 사용 권장
 * - 직접 DaySchedule만 필요한 경우에만 사용
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 일반적으로 Template을 통해 조회 (권장)
 * Template template = templateRepository.findByIdAndUserWithDaySchedules(id, user);
 * List<DaySchedule> schedules = template.getDaySchedules();
 * 
 * // 또는 직접 조회 (특수한 경우)
 * List<DaySchedule> schedules = dayScheduleRepository
 *     .findByTemplateIdOrderByDayNumberAsc(templateId);
 * }
 * </pre>
 * 
 * @see DaySchedule
 * @see TemplateRepository
 * @see JpaRepository
 */
public interface DayScheduleRepository extends JpaRepository<DaySchedule, Long> {
    
    /**
     * 템플릿별 일정 목록 조회 (일차순 정렬)
     * 
     * 역할:
     * - 특정 템플릿의 모든 일정 조회
     * - dayNumber 기준 오름차순 정렬
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM day_schedules 
     * WHERE template_id = ? 
     * ORDER BY day_number ASC;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * List<DaySchedule> schedules = dayScheduleRepository
     *     .findByTemplateIdOrderByDayNumberAsc(templateId);
     * 
     * for (DaySchedule schedule : schedules) {
     *     System.out.println("Day " + schedule.getDayNumber() + ": " + 
     *                        schedule.getTitle());
     * }
     * }
     * </pre>
     * 
     * @param templateId 템플릿 ID
     * @return List<DaySchedule> 일정 목록 (일차순)
     */
    List<DaySchedule> findByTemplateIdOrderByDayNumberAsc(Long templateId);
}
