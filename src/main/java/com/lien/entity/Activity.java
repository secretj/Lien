package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ==============================================================================
 * Activity Entity (활동 엔티티)
 * ==============================================================================
 * 
 * 역할:
 * - 하루 일정 내의 개별 활동을 표현하는 엔티티
 * - 시간, 장소, 설명을 포함하는 최소 단위
 * - 이전 장소 정보로 이동 경로 계산 가능
 * 
 * 이점:
 * 1. 세부 일정 관리 (시간별 활동)
 * 2. 위치 정보 연동 (Google Maps)
 * 3. 이동 거리/시간 계산
 * 4. 순서 보장 (orderIndex)
 * 
 * 데이터베이스 스키마:
 * <pre>
 * CREATE TABLE activities (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     day_schedule_id BIGINT NOT NULL,
 *     time VARCHAR(50) NOT NULL,
 *     description VARCHAR(300) NOT NULL,
 *     location_id BIGINT NOT NULL,
 *     previous_location_id BIGINT,
 *     order_index INT NOT NULL,
 *     FOREIGN KEY (day_schedule_id) REFERENCES day_schedules(id),
 *     FOREIGN KEY (location_id) REFERENCES locations(id),
 *     FOREIGN KEY (previous_location_id) REFERENCES locations(id)
 * );
 * </pre>
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 첫 번째 활동 (이전 장소 없음)
 * Activity activity1 = Activity.builder()
 *     .daySchedule(day1)
 *     .time("09:00")
 *     .description("호텔에서 출발")
 *     .location(hotel)
 *     .previousLocation(null)
 *     .orderIndex(1)
 *     .build();
 * 
 * // 두 번째 활동 (호텔 → 성산일출봉)
 * Activity activity2 = Activity.builder()
 *     .daySchedule(day1)
 *     .time("10:30")
 *     .description("성산일출봉 도착 및 등반")
 *     .location(seongsan)
 *     .previousLocation(hotel)  // 이동 경로 계산 가능
 *     .orderIndex(2)
 *     .build();
 * 
 * // 이동 거리 계산
 * double distance = calculateDistance(
 *     activity2.getPreviousLocation(),
 *     activity2.getLocation()
 * );
 * System.out.println("이동 거리: " + distance + "km");
 * }
 * </pre>
 * 
 * 테이블 데이터 예시:
 * <pre>
 * +----+-----------------+-------+---------------------------+-------------+----------------------+-------------+
 * | id | day_schedule_id | time  | description               | location_id | previous_location_id | order_index |
 * +----+-----------------+-------+---------------------------+-------------+----------------------+-------------+
 * | 1  | 1               | 09:00 | 호텔 조식                 | 1           | NULL                 | 1           |
 * | 2  | 1               | 10:00 | 성산일출봉 출발           | 1           | NULL                 | 2           |
 * | 3  | 1               | 12:00 | 성산일출봉 도착 및 등반   | 2           | 1                    | 3           |
 * | 4  | 1               | 14:00 | 점심 식사                 | 3           | 2                    | 4           |
 * | 5  | 1               | 16:00 | 섭지코지 방문             | 4           | 3                    | 5           |
 * +----+-----------------+-------+---------------------------+-------------+----------------------+-------------+
 * </pre>
 * 
 * 이동 경로 시각화:
 * <pre>
 * 호텔(1) → 성산일출봉(2) → 식당(3) → 섭지코지(4)
 *    ↓         ↓             ↓           ↓
 * [09:00]   [12:00]       [14:00]     [16:00]
 * 거리: -    15.2km        2.3km       5.8km
 * </pre>
 * 
 * 관계:
 * - Activity N : 1 DaySchedule (활동은 하나의 일정에 속함)
 * - Activity N : 1 Location (활동은 하나의 장소에서 발생)
 * - Activity N : 1 Location (이전 장소 참조, 선택사항)
 * 
 * @see DaySchedule
 * @see Location
 */
