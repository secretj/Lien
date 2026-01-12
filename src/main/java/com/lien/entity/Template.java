package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * Template Entity (여행 템플릿 엔티티)
 * ==============================================================================
 * 
 * 역할:
 * - 사용자의 여행 계획을 저장하는 핵심 엔티티
 * - 일정(DaySchedule), 체크리스트(ChecklistSection)를 포함하는 집합 루트(Aggregate Root)
 * - 여행의 기본 정보(목적지, 기간, 숙소, 교통) 관리
 * 
 * 이점:
 * 1. 구조화된 여행 계획 관리
 * 2. 일정 및 체크리스트 자동 관리 (Cascade)
 * 3. 날짜 기반 검색 및 정렬 가능
 * 4. 재사용 가능한 템플릿
 * 
 * 데이터베이스 스키마:
 * <pre>
 * CREATE TABLE templates (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     user_id BIGINT NOT NULL,
 *     title VARCHAR(200) NOT NULL,
 *     destination VARCHAR(200) NOT NULL,
 *     start_date DATE NOT NULL,
 *     end_date DATE NOT NULL,
 *     total_days INT NOT NULL,
 *     accommodation VARCHAR(200),
 *     transportation VARCHAR(200),
 *     created_at DATETIME NOT NULL,
 *     updated_at DATETIME NOT NULL,
 *     INDEX idx_template_user_id (user_id),
 *     INDEX idx_template_dates (start_date, end_date),
 *     FOREIGN KEY fk_template_user (user_id) REFERENCES users(id)
 * );
 * </pre>
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 템플릿 생성
 * Template template = Template.builder()
 *     .user(user)
 *     .title("제주도 3박 4일 여행")
 *     .destination("제주도")
 *     .startDate(LocalDate.of(2024, 2, 1))
 *     .endDate(LocalDate.of(2024, 2, 4))
 *     .totalDays(4)
 *     .accommodation("제주 신라호텔")
 *     .transportation("대한항공 KE1234")
 *     .build();
 * templateRepository.save(template);
 * 
 * // 일정 추가 (Cascade)
 * DaySchedule day1 = DaySchedule.builder()
 *     .template(template)
 *     .dayNumber(1)
 *     .title("첫째 날")
 *     .build();
 * template.getDaySchedules().add(day1);
 * templateRepository.save(template); // day1도 자동 저장
 * 
 * // 사용자별 템플릿 조회
 * List<Template> templates = templateRepository.findAllByUserId(userId);
 * 
 * // 날짜 범위로 검색
 * List<Template> templates = templateRepository.findByDateRange(
 *     LocalDate.of(2024, 2, 1),
 *     LocalDate.of(2024, 2, 28)
 * );
 * }
 * </pre>
 * 
 * 테이블 데이터 예시:
 * <pre>
 * +----+---------+------------------+--------+------------+------------+------------+-------------+---------------+
 * | id | user_id | title            | dest   | start_date | end_date   | total_days | accomm      | transport     |
 * +----+---------+------------------+--------+------------+------------+------------+-------------+---------------+
 * | 1  | 1       | 제주도 3박 4일   | 제주도  | 2024-02-01 | 2024-02-04 | 4          | 신라호텔    | 대한항공      |
 * | 2  | 1       | 부산 2박 3일     | 부산    | 2024-03-10 | 2024-03-12 | 3          | 파라다이스  | KTX          |
 * | 3  | 2       | 일본 도쿄 5박 6일| 도쿄    | 2024-04-01 | 2024-04-06 | 6          | 힐튼호텔    | 아시아나      |
 * +----+---------+------------------+--------+------------+------------+------------+-------------+---------------+
 * </pre>
 * 
 * 관계:
 * - Template N : 1 User (템플릿은 한 사용자에 속함)
 * - Template 1 : N DaySchedule (템플릿은 여러 일정 포함)
 * - Template 1 : N ChecklistSection (템플릿은 여러 체크리스트 섹션 포함)
 * 
 * @see User
 * @see DaySchedule
 * @see ChecklistSection
 * @see BaseTimeEntity
 */
@Entity
@Table(name = "templates", indexes = {
    // 사용자별 템플릿 조회 최적화
    // 쿼리: SELECT * FROM templates WHERE user_id = ?
    // 이점: 사용자의 모든 템플릿 빠르게 조회 (Full Scan → Index Scan)
    @Index(name = "idx_template_user_id", columnList = "user_id"),
    
    // 날짜 범위 검색 최적화
    // 쿼리: SELECT * FROM templates WHERE start_date >= ? AND end_date <= ?
    // 이점: 특정 기간의 여행 계획 검색 성능 향상
    // 예시: 2024년 2월에 시작하는 모든 여행 조회
    @Index(name = "idx_template_dates", columnList = "startDate, endDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template extends BaseTimeEntity {
    
    /**
     * 템플릿 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 템플릿 소유자 (다대일 관계)
     * 
     * @ManyToOne: 여러 템플릿이 한 사용자에 속함
     * @JoinColumn: 외래 키 컬럼명 지정 (user_id)
     * 
     * fetch = FetchType.LAZY:
     * - 지연 로딩 (템플릿 조회 시 사용자 정보는 필요할 때만 로딩)
     * - N+1 문제 방지: fetch join 사용 권장
     * 
     * 예시:
     * {@code
     * // 지연 로딩 (2번의 쿼리)
     * Template template = templateRepository.findById(1L).get();  // SELECT templates
     * String userName = template.getUser().getName();             // SELECT users (여기서 로딩)
     * 
     * // Fetch Join (1번의 쿼리, 권장)
     * @Query("SELECT t FROM Template t JOIN FETCH t.user WHERE t.id = :id")
     * Template findByIdWithUser(@Param("id") Long id);
     * }
     * 
     * foreignKey = @ForeignKey(name = "fk_template_user"):
     * - 외래 키 제약 조건명 명시
     * - 데이터 무결성 보장 (존재하지 않는 user_id 방지)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_template_user"))
    private User user;
    
    /**
     * 템플릿 제목
     * 
     * 예시:
     * - "제주도 3박 4일 힐링 여행"
     * - "부산 가족 여행"
     * - "일본 도쿄 자유여행"
     * 
     * 제약: 최대 200자
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 목적지
     * 
     * 예시:
     * - "제주도"
     * - "부산"
     * - "일본 도쿄"
     * - "프랑스 파리"
     * 
     * 제약: 최대 200자
     * 
     * 활용: 목적지별 통계, 인기 여행지 분석 가능
     */
    @Column(nullable = false, length = 200)
    private String destination;
    
    /**
     * 여행 시작일
     * 
     * 예시: 2024-02-01
     * 
     * 활용:
     * - 다가오는 여행 알림
     * - 월별/계절별 여행 통계
     * - 날짜 범위 검색
     */
    @Column(nullable = false)
    private LocalDate startDate;
    
    /**
     * 여행 종료일
     * 
     * 예시: 2024-02-04
     * 
     * 검증: endDate >= startDate (서비스 레이어에서 검증)
     */
    @Column(nullable = false)
    private LocalDate endDate;
    
    /**
     * 총 여행 일수
     * 
     * 계산 방법: DAYS_BETWEEN(endDate, startDate) + 1
     * 
     * 예시:
     * - startDate: 2024-02-01, endDate: 2024-02-04
     * - totalDays: 4 (3박 4일)
     * 
     * 용도:
     * - 일정 개수 검증 (daySchedules.size() == totalDays)
     * - UI 표시 ("3박 4일")
     */
    @Column(nullable = false)
    private Integer totalDays;
    
    /**
     * 숙소 정보 (선택사항)
     * 
     * 예시:
     * - "제주 신라호텔"
     * - "부산 파라다이스호텔"
     * - "에어비앤비 강남"
     * - null (미정)
     * 
     * 제약: 최대 200자
     */
    @Column(length = 200)
    private String accommodation;
    
    /**
     * 교통 수단 정보 (선택사항)
     * 
     * 예시:
     * - "대한항공 KE1234"
     * - "KTX 15:30"
     * - "렌터카"
     * - null (미정)
     * 
     * 제약: 최대 200자
     */
    @Column(length = 200)
    private String transportation;
    
    /**
     * 체크리스트 섹션 목록 (일대다 관계)
     * 
     * @OneToMany: 하나의 템플릿이 여러 체크리스트 섹션 포함
     * mappedBy = "template": ChecklistSection의 template 필드가 관계 주인
     * 
     * cascade = CascadeType.ALL:
     * - 템플릿 저장 시 체크리스트도 자동 저장
     * - 템플릿 수정 시 체크리스트도 자동 수정
     * - 템플릿 삭제 시 체크리스트도 자동 삭제
     * 
     * orphanRemoval = true:
     * - 리스트에서 제거된 체크리스트 자동 삭제
     * 
     * 예시:
     * {@code
     * // 체크리스트 추가 (자동 저장)
     * ChecklistSection section = ChecklistSection.builder()
     *     .template(template)
     *     .title("준비물")
     *     .build();
     * template.getChecklistSections().add(section);
     * templateRepository.save(template); // section도 함께 저장
     * 
     * // 체크리스트 제거 (자동 삭제)
     * template.getChecklistSections().remove(0);
     * templateRepository.save(template); // 제거된 section 자동 삭제
     * }
     * 
     * 데이터 예시:
     * - 섹션 1: "준비물" (여권, 카메라, 충전기)
     * - 섹션 2: "예약 확인" (호텔, 항공권, 렌터카)
     * - 섹션 3: "현지 정보" (환율, 긴급연락처)
     */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChecklistSection> checklistSections = new ArrayList<>();
    
    /**
     * 일정 목록 (일대다 관계)
     * 
     * @OneToMany: 하나의 템플릿이 여러 일정 포함
     * mappedBy = "template": DaySchedule의 template 필드가 관계 주인
     * 
     * cascade = CascadeType.ALL:
     * - 템플릿 저장/수정/삭제 시 일정도 자동 처리
     * 
     * orphanRemoval = true:
     * - 리스트에서 제거된 일정 자동 삭제
     * 
     * 예시:
     * {@code
     * // 일정 추가
     * DaySchedule day1 = DaySchedule.builder()
     *     .template(template)
     *     .dayNumber(1)
     *     .date(LocalDate.of(2024, 2, 1))
     *     .title("첫째 날 - 도착 및 호텔 체크인")
     *     .build();
     * template.getDaySchedules().add(day1);
     * 
     * DaySchedule day2 = DaySchedule.builder()
     *     .template(template)
     *     .dayNumber(2)
     *     .date(LocalDate.of(2024, 2, 2))
     *     .title("둘째 날 - 관광")
     *     .build();
     * template.getDaySchedules().add(day2);
     * 
     * templateRepository.save(template); // 모든 일정 자동 저장
     * }
     * 
     * 데이터 예시 (3박 4일):
     * - Day 1 (2024-02-01): "도착 및 호텔 체크인"
     * - Day 2 (2024-02-02): "성산일출봉 → 섭지코지"
     * - Day 3 (2024-02-03): "한라산 등반"
     * - Day 4 (2024-02-04): "쇼핑 및 출발"
     */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DaySchedule> daySchedules = new ArrayList<>();
    
    /**
     * equals 메서드 재정의
     * 
     * 역할: JPA 엔티티 동등성 비교
     * 
     * 구현: ID 기반 비교 (프록시 객체 고려)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Template)) return false;
        Template template = (Template) o;
        return id != null && id.equals(template.getId());
    }
    
    /**
     * hashCode 메서드 재정의
     * 
     * 역할: Hash 기반 컬렉션 사용 시 일관성 유지
     * 
     * 구현: 클래스 타입 기반 (불변 값)
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}