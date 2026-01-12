/**
 * ==============================================================================
 * 인증 관리 클래스
 * ==============================================================================
 * 
 * 역할:
 * - JWT 토큰 저장 및 관리 (localStorage)
 * - 사용자 정보 저장 및 관리 (localStorage)
 * - 인증 상태 확인
 * - 로그아웃 처리
 * - JWT 토큰 파싱 및 만료 확인
 * 
 * 이점:
 * 1. 중앙화된 인증 로직: 모든 인증 관련 로직을 한 곳에서 관리
 * 2. LocalStorage 추상화: 직접 localStorage 접근 불필요
 * 3. 일관된 키 관리: 토큰과 사용자 정보 키를 상수로 관리
 * 4. 토큰 검증: JWT 파싱 및 만료 시간 확인
 * 
 * 사용 기술:
 * - LocalStorage: 브라우저 로컬 저장소
 * - Base64 Decoding: JWT Payload 파싱
 * - JSON: 사용자 정보 직렬화/역직렬화
 * 
 * 보안 고려사항:
 * - LocalStorage는 XSS 공격에 취약 (HTTPS 사용 권장)
 * - HttpOnly Cookie 사용 고려 (더 안전하지만 CORS 설정 필요)
 * - 민감한 정보는 저장하지 말 것 (비밀번호 등)
 * 
 * 사용 예시:
 * ```javascript
 * // 로그인 성공 시
 * const { token, email, name } = await API.login('user@example.com', 'password123');
 * Auth.saveToken(token);
 * Auth.saveUser({ email, name });
 * 
 * // 인증 상태 확인
 * if (Auth.isAuthenticated()) {
 *     console.log('로그인됨');
 *     const user = Auth.getUser();
 *     console.log('사용자:', user.name);
 * } else {
 *     console.log('로그인 필요');
 *     window.location.href = 'index.html';
 * }
 * 
 * // 로그아웃
 * Auth.logout();  // 토큰 및 사용자 정보 삭제 후 로그인 페이지로 이동
 * ```
 * 
 * LocalStorage 구조:
 * ```
 * jwt_token: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0..."
 * user_info: '{"email":"user@example.com","name":"홍길동"}'
 * ```
 * 
 * @see API
 * @see https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage
 */

/**
 * 인증 관리 클래스
 * 
 * JWT 토큰 및 사용자 정보를 LocalStorage에 저장하고 관리합니다.
 * 정적 메서드로 구현되어 인스턴스 생성 없이 사용 가능합니다.
 */
class Auth {
    /**
     * JWT 토큰 저장 키
     * 
     * LocalStorage에서 JWT 토큰을 저장/조회할 때 사용하는 키입니다.
     * 
     * @constant {string}
     */
    static TOKEN_KEY = 'jwt_token';
    
    /**
     * 사용자 정보 저장 키
     * 
     * LocalStorage에서 사용자 정보를 저장/조회할 때 사용하는 키입니다.
     * 
     * @constant {string}
     */
    static USER_KEY = 'user_info';

    /**
     * JWT 토큰 저장
     * 
     * 로그인 성공 시 받은 JWT 토큰을 LocalStorage에 저장합니다.
     * 
     * 사용 예시:
     * ```javascript
     * const { token } = await API.login('user@example.com', 'password123');
     * Auth.saveToken(token);
     * ```
     * 
     * LocalStorage 저장:
     * ```
     * Key: jwt_token
     * Value: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...
     * ```
     * 
     * @param {string} token - JWT 토큰 문자열
     */
    static saveToken(token) {
        localStorage.setItem(this.TOKEN_KEY, token);
    }

    /**
     * JWT 토큰 조회
     * 
     * LocalStorage에 저장된 JWT 토큰을 가져옵니다.
     * 
     * 사용 예시:
     * ```javascript
     * const token = Auth.getToken();
     * if (token) {
     *     const data = await API.getProtectedData(token);
     * } else {
     *     alert('로그인이 필요합니다.');
     * }
     * ```
     * 
     * @returns {string|null} JWT 토큰 또는 null (없는 경우)
     */
    static getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    /**
     * JWT 토큰 삭제
     * 
     * LocalStorage에서 JWT 토큰을 제거합니다.
     * 로그아웃 시 호출됩니다.
     * 
     * 사용 예시:
     * ```javascript
     * Auth.removeToken();
     * ```
     */
    static removeToken() {
        localStorage.removeItem(this.TOKEN_KEY);
    }

