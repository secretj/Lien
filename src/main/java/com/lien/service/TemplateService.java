package com.lien.service;

import com.lien.dto.request.ActivityRequest;
import com.lien.dto.request.ChecklistSectionRequest;
import com.lien.dto.request.DayScheduleRequest;
import com.lien.dto.request.TemplateCreateRequest;
import com.lien.dto.response.LocationResponse;
import com.lien.dto.response.TemplateResponse;
import com.lien.entity.Activity;
import com.lien.entity.ChecklistItem;
import com.lien.entity.ChecklistSection;
import com.lien.entity.DaySchedule;
import com.lien.entity.Location;
import com.lien.entity.Template;
import com.lien.entity.User;
import com.lien.repository.ActivityRepository;
import com.lien.repository.ChecklistSectionRepository;
import com.lien.repository.DayScheduleRepository;
import com.lien.repository.LocationRepository;
import com.lien.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService
{

    private final TemplateRepository templateRepository;
    private final LocationRepository locationRepository;
    private final ChecklistSectionRepository checklistSectionRepository;
    private final DayScheduleRepository dayScheduleRepository;
    private final ActivityRepository activityRepository;

    @Transactional
    public TemplateResponse createTemplate(User user, TemplateCreateRequest request)
    {
        Template template = Template.builder()
                .user(user)
                .title(request.getTitle())
                .destination(request.getDestination())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(request.getTotalDays())
                .accommodation(request.getAccommodation())
                .transportation(request.getTransportation())
                .build();

        template = templateRepository.save(template);
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplates(User user, Pageable pageable)
    {
        return templateRepository.findByUser(user, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Object getTemplateDetail(User user, Long templateId)
    {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다"));

        if (!template.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

        return toDetailResponse(template);
    }

    @Transactional
    public TemplateResponse updateTemplate(
        User user,
        Long templateId,
        TemplateCreateRequest request
    ) {
        Template template = findTemplateByIdAndUser(templateId, user);

        template.setTitle(request.getTitle());
        template.setDestination(request.getDestination());
        template.setStartDate(request.getStartDate());
        template.setEndDate(request.getEndDate());
        template.setTotalDays(request.getTotalDays());
        template.setAccommodation(request.getAccommodation());
        template.setTransportation(request.getTransportation());

        return toResponse(templateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(User user, Long templateId)
    {
        Template template = findTemplateByIdAndUser(templateId, user);
        templateRepository.delete(template);
    }

    // Checklist Section Methods
    @Transactional
    public Object addChecklistSection(
        User user,
        Long templateId,
        ChecklistSectionRequest request
    ) {
        Template template = findTemplateByIdAndUser(templateId, user);

        ChecklistSection section = ChecklistSection.builder()
                .template(template)
                .title(request.getTitle())
                .icon(request.getIcon())
                .orderIndex(request.getOrderIndex())
                .build();

        for (ChecklistSectionRequest.ChecklistItemDto itemDto : request.getItems()) {
            ChecklistItem item = ChecklistItem.builder()
                    .section(section)
                    .label(itemDto.getLabel())
                    .orderIndex(itemDto.getOrderIndex())
                    .build();
            section.getItems().add(item);
        }

        section = checklistSectionRepository.save(section);
        return toChecklistSectionResponse(section);
    }

    @Transactional
    public Object updateChecklistSection(
        User user,
        Long templateId,
        Long sectionId,
        ChecklistSectionRequest request
    ) {
        Template template = findTemplateByIdAndUser(templateId, user);

        ChecklistSection section = checklistSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("섹션을 찾을 수 없습니다"));

        if (!section.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("잘못된 요청입니다");
        }

        section.setTitle(request.getTitle());
        section.setIcon(request.getIcon());
        section.setOrderIndex(request.getOrderIndex());

        // 기존 아이템 삭제 후 새로 추가
        section.getItems().clear();
        for (ChecklistSectionRequest.ChecklistItemDto itemDto : request.getItems()) {
            ChecklistItem item = ChecklistItem.builder()
                    .section(section)
                    .label(itemDto.getLabel())
                    .orderIndex(itemDto.getOrderIndex())
                    .build();
            section.getItems().add(item);
        }

        return toChecklistSectionResponse(checklistSectionRepository.save(section));
    }

    @Transactional
    public void deleteChecklistSection(User user, Long templateId, Long sectionId)
    {
        Template template = findTemplateByIdAndUser(templateId, user);

        ChecklistSection section = checklistSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("섹션을 찾을 수 없습니다"));

        if (!section.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("잘못된 요청입니다");
        }

        checklistSectionRepository.delete(section);
    }

    // Day Schedule Methods
    @Transactional
    public Object addDaySchedule(User user, Long templateId, DayScheduleRequest request)
    {
        Template template = findTemplateByIdAndUser(templateId, user);

        DaySchedule daySchedule = DaySchedule.builder()
                .template(template)
                .dayNumber(request.getDayNumber())
                .date(request.getDate())
                .title(request.getTitle())
                .color(request.getColor())
                .build();

        daySchedule = dayScheduleRepository.save(daySchedule);
        return toDayScheduleResponse(daySchedule);
    }

    @Transactional
    public Object updateDaySchedule(
        User user,
        Long templateId,
        Long dayId,
        DayScheduleRequest request
    ) {
        findTemplateByIdAndUser(templateId, user);

        DaySchedule daySchedule = dayScheduleRepository.findById(dayId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다"));

        if (!daySchedule.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("잘못된 요청입니다");
        }

        daySchedule.setDayNumber(request.getDayNumber());
        daySchedule.setDate(request.getDate());
        daySchedule.setTitle(request.getTitle());
        daySchedule.setColor(request.getColor());

        return toDayScheduleResponse(dayScheduleRepository.save(daySchedule));
    }

    @Transactional
    public void deleteDaySchedule(User user, Long templateId, Long dayId)
    {
        findTemplateByIdAndUser(templateId, user);

        DaySchedule daySchedule = dayScheduleRepository.findById(dayId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다"));

        if (!daySchedule.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("잘못된 요청입니다");
        }

        dayScheduleRepository.delete(daySchedule);
    }

    // Activity Methods
    @Transactional
    public Object addActivity(
        User user,
        Long templateId,
        Long dayId,
        ActivityRequest request
    ) {
        findTemplateByIdAndUser(templateId, user);

        DaySchedule daySchedule = dayScheduleRepository.findById(dayId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다"));

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("위치를 찾을 수 없습니다"));

        Location previousLocation = null;
        if (request.getPreviousLocationId() != null) {
            previousLocation = locationRepository.findById(request.getPreviousLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("이전 위치를 찾을 수 없습니다"));
        }

        Activity activity = Activity.builder()
                .daySchedule(daySchedule)
                .time(request.getTime())
                .description(request.getDescription())
                .location(location)
                .previousLocation(previousLocation)
                .orderIndex(request.getOrderIndex())
                .build();

        activity = activityRepository.save(activity);
        return toActivityResponse(activity);
    }

    @Transactional
    public Object updateActivity(
        User user,
        Long templateId,
        Long dayId,
        Long activityId,
        ActivityRequest request
    ) {
        findTemplateByIdAndUser(templateId, user);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없습니다"));

        if (!activity.getDaySchedule().getId().equals(dayId)) {
            throw new IllegalArgumentException("잘못된 요청입니다");
        }

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("위치를 찾을 수 없습니다"));

        activity.setTime(request.getTime());
        activity.setDescription(request.getDescription());
        activity.setLocation(location);
        activity.setOrderIndex(request.getOrderIndex());

        if (request.getPreviousLocationId() != null) {
            Location previousLocation = locationRepository.findById(request.getPreviousLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("이전 위치를 찾을 수 없습니다"));
            activity.setPreviousLocation(previousLocation);
        }

        return toActivityResponse(activityRepository.save(activity));
    }

    @Transactional
    public void deleteActivity(User user, Long templateId, Long dayId, Long activityId)
    {
        findTemplateByIdAndUser(templateId, user);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없습니다"));

        if (!activity.getDaySchedule().getId().equals(dayId)) {
            throw new IllegalArgumentException("잘못된 요청입니다");
        }

        activityRepository.delete(activity);
    }

    // Helper Methods
    private Template findTemplateByIdAndUser(Long templateId, User user)
    {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다"));

        if (!template.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

        return template;
    }

    private TemplateResponse toResponse(Template template)
    {
        return TemplateResponse.builder()
                .id(template.getId())
                .title(template.getTitle())
                .destination(template.getDestination())
                .startDate(template.getStartDate())
                .endDate(template.getEndDate())
                .totalDays(template.getTotalDays())
                .accommodation(template.getAccommodation())
                .transportation(template.getTransportation())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private Object toDetailResponse(Template templateEntity)
    {
        final TemplateResponse templateResponse = toResponse(templateEntity);
        return new Object() {
            public final TemplateResponse template = templateResponse;
            public final java.util.List<?> checklistSections = templateEntity.getChecklistSections()
                    .stream().map(TemplateService.this::toChecklistSectionResponse)
                    .collect(Collectors.toList());
            public final java.util.List<?> daySchedules = templateEntity.getDaySchedules()
                    .stream().map(TemplateService.this::toDayScheduleResponse)
                    .collect(Collectors.toList());
        };
    }

    private Object toChecklistSectionResponse(ChecklistSection section)
    {
        return new Object() {
            public final Long id = section.getId();
            public final String title = section.getTitle();
            public final String icon = section.getIcon();
            public final Integer orderIndex = section.getOrderIndex();
            public final java.util.List<?> items = section.getItems().stream()
                    .map(item -> new Object() {
                        public final Long id = item.getId();
                        public final String label = item.getLabel();
                        public final Integer orderIndex = item.getOrderIndex();
                    }).collect(Collectors.toList());
        };
    }

    private Object toDayScheduleResponse(DaySchedule daySchedule)
    {
        return new Object() {
            public final Long id = daySchedule.getId();
            public final Integer dayNumber = daySchedule.getDayNumber();
            public final java.time.LocalDate date = daySchedule.getDate();
            public final String title = daySchedule.getTitle();
            public final String color = daySchedule.getColor();
            public final java.util.List<?> activities = daySchedule.getActivities()
                    .stream().map(TemplateService.this::toActivityResponse)
                    .collect(Collectors.toList());
        };
    }

    private Object toActivityResponse(Activity activity)
    {
        return new Object() {
            public final Long id = activity.getId();
            public final String time = activity.getTime();
            public final String description = activity.getDescription();
            public final LocationResponse location = LocationResponse.builder()
                    .id(activity.getLocation().getId())
                    .name(activity.getLocation().getName())
                    .category(activity.getLocation().getCategory())
                    .latitude(activity.getLocation().getLatitude())
                    .longitude(activity.getLocation().getLongitude())
                    .address(activity.getLocation().getAddress())
                    .description(activity.getLocation().getDescription())
                    .isPublic(activity.getLocation().getIsPublic())
                    .createdAt(activity.getLocation().getCreatedAt())
                    .build();
            public final LocationResponse previousLocation = activity.getPreviousLocation() != null
                    ? LocationResponse.builder()
                            .id(activity.getPreviousLocation().getId())
                            .name(activity.getPreviousLocation().getName())
                            .category(activity.getPreviousLocation().getCategory())
                            .latitude(activity.getPreviousLocation().getLatitude())
                            .longitude(activity.getPreviousLocation().getLongitude())
                            .address(activity.getPreviousLocation().getAddress())
                            .build()
                    : null;
            public final Integer orderIndex = activity.getOrderIndex();
        };
    }
}

