package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * DaySchedule Entity (일별 일정 엔티티)
 * ==============================================================================
 * 
 * 역할:
 * - 여행의 하루 일정을 표현하는 엔티티
 * - 여러 Activity(활동)를 포함하는 집합
 * - 날짜와 순서 정보로 일정 관리
 * 
 * 이점:
 * 1. 일자별 여행 계획 구조화
 * 2. 활동(Activity) 자동 관리 (Cascade)
 * 3. 순서 보장 (@OrderBy)
 * 4. 색상 코드로 UI 시각화
 * 
 * 데이터베이스 스키마:
 * <pre>
 * CREATE TABLE day_schedules (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     template_id BIGINT NOT NULL,
 *     day_number INT NOT NULL,
 *     date DATE NOT NULL,
 *     title VARCHAR(100) NOT NULL,
 *     color VARCHAR(20),
 *     FOREIGN KEY (template_id) REFERENCES templates(id)
 * );
 * </pre>
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 일정 생성
 * DaySchedule day1 = DaySchedule.builder()
 *     .template(template)
 *     .dayNumber(1)
 *     .date(LocalDate.of(2024, 2, 1))
 *     .title("첫째 날 - 제주 도착")
 *     .color("#FF6B6B")
 *     .build();
 * 
 * // 활동 추가
 * Activity activity1 = Activity.builder()
 *     .daySchedule(day1)
 *     .time("14:00")
 *     .description("공항 도착 및 렌터카 픽업")
 *     .orderIndex(1)
 *     .build();
 * day1.getActivities().add(activity1);
 * 
 * Activity activity2 = Activity.builder()
 *     .daySchedule(day1)
 *     .time("16:00")
 *     .description("호텔 체크인")
 *     .orderIndex(2)
 *     .build();
 * day1.getActivities().add(activity2);
 * }
 * </pre>
 * 
 * 테이블 데이터 예시:
 * <pre>
 * +----+-------------+------------+------------+------------------------+----------+
 * | id | template_id | day_number | date       | title                  | color    |
 * +----+-------------+------------+------------+------------------------+----------+
 * | 1  | 1           | 1          | 2024-02-01 | 첫째 날 - 제주 도착    | #FF6B6B  |
 * | 2  | 1           | 2          | 2024-02-02 | 둘째 날 - 동부 관광    | #4ECDC4  |
 * | 3  | 1           | 3          | 2024-02-03 | 셋째 날 - 서부 관광    | #45B7D1  |
 * | 4  | 1           | 4          | 2024-02-04 | 넷째 날 - 출발         | #FFA07A  |
 * +----+-------------+------------+------------+------------------------+----------+
 * </pre>
 * 
 * 관계:
 * - DaySchedule N : 1 Template (일정은 하나의 템플릿에 속함)
 * - DaySchedule 1 : N Activity (일정은 여러 활동 포함)
 * 
 * @see Template
 * @see Activity
 */
@Entity
@Table(name = "day_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DaySchedule {
    
    /**
     * 일정 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 소속 템플릿 (다대일 관계)
     * 
     * fetch = FetchType.LAZY: 지연 로딩
     * - 일정 조회 시 템플릿은 필요할 때만 로딩
     * 
     * 예시:
     * {@code
     * // Fetch Join으로 N+1 방지
     * @Query("SELECT d FROM DaySchedule d JOIN FETCH d.template WHERE d.id = :id")
     * DaySchedule findByIdWithTemplate(@Param("id") Long id);
     * }
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
    
    /**
     * 일차 번호 (1부터 시작)
     * 
     * 용도:
     * - UI 표시 ("Day 1", "1일차")
     * - 정렬 기준
     * - 일정 순서 관리
     * 
     * 예시:
     * - 3박 4일: dayNumber = 1, 2, 3, 4
     * - 5박 6일: dayNumber = 1, 2, 3, 4, 5, 6
     * 
     * 검증: 1 <= dayNumber <= template.totalDays
     */
    @Column(nullable = false)
    private Integer dayNumber;
    
    /**
     * 해당 일자
     * 
     * 용도:
     * - 달력 표시
     * - 날짜별 검색
     * - 요일 계산 (월/화/수...)
     * 
     * 예시:
     * - Day 1: 2024-02-01 (목요일)
     * - Day 2: 2024-02-02 (금요일)
     * 
     * 계산:
     * date = template.startDate.plusDays(dayNumber - 1)
     */
    @Column(nullable = false)
    private LocalDate date;
    
    /**
     * 일정 제목
     * 
     * 예시:
     * - "첫째 날 - 제주 도착"
     * - "둘째 날 - 동부 관광 (성산일출봉, 섭지코지)"
     * - "셋째 날 - 한라산 등반"
     * - "넷째 날 - 쇼핑 및 출발"
     * 
     * 제약: 최대 100자
     */
    @Column(nullable = false, length = 100)
    private String title;
    
    /**
     * 색상 코드 (선택사항)
     * 
     * 용도:
     * - UI에서 일정 구분 (캘린더, 타임라인)
     * - 시각적 구분
     * 
     * 형식: Hex Color Code (#RRGGBB)
     * 
     * 예시:
     * - "#FF6B6B" (빨강)
     * - "#4ECDC4" (청록)
     * - "#45B7D1" (파랑)
     * - "#FFA07A" (주황)
     * - null (기본 색상 사용)
     * 
     * 제약: 최대 20자
     */
    @Column(length = 20)
    private String color;
    
    /**
     * 활동 목록 (일대다 관계)
     * 
     * @OneToMany: 하나의 일정이 여러 활동 포함
     * mappedBy = "daySchedule": Activity의 daySchedule 필드가 관계 주인
     * 
     * cascade = CascadeType.ALL:
     * - 일정 저장/수정/삭제 시 활동도 자동 처리
     * 
     * orphanRemoval = true:
     * - 리스트에서 제거된 활동 자동 삭제
     * 
     * @OrderBy("orderIndex ASC"):
     * - 조회 시 orderIndex 기준 오름차순 정렬
     * - 시간 순서대로 자동 정렬
     * 
     * 예시:
     * {@code
     * // 활동 추가 (순서대로)
     * Activity activity1 = Activity.builder()
     *     .daySchedule(day1)
     *     .time("09:00")
     *     .description("호텔 조식")
     *     .orderIndex(1)
     *     .build();
     * 
     * Activity activity2 = Activity.builder()
     *     .daySchedule(day1)
     *     .time("10:00")
     *     .description("성산일출봉 출발")
     *     .orderIndex(2)
     *     .build();
     * 
     * day1.getActivities().add(activity1);
     * day1.getActivities().add(activity2);
     * // 조회 시 orderIndex로 자동 정렬됨
     * }
     * 
     * 데이터 예시 (첫째 날):
     * 1. 09:00 - 호텔 조식
     * 2. 10:00 - 성산일출봉 출발
     * 3. 12:00 - 성산일출봉 도착 및 등반
     * 4. 14:00 - 점심 식사
     * 5. 16:00 - 섭지코지 방문
     * 6. 18:00 - 저녁 식사
     * 7. 20:00 - 호텔 복귀
     */
    @OneToMany(mappedBy = "daySchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Activity> activities = new ArrayList<>();
}