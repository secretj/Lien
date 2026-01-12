package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * ChecklistSection Entity (체크리스트 섹션 엔티티)
 * ==============================================================================
 * 
 * 역할:
 * - 여행 준비물 체크리스트의 섹션(카테고리)를 표현
 * - 여러 ChecklistItem을 그룹화
 * - 아이콘과 순서로 시각화
 * 
 * 이점:
 * 1. 체계적인 준비물 관리
 * 2. 카테고리별 그룹화 (서류, 의류, 전자기기 등)
 * 3. 체크 진행률 계산 가능
 * 4. 템플릿별 맞춤형 체크리스트
 * 
 * 데이터베이스 스키마:
 * <pre>
 * CREATE TABLE checklist_sections (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     template_id BIGINT NOT NULL,
 *     title VARCHAR(100) NOT NULL,
 *     icon VARCHAR(10),
 *     order_index INT NOT NULL,
 *     FOREIGN KEY (template_id) REFERENCES templates(id)
 * );
 * </pre>
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 체크리스트 섹션 생성
 * ChecklistSection documents = ChecklistSection.builder()
 *     .template(template)
 *     .title("필수 서류")
 *     .icon("📄")
 *     .orderIndex(1)
 *     .build();
 * 
 * ChecklistSection clothes = ChecklistSection.builder()
 *     .template(template)
 *     .title("의류")
 *     .icon("👕")
 *     .orderIndex(2)
 *     .build();
 * 
 * // 아이템 추가
 * ChecklistItem passport = ChecklistItem.builder()
 *     .section(documents)
 *     .label("여권")
 *     .orderIndex(1)
 *     .build();
 * documents.getItems().add(passport);
 * 
 * // 진행률 계산
 * int total = section.getItems().size();
 * int checked = (int) section.getItems().stream()
 *     .filter(ChecklistItem::isChecked)
 *     .count();
 * double progress = (double) checked / total * 100;
 * System.out.println("진행률: " + progress + "%");
 * }
 * </pre>
 * 
 * 테이블 데이터 예시:
 * <pre>
 * +----+-------------+------------------+------+-------------+
 * | id | template_id | title            | icon | order_index |
 * +----+-------------+------------------+------+-------------+
 * | 1  | 1           | 필수 서류        | 📄   | 1           |
 * | 2  | 1           | 의류             | 👕   | 2           |
 * | 3  | 1           | 전자기기         | 📱   | 3           |
 * | 4  | 1           | 세면도구         | 🧴   | 4           |
 * | 5  | 1           | 의약품           | 💊   | 5           |
 * +----+-------------+------------------+------+-------------+
 * </pre>
 * 
 * UI 표시 예시:
 * <pre>
 * 여행 준비 체크리스트
 * 
 * 📄 필수 서류 (3/4 완료)
 *    ✅ 여권
 *    ✅ 항공권
 *    ✅ 호텔 예약 확인서
 *    ☐  여행자 보험
 * 
 * 👕 의류 (2/5 완료)
 *    ✅ 티셔츠 3벌
 *    ✅ 바지 2벌
 *    ☐  외투
 *    ☐  속옷
 *    ☐  양말
 * 
 * 📱 전자기기 (1/3 완료)
 *    ✅ 스마트폰
 *    ☐  충전기
 *    ☐  보조배터리
 * </pre>
 * 
 * 관계:
 * - ChecklistSection N : 1 Template (섹션은 하나의 템플릿에 속함)
 * - ChecklistSection 1 : N ChecklistItem (섹션은 여러 아이템 포함)
 * 
 * @see Template
 * @see ChecklistItem
 */
@Entity
@Table(name = "checklist_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistSection {
    
    /**
     * 섹션 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 소속 템플릿 (다대일 관계)
     * 
     * fetch = FetchType.LAZY: 지연 로딩
     * - 섹션 조회 시 템플릿은 필요할 때만 로딩
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
    
    /**
     * 섹션 제목
     * 
     * 예시:
     * - "필수 서류" (여권, 항공권, 예약 확인서)
     * - "의류" (티셔츠, 바지, 외투)
     * - "전자기기" (스마트폰, 충전기, 보조배터리)
     * - "세면도구" (칫솔, 치약, 샴푸)
     * - "의약품" (소화제, 진통제, 밴드)
     * - "기타" (선글라스, 우산, 가이드북)
     * 
     * 제약: 최대 100자
     */
    @Column(nullable = false, length = 100)
    private String title;
    
    /**
     * 아이콘 (선택사항)
     * 
     * 용도:
     * - UI 시각화
     * - 섹션 빠른 식별
     * - 사용자 경험 향상
     * 
     * 형식: 이모지 또는 아이콘 코드
     * 
     * 예시:
     * - "📄" (서류)
     * - "👕" (의류)
     * - "📱" (전자기기)
     * - "🧴" (세면도구)
     * - "💊" (의약품)
     * - "🎒" (가방/기타)
     * - null (아이콘 없음)
     * 
     * 제약: 최대 10자
     */
    @Column(length = 10)
    private String icon;
    
    /**
     * 정렬 순서 (인덱스)
     * 
     * 역할:
     * - 섹션 순서 보장
     * - UI 표시 순서
     * - 중요도 순 정렬
     * 
     * 규칙:
     * - 1부터 시작 (template 내에서 순차적)
     * - 일반적인 순서: 서류 → 의류 → 전자기기 → 세면도구 → 의약품 → 기타
     * 
     * 예시:
     * - 1: 필수 서류 (가장 중요)
     * - 2: 의류
     * - 3: 전자기기
     * - 4: 세면도구
     * - 5: 의약품
     * - 6: 기타
     */
    @Column(nullable = false)
    private Integer orderIndex;
    
    /**
     * 체크리스트 아이템 목록 (일대다 관계)
     * 
     * @OneToMany: 하나의 섹션이 여러 아이템 포함
     * mappedBy = "section": ChecklistItem의 section 필드가 관계 주인
     * 
     * cascade = CascadeType.ALL:
     * - 섹션 저장/수정/삭제 시 아이템도 자동 처리
     * 
     * orphanRemoval = true:
     * - 리스트에서 제거된 아이템 자동 삭제
     * 
     * @OrderBy("orderIndex ASC"):
     * - 조회 시 orderIndex 기준 오름차순 정렬
     * 
     * 예시:
     * {@code
     * // 아이템 추가
     * ChecklistItem item1 = ChecklistItem.builder()
     *     .section(section)
     *     .label("여권")
     *     .orderIndex(1)
     *     .build();
     * 
     * ChecklistItem item2 = ChecklistItem.builder()
     *     .section(section)
     *     .label("항공권")
     *     .orderIndex(2)
     *     .build();
     * 
     * section.getItems().add(item1);
     * section.getItems().add(item2);
     * // 조회 시 orderIndex로 자동 정렬됨
     * 
     * // 진행률 계산
     * int total = section.getItems().size();
     * int checked = (int) section.getItems().stream()
     *     .filter(ChecklistItem::isChecked)
     *     .count();
     * System.out.println(checked + "/" + total + " 완료");
     * }
     * 
     * 데이터 예시 (필수 서류 섹션):
     * 1. ✅ 여권
     * 2. ✅ 항공권
     * 3. ✅ 호텔 예약 확인서
     * 4. ☐  여행자 보험
     * 5. ☐  국제 운전면허증
     */
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<ChecklistItem> items = new ArrayList<>();
}