    /**
     * 사용자 정보 저장
     * 
     * 로그인 성공 시 받은 사용자 정보를 LocalStorage에 JSON 형태로 저장합니다.
     * 
     * 사용 예시:
     * ```javascript
     * const { email, name } = await API.login('user@example.com', 'password123');
     * Auth.saveUser({ email, name });
     * ```
     * 
     * LocalStorage 저장:
     * ```
     * Key: user_info
     * Value: '{"email":"user@example.com","name":"홍길동"}'
     * ```
     * 
     * @param {Object} user - 사용자 정보 객체 (email, name 등)
     */
    static saveUser(user) {
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }

    /**
     * 사용자 정보 조회
     * 
     * LocalStorage에 저장된 사용자 정보를 JSON에서 객체로 파싱하여 반환합니다.
     * 
     * 사용 예시:
     * ```javascript
     * const user = Auth.getUser();
     * if (user) {
     *     console.log(`안녕하세요, ${user.name}님!`);
     *     console.log(`이메일: ${user.email}`);
     * } else {
     *     console.log('로그인이 필요합니다.');
     * }
     * ```
     * 
     * @returns {Object|null} 사용자 정보 객체 또는 null (없는 경우)
     */
    static getUser() {
        const user = localStorage.getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    }

    /**
     * 사용자 정보 삭제
     * 
     * LocalStorage에서 사용자 정보를 제거합니다.
     * 로그아웃 시 호출됩니다.
     * 
     * 사용 예시:
     * ```javascript
     * Auth.removeUser();
     * ```
     */
    static removeUser() {
        localStorage.removeItem(this.USER_KEY);
    }

    /**
     * 인증 상태 확인
     * 
     * JWT 토큰이 LocalStorage에 존재하는지 확인합니다.
     * 
     * 사용 예시:
     * ```javascript
     * if (Auth.isAuthenticated()) {
     *     // 로그인된 상태
     *     console.log('접근 허용');
     * } else {
     *     // 로그인되지 않은 상태
     *     alert('로그인이 필요합니다.');
     *     window.location.href = 'index.html';
     * }
     * ```
     * 
     * 페이지 로드 시 확인:
     * ```javascript
     * // home.html 페이지 로드 시
     * if (!Auth.isAuthenticated()) {
     *     window.location.href = 'index.html';
     * }
     * ```
     * 
     * @returns {boolean} true: 로그인됨, false: 로그인 안 됨
     */
    static isAuthenticated() {
        return !!this.getToken();  // 토큰이 있으면 true, 없으면 false
    }

    /**
     * 로그아웃
     * 
     * JWT 토큰과 사용자 정보를 LocalStorage에서 제거하고
     * 로그인 페이지로 리다이렉트합니다.
     * 
     * 사용 예시:
     * ```javascript
     * // 로그아웃 버튼 클릭 시
     * document.getElementById('logoutBtn').addEventListener('click', () => {
     *     if (confirm('로그아웃하시겠습니까?')) {
     *         Auth.logout();
     *     }
     * });
     * ```
     * 
     * 동작 과정:
     * 1. JWT 토큰 삭제 (localStorage.removeItem('jwt_token'))
     * 2. 사용자 정보 삭제 (localStorage.removeItem('user_info'))
     * 3. 로그인 페이지로 이동 (index.html)
     */
    static logout() {
        this.removeToken();  // JWT 토큰 삭제
        this.removeUser();   // 사용자 정보 삭제
        window.location.href = 'index.html';  // 로그인 페이지로 이동
    }

