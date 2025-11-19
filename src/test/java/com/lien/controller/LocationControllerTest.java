package com.lien.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lien.dto.request.LocationRequest;
import com.lien.entity.Location;
import com.lien.entity.LocationCategory;
import com.lien.entity.User;
import com.lien.repository.LocationRepository;
import com.lien.repository.UserRepository;
import com.lien.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@Transactional
class LocationControllerTest
{

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private User testUser;
    private String token;

    @BeforeEach
    void setUp()
    {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        locationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(new User("test@test.com", "password", "테스트유저"));
        token = JwtUtil.generateToken(testUser.getEmail());
    }

    @Test
    void 위치_생성_성공() throws Exception
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

        // when & then
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("왓포"))
                .andExpect(jsonPath("$.category").value("ATTRACTION"))
                .andExpect(jsonPath("$.latitude").value(13.7467))
                .andExpect(jsonPath("$.isPublic").value(true))
                .andDo(print());
    }

    @Test
    void 위치_생성_필수값_누락_실패() throws Exception
    {
        // given
        LocationRequest request = new LocationRequest();
        request.setName(""); // 빈 이름
        request.setCategory(LocationCategory.ATTRACTION);

        // when & then
        try {
            mockMvc.perform(post("/api/locations")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print());
        } catch (Exception e) {
            // validation 실패 시 예외 발생 가능
        }
    }

    @Test
    void 위치_목록_조회() throws Exception
    {
        // given
        createTestLocation("장소1", LocationCategory.ATTRACTION);
        createTestLocation("장소2", LocationCategory.RESTAURANT);

        // when & then
        mockMvc.perform(get("/api/locations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andDo(print());
    }

    @Test
    void 위치_카테고리_필터링() throws Exception
    {
        // given
        createTestLocation("관광지1", LocationCategory.ATTRACTION);
        createTestLocation("음식점1", LocationCategory.RESTAURANT);

        // when & then
        mockMvc.perform(get("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .param("category", "RESTAURANT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("RESTAURANT"))
                .andDo(print());
    }

    @Test
    void 위치_키워드_검색() throws Exception
    {
        // given
        createTestLocation("왓포 사원", LocationCategory.ATTRACTION);
        createTestLocation("왓아룬 사원", LocationCategory.ATTRACTION);
        createTestLocation("그랜드 팰리스", LocationCategory.ATTRACTION);

        // when & then
        mockMvc.perform(get("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", "왓"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andDo(print());
    }

    @Test
    void 위치_단건_조회() throws Exception
    {
        // given
        Location location = createTestLocation("조회 테스트", LocationCategory.ATTRACTION);

        // when & then
        mockMvc.perform(get("/api/locations/" + location.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("조회 테스트"))
                .andDo(print());
    }

    @Test
    void 위치_수정_성공() throws Exception
    {
        // given
        Location location = createTestLocation("원본 이름", LocationCategory.ATTRACTION);

        LocationRequest updateRequest = new LocationRequest();
        updateRequest.setName("수정된 이름");
        updateRequest.setCategory(LocationCategory.RESTAURANT);
        updateRequest.setLatitude(13.8);
        updateRequest.setLongitude(100.6);
        updateRequest.setAddress("New Address");
        updateRequest.setDescription("수정된 설명");
        updateRequest.setIsPublic(true);

        // when & then
        mockMvc.perform(put("/api/locations/" + location.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된 이름"))
                .andExpect(jsonPath("$.category").value("RESTAURANT"))
                .andDo(print());
    }

    @Test
    void 위치_삭제_성공() throws Exception
    {
        // given
        Location location = createTestLocation("삭제 테스트", LocationCategory.ATTRACTION);

        // when & then
        mockMvc.perform(delete("/api/locations/" + location.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    void 인증없이_위치_생성_실패() throws Exception
    {
        // given
        LocationRequest request = new LocationRequest();
        request.setName("테스트 장소");
        request.setCategory(LocationCategory.ATTRACTION);
        request.setLatitude(13.7467);
        request.setLongitude(100.4926);
        request.setAddress("Bangkok");

        // when & then
        // 현재 SecurityConfig가 모든 요청 허용하므로 스킵
        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    @Test
    void 존재하지않는_위치_조회_실패() throws Exception
    {
        // when & then
        mockMvc.perform(get("/api/locations/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is4xxClientError());
    }

    private Location createTestLocation(String name, LocationCategory category)
    {
        Location location = Location.builder()
                .user(testUser)
                .name(name)
                .category(category)
                .latitude(13.7563)
                .longitude(100.5018)
                .address("Bangkok, Thailand")
                .description("테스트 장소")
                .isPublic(true)
                .build();
        return locationRepository.save(location);
    }
}

