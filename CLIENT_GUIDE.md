# 🎨 Lien 클라이언트 가이드

## 📋 개요

순수 HTML/CSS/JavaScript로 제작된 현대적이고 간단한 인증 시스템 클라이언트입니다.

## 🚀 빠른 시작

### Docker로 실행

```bash
# 전체 시스템 실행 (백엔드 + 프론트엔드)
docker compose up -d

# 상태 확인
docker compose ps
```

### 접속 정보

| 서비스 | URL | 설명 |
|--------|-----|------|
| 🎨 클라이언트 | http://localhost:8081 | 웹 UI |
| 🔧 백엔드 API | http://localhost:8080/api | REST API |

## 📱 페이지 구성

### 1️⃣ 로그인 페이지 (index.html)

**URL**: http://localhost:8081/

**기능**:
- 이메일/비밀번호 입력
- JWT 토큰 발급 및 저장
- 자동 리다이렉트 (이미 로그인된 경우)

**사용 방법**:
```
1. 이메일 입력: your@email.com
2. 비밀번호 입력: ••••••••
3. [로그인] 버튼 클릭
4. 성공 시 홈 페이지로 자동 이동
```

---

### 2️⃣ 회원가입 페이지 (register.html)

**URL**: http://localhost:8081/register.html

**기능**:
- 새 계정 생성
- 이메일 중복 체크
- 비밀번호 확인 검증

**사용 방법**:
```
1. 이름 입력
2. 이메일 입력
3. 비밀번호 입력 (최소 6자)
4. 비밀번호 확인
5. [회원가입] 버튼 클릭
6. 성공 시 로그인 페이지로 이동
```

**유효성 검사**:
- ✅ 이메일 형식 검증
- ✅ 비밀번호 최소 6자
- ✅ 비밀번호 일치 확인
- ✅ 중복 이메일 체크

---

### 3️⃣ 메인 페이지 (home.html)

**URL**: http://localhost:8081/home.html

**기능**:
- JWT 토큰 표시
- 토큰 디코딩 정보 표시
- 사용자 정보 표시
- 토큰 만료 시간 표시
- 로그아웃

**표시 정보**:
```
✓ 사용자 이름
✓ 이메일
✓ 로그인 시간
✓ 토큰 만료 시간
✓ JWT 토큰 (전체)
✓ 디코딩된 Payload
```

**보안 기능**:
- 🔒 미인증 시 자동 로그인 페이지 이동
- ⏰ 토큰 만료 자동 체크 (1분마다)
- 🚪 안전한 로그아웃

---

## 💻 기술 구조

### 파일 구조

```
client/
├── index.html          # 로그인 페이지
├── register.html       # 회원가입 페이지
├── home.html          # 메인 페이지 (인증 필요)
├── css/
│   └── style.css      # 공통 스타일
├── js/
│   ├── api.js         # API 통신
│   └── auth.js        # 인증 관리
├── Dockerfile         # Docker 이미지
└── nginx.conf         # Nginx 설정
```

### 핵심 모듈

#### 🔌 API 모듈 (api.js)

```javascript
// 회원가입
API.register(email, password, name)

// 로그인
API.login(email, password)
```

#### 🔐 인증 모듈 (auth.js)

```javascript
// 토큰 저장/조회/삭제
Auth.saveToken(token)
Auth.getToken()
Auth.removeToken()

// 사용자 정보 관리
Auth.saveUser(user)
Auth.getUser()

// 인증 체크
Auth.isAuthenticated()
Auth.requireAuth()

// 토큰 분석
Auth.parseJWT(token)
Auth.isTokenExpired(token)

// 로그아웃
Auth.logout()
```

---

## 🎯 사용 시나리오

### 시나리오 1: 신규 사용자 등록

```
1. http://localhost:8081 접속
2. "회원가입" 링크 클릭
3. 정보 입력 후 회원가입
4. 로그인 페이지로 자동 이동
5. 로그인
6. 메인 페이지에서 JWT 토큰 확인
```

### 시나리오 2: 기존 사용자 로그인

```
1. http://localhost:8081 접속
2. 이메일/비밀번호 입력
3. 로그인
4. 메인 페이지에서 사용자 정보 확인
```

### 시나리오 3: 토큰 만료 처리

```
1. 로그인 후 1시간 대기 (토큰 만료)
2. 페이지 새로고침 또는 1분 경과
3. "토큰이 만료되었습니다" 알림
4. 자동으로 로그인 페이지 이동
```