@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {
    
    /**
     * 활동 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 소속 일정 (다대일 관계)
     * 
     * fetch = FetchType.LAZY: 지연 로딩
     * - 활동 조회 시 일정은 필요할 때만 로딩
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_schedule_id", nullable = false)
    private DaySchedule daySchedule;
    
    /**
     * 시간
     * 
     * 형식: "HH:MM" 또는 자유 형식
     * 
     * 예시:
     * - "09:00" (정확한 시간)
     * - "10:30" (30분 단위)
     * - "오전" (대략적인 시간)
     * - "점심 시간" (설명적 표현)
     * 
     * 용도:
     * - 타임라인 표시
     * - 시간순 정렬
     * - 일정 밀도 계산
     * 
     * 제약: 최대 50자
     */
    @Column(nullable = false, length = 50)
    private String time;
    
    /**
     * 활동 설명
     * 
     * 예시:
     * - "성산일출봉 등반 (소요시간: 약 1시간)"
     * - "점심 식사 - 해물뚝배기 추천"
     * - "섭지코지 산책 및 사진 촬영"
     * - "호텔 체크인 및 휴식"
     * 
     * 활용:
     * - 상세 일정 표시
     * - 메모 및 팁 저장
     * - 예약 정보 기록
     * 
     * 제약: 최대 300자
     */
    @Column(nullable = false, length = 300)
    private String description;
    
    /**
     * 활동 장소 (다대일 관계)
     * 
     * 역할:
     * - 현재 활동이 이루어지는 장소
     * - Google Maps 연동
     * - 위치 기반 검색
     * 
     * 예시:
     * - 성산일출봉 (관광지)
     * - 제주 신라호텔 (숙소)
     * - 올레국수 (식당)
     * 
     * fetch = FetchType.LAZY: 지연 로딩
     * - N+1 문제 방지: fetch join 권장
     * 
     * 쿼리 예시:
     * {@code
     * @Query("SELECT a FROM Activity a " +
     *        "JOIN FETCH a.location " +
     *        "WHERE a.daySchedule.id = :scheduleId")
     * List<Activity> findAllByScheduleIdWithLocation(@Param("scheduleId") Long scheduleId);
     * }
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    /**
     * 이전 장소 (다대일 관계, 선택사항)
     * 
     * 역할:
     * - 이동 경로 계산
     * - 이동 시간/거리 추정
     * - 네비게이션 연동
     * 
     * 용도:
     * 1. Google Maps Directions API 호출
     * 2. 이동 시간 계산
     * 3. 경로 최적화
     * 
     * 예시:
     * {@code
     * // 이동 거리 계산 (Haversine Formula)
     * double distance = calculateDistance(
     *     previousLocation.getLatitude(),
     *     previousLocation.getLongitude(),
     *     location.getLatitude(),
     *     location.getLongitude()
     * );
     * 
     * // Google Maps Directions API
     * DirectionsResult result = DirectionsApi.newRequest(context)
     *     .origin(new LatLng(
     *         previousLocation.getLatitude(),
     *         previousLocation.getLongitude()
     *     ))
     *     .destination(new LatLng(
     *         location.getLatitude(),
     *         location.getLongitude()
     *     ))
     *     .await();
     * 
     * System.out.println("이동 시간: " + result.routes[0].legs[0].duration.humanReadable);
     * System.out.println("이동 거리: " + result.routes[0].legs[0].distance.humanReadable);
     * // 출력: 이동 시간: 25분, 이동 거리: 15.2km
     * }
     * 
     * null인 경우:
     * - 일정의 첫 번째 활동
     * - 이동이 없는 활동 (같은 장소에서 연속 활동)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_location_id")
    private Location previousLocation;
    
    /**
     * 정렬 순서 (인덱스)
     * 
     * 역할:
     * - 활동 순서 보장
     * - 시간순 정렬
     * - UI 표시 순서
     * 
     * 규칙:
     * - 1부터 시작 (daySchedule 내에서 순차적)
     * - 중간 삽입 시 재정렬 필요
     * 
     * 예시:
     * - Activity 1: orderIndex = 1 (09:00 - 호텔 출발)
     * - Activity 2: orderIndex = 2 (10:30 - 성산일출봉)
     * - Activity 3: orderIndex = 3 (14:00 - 점심 식사)
     * 
     * 재정렬 예시:
     * {@code
     * // Activity 2와 3 사이에 새 활동 삽입
     * Activity newActivity = Activity.builder()
     *     .orderIndex(3)  // 기존 3번 자리
     *     .build();
     * 
     * // 기존 orderIndex 3 이상인 활동들의 orderIndex + 1
     * activities.stream()
     *     .filter(a -> a.getOrderIndex() >= 3)
     *     .forEach(a -> a.setOrderIndex(a.getOrderIndex() + 1));
     * 
     * activities.add(newActivity);
     * // 결과: 1, 2, 3(new), 4, 5
     * }
     * 
     * @OrderBy 사용:
     * DaySchedule의 activities 리스트는 orderIndex로 자동 정렬됨
     */
    @Column(nullable = false)
    private Integer orderIndex;
}
