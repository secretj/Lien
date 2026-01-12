package com.lien.entity;

/**
 * ==============================================================================
 * LocationCategory Enum (위치 카테고리 열거형)
 * ==============================================================================
 * 
 * 역할:
 * - 위치의 카테고리를 정의하는 열거형
 * - 타입 안전성 보장 (잘못된 카테고리 입력 방지)
 * - UI 아이콘 및 색상 매핑
 * 
 * 이점:
 * 1. 일관된 카테고리 관리
 * 2. 컴파일 타임 검증
 * 3. 오타 방지
 * 4. IDE 자동완성 지원
 * 
 * 데이터베이스 저장:
 * - @Enumerated(EnumType.STRING)으로 사용
 * - DB에 "ATTRACTION", "HOTEL" 등 문자열로 저장
 * - ORDINAL 사용 금지 (순서 변경 시 데이터 손상 위험)
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 위치 생성 시
 * Location location = Location.builder()
 *     .category(LocationCategory.ATTRACTION)
 *     .build();
 * 
 * // 카테고리별 조회
 * List<Location> attractions = locationRepository
 *     .findByCategory(LocationCategory.ATTRACTION);
 * 
 * // 카테고리 검증
 * if (location.getCategory() == LocationCategory.HOTEL) {
 *     // 호텔 관련 로직
 * }
 * 
 * // Switch 문 (모든 케이스 컴파일 검증)
 * String icon = switch (category) {
 *     case ATTRACTION -> "🏛️";
 *     case HOTEL -> "🏨";
 *     case AIRPORT -> "✈️";
 *     case RESTAURANT -> "🍽️";
 *     case MASSAGE -> "💆";
 *     case SHOPPING -> "🛍️";
 * };
 * }
 * </pre>
 * 
 * 데이터베이스 예시:
 * <pre>
 * locations 테이블:
 * +----+-------------------+------------+
 * | id | name              | category   |
 * +----+-------------------+------------+
 * | 1  | 성산일출봉        | ATTRACTION |
 * | 2  | 제주 신라호텔     | HOTEL      |
 * | 3  | 제주국제공항      | AIRPORT    |
 * | 4  | 올레국수          | RESTAURANT |
 * | 5  | 제주 스파         | MASSAGE    |
 * | 6  | 신라면세점        | SHOPPING   |
 * +----+-------------------+------------+
 * </pre>
 * 
 * UI 매핑 예시:
 * <pre>
 * 카테고리        아이콘    색상      설명
 * ------------------------------------------------
 * ATTRACTION    🏛️      #FF6B6B   관광 명소, 박물관, 공원
 * HOTEL         🏨      #4ECDC4   호텔, 리조트, 펜션, 게스트하우스
 * AIRPORT       ✈️      #95E1D3   공항, 터미널
 * RESTAURANT    🍽️      #FFA07A   식당, 카페, 음식점
 * MASSAGE       💆      #A29BFE   마사지, 스파, 찜질방
 * SHOPPING      🛍️      #FDCB6E   쇼핑몰, 면세점, 시장
 * </pre>
 * 
 * 확장 방법:
 * <pre>
 * {@code
 * // 새 카테고리 추가 (열거형 끝에 추가)
 * public enum LocationCategory {
 *     ATTRACTION,
 *     HOTEL,
 *     AIRPORT,
 *     RESTAURANT,
 *     MASSAGE,
 *     SHOPPING,
 *     ENTERTAINMENT  // 새로 추가 (순서가 바뀌어도 안전)
 * }
 * }
 * </pre>
 * 
 * @see Location
 */
public enum LocationCategory {
    /**
     * 관광지
     * 
     * 포함:
     * - 관광 명소 (성산일출봉, 한라산, 에펠탑)
     * - 박물관, 미술관
     * - 공원, 정원
     * - 역사 유적지
     * - 자연 경관
     * 
     * 예시:
     * - 성산일출봉 (제주)
     * - 해운대 (부산)
     * - 경복궁 (서울)
     * - 도쿄 타워 (일본)
     */
    ATTRACTION,
    
    /**
     * 숙소
     * 
     * 포함:
     * - 호텔
     * - 리조트
     * - 펜션
     * - 게스트하우스
     * - 에어비앤비
     * - 모텔
     * 
     * 예시:
     * - 제주 신라호텔
     * - 부산 파라다이스호텔
     * - 서울 롯데호텔
     */
    HOTEL,
    
    /**
     * 공항
     * 
     * 포함:
     * - 국제공항
     * - 국내공항
     * - 터미널
     * 
     * 예시:
     * - 제주국제공항
     * - 김포공항
     * - 인천국제공항
     * - 나리타 공항 (일본)
     */
    AIRPORT,
    
    /**
     * 음식점
     * 
     * 포함:
     * - 식당 (한식, 중식, 일식, 양식)
     * - 카페
     * - 베이커리
     * - 푸드코트
     * - 술집, 바
     * 
     * 예시:
     * - 올레국수 (제주)
     * - 스타벅스
     * - 미슐랭 레스토랑
     */
    RESTAURANT,
    
    /**
     * 마사지 / 스파
     * 
     * 포함:
     * - 마사지 샵
     * - 스파
     * - 찜질방
     * - 사우나
     * - 온천
     * 
     * 예시:
     * - 제주 스파
     * - 용두암 마사지
     * - 찜질방
     */
    MASSAGE,
    
    /**
     * 쇼핑
     * 
     * 포함:
     * - 쇼핑몰
     * - 백화점
     * - 면세점
     * - 시장 (전통시장, 야시장)
     * - 아울렛
     * 
     * 예시:
     * - 신라면세점
     * - 동대문 시장
     * - 명동 쇼핑거리
     */
    SHOPPING
}