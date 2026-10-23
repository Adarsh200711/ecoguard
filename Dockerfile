
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -DskipTests package
CMD ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar target/*.jar"]
