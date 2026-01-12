package com.lien.repository;

import com.lien.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ==============================================================================
 * Activity Repository (활동 레포지토리)
 * ==============================================================================
 * 
 * 역할:
 * - Activity 엔티티의 데이터베이스 접근 계층
 * - 일정별 활동 조회
 * - 위치별 활동 조회
 * 
 * 이점:
 * 1. JpaRepository 기본 CRUD
 * 2. DaySchedule을 통한 Cascade 관리
 * 3. @OrderBy로 자동 정렬
 * 
 * 주의사항:
 * - 대부분의 경우 TemplateRepository의 Fetch Join 사용 권장
 * - 활동만 필요한 특수한 경우에 사용
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 일반적으로 Template을 통해 조회 (권장)
 * Template template = templateRepository.findByIdAndUserWithDaySchedules(id, user);
 * for (DaySchedule day : template.getDaySchedules()) {
 *     for (Activity activity : day.getActivities()) {
 *         System.out.println(activity.getDescription());
 *     }
 * }
 * 
 * // 또는 직접 조회 (특수한 경우)
 * List<Activity> activities = activityRepository
 *     .findByDayScheduleIdOrderByOrderIndexAsc(scheduleId);
 * }
 * </pre>
 * 
 * @see Activity
 * @see DaySchedule
 * @see JpaRepository
 */
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    /**
     * 일정별 활동 목록 조회 (순서대로)
     * 
     * 역할:
     * - 특정 일정의 모든 활동 조회
     * - orderIndex 기준 오름차순 정렬
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM activities 
     * WHERE day_schedule_id = ? 
     * ORDER BY order_index ASC;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * List<Activity> activities = activityRepository
     *     .findByDayScheduleIdOrderByOrderIndexAsc(scheduleId);
     * 
     * for (Activity activity : activities) {
     *     System.out.println(activity.getTime() + " - " + 
     *                        activity.getDescription());
     * }
     * }
     * </pre>
     * 
     * @param dayScheduleId 일정 ID
     * @return List<Activity> 활동 목록 (순서대로)
     */
    List<Activity> findByDayScheduleIdOrderByOrderIndexAsc(Long dayScheduleId);
    
    /**
     * 위치별 활동 조회
     * 
     * 역할:
     * - 특정 위치가 사용된 모든 활동 조회
     * - 위치 삭제 전 사용 여부 확인
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM activities WHERE location_id = ?;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 위치 삭제 전 사용 여부 확인
     * List<Activity> activities = activityRepository.findByLocationId(locationId);
     * if (!activities.isEmpty()) {
     *     throw new IllegalStateException(
     *         "이 위치는 " + activities.size() + "개의 활동에서 사용 중입니다"
     *     );
     * }
     * locationRepository.delete(location);
     * }
     * </pre>
     * 
     * @param locationId 위치 ID
     * @return List<Activity> 해당 위치를 사용하는 활동 목록
     */
    List<Activity> findByLocationId(Long locationId);
}