---

## 🎨 UI/UX 특징

### 디자인
- 🌈 모던한 그라디언트 배경
- 🎴 카드형 레이아웃
- 📱 반응형 디자인 (모바일 지원)
- ✨ 부드러운 애니메이션

### 사용자 경험
- ⚡ 즉각적인 피드백
- 🎯 명확한 에러 메시지
- 🔄 자동 리다이렉트
- 💾 자동 로그인 유지 (localStorage)

### 색상 테마

```css
Primary:   #4f46e5 (Indigo)
Success:   #10b981 (Green)
Error:     #ef4444 (Red)
Background: Linear Gradient (Purple to Blue)
```

---

## 🔧 커스터마이징

### API URL 변경

`js/api.js` 파일 수정:

```javascript
const API_BASE_URL = 'https://your-api-domain.com/api';
```

### 스타일 변경

`css/style.css` 파일의 CSS 변수 수정:

```css
:root {
    --primary-color: #your-color;
    --primary-hover: #your-hover-color;
}
```

### 토큰 만료 시간 조정

`home.html` 파일의 체크 주기 변경:

```javascript
// 1분마다 → 30초마다
setInterval(() => {
    // ...
}, 30000); // 30초
```

---

## 🛡️ 보안 고려사항

### 구현된 보안 기능

✅ **CORS 설정**: 백엔드에서 허용된 Origin만 접근  
✅ **JWT 토큰**: 안전한 인증 방식  
✅ **LocalStorage**: 클라이언트 사이드 토큰 저장  
✅ **자동 만료**: 토큰 만료 시 자동 로그아웃  
✅ **입력 검증**: 클라이언트/서버 양측 검증  

### 프로덕션 권장사항

⚠️ **HTTPS 필수**: 프로덕션에서는 반드시 HTTPS 사용  
⚠️ **HttpOnly Cookie**: LocalStorage 대신 HttpOnly Cookie 고려  
⚠️ **XSS 방지**: 사용자 입력 Sanitization  
⚠️ **CSRF 보호**: CSRF 토큰 구현  
⚠️ **Rate Limiting**: API 호출 제한  

---

## 🧪 테스트 시나리오

### 1. 정상 플로우 테스트

```bash
✓ 회원가입 → 로그인 → 메인 페이지 접근
✓ JWT 토큰 정상 발급 확인
✓ 사용자 정보 정상 표시 확인
```

### 2. 에러 처리 테스트

```bash
✓ 잘못된 비밀번호로 로그인 시도
✓ 중복 이메일로 회원가입 시도
✓ 비밀번호 불일치 체크
✓ 미인증 상태로 메인 페이지 접근
```

### 3. 토큰 관리 테스트

```bash
✓ 로그인 후 새로고침 시 로그인 유지
✓ 토큰 만료 후 자동 로그아웃
✓ 로그아웃 후 토큰 삭제 확인
```

---

## 📊 성능

- **초기 로딩**: < 1초
- **API 응답**: < 200ms (로컬)
- **페이지 전환**: 즉시 (SPA 스타일)
- **번들 크기**: < 30KB (압축 전)

---

## 🐛 문제 해결

### 로그인이 안 될 때

```bash
1. 백엔드 API 상태 확인
   docker compose logs app

2. CORS 설정 확인
   브라우저 개발자 도구 → Console

3. 네트워크 요청 확인
   개발자 도구 → Network 탭
```

### JWT 토큰이 표시 안 될 때

```bash
1. LocalStorage 확인
   개발자 도구 → Application → Local Storage

2. 토큰 만료 확인
   jwt.io 사이트에서 토큰 디코딩

3. 브라우저 캐시 삭제
   Ctrl+Shift+Delete
```

### 페이지가 안 열릴 때

```bash
1. 클라이언트 컨테이너 상태 확인
   docker compose ps

2. Nginx 로그 확인
   docker compose logs client

3. 포트 충돌 확인
   netstat -ano | findstr :8081
```

---

## 📚 참고 자료

- [JWT 소개](https://jwt.io/introduction)
- [Fetch API 문서](https://developer.mozilla.org/ko/docs/Web/API/Fetch_API)
- [LocalStorage API](https://developer.mozilla.org/ko/docs/Web/API/Window/localStorage)

---

## 🎉 완료!

클라이언트가 성공적으로 배포되었습니다.

**브라우저에서 접속하세요**: http://localhost:8081

즐거운 개발 되세요! 🚀

