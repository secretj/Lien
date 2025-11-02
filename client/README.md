# Lien Client

순수 HTML/CSS/JavaScript로 만든 간단하고 현대적인 인증 클라이언트

## 기능

- 🔐 로그인
- ✨ 회원가입
- 🏠 인증된 사용자 홈 페이지
- 🔑 JWT 토큰 관리 및 표시
- 🚪 로그아웃
- ⏰ 토큰 만료 체크

## 페이지 구조

- `index.html` - 로그인 페이지
- `register.html` - 회원가입 페이지
- `home.html` - 메인 페이지 (인증 필요)

## 로컬 실행 방법

### 방법 1: Docker로 실행 (권장)

```bash
# 루트 디렉토리에서
docker compose up client -d
```

접속: http://localhost:8081

### 방법 2: 로컬 서버로 실행

Python 서버:
```bash
cd client
python -m http.server 8081
```

Node.js 서버:
```bash
cd client
npx serve -p 8081
```

PHP 서버:
```bash
cd client
php -S localhost:8081
```

VS Code Live Server:
- VS Code에서 `index.html` 우클릭 → "Open with Live Server"

## 주요 기능

### 인증 흐름
1. 사용자가 로그인 폼에 이메일/비밀번호 입력
2. API를 통해 백엔드로 인증 요청
3. 성공 시 JWT 토큰과 사용자 정보 수신
4. `localStorage`에 토큰과 사용자 정보 저장
5. 메인 페이지로 리다이렉트

### JWT 토큰 관리
- **저장**: `localStorage`에 토큰 저장
- **검증**: 페이지 로드 시 토큰 존재 여부 체크
- **만료 체크**: 토큰의 `exp` 클레임 확인
- **자동 로그아웃**: 토큰 만료 시 자동 로그인 페이지 이동

### 보안 기능
- XSS 방지를 위한 입력값 검증
- HTTPS 사용 권장 (프로덕션)
- 토큰 만료 시간 자동 체크
- 인증 필요 페이지 접근 제어

## API 엔드포인트

```javascript
// 회원가입
POST /api/auth/register
Body: { email, password, name }

// 로그인
POST /api/auth/login
Body: { email, password }
Response: { token, email, name }
```

## 파일 구조

```
client/
├── index.html          # 로그인 페이지
├── register.html       # 회원가입 페이지
├── home.html          # 메인 페이지
├── css/
│   └── style.css      # 공통 스타일
├── js/
│   ├── api.js         # API 통신 로직
│   └── auth.js        # 인증 관리 로직
├── Dockerfile         # Docker 이미지
└── nginx.conf         # Nginx 설정
```

## 기술 스택

- **HTML5**: 시맨틱 마크업
- **CSS3**: 모던 스타일링 (Flexbox, Grid)
- **JavaScript ES6+**: 모듈화된 코드
- **Nginx**: 정적 파일 서빙 (Docker)
- **LocalStorage**: 클라이언트 사이드 저장소

## 브라우저 지원

- Chrome/Edge (최신 버전)
- Firefox (최신 버전)
- Safari (최신 버전)

## 개발 팁

### API URL 변경
`js/api.js` 파일에서 `API_BASE_URL` 수정:

```javascript
const API_BASE_URL = 'http://your-api-url:8080/api';
```

### 스타일 커스터마이징
`css/style.css` 파일의 CSS 변수 수정:

```css
:root {
    --primary-color: #4f46e5;
    --primary-hover: #4338ca;
    /* ... */
}
```

## 프로덕션 배포

1. **HTTPS 활성화**: SSL/TLS 인증서 설정
2. **API URL 업데이트**: 프로덕션 백엔드 URL로 변경
3. **환경변수 사용**: API URL을 환경변수로 관리
4. **CORS 설정**: 프로덕션 도메인 추가

## 라이선스

Private

