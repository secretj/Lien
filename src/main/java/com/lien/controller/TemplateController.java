package com.lien.controller;

import com.lien.dto.request.ActivityRequest;
import com.lien.dto.request.ChecklistSectionRequest;
import com.lien.dto.request.DayScheduleRequest;
import com.lien.dto.request.TemplateCreateRequest;
import com.lien.dto.response.TemplateResponse;
import com.lien.entity.User;
import com.lien.security.CurrentUser;
import com.lien.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @CurrentUser User user,
            @Valid @RequestBody TemplateCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(templateService.createTemplate(user, request));
    }

    @GetMapping
    public ResponseEntity<Page<TemplateResponse>> getTemplates(
            @CurrentUser User user,
            Pageable pageable) {
        return ResponseEntity.ok(templateService.getTemplates(user, pageable));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<?> getTemplate(
            @CurrentUser User user,
            @PathVariable Long templateId) {
        return ResponseEntity.ok(templateService.getTemplateDetail(user, templateId));
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @Valid @RequestBody TemplateCreateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(user, templateId, request));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @CurrentUser User user,
            @PathVariable Long templateId) {
        templateService.deleteTemplate(user, templateId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{templateId}/checklist-sections")
    public ResponseEntity<?> addChecklistSection(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @Valid @RequestBody ChecklistSectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(templateService.addChecklistSection(user, templateId, request));
    }

    @PutMapping("/{templateId}/checklist-sections/{sectionId}")
    public ResponseEntity<?> updateChecklistSection(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @PathVariable Long sectionId,
            @Valid @RequestBody ChecklistSectionRequest request) {
        return ResponseEntity.ok(
                templateService.updateChecklistSection(user, templateId, sectionId, request));
    }

    @DeleteMapping("/{templateId}/checklist-sections/{sectionId}")
    public ResponseEntity<Void> deleteChecklistSection(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @PathVariable Long sectionId) {
        templateService.deleteChecklistSection(user, templateId, sectionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{templateId}/days")
    public ResponseEntity<?> addDaySchedule(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @Valid @RequestBody DayScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(templateService.addDaySchedule(user, templateId, request));
    }

    @PutMapping("/{templateId}/days/{dayId}")
    public ResponseEntity<?> updateDaySchedule(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @PathVariable Long dayId,
            @Valid @RequestBody DayScheduleRequest request) {
        return ResponseEntity.ok(
                templateService.updateDaySchedule(user, templateId, dayId, request));
    }

    @DeleteMapping("/{templateId}/days/{dayId}")
    public ResponseEntity<Void> deleteDaySchedule(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @PathVariable Long dayId) {
        templateService.deleteDaySchedule(user, templateId, dayId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{templateId}/days/{dayId}/activities")
    public ResponseEntity<?> addActivity(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @PathVariable Long dayId,
            @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(templateService.addActivity(user, templateId, dayId, request));
    }

    @PutMapping("/{templateId}/days/{dayId}/activities/{activityId}")
    public ResponseEntity<?> updateActivity(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @PathVariable Long dayId,
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(
                templateService.updateActivity(user, templateId, dayId, activityId, request));
    }

    @DeleteMapping("/{templateId}/days/{dayId}/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @CurrentUser User user,
            @PathVariable Long templateId,
            @PathVariable Long dayId,
            @PathVariable Long activityId) {
        templateService.deleteActivity(user, templateId, dayId, activityId);
        return ResponseEntity.noContent().build();
    }
}

