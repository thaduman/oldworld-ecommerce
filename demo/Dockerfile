# 1. Aşama: Build (Maven kullanarak projeyi derliyoruz)
FROM maven:3.9.6-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# 2. Aşama: Run (Sadece JRE kullanarak uygulamayı çalıştırıyoruz)
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]