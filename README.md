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

## 폴더 구조

```
DataSheed_backEnd/
├── build.gradle
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/datasheet/backend/
│   │   │   ├── BackendApplication.java      # 애플리케이션 진입점
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.java       # Swagger/OpenAPI 설정
│   │   │   ├── auth/                        # 로그인·인증 API
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── LoginAttemptService.java # 로그인 실패/잠금 처리
│   │   │   │   └── dto/
│   │   │   │       ├── LoginRequest.java
│   │   │   │       ├── LoginResponse.java
│   │   │   │       └── UserResponse.java
│   │   │   ├── user/                        # 사용자 도메인
│   │   │   │   ├── User.java                # users 테이블 엔티티
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/                    # 보안·JWT 설정
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtProperties.java
│   │   │   │   ├── AuthProperties.java
│   │   │   │   ├── CustomUserDetails.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   └── common/                      # 공통 예외·응답
│   │   │       ├── ApiErrorResponse.java
│   │   │       ├── BusinessException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.properties       # DB·JWT 설정
│   └── test/
│       └── java/com/datasheet/backend/
│           └── BackendApplicationTests.java
└── README.md
```
# DataSheet_backEnd
