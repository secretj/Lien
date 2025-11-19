package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checklist_items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private ChecklistSection section;
    
    @Column(nullable = false, length = 200)
    private String label;
    
    @Column(nullable = false)
    private Integer orderIndex;
}