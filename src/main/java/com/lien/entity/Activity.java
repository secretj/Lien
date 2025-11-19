package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activities")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_schedule_id", nullable = false)
    private DaySchedule daySchedule;
    
    @Column(nullable = false, length = 50)
    private String time;
    
    @Column(nullable = false, length = 300)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_location_id")
    private Location previousLocation;
    
    @Column(nullable = false)
    private Integer orderIndex;
}
