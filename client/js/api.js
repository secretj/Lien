/**
 * ==============================================================================
 * API 통신 클래스
 * ==============================================================================
 * 
 * 역할:
 * - 백엔드 API와의 HTTP 통신 담당
 * - 공통 요청 로직 관리 (에러 처리, 헤더 설정 등)
 * - RESTful API 호출을 위한 유틸리티 메서드 제공
 * 
 * 이점:
 * 1. 중복 코드 제거: 모든 API 호출에 공통 로직 적용
 * 2. 일관된 에러 처리: 통합된 에러 핸들링
 * 3. 유지보수 용이성: API URL 변경 시 한 곳만 수정
 * 4. 타입 안정성: 메서드별로 명확한 파라미터 정의
 * 
 * 사용 기술:
 * - Fetch API: 모던 브라우저 네이티브 HTTP 클라이언트
 * - Promise/Async-Await: 비동기 처리
 * - ES6 Class: 정적 메서드로 유틸리티 구현
 * 
 * 사용 예시:
 * ```javascript
 * // 회원가입
 * try {
 *     const result = await API.register('user@example.com', 'password123', '홍길동');
 *     console.log('회원가입 성공:', result);
 * } catch (error) {
 *     console.error('회원가입 실패:', error.message);
 * }
 * 
 * // 로그인
 * try {
 *     const { token, email, name } = await API.login('user@example.com', 'password123');
 *     Auth.saveToken(token);
 *     Auth.saveUser({ email, name });
 *     window.location.href = 'home.html';
 * } catch (error) {
 *     alert('로그인 실패: ' + error.message);
 * }
 * ```
 * 
 * 프로덕션 배포:
 * - API_BASE_URL을 환경 변수로 관리
 * - HTTPS 사용 필수
 * - CORS 설정 확인
 * 
 * @see Auth
 * @see https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API
 */

/**
 * 백엔드 API Base URL
 * 
 * 개발 환경: http://localhost:8080/api
 * 프로덕션: https://api.yourdomain.com/api
 * 
 * 환경별 설정:
 * - 개발: localhost:8080
 * - 스테이징: staging-api.yourdomain.com
 * - 프로덕션: api.yourdomain.com
 * 
 * 환경 변수 사용 예시:
 * ```javascript
 * const API_BASE_URL = process.env.API_URL || 'http://localhost:8080/api';
 * ```
 */
const API_BASE_URL = 'http://localhost:8080/api';

/**
 * API 통신 클래스
 * 
 * 모든 API 호출을 관리하는 유틸리티 클래스입니다.
 * 정적 메서드로 구현되어 인스턴스 생성 없이 사용 가능합니다.
 */
class API {
    /**
     * 공통 API 요청 메서드
     * 
     * 모든 API 호출의 기본이 되는 메서드입니다.
     * Fetch API를 래핑하여 에러 처리 및 공통 설정을 적용합니다.
     * 
     * 기능:
     * 1. Content-Type 헤더 자동 설정 (application/json)
     * 2. 커스텀 헤더 병합 (Authorization 등)
     * 3. HTTP 에러 처리 (4xx, 5xx)
     * 4. JSON 응답 자동 파싱
     * 5. 에러 로깅
     * 
     * 요청 흐름:
     * ```
     * 1. API.request('/auth/login', { method: 'POST', body: '...' })
     * 2. URL 생성: http://localhost:8080/api/auth/login
     * 3. 헤더 설정: Content-Type: application/json
     * 4. Fetch 실행
     * 5. 응답 검증 (response.ok)
     * 6. JSON 파싱
     * 7. 데이터 반환 또는 에러 발생
     * ```
     * 
     * 사용 예시:
     * ```javascript
     * // GET 요청
     * const templates = await API.request('/templates', {
     *     method: 'GET',
     *     headers: {
     *         'Authorization': `Bearer ${token}`
     *     }
     * });
     * 
     * // POST 요청
     * const newTemplate = await API.request('/templates', {
     *     method: 'POST',
     *     headers: {
     *         'Authorization': `Bearer ${token}`
     *     },
     *     body: JSON.stringify({
     *         title: '제주도 여행',
     *         destination: '제주도'
     *     })
     * });
     * ```
     * 
     * 에러 처리:
     * - 네트워크 에러: fetch 자체 실패
     * - HTTP 에러: 4xx, 5xx 응답
     * - JSON 파싱 에러: 응답이 JSON이 아닌 경우
     * 
     * @param {string} endpoint - API 엔드포인트 (예: '/auth/login')
     * @param {Object} options - Fetch 옵션 (method, headers, body 등)
     * @returns {Promise<Object>} JSON 응답 데이터
     * @throws {Error} HTTP 에러 또는 네트워크 에러
     */
    static async request(endpoint, options = {}) {
        // 1. 전체 URL 생성
        const url = `${API_BASE_URL}${endpoint}`;
        
        // 2. 요청 설정 구성
        const config = {
            headers: {
                'Content-Type': 'application/json',  // 기본 헤더
                ...options.headers,  // 커스텀 헤더 병합 (Authorization 등)
            },
            ...options,  // method, body 등
        };

        try {
            // 3. HTTP 요청 실행
            const response = await fetch(url, config);
            
            // 4. JSON 응답 파싱 (실패 시 null 반환)
            const data = await response.json().catch(() => null);

            // 5. HTTP 에러 체크 (4xx, 5xx)
            if (!response.ok) {
                // 백엔드에서 제공하는 에러 메시지 또는 기본 메시지 사용
                throw new Error(data?.message || `HTTP error! status: ${response.status}`);
            }

            // 6. 성공 시 데이터 반환
            return data;
        } catch (error) {
            // 7. 에러 로깅 (개발 환경)
            console.error('API Error:', error);
            
            // 8. 에러 재발생 (호출자에게 전달)
            throw error;
        }
    }