    /**
     * 인증 필수 체크
     * 
     * 현재 페이지가 인증이 필요한 페이지인 경우 호출합니다.
     * 인증되지 않은 경우 자동으로 로그인 페이지로 리다이렉트합니다.
     * 
     * 사용 예시:
     * ```javascript
     * // home.html 페이지 로드 시
     * if (!Auth.requireAuth()) {
     *     // 인증 실패 시 자동으로 index.html로 이동
     *     return;
     * }
     * 
     * // 인증 성공 시 이후 로직 실행
     * const user = Auth.getUser();
     * document.getElementById('userName').textContent = user.name;
     * ```
     * 
     * 보호된 페이지 예시:
     * - home.html: 메인 페이지
     * - templates.html: 템플릿 목록
     * - profile.html: 프로필 페이지
     * 
     * @returns {boolean} true: 인증됨, false: 인증 안 됨 (로그인 페이지로 이동)
     */
    static requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = 'index.html';  // 로그인 페이지로 강제 이동
            return false;
        }
        return true;
    }

    /**
     * JWT 토큰 파싱
     * 
     * JWT 토큰의 Payload 부분을 Base64 디코딩하여 JSON 객체로 변환합니다.
     * 
     * JWT 구조:
     * ```
     * eyJhbGciOiJIUzI1NiJ9          ← Header (알고리즘)
     * .eyJzdWIiOiJ1c2VyQGV4YW1...   ← Payload (사용자 정보, 만료 시간 등)
     * .SflKxwRJSMeKKF2QT4fwpM...   ← Signature (서명)
     * ```
     * 
     * Payload 예시:
     * ```json
     * {
     *   "sub": "user@example.com",  ← 사용자 이메일
     *   "iat": 1705324800,           ← 발급 시간 (Issued At)
     *   "exp": 1705328400            ← 만료 시간 (Expiration)
     * }
     * ```
     * 
     * 사용 예시:
     * ```javascript
     * const token = Auth.getToken();
     * const payload = Auth.parseJWT(token);
     * 
     * if (payload) {
     *     console.log('사용자 이메일:', payload.sub);
     *     console.log('발급 시간:', new Date(payload.iat * 1000));
     *     console.log('만료 시간:', new Date(payload.exp * 1000));
     * }
     * ```
     * 
     * 파싱 과정:
     * 1. JWT를 '.'으로 분리하여 Payload 부분 추출 (token.split('.')[1])
     * 2. Base64 URL 디코딩 (- → +, _ → /)
     * 3. atob()로 Base64 디코딩
     * 4. UTF-8 디코딩 (decodeURIComponent + percent encoding)
     * 5. JSON.parse()로 객체 변환
     * 
     * @param {string} token - JWT 토큰 문자열
     * @returns {Object|null} JWT Payload 객체 또는 null (파싱 실패 시)
     */
    static parseJWT(token) {
        try {
            // 1. JWT에서 Payload 부분 추출 (두 번째 부분)
            const base64Url = token.split('.')[1];
            
            // 2. Base64 URL → Base64 변환
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            
            // 3. Base64 디코딩 후 UTF-8 디코딩
            const jsonPayload = decodeURIComponent(
                atob(base64).split('').map(c => {
                    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join('')
            );
            
            // 4. JSON 파싱
            return JSON.parse(jsonPayload);
        } catch (e) {
            // 파싱 실패 시 null 반환 (잘못된 토큰 형식)
            return null;
        }
    }

    /**
     * JWT 토큰 만료 확인
     * 
     * JWT 토큰의 만료 시간(exp)을 확인하여 토큰이 만료되었는지 검사합니다.
     * 
     * 사용 예시:
     * ```javascript
     * const token = Auth.getToken();
     * if (!token) {
     *     alert('로그인이 필요합니다.');
     *     window.location.href = 'index.html';
     * } else if (Auth.isTokenExpired(token)) {
     *     alert('토큰이 만료되었습니다. 다시 로그인해주세요.');
     *     Auth.logout();
     * } else {
     *     // 토큰이 유효함
     *     console.log('토큰 유효');
     * }
     * ```
     * 
     * 자동 만료 체크:
     * ```javascript
     * // 페이지 로드 시 토큰 만료 체크
     * setInterval(() => {
     *     const token = Auth.getToken();
     *     if (token && Auth.isTokenExpired(token)) {
     *         alert('세션이 만료되었습니다. 다시 로그인해주세요.');
     *         Auth.logout();
     *     }
     * }, 60000);  // 1분마다 체크
     * ```
     * 
     * 만료 시간 계산:
     * - exp는 Unix Timestamp (초 단위)
     * - JavaScript Date.now()는 밀리초 단위
     * - exp * 1000으로 밀리초 변환 후 비교
     * 
     * @param {string} token - JWT 토큰 문자열
     * @returns {boolean} true: 만료됨, false: 유효함
     */
    static isTokenExpired(token) {
        // 1. JWT Payload 파싱
        const payload = this.parseJWT(token);
        
        // 2. Payload가 없거나 exp가 없으면 만료된 것으로 간주
        if (!payload || !payload.exp) return true;
        
        // 3. 현재 시간과 만료 시간 비교
        // Date.now(): 현재 시간 (밀리초)
        // payload.exp * 1000: 만료 시간 (밀리초)
        return Date.now() >= payload.exp * 1000;
    }
}

