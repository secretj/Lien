package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "day_schedules")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DaySchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
    
    @Column(nullable = false)
    private Integer dayNumber;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(length = 20)
    private String color;
    
    @OneToMany(mappedBy = "daySchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Activity> activities = new ArrayList<>();
}