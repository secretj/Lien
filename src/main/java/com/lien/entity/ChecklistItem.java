package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ==============================================================================
 * ChecklistItem Entity (체크리스트 항목 엔티티)
 * ==============================================================================
 * 
 * 역할:
 * - 체크리스트 섹션 내의 개별 항목
 * - 준비물 또는 할 일 목록의 최소 단위
 * - 체크 상태 관리 (완료/미완료)
 * 
 * 이점:
 * 1. 상세한 준비물 관리
 * 2. 체크 여부 추적
 * 3. 순서 보장
 * 4. 진행률 계산
 * 
 * 데이터베이스 스키마:
 * <pre>
 * CREATE TABLE checklist_items (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     section_id BIGINT NOT NULL,
 *     label VARCHAR(200) NOT NULL,
 *     order_index INT NOT NULL,
 *     FOREIGN KEY (section_id) REFERENCES checklist_sections(id)
 * );
 * </pre>
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 체크리스트 아이템 생성
 * ChecklistItem passport = ChecklistItem.builder()
 *     .section(documentsSection)
 *     .label("여권 (유효기간 확인)")
 *     .orderIndex(1)
 *     .build();
 * 
 * ChecklistItem ticket = ChecklistItem.builder()
 *     .section(documentsSection)
 *     .label("항공권 (e-티켓 출력)")
 *     .orderIndex(2)
 *     .build();
 * 
 * ChecklistItem insurance = ChecklistItem.builder()
 *     .section(documentsSection)
 *     .label("여행자 보험 가입")
 *     .orderIndex(3)
 *     .build();
 * 
 * // 섹션에 추가
 * documentsSection.getItems().add(passport);
 * documentsSection.getItems().add(ticket);
 * documentsSection.getItems().add(insurance);
 * }
 * </pre>
 * 
 * 테이블 데이터 예시:
 * <pre>
 * +----+------------+--------------------------------+-------------+
 * | id | section_id | label                          | order_index |
 * +----+------------+--------------------------------+-------------+
 * | 1  | 1          | 여권 (유효기간 6개월 이상)     | 1           |
 * | 2  | 1          | 항공권 (e-티켓 출력)           | 2           |
 * | 3  | 1          | 호텔 예약 확인서               | 3           |
 * | 4  | 1          | 여행자 보험                    | 4           |
 * | 5  | 2          | 티셔츠 3벌                     | 1           |
 * | 6  | 2          | 긴팔 1벌                       | 2           |
 * | 7  | 2          | 바지 2벌                       | 3           |
 * | 8  | 3          | 스마트폰                       | 1           |
 * | 9  | 3          | 충전기 (220V 변환 어댑터)      | 2           |
 * +----+------------+--------------------------------+-------------+
 * </pre>
 * 
 * UI 표시 예시:
 * <pre>
 * 📄 필수 서류
 *    ✅ 여권 (유효기간 6개월 이상)
 *    ✅ 항공권 (e-티켓 출력)
 *    ✅ 호텔 예약 확인서
 *    ☐  여행자 보험
 *    ☐  국제 운전면허증
 * 
 * 👕 의류
 *    ✅ 티셔츠 3벌
 *    ✅ 바지 2벌
 *    ☐  외투
 *    ☐  속옷 5벌
 *    ☐  양말 5켤레
 *    ☐  운동화
 * 
 * 📱 전자기기
 *    ✅ 스마트폰
 *    ☐  충전기 (220V 변환 어댑터)
 *    ☐  보조배터리
 *    ☐  카메라
 * </pre>
 * 
 * 체크 상태 관리:
 * - 현재는 체크 상태 필드가 없음
 * - 향후 확장: isChecked Boolean 필드 추가 가능
 * - 또는 별도의 ChecklistProgress 테이블로 관리
 * 
 * 확장 예시:
 * <pre>
 * {@code
 * @Column(nullable = false)
 * @Builder.Default
 * private Boolean checked = false;
 * 
 * // 체크 토글
 * item.setChecked(!item.getChecked());
 * 
 * // 진행률 계산
 * long total = section.getItems().size();
 * long checked = section.getItems().stream()
 *     .filter(ChecklistItem::getChecked)
 *     .count();
 * double progress = (double) checked / total * 100;
 * }
 * </pre>
 * 
 * 관계:
 * - ChecklistItem N : 1 ChecklistSection (아이템은 하나의 섹션에 속함)
 * 
 * @see ChecklistSection
 */
@Entity
@Table(name = "checklist_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistItem {
    
    /**
     * 아이템 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 소속 섹션 (다대일 관계)
     * 
     * fetch = FetchType.LAZY: 지연 로딩
     * - 아이템 조회 시 섹션은 필요할 때만 로딩
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private ChecklistSection section;
    
    /**
     * 항목 레이블 (설명)
     * 
     * 예시:
     * - "여권 (유효기간 6개월 이상 확인)"
     * - "항공권 (e-티켓 출력 또는 모바일 저장)"
     * - "호텔 예약 확인서"
     * - "여행자 보험 (해외 의료비 보장)"
     * - "국제 운전면허증"
     * - "티셔츠 3벌"
     * - "충전기 (220V 변환 어댑터 필요)"
     * - "상비약 (소화제, 진통제)"
     * 
     * 팁:
     * - 구체적으로 작성 (개수, 주의사항 포함)
     * - 추가 정보 괄호로 표시
     * 
     * 제약: 최대 200자
     */
    @Column(nullable = false, length = 200)
    private String label;
    
    /**
     * 정렬 순서 (인덱스)
     * 
     * 역할:
     * - 아이템 순서 보장
     * - UI 표시 순서
     * - 중요도 또는 단계별 정렬
     * 
     * 규칙:
     * - 1부터 시작 (section 내에서 순차적)
     * - 중요한 항목을 앞에 배치
     * 
     * 예시 (필수 서류 섹션):
     * - 1: 여권 (가장 중요)
     * - 2: 항공권
     * - 3: 호텔 예약 확인서
     * - 4: 여행자 보험
     * - 5: 국제 운전면허증
     * 
     * 재정렬:
     * {@code
     * // 아이템 2와 3 사이에 새 아이템 삽입
     * ChecklistItem newItem = ChecklistItem.builder()
     *     .orderIndex(3)
     *     .build();
     * 
     * // 기존 orderIndex 3 이상인 아이템들 +1
     * items.stream()
     *     .filter(i -> i.getOrderIndex() >= 3)
     *     .forEach(i -> i.setOrderIndex(i.getOrderIndex() + 1));
     * }
     */
    @Column(nullable = false)
    private Integer orderIndex;
}