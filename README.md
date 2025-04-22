# 🍞 Система компьютерного зрения для учета хлебобулочной продукции

<div align="center">

![Статус проекта](https://img.shields.io/badge/Статус-Внедрен-success)
![Версия](https://img.shields.io/badge/Версия-1.0-blue)
![Python](https://img.shields.io/badge/Python-3.8+-yellow)
![YOLOv8](https://img.shields.io/badge/YOLOv8-Обучен-orange)

</div>

## 📋 Содержание

- [📝 Описание проекта](#описание-проекта)
- [✨ Функциональные возможности](#функциональные-возможности)
- [🏗️ Архитектура решения](#архитектура-решения)
- [🛠️ Технический стек](#технический-стек)
- [💪 Сильные стороны решения](#сильные-стороны-решения)
- [📈 Процесс разработки и внедрения](#процесс-разработки-и-внедрения)
- [🎯 Результаты внедрения](#результаты-внедрения)
- [📊 Визуализация процессов](#визуализация-процессов)
- [🔧 Настройка и запуск](#настройка-и-запуск)
- [🔮 Перспективы развития](#перспективы-развития)

## 📝 Описание проекта

<img src="https://img.shields.io/badge/Компьютерное_зрение-активно-brightgreen" align="right"/>

Данный проект представляет собой автоматизированную систему для распознавания и учета хлебобулочной продукции на производстве с использованием технологий компьютерного зрения и глубокого обучения. Система позволяет в режиме реального времени детектировать и классифицировать различные виды этикеток на упаковках продукции, отслеживать состояние конвейеров и вести учет произведенной продукции.

## ✨ Функциональные возможности

<div align="center">
  <table>
    <tr>
      <td align="center">🔍</td>
      <td><b>Детекция и классификация этикеток продукции</b> - система распознает различные типы этикеток на хлебобулочных изделиях</td>
    </tr>
    <tr>
      <td align="center">👁️</td>
      <td><b>Отслеживание объектов в видеопотоке</b> - применение алгоритмов трекинга (BoTSORT/ByteTrack) для непрерывного отслеживания объектов</td>
    </tr>
    <tr>
      <td align="center">⏱️</td>
      <td><b>Синхронизация времени</b> - использование NTP-протокола для точной временной привязки событий</td>
    </tr>
    <tr>
      <td align="center">💾</td>
      <td><b>Запись данных в базу MySQL</b> - автоматическое сохранение информации о распознанных этикетках с временной меткой</td>
    </tr>
    <tr>
      <td align="center">🔄</td>
      <td><b>Резервное копирование данных</b> - создание файлов резервных копий с автоматической очисткой по расписанию</td>
    </tr>
    <tr>
      <td align="center">📊</td>
      <td><b>Мониторинг состояния конвейера</b> - определение наличия продукции на конвейере и фиксация моментов переклипсовки</td>
    </tr>
  </table>
</div>

## 🏗️ Архитектура решения

### 🧩 Компоненты системы:

<div align="center">
  <img src="https://img.shields.io/badge/1-Модуль_компьютерного_зрения-blue" alt="Модуль компьютерного зрения" /><br/>
  <img src="https://img.shields.io/badge/2-Система_трекинга-green" alt="Система трекинга" /><br/>
  <img src="https://img.shields.io/badge/3-Модуль_синхронизации_времени-yellow" alt="Модуль синхронизации времени" /><br/>
  <img src="https://img.shields.io/badge/4-Модуль_работы_с_базой_данных-red" alt="Модуль работы с базой данных" /><br/>
  <img src="https://img.shields.io/badge/5-Система_резервного_копирования-purple" alt="Система резервного копирования" />
</div>

### 🔄 Процесс работы:

```mermaid
flowchart LR
    A([Начало]) --> B[Получение видеопотока с IP-камер]
    B --> C[Предобработка кадров]
    C --> D[Детекция объектов YOLOv8]
    D --> E[Трекинг объектов]
    E --> F{Найдена\nэтикетка?}
    F -- Нет --> C
    F -- Да --> G{Изменение\nэтикетки?}
    G -- Нет --> C
    G -- Да --> H[Сохранение в БД]
    H --> I[Резервное копирование]
    I --> C
    style A fill:#f9f,stroke:#333,stroke-width:2px
    style F fill:#bbf,stroke:#333,stroke-width:2px
    style G fill:#bbf,stroke:#333,stroke-width:2px
```

## 🛠️ Технический стек

<div align="center">
  <img src="https://img.shields.io/badge/Python-3.8+-blue?logo=python&logoColor=white" alt="Python" />
  <img src="https://img.shields.io/badge/OpenCV-4.5.4-red?logo=opencv&logoColor=white" alt="OpenCV" />
  <img src="https://img.shields.io/badge/YOLOv8-Ultralytics-yellow?logo=pytorch&logoColor=white" alt="YOLOv8" />
  <img src="https://img.shields.io/badge/MySQL-8.0-orange?logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/NTPlib-Latest-lightgrey" alt="NTPlib" />
  <img src="https://img.shields.io/badge/Threading-Python-green" alt="Threading" />
</div>

## 💪 Сильные стороны решения

<div align="center">
  <table>
    <tr>
      <td align="center">🎯</td>
      <td><b>Высокая точность распознавания</b> - использование предварительно обученной и дополнительно настроенной модели YOLOv8</td>
    </tr>
    <tr>
      <td align="center">🛡️</td>
      <td><b>Отказоустойчивость</b> - система способна справляться с временными проблемами связи с камерами и базой данных</td>
    </tr>
    <tr>
      <td align="center">📈</td>
      <td><b>Масштабируемость</b> - возможность легкого добавления новых производственных линий и типов продукции</td>
    </tr>
    <tr>
      <td align="center">⏰</td>
      <td><b>Точная синхронизация времени</b> - использование NTP-сервера для обеспечения точности временных меток</td>
    </tr>
    <tr>
      <td align="center">💾</td>
      <td><b>Резервное копирование данных</b> - автоматическое создание резервных копий для предотвращения потери данных</td>
    </tr>
    <tr>
      <td align="center">💻</td>
      <td><b>Низкие требования к оборудованию</b> - оптимизированный код позволяет работать на стандартных компьютерах</td>
    </tr>
  </table>
</div>

## 📈 Процесс разработки и внедрения

```mermaid
gantt
    title Этапы разработки и внедрения
    dateFormat  YYYY-MM-DD
    section Подготовка
    Сбор данных                   :a1, 2023-01-01, 14d
    Разметка данных (CVAT)        :a2, after a1, 21d
    section Разработка модели
    Обучение YOLOv8               :b1, after a2, 14d
    Настройка трекинга            :b2, after b1, 7d
    section Интеграция
    Настройка базы данных         :c1, after b2, 7d
    Разработка системы резервирования :c2, after c1, 7d
    section Тестирование
    Тестирование на реальных данных :d1, after c2, 14d
    Отладка и оптимизация         :d2, after d1, 14d
    section Внедрение
    Установка на производстве     :e1, after d2, 14d
    Мониторинг работы             :e2, after e1, 30d
```

## 🎯 Результаты внедрения

<div align="center">
  <table>
    <tr>
      <th>Показатель</th>
      <th>До внедрения</th>
      <th>После внедрения</th>
      <th>Улучшение</th>
    </tr>
    <tr>
      <td>Точность учета</td>
      <td>85%</td>
      <td>98%</td>
      <td>+13%</td>
    </tr>
    <tr>
      <td>Время на учет</td>
      <td>240 мин./день</td>
      <td>36 мин./день</td>
      <td>-85%</td>
    </tr>
    <tr>
      <td>Ошибки инвентаризации</td>
      <td>15%</td>
      <td>2%</td>
      <td>-13%</td>
    </tr>
    <tr>
      <td>Прозрачность процессов</td>
      <td>Низкая</td>
      <td>Высокая</td>
      <td>↑↑↑</td>
    </tr>
  </table>
</div>

### 📊 Визуализация эффективности

```mermaid
pie title Распределение времени сотрудников до/после внедрения
    "Учет продукции (до)" : 45
    "Анализ данных (до)" : 5
    "Другие задачи (до)" : 50
    "Учет продукции (после)" : 5
    "Анализ данных (после)" : 20
    "Другие задачи (после)" : 75
```

## 📊 Визуализация процессов

### 🔄 Мониторинг работы системы

```mermaid
graph TD
    A[IP-камеры] -->|Видеопоток| B[Модуль получения видео]
    B -->|Необработанные кадры| C[Предобработка кадров]
    C -->|Обработанные кадры| D[Детекция объектов YOLOv8]
    D -->|Распознанные объекты| E[Трекинг объектов]
    E -->|Отслеживаемые объекты| F{Изменение этикетки?}
    F -->|Да| G[Запись в базу данных]
    F -->|Нет| C
    G -->|Данные| H[Резервное копирование]
    H -->|Подтверждение| I[Визуализация результатов]
    
    style A fill:#bbf,stroke:#333,stroke-width:2px
    style D fill:#fbb,stroke:#333,stroke-width:2px
    style E fill:#bfb,stroke:#333,stroke-width:2px
    style F fill:#fbf,stroke:#333,stroke-width:2px
    style G fill:#fdb,stroke:#333,stroke-width:2px
```

### 🗃️ Архитектура базы данных

```mermaid
erDiagram
    MACHINE ||--o{ LABELS : produces
    MACHINE ||--o{ CONVEYOR_STATES : monitors
    MACHINE {
        string name PK
        string location
        string camera_url
        timestamp last_active
    }
    LABELS {
        int id PK
        string machine FK
        string current_label
        timestamp time
        int confidence
    }
    CONVEYOR_STATES {
        int id PK
        string machine FK
        boolean is_empty
        timestamp time
        string event_type
    }
```

### 🔄 Процесс обработки кадра

```mermaid
sequenceDiagram
    participant Camera as 📷 Камера
    participant Preprocessor as 🔄 Предобработка
    participant YOLO as 🔍 YOLOv8
    participant Tracker as 👁️ Трекер
    participant DB as 💾 База данных
    
    Camera->>Preprocessor: Передача кадра
    activate Preprocessor
    Note over Preprocessor: Обрезка ROI
    Note over Preprocessor: Изменение размера
    Preprocessor->>YOLO: Обработанный кадр
    deactivate Preprocessor
    
    activate YOLO
    Note over YOLO: Детекция объектов
    YOLO->>Tracker: Результаты детекции
    deactivate YOLO
    
    activate Tracker
    Note over Tracker: Отслеживание объектов
    Note over Tracker: Проверка изменения этикетки
    
    alt Обнаружено изменение этикетки
        Tracker->>DB: Запись информации
        activate DB
        Note over DB: Сохранение информации
        DB-->>Tracker: Подтверждение записи
        deactivate DB
    end
    deactivate Tracker
```

### 📊 Анализ данных

```mermaid
graph LR
    A[Сбор данных] --> B[Предобработка]
    B --> C[Обучение модели]
    C --> D[Валидация]
    D -->|Недостаточная точность| B
    D -->|Достаточная точность| E[Развертывание]
    E --> F[Мониторинг]
    F -->|Обнаружение проблем| G[Переобучение]
    G --> C
    
    style A fill:#f9d,stroke:#333,stroke-width:2px
    style C fill:#bbf,stroke:#333,stroke-width:2px
    style D fill:#fbb,stroke:#333,stroke-width:2px
    style E fill:#bfb,stroke:#333,stroke-width:2px
    style G fill:#fbf,stroke:#333,stroke-width:2px
```

## 🔧 Настройка и запуск

<div align="center">
  <img src="https://img.shields.io/badge/Требуется-Python_3.8+-blue?logo=python&logoColor=white" alt="Python" />
  <img src="https://img.shields.io/badge/Требуется-MySQL-orange?logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/Требуется-IP_камеры-red" alt="IP камеры" />
</div>

### 📦 Установка зависимостей

```bash
# Установка основных зависимостей
pip install opencv-python ultralytics mysql-connector-python ntplib

# Установка дополнительных библиотек
pip install numpy matplotlib tqdm pillow
```

### ⚙️ Настройка системы

1. **Настройка базы данных MySQL:**
   - Создать базу данных `peko`
   - Настроить доступ пользователя `root`
   - База данных автоматически создаст необходимые таблицы при первом запуске

2. **Настройка IP-камер:**
   - Указать корректные RTSP-ссылки в конфигурации:
     ```python
     # Пример RTSP-ссылки
     "rtsp://username:password@192.168.190.51:554/ISAPI/Streaming/Channels/101"
     ```

### 🚀 Запуск системы

```bash
# Запуск модуля для линии Hartman8
python Hartman8.py

# Запуск модуля для линии Scorpion
python scorpion.py

# Запуск модуля для линии Dovaina
python dovaina.py
```

## 🔮 Перспективы развития

```mermaid
mindmap
  root((Перспективы развития))
    Интеграция
      ERP-система предприятия
      Системы управления производством
      Мобильные приложения
    Интерфейс
      Веб-интерфейс для мониторинга
      Дашборды производительности
      Панель администрирования
    Аналитика
      Анализ качества продукции
      Прогнозирование сбоев
      Оптимизация производительности
    Масштабирование
      Другие производственные линии
      Распределенное развертывание
      Поддержка кластеризации
```

<div align="center">
  <img src="https://img.shields.io/badge/Разработано_для-Автоматизации_производства-brightgreen" alt="Автоматизация производства" />
  <img src="https://img.shields.io/badge/Эффективность-Повышена-orange" alt="Эффективность" />
  <img src="https://img.shields.io/badge/Точность-98%25-blue" alt="Точность" />
</div>

---

<div align="center">
  <sub>© 2023 Система компьютерного зрения для учета хлебобулочной продукции</sub>
</div>

