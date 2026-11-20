# DataSheet Backend

제품검사 데이터시트 전자입력 시스템 백엔드

## 기술 스택

| 구분        | 기술                           | 버전   |
| ----------- | ------------------------------ | ------ |
| Language    | Java                           | 17     |
| Framework   | Spring Boot                    | 4.1.2  |
| Build       | Gradle                         | 9.x    |
| Security    | Spring Security + JWT (JJWT)   | 0.13.0 |
| Persistence | Spring Data JPA / Hibernate    | -      |
| Database    | Microsoft SQL Server           | -      |
| Validation  | Spring Validation              | -      |
| Utility     | Lombok                         | -      |
| API Docs    | springdoc-openapi (Swagger UI) | 3.2.0  |

### 주요 의존성

- `spring-boot-starter-webmvc` — REST API
- `spring-boot-starter-security` — 인증/인가
- `spring-boot-starter-data-jpa` — ORM
- `spring-boot-starter-validation` — 요청 검증
- `jjwt-api` / `jjwt-impl` / `jjwt-jackson` — JWT 발급·검증
- `mssql-jdbc` — SQL Server 드라이버
- `springdoc-openapi-starter-webmvc-ui` — Swagger UI

### Swagger

- UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### 인증 API

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/auth/login` | 로그인 — Access(30분)·Refresh(12시간) HttpOnly 쿠키 발급 |
| POST | `/api/auth/refresh` | Refresh로 Access 재발급 (Refresh 회전) |
| POST | `/api/auth/logout` | Refresh 폐기 + 쿠키 삭제 |
| GET | `/api/auth/me` | 내 정보 조회 |

- Access Token 쿠키: `ACCESS_TOKEN`
- Refresh Token 쿠키: `REFRESH_TOKEN` (DB에 해시 저장, 로그아웃 시 폐기)
- 프론트: Access 만료/401 시 `/api/auth/refresh` 호출, 30분 미사용 시 `/api/auth/logout`

## 폴더 구조

```
DataSheed_backEnd/
├── build.gradle
├── settings.gradle
├── sql/
│   └── create_refresh_tokens.sql            # refresh_tokens 테이블 DDL
├── src/
│   ├── main/
│   │   ├── java/com/datasheet/backend/
│   │   │   ├── BackendApplication.java
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── auth/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── LoginAttemptService.java
│   │   │   │   ├── RefreshToken.java
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   ├── RefreshTokenService.java
│   │   │   │   └── dto/
│   │   │   ├── user/
│   │   │   ├── security/
│   │   │   │   ├── AuthCookieService.java
│   │   │   │   └── ...
│   │   │   └── common/
│   │   └── resources/
│   │       ├── application.properties.example
│   │       └── application.properties       # gitignore
│   └── test/
└── README.md
```
