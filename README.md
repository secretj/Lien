# 🔐 Lien

**Spring Boot 기반 JWT 인증 시스템 (풀스택)**

백엔드(Spring Boot + MySQL + Redis) + 프론트엔드(HTML/CSS/JS)

## 보안 설정

이 프로젝트는 민감한 정보를 환경변수로 관리합니다.

### 초기 설정

1. `.env.example` 파일을 복사하여 `.env` 파일 생성:
```bash
cp .env.example .env
```

2. `.env` 파일을 열어 실제 값으로 수정:
```bash
# Database Configuration
DB_HOST=mysql
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_DATABASE=lien_db
MYSQL_USER=your_db_user
MYSQL_PASSWORD=your_secure_db_password

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET_KEY=your_jwt_secret_key_at_least_32_characters_long
JWT_EXPIRATION_MS=3600000

# Spring Application
SPRING_PROFILES_ACTIVE=dev
```

### 운영 환경 주의사항

⚠️ **프로덕션 배포 전 필수 변경 사항:**
- `JWT_SECRET_KEY`: 최소 32자 이상의 강력한 랜덤 문자열로 변경
- `MYSQL_ROOT_PASSWORD`: 강력한 비밀번호로 변경
- `MYSQL_PASSWORD`: 강력한 비밀번호로 변경
- `.env` 파일을 절대 Git에 커밋하지 마세요 (이미 `.gitignore`에 포함됨)

## 🚀 실행 방법

### Docker로 실행 (권장)

```bash
# 전체 시스템 실행 (백엔드 + 프론트엔드)
docker compose up --build -d

# 로그 확인
docker compose logs -f app

# 특정 서비스만 로그 확인
docker compose logs -f client

# 중지
docker compose down

# 볼륨까지 삭제 (데이터베이스 초기화)
docker compose down -v
```

### 로컬 개발 환경

```bash
# 환경변수 설정 (Windows PowerShell)
$env:DB_HOST="localhost"
$env:MYSQL_DATABASE="lien_db"
$env:MYSQL_USER="secretj"
$env:MYSQL_PASSWORD="test!"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:JWT_SECRET_KEY="your-secret-key"
$env:JWT_EXPIRATION_MS="3600000"

# 또는 IntelliJ IDEA Run Configuration에서 환경변수 설정

# Gradle 빌드 및 실행
./gradlew bootRun
```

## 📍 접속 정보

### 프론트엔드
- **웹 클라이언트**: http://localhost:8081
  - 로그인: http://localhost:8081/
  - 회원가입: http://localhost:8081/register.html
  - 메인(인증): http://localhost:8081/home.html

### 백엔드
- **REST API**: http://localhost:8080/api
- **MySQL**: localhost:3306
- **Redis**: localhost:6379

## 🛠️ 기술 스택

### 백엔드
- **Spring Boot** 3.5.3
- **Java** 21
- **MySQL** 8.0
- **Redis** Latest
- **JJWT** 0.12.6 (JWT 인증)
- **Spring Security**
- **JPA/Hibernate**

### 프론트엔드
- **HTML5**
- **CSS3** (Modern Design)
- **JavaScript ES6+**
- **Nginx** Alpine (정적 파일 서빙)

### 인프라
- **Docker** & **Docker Compose**
- **Gradle** 8.5

## 📡 API 엔드포인트

### 인증
- `POST /api/auth/register` - 회원가입
  - Request: `{ email, password, name }`
  - Response: `{ id, email, name }`
  
- `POST /api/auth/login` - 로그인
  - Request: `{ email, password }`
  - Response: `{ token, email, name }`

## 🎨 프론트엔드 기능

### 페이지
1. **로그인 페이지** (`index.html`)
   - 이메일/비밀번호 입력
   - JWT 토큰 발급 및 저장
   - 자동 리다이렉트

2. **회원가입 페이지** (`register.html`)
   - 사용자 정보 입력
   - 이메일 중복 체크
   - 비밀번호 확인 검증

3. **메인 페이지** (`home.html`)
   - JWT 토큰 표시
   - 토큰 디코딩 정보
   - 사용자 정보 표시
   - 로그아웃 기능

### 주요 기능
- ✅ JWT 토큰 자동 관리 (LocalStorage)
- ✅ 토큰 만료 자동 체크
- ✅ 인증 필요 페이지 접근 제어
- ✅ 반응형 디자인
- ✅ 실시간 입력 검증

## 🏗️ 프로젝트 구조

```
Lien/
├── src/                      # 백엔드 소스
│   ├── main/
│   │   ├── java/com/lien/
│   │   │   ├── controller/  # REST 컨트롤러
│   │   │   ├── service/     # 비즈니스 로직
│   │   │   ├── repository/  # 데이터 접근
│   │   │   ├── entity/      # JPA 엔티티
│   │   │   ├── security/    # 보안 설정
│   │   │   └── exception/   # 예외 처리
│   │   └── resources/
│   │       └── application.properties
│   └── test/                # 테스트 코드
│
├── client/                   # 프론트엔드
│   ├── index.html           # 로그인
│   ├── register.html        # 회원가입
│   ├── home.html            # 메인
│   ├── css/
│   │   └── style.css
│   ├── js/
│   │   ├── api.js
│   │   └── auth.js
│   ├── Dockerfile
│   └── nginx.conf
│
├── .env                     # 환경변수 (Git 제외)
├── .env.example             # 환경변수 템플릿
├── compose.yaml             # Docker Compose
├── Dockerfile               # 백엔드 Docker
└── build.gradle             # Gradle 빌드
```

## 🔒 보안 체크리스트

- [x] 민감 정보를 `.env` 파일로 분리
- [x] `.env` 파일을 `.gitignore`에 추가
- [x] `.env.example` 템플릿 제공
- [x] JWT 시크릿 키를 환경변수로 관리 (32자 이상)
- [x] CORS 설정으로 허용된 Origin만 접근
- [x] JJWT 최신 버전 사용 (0.12.6)
- [x] 비밀번호 암호화 (BCrypt)
- [x] JWT 토큰 만료 시간 설정
- [ ] HTTPS 설정 (프로덕션 배포 시)
- [ ] Rate Limiting 추가
- [ ] 로그에서 민감 정보 마스킹

## 📚 추가 문서

- [클라이언트 가이드](CLIENT_GUIDE.md) - 프론트엔드 상세 설명
- [클라이언트 README](client/README.md) - 클라이언트 기술 문서

## 🐛 문제 해결

### 포트 충돌
```bash
# 8080 포트 확인
netstat -ano | findstr :8080

# 8081 포트 확인
netstat -ano | findstr :8081
```

### 컨테이너 재시작
```bash
# 특정 서비스만 재시작
docker compose restart app
docker compose restart client
```

### 로그 확인
```bash
# 실시간 로그
docker compose logs -f

# 최근 100줄
docker compose logs --tail 100
```

## 📝 라이선스

Private

