# Use official Kotlin/Java base image
FROM amazoncorretto:25

WORKDIR /app

# Copy your compiled jar
COPY build/libs/personalstatus-api-all.jar ./personalstatus-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "personalstatus-api.jar"]