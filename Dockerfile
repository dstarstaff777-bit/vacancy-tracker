# Мультистейдж сборка — сначала собираем, потом запускаем

# Этап 1: Сборка приложения
FROM gradle:8.14-jdk21-alpine AS build
# используем официальный образ с Gradle и Java 21

WORKDIR /app
# рабочая директория внутри контейнера

# Копируем gradle wrapper и зависимости
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Скачиваем зависимости (этот слой кэшируется если зависимости не менялись)
RUN ./gradlew dependencies --no-daemon

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./gradlew bootJar --no-daemon
# bootJar создаёт fat jar со всеми зависимостями

# Этап 2: Запуск приложения
FROM eclipse-temurin:21-jre-alpine
# JRE достаточно для запуска, JDK не нужен
# eclipse-temurin — рекомендуемый OpenJDK дистрибутив

WORKDIR /app

# Копируем jar из предыдущего этапа
COPY --from=build /app/build/libs/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Точка входа
ENTRYPOINT ["java", "-jar", "app.jar"]

# Можно добавить JVM параметры для оптимизации:
# ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
```

---

**3. Создаём .dockerignore**
```
# Не копировать в Docker образ
.git
.gradle
build
*.md
.idea
.gitignore
docker-compose.yml
Dockerfile