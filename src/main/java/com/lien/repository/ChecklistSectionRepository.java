package com.lien.repository;

import com.lien.entity.ChecklistSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ==============================================================================
 * ChecklistSection Repository (체크리스트 섹션 레포지토리)
 * ==============================================================================
 * 
 * 역할:
 * - ChecklistSection 엔티티의 데이터베이스 접근 계층
 * - 템플릿별 체크리스트 섹션 조회
 * 
 * 이점:
 * 1. JpaRepository 기본 CRUD
 * 2. Template을 통한 Cascade 관리
 * 3. @OrderBy로 자동 정렬
 * 
 * 주의사항:
 * - 대부분의 경우 TemplateRepository의 Fetch Join 사용 권장
 * - 체크리스트만 필요한 경우에 사용
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 일반적으로 Template을 통해 조회 (권장)
 * Template template = templateRepository
 *     .findByIdAndUserWithChecklistSections(id, user);
 * List<ChecklistSection> sections = template.getChecklistSections();
 * 
 * // 또는 직접 조회 (특수한 경우)
 * List<ChecklistSection> sections = checklistSectionRepository
 *     .findByTemplateIdOrderByOrderIndexAsc(templateId);
 * }
 * </pre>
 * 
 * @see ChecklistSection
 * @see TemplateRepository
 * @see JpaRepository
 */
public interface ChecklistSectionRepository extends JpaRepository<ChecklistSection, Long> {
    
    /**
     * 템플릿별 체크리스트 섹션 조회 (순서대로)
     * 
     * 역할:
     * - 특정 템플릿의 모든 체크리스트 섹션 조회
     * - orderIndex 기준 오름차순 정렬
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM checklist_sections 
     * WHERE template_id = ? 
     * ORDER BY order_index ASC;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * List<ChecklistSection> sections = checklistSectionRepository
     *     .findByTemplateIdOrderByOrderIndexAsc(templateId);
     * 
     * for (ChecklistSection section : sections) {
     *     System.out.println(section.getIcon() + " " + section.getTitle());
     *     System.out.println("  항목 수: " + section.getItems().size());
     * }
     * 
     * // 출력 예시:
     * // 📄 필수 서류
     * //   항목 수: 4
     * // 👕 의류
     * //   항목 수: 5
     * }
     * </pre>
     * 
     * @param templateId 템플릿 ID
     * @return List<ChecklistSection> 섹션 목록 (순서대로)
     */
    List<ChecklistSection> findByTemplateIdOrderByOrderIndexAsc(Long templateId);
}
