# Сервис Аутентификации и Авторизации (auth-service)

Этот проект представляет собой микросервис на Spring Boot, отвечающий за аутентификацию и авторизацию пользователей с использованием JSON Web Tokens (JWT).

## Назначение

-   **Управление пользователями:** Регистрация новых пользователей и управление их данными (имя, email, пароль).
-   **Управление ролями:** Присвоение ролей пользователям (например, USER, ADMIN).
-   **Аутентификация:** Проверка учетных данных пользователя при входе и выдача JWT-токена.
-   **Авторизация:** Проверка JWT-токена в заголовках запросов для защиты эндпоинтов.
-   **Интеграция:** Потенциальное взаимодействие с другими микросервисами для проверки прав доступа или передачи информации о пользователе.

## Структура Проекта

```
auth-service/
└── src/
    └── main/
        └── java/
            └── com/
                └── tugas_integra/
                    └── uas/
                        ├── client/           # Классы для взаимодействия с другими сервисами
                        │   └── ServiceController.java
                        ├── controller/       # Обработка входящих HTTP-запросов (API Endpoints)
                        │   ├── AuthController.java
                        │   ├── LoginRequest.java
                        │   └── SignupRequest.java
                        ├── jwt/              # Логика работы с JWT
                        │   ├── AuthEntryPointJwt.java
                        │   ├── AuthTokenFilter.java
                        │   ├── JwtResponse.java
                        │   ├── JwtUtils.java
                        │   └── MessageResponse.java
                        ├── model/            # Модели данных (JPA Entities)
                        │   ├── ERole.java
                        │   ├── Role.java
                        │   └── User.java
                        ├── request/          # DTO для запросов к другим доменам (интеграция)
                        │   ├── distribusi/
                        │   ├── gudang/
                        │   ├── karyawan/
                        │   ├── mitra/
                        │   └── product/
                        └── security/         # Конфигурация Spring Security
                            └── SecurityConfig.java
```

### Описание Папок

-   **`client`**: Содержит классы для взаимодействия с другими микросервисами (например, через `RestTemplate` или `Feign`). `ServiceController` может быть фасадом для этих взаимодействий.
-   **`controller`**: Обрабатывает HTTP-запросы. `AuthController` отвечает за эндпоинты `/api/auth/signup` и `/api/auth/signin`. `LoginRequest` и `SignupRequest` — это DTO для данных входа и регистрации.
-   **`jwt`**: Ядро функциональности JWT. `JwtUtils` генерирует и проверяет токены. `AuthTokenFilter` перехватывает запросы, проверяет токен и аутентифицирует пользователя. `AuthEntryPointJwt` обрабатывает ошибки доступа (401 Unauthorized). `JwtResponse` и `MessageResponse` — DTO для ответов.
-   **`model`**: Определяет сущности базы данных (`User`, `Role`) с помощью JPA. `ERole` — перечисление для стандартных ролей.
-   **`request`**: Содержит DTO для специфичных запросов, вероятно, используемых при интеграции с другими сервисами (`distribusi`, `gudang`, `karyawan`, `mitra`, `product`).
-   **`security`**: Конфигурация Spring Security (`SecurityConfig`). Здесь настраиваются правила доступа, `PasswordEncoder`, `AuthenticationManager`, и регистрируются компоненты из пакета `jwt`.

## Диаграмма Компонентов (Mermaid)

```mermaid
graph TD
    subgraph "Входящий HTTP Запрос"
        direction LR
        REQ(Запрос на /api/auth/...)
    end

    subgraph "Spring Security"
        direction TB
        FILTER(AuthTokenFilter) -->|Проверка JWT| UTILS(JwtUtils)
        FILTER -->|Успешно| CTX(SecurityContextHolder)
        FILTER -->|Ошибка/Нет JWT| ENTRY_POINT(AuthEntryPointJwt)
        SEC_CONFIG(SecurityConfig) --> FILTER
        SEC_CONFIG --> ENTRY_POINT
        SEC_CONFIG --> AUTH_MANAGER(AuthenticationManager)
        SEC_CONFIG --> PWD_ENCODER(PasswordEncoder)
    end

    subgraph "Контроллеры (controller)"
        direction TB
        AUTH_CTRL(AuthController)
        REQ --> AUTH_CTRL
        AUTH_CTRL --> LOGIN_REQ(LoginRequest)
        AUTH_CTRL --> SIGNUP_REQ(SignupRequest)
    end

    subgraph "Логика JWT (jwt)"
        direction TB
        AUTH_CTRL -- Вход --> UTILS
        UTILS --> JWT_RESPONSE(JwtResponse)
    end

    subgraph "Модели Данных (model)"
        direction TB
        USER(User)
        ROLE(Role)
        EROLE(ERole)
        USER -- ManyToMany --> ROLE
        ROLE -- Enum --> EROLE
    end

    subgraph "Взаимодействие с БД (JPA/Hibernate)"
        direction TB
        REPO(UserRepository/RoleRepository <br> *Не показаны, но подразумеваются*) --> |CRUD| DB[(База Данных)]
        AUTH_CTRL --> REPO
        AUTH_MANAGER --> REPO # (через UserDetailsService)
    end

    subgraph "Клиенты (client)"
         direction TB
         CLIENT_CTRL(ServiceController) --> |HTTP| EXT_SVC(Другой Микросервис)
         AUTH_CTRL --> CLIENT_CTRL # Возможная связь
     end

    AUTH_CTRL -- Использование --> PWD_ENCODER
    AUTH_CTRL -- Использование --> AUTH_MANAGER

    AUTH_CTRL --> MSG_RESP(MessageResponse) # Ответ о регистрации

    UTILS -- Генерация/Валидация --> USER
    FILTER -- Загрузка пользователя --> USER
```

## Как запустить

*(Добавьте сюда инструкции по сборке и запуску проекта, например, с использованием Maven или Gradle)*

```bash
# Пример для Maven
mvn spring-boot:run
```

## Зависимости

*(Добавьте сюда основные зависимости, например, Spring Web, Spring Security, Spring Data JPA, JJWT, база данных)*

-   Spring Boot Starter Web
-   Spring Boot Starter Security
-   Spring Boot Starter Data JPA
-   `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`
-   Драйвер соответствующей БД (например, PostgreSQL, MySQL) 
