package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checklist_sections")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistSection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(length = 10)
    private String icon;
    
    @Column(nullable = false)
    private Integer orderIndex;
    
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<ChecklistItem> items = new ArrayList<>();
}