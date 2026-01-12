# ==============================================================================
# Lien Client Dockerfile
# ==============================================================================
#
# 역할:
# - 정적 웹 파일(HTML, CSS, JavaScript)을 Nginx로 서빙
# - 프론트엔드 애플리케이션을 Docker 컨테이너로 배포
# - 개발 및 프로덕션 환경에서 일관된 실행 환경 제공
#
# 이점:
# 1. 경량 이미지: Alpine Linux 기반 (약 5MB)
# 2. 빠른 빌드: 정적 파일 복사만 수행
# 3. 일관성: 모든 환경에서 동일하게 실행
# 4. 간단한 배포: docker-compose로 쉽게 배포
#
# 사용 방법:
# ```bash
# # 이미지 빌드
# docker build -t lien-client .
#
# # 컨테이너 실행
# docker run -d -p 8081:80 lien-client
#
# # 접속
# http://localhost:8081
# ```
#
# Docker Compose:
# ```yaml
# services:
#   client:
#     build:
#       context: ./client
#       dockerfile: Dockerfile
#     ports:
#       - "8081:80"
# ```
#
# @see nginx.conf
# @see compose.yaml

# ==============================================================================
# Base Image: Nginx Alpine
# ==============================================================================
#
# Nginx Alpine 이미지를 사용합니다.
#
# 선택 이유:
# - Alpine Linux: 경량 리눅스 배포판 (약 5MB)
# - Nginx: 고성능 웹 서버 및 리버스 프록시
# - 안정성: Nginx 공식 이미지
#
# 이미지 크기 비교:
# - nginx:alpine: ~23MB
# - nginx:latest: ~141MB
# - nginx:mainline: ~141MB
#
# 대안:
# - nginx:alpine3.18 (특정 Alpine 버전)
# - nginx:1.25-alpine (특정 Nginx 버전)
FROM nginx:alpine

# ==============================================================================
# Copy Static Files
# ==============================================================================
#
# 정적 파일(HTML, CSS, JavaScript)을 Nginx의 기본 웹 루트로 복사합니다.
#
# 복사되는 파일:
# - index.html, register.html, home.html, templates.html
# - css/ 디렉토리 (style.css, templates.css)
# - js/ 디렉토리 (api.js, auth.js, templates-api.js, templates.js)
# - 기타 정적 파일
#
# Nginx 기본 웹 루트: /usr/share/nginx/html/
#
# 접속 시 동작:
# - http://localhost:8081/ → /usr/share/nginx/html/index.html
# - http://localhost:8081/register.html → /usr/share/nginx/html/register.html
# - http://localhost:8081/css/style.css → /usr/share/nginx/html/css/style.css
#
# 제외 파일 (.dockerignore):
# - Dockerfile
# - README.md
# - *.md
COPY . /usr/share/nginx/html/

# ==============================================================================
# Copy Custom Nginx Configuration
# ==============================================================================
#
# 커스텀 Nginx 설정 파일을 컨테이너로 복사합니다.
#
# nginx.conf 설정:
# - SPA (Single Page Application) 라우팅 설정
# - GZIP 압축 활성화
# - 캐시 헤더 설정
# - CORS 헤더 설정 (필요 시)
#
# 기본 설정 파일 경로: /etc/nginx/conf.d/default.conf
#
# Nginx 설정 우선순위:
# 1. /etc/nginx/nginx.conf (메인 설정)
# 2. /etc/nginx/conf.d/*.conf (추가 설정)
#
# 커스텀 설정 예시 (nginx.conf):
# ```nginx
# server {
#     listen 80;
#     server_name localhost;
#     root /usr/share/nginx/html;
#     index index.html;
#
#     # SPA 라우팅
#     location / {
#         try_files $uri $uri/ /index.html;
#     }
#
#     # GZIP 압축
#     gzip on;
#     gzip_types text/plain text/css application/json application/javascript;
# }
# ```
COPY nginx.conf /etc/nginx/conf.d/default.conf

# ==============================================================================
# Expose Port
# ==============================================================================
#
# 컨테이너가 80번 포트를 사용함을 선언합니다.
#
# 포트 매핑:
# - 컨테이너 내부: 80
# - 호스트 포트: 8081 (docker-compose에서 설정)
# - 실제 접속: http://localhost:8081
#
# Docker 실행 예시:
# ```bash
# docker run -p 8081:80 lien-client
# ```
#
# Docker Compose 예시:
# ```yaml
# ports:
#   - "8081:80"  # 호스트:컨테이너
# ```
EXPOSE 80

# ==============================================================================
# Start Nginx
# ==============================================================================
#
# Nginx를 포그라운드 모드로 실행합니다.
#
# nginx -g "daemon off;":
# - -g: 글로벌 설정 지시자
# - daemon off: 백그라운드가 아닌 포그라운드로 실행
#
# 포그라운드 실행 이유:
# - Docker 컨테이너는 PID 1 프로세스가 종료되면 컨테이너도 종료됨
# - daemon off로 Nginx를 포그라운드로 실행하여 컨테이너 유지
#
# Nginx 로그:
# - Access Log: /var/log/nginx/access.log
# - Error Log: /var/log/nginx/error.log
#
# 로그 확인:
# ```bash
# # 컨테이너 로그 확인
# docker logs lien-client
#
# # 실시간 로그 확인
# docker logs -f lien-client
# ```
CMD ["nginx", "-g", "daemon off;"]

