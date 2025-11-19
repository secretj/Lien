package com.lien.service;

import com.lien.dto.request.LocationRequest;
import com.lien.dto.response.LocationResponse;
import com.lien.entity.LocationCategory;
import com.lien.entity.User;
import com.lien.repository.LocationRepository;
import com.lien.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class LocationServiceTest
{

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp()
    {
        locationRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(new User("test@test.com", "password", "테스트유저"));
    }

    @Test
    void 위치_생성_성공()
    {
        // given
        LocationRequest request = new LocationRequest();
        request.setName("왓포");
        request.setCategory(LocationCategory.ATTRACTION);
        request.setLatitude(13.7467);
        request.setLongitude(100.4926);
        request.setAddress("2 Sanam Chai Rd, Bangkok");
        request.setDescription("방콕의 유명한 사원");
        request.setIsPublic(true);

        // when
        LocationResponse response = locationService.createLocation(testUser, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("왓포");
        assertThat(response.getCategory()).isEqualTo(LocationCategory.ATTRACTION);
        assertThat(response.getLatitude()).isEqualTo(13.7467);
        assertThat(response.getIsPublic()).isTrue();
    }

    @Test
    void 위치_목록_조회()
    {
        // given
        createTestLocation("장소1", LocationCategory.ATTRACTION, true);
        createTestLocation("장소2", LocationCategory.RESTAURANT, false);

        // when
        List<LocationResponse> locations = locationService.getLocations(testUser, null, null);

        // then
        assertThat(locations).hasSize(2);
    }

    @Test
    void 위치_카테고리_필터링()
    {
        // given
        createTestLocation("관광지1", LocationCategory.ATTRACTION, true);
        createTestLocation("음식점1", LocationCategory.RESTAURANT, true);
        createTestLocation("호텔1", LocationCategory.HOTEL, true);

        // when
        List<LocationResponse> restaurants = locationService.getLocations(
                testUser, LocationCategory.RESTAURANT, null);

        // then
        assertThat(restaurants).hasSize(1);
        assertThat(restaurants.get(0).getCategory()).isEqualTo(LocationCategory.RESTAURANT);
    }

    @Test
    void 위치_키워드_검색()
    {
        // given
        createTestLocation("왓포 사원", LocationCategory.ATTRACTION, true);
        createTestLocation("왓아룬 사원", LocationCategory.ATTRACTION, true);
        createTestLocation("그랜드 팰리스", LocationCategory.ATTRACTION, true);

        // when
        List<LocationResponse> results = locationService.getLocations(testUser, null, "왓");

        // then
        assertThat(results).hasSize(2);
    }

    @Test
    void 위치_단건_조회_성공()
    {
        // given
        LocationResponse created = createTestLocation("조회 테스트", LocationCategory.ATTRACTION, true);

        // when
        LocationResponse found = locationService.getLocation(testUser, created.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("조회 테스트");
    }

    @Test
    void 비공개_위치_다른_유저_조회_실패()
    {
        // given
        LocationResponse privateLocation = createTestLocation("비공개 장소", LocationCategory.ATTRACTION, false);
        User otherUser = userRepository.save(new User("other@test.com", "password", "다른유저"));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            locationService.getLocation(otherUser, privateLocation.getId());
        });
    }

    @Test
    void 공개_위치_다른_유저_조회_성공()
    {
        // given
        LocationResponse publicLocation = createTestLocation("공개 장소", LocationCategory.ATTRACTION, true);
        User otherUser = userRepository.save(new User("other@test.com", "password", "다른유저"));

        // when
        LocationResponse found = locationService.getLocation(otherUser, publicLocation.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("공개 장소");
    }

    @Test
    void 위치_수정_성공()
    {
        // given
        LocationResponse original = createTestLocation("원본 이름", LocationCategory.ATTRACTION, false);

        LocationRequest updateRequest = new LocationRequest();
        updateRequest.setName("수정된 이름");
        updateRequest.setCategory(LocationCategory.RESTAURANT);
        updateRequest.setLatitude(13.8);
        updateRequest.setLongitude(100.6);
        updateRequest.setAddress("New Address");
        updateRequest.setDescription("수정된 설명");
        updateRequest.setIsPublic(true);

        // when
        LocationResponse updated = locationService.updateLocation(testUser, original.getId(), updateRequest);

        // then
        assertThat(updated.getName()).isEqualTo("수정된 이름");
        assertThat(updated.getCategory()).isEqualTo(LocationCategory.RESTAURANT);
        assertThat(updated.getIsPublic()).isTrue();
    }

    @Test
    void 위치_삭제_성공()
    {
        // given
        LocationResponse location = createTestLocation("삭제 테스트", LocationCategory.ATTRACTION, true);
        Long locationId = location.getId();

        // when
        locationService.deleteLocation(testUser, locationId);

        // then
        assertThat(locationRepository.findById(locationId)).isEmpty();
    }

    @Test
    void 다른_유저의_위치_수정_실패()
    {
        // given
        LocationResponse location = createTestLocation("테스트", LocationCategory.ATTRACTION, false);
        User otherUser = userRepository.save(new User("other@test.com", "password", "다른유저"));

        LocationRequest updateRequest = new LocationRequest();
        updateRequest.setName("수정 시도");
        updateRequest.setCategory(LocationCategory.ATTRACTION);
        updateRequest.setLatitude(13.7);
        updateRequest.setLongitude(100.5);
        updateRequest.setAddress("Address");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            locationService.updateLocation(otherUser, location.getId(), updateRequest);
        });
    }

    private LocationResponse createTestLocation(String name, LocationCategory category, boolean isPublic)
    {
        LocationRequest request = new LocationRequest();
        request.setName(name);
        request.setCategory(category);
        request.setLatitude(13.7563);
        request.setLongitude(100.5018);
        request.setAddress("Bangkok, Thailand");
        request.setDescription("테스트 장소");
        request.setIsPublic(isPublic);

        return locationService.createLocation(testUser, request);
    }
}

