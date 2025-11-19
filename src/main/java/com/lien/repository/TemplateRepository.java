package com.lien.repository;

import com.lien.entity.Template;
import com.lien.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    Page<Template> findByUser(User user, Pageable pageable);
    
    @Query("SELECT t FROM Template t LEFT JOIN FETCH t.checklistSections cs " +
           "LEFT JOIN FETCH cs.items WHERE t.id = :templateId AND t.user = :user")
    Template findByIdAndUserWithChecklistSections(@Param("templateId") Long templateId, 
                                                   @Param("user") User user);
    
    @Query("SELECT t FROM Template t LEFT JOIN FETCH t.daySchedules ds " +
           "LEFT JOIN FETCH ds.activities a LEFT JOIN FETCH a.location " +
           "WHERE t.id = :templateId AND t.user = :user")
    Template findByIdAndUserWithDaySchedules(@Param("templateId") Long templateId, 
                                             @Param("user") User user);
}