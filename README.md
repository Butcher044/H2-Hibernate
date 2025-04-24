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
