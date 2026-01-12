package com.lien.service;

import com.lien.dto.request.ActivityRequest;
import com.lien.dto.request.ChecklistSectionRequest;
import com.lien.dto.request.DayScheduleRequest;
import com.lien.dto.request.TemplateCreateRequest;
import com.lien.dto.response.TemplateResponse;
import com.lien.entity.Location;
import com.lien.entity.LocationCategory;
import com.lien.entity.Template;
import com.lien.entity.User;
import com.lien.repository.ActivityRepository;
import com.lien.repository.ChecklistSectionRepository;
import com.lien.repository.DayScheduleRepository;
import com.lien.repository.LocationRepository;
import com.lien.repository.TemplateRepository;
import com.lien.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class TemplateServiceTest
{

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DayScheduleRepository dayScheduleRepository;

    @Autowired
    private ChecklistSectionRepository checklistSectionRepository;

    @Autowired
    private ActivityRepository activityRepository;

    private User testUser;

    @BeforeEach
    void setUp()
    {
        activityRepository.deleteAll();
        dayScheduleRepository.deleteAll();
        checklistSectionRepository.deleteAll();
        templateRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .email("test@test.com")
                .password("password")
                .name("테스트유저")
                .enabled(true)
                .build());
    }

    @Test
    void 템플릿_생성_성공()
    {
        // given
        TemplateCreateRequest request = new TemplateCreateRequest();
        request.setTitle("방콕 여행");
        request.setDestination("Bangkok, Thailand");
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 1, 5));
        request.setTotalDays(5);
        request.setAccommodation("호텔");
        request.setTransportation("비행기");

        // when
        TemplateResponse response = templateService.createTemplate(testUser, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("방콕 여행");
        assertThat(response.getDestination()).isEqualTo("Bangkok, Thailand");
        assertThat(response.getTotalDays()).isEqualTo(5);
    }

    @Test
    void 템플릿_목록_조회()
    {
        // given
        createTestTemplate("여행1");
        createTestTemplate("여행2");

        // when
        Page<TemplateResponse> templates = templateService.getTemplates(testUser, PageRequest.of(0, 10));

        // then
        assertThat(templates.getContent()).hasSize(2);
    }

    @Test
    void 템플릿_상세_조회()
    {
        // given
        Template template = createTestTemplate("상세조회 테스트");

        // when
        Object detail = templateService.getTemplateDetail(testUser, template.getId());

        // then
        assertThat(detail).isNotNull();
    }

    @Test
    void 템플릿_수정_성공()
    {
        // given
        Template template = createTestTemplate("원본 제목");
        TemplateCreateRequest updateRequest = new TemplateCreateRequest();
        updateRequest.setTitle("수정된 제목");
        updateRequest.setDestination("New Destination");
        updateRequest.setStartDate(LocalDate.of(2024, 2, 1));
        updateRequest.setEndDate(LocalDate.of(2024, 2, 5));
        updateRequest.setTotalDays(5);

        // when
        TemplateResponse response = templateService.updateTemplate(testUser, template.getId(), updateRequest);

        // then
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getDestination()).isEqualTo("New Destination");
    }

    @Test
    void 템플릿_삭제_성공()
    {
        // given
        Template template = createTestTemplate("삭제 테스트");
        Long templateId = template.getId();

        // when
        templateService.deleteTemplate(testUser, templateId);

        // then
        assertThat(templateRepository.findById(templateId)).isEmpty();
    }

    @Test
    void 다른_유저의_템플릿_접근_실패()
    {
        // given
        Template template = createTestTemplate("테스트");
        User otherUser = userRepository.save(User.builder()
                .email("other@test.com")
                .password("password")
                .name("다른유저")
                .enabled(true)
                .build());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            templateService.getTemplateDetail(otherUser, template.getId());
        });
    }

    @Test
    void 체크리스트_섹션_추가()
    {
        // given
        Template template = createTestTemplate("체크리스트 테스트");
        ChecklistSectionRequest request = new ChecklistSectionRequest();
        request.setTitle("준비물");
        request.setIcon("📦");
        request.setOrderIndex(0);

        ChecklistSectionRequest.ChecklistItemDto item1 = new ChecklistSectionRequest.ChecklistItemDto();
        item1.setLabel("여권");
        item1.setOrderIndex(0);

        ChecklistSectionRequest.ChecklistItemDto item2 = new ChecklistSectionRequest.ChecklistItemDto();
        item2.setLabel("항공권");
        item2.setOrderIndex(1);

        request.setItems(List.of(item1, item2));

        // when
        Object result = templateService.addChecklistSection(testUser, template.getId(), request);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void 일정_추가()
    {
        // given
        Template template = createTestTemplate("일정 테스트");
        DayScheduleRequest request = new DayScheduleRequest();
        request.setDayNumber(1);
        request.setDate(LocalDate.of(2024, 1, 1));
        request.setTitle("첫째 날");
        request.setColor("#4f46e5");

        // when
        Object result = templateService.addDaySchedule(testUser, template.getId(), request);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void 활동_추가()
    {
        // given
        Template template = createTestTemplate("활동 테스트");
        DayScheduleRequest dayRequest = new DayScheduleRequest();
        dayRequest.setDayNumber(1);
        dayRequest.setDate(LocalDate.of(2024, 1, 1));
        dayRequest.setTitle("첫째 날");
        dayRequest.setColor("#4f46e5");

        Object dayResult = templateService.addDaySchedule(testUser, template.getId(), dayRequest);
        Long dayId = extractIdFromAnonymousObject(dayResult);

        Location location = createTestLocation();

        ActivityRequest activityRequest = new ActivityRequest();
        activityRequest.setTime("09:00");
        activityRequest.setDescription("관광지 방문");
        activityRequest.setLocationId(location.getId());
        activityRequest.setOrderIndex(0);

        // when
        Object result = templateService.addActivity(testUser, template.getId(), dayId, activityRequest);

        // then
        assertThat(result).isNotNull();
    }

    private Template createTestTemplate(String title)
    {
        TemplateCreateRequest request = new TemplateCreateRequest();
        request.setTitle(title);
        request.setDestination("Test Destination");
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 1, 5));
        request.setTotalDays(5);

        TemplateResponse response = templateService.createTemplate(testUser, request);
        return templateRepository.findById(response.getId()).orElseThrow();
    }

    private Location createTestLocation()
    {
        Location location = Location.builder()
                .user(testUser)
                .name("테스트 장소")
                .category(LocationCategory.ATTRACTION)
                .latitude(13.7563)
                .longitude(100.5018)
                .address("Bangkok, Thailand")
                .description("테스트용 장소")
                .isPublic(false)
                .build();
        return locationRepository.save(location);
    }

    private Long extractIdFromAnonymousObject(Object obj)
    {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField("id");
            field.setAccessible(true);
            return (Long) field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("ID 추출 실패", e);
        }
    }
}