    /**
     * 회원가입 API
     * 
     * 새로운 사용자를 등록합니다.
     * 
     * API:
     * - Method: POST
     * - Endpoint: /api/auth/register
     * - Body: { email, password, name }
     * 
     * 요청 예시:
     * ```javascript
     * try {
     *     const result = await API.register(
     *         'user@example.com',
     *         'password123',
     *         '홍길동'
     *     );
     *     console.log('회원가입 성공:', result);
     *     // { id: 1, email: 'user@example.com', name: '홍길동' }
     * } catch (error) {
     *     console.error('회원가입 실패:', error.message);
     *     // '이미 가입된 이메일입니다.'
     * }
     * ```
     * 
     * 성공 응답:
     * ```json
     * {
     *   "id": 1,
     *   "email": "user@example.com",
     *   "name": "홍길동"
     * }
     * ```
     * 
     * 에러 응답:
     * ```json
     * {
     *   "message": "이미 가입된 이메일입니다."
     * }
     * ```
     * 
     * @param {string} email - 사용자 이메일
     * @param {string} password - 비밀번호 (최소 8자)
     * @param {string} name - 사용자 이름
     * @returns {Promise<Object>} 생성된 사용자 정보 (id, email, name)
     * @throws {Error} 이메일 중복 또는 유효성 검증 실패
     */
    static async register(email, password, name) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ email, password, name }),
        });
    }

    /**
     * 로그인 API
     * 
     * 이메일과 비밀번호로 로그인하여 JWT 토큰을 받습니다.
     * 
     * API:
     * - Method: POST
     * - Endpoint: /api/auth/login
     * - Body: { email, password }
     * 
     * 요청 예시:
     * ```javascript
     * try {
     *     const result = await API.login('user@example.com', 'password123');
     *     console.log('로그인 성공:', result);
     *     // { token: 'eyJhbGciOiJIUzI1NiJ9...', email: '...', name: '...' }
     *     
     *     // JWT 토큰 및 사용자 정보 저장
     *     Auth.saveToken(result.token);
     *     Auth.saveUser({ email: result.email, name: result.name });
     *     
     *     // 홈 페이지로 이동
     *     window.location.href = 'home.html';
     * } catch (error) {
     *     console.error('로그인 실패:', error.message);
     *     alert('이메일 또는 비밀번호가 올바르지 않습니다.');
     * }
     * ```
     * 
     * 성공 응답:
     * ```json
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...",
     *   "email": "user@example.com",
     *   "name": "홍길동"
     * }
     * ```
     * 
     * 에러 응답:
     * ```json
     * {
     *   "message": "이메일 또는 비밀번호가 올바르지 않습니다."
     * }
     * ```
     * 
     * JWT 토큰 사용:
     * - localStorage에 저장
     * - API 요청 시 Authorization 헤더에 포함
     * - 만료 시간 확인 (기본 1시간)
     * 
     * @param {string} email - 사용자 이메일
     * @param {string} password - 비밀번호
     * @returns {Promise<Object>} JWT 토큰 및 사용자 정보
     * @throws {Error} 인증 실패 (이메일 또는 비밀번호 오류)
     */
    static async login(email, password) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password }),
        });
    }

    /**
     * 인증된 데이터 조회 API
     * 
     * JWT 토큰을 사용하여 보호된 리소스에 접근합니다.
     * 
     * API:
     * - Method: GET
     * - Endpoint: /api/protected
     * - Headers: Authorization: Bearer {token}
     * 
     * 요청 예시:
     * ```javascript
     * const token = Auth.getToken();
     * if (!token) {
     *     alert('로그인이 필요합니다.');
     *     window.location.href = 'index.html';
     *     return;
     * }
     * 
     * try {
     *     const data = await API.getProtectedData(token);
     *     console.log('보호된 데이터:', data);
     * } catch (error) {
     *     console.error('접근 실패:', error.message);
     *     if (error.message.includes('401')) {
     *         // 토큰 만료 또는 유효하지 않음
     *         Auth.logout();
     *     }
     * }
     * ```
     * 
     * 성공 응답:
     * ```json
     * {
     *   "message": "인증된 사용자만 볼 수 있는 데이터",
     *   "userId": 1,
     *   "email": "user@example.com"
     * }
     * ```
     * 
     * 에러 응답:
     * - 401 Unauthorized: 토큰 없음 또는 만료
     * - 403 Forbidden: 권한 없음
     * 
     * @param {string} token - JWT 토큰
     * @returns {Promise<Object>} 보호된 데이터
     * @throws {Error} 인증 실패 또는 권한 없음
     */
    static async getProtectedData(token) {
        return this.request('/protected', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,  // JWT 토큰 포함
            },
        });
    }
}

