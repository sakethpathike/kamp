FROM gradle:latest AS cache

RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home

WORKDIR /home/gradle/app

COPY build.gradle.* gradle.properties settings.gradle.kts /home/gradle/app/
COPY gradle /home/gradle/app/gradle

RUN gradle dependencies --no-daemon

FROM gradle:latest AS build
ENV GRADLE_USER_HOME=/home/gradle/cache_home

WORKDIR /home/gradle/app

COPY --from=cache /home/gradle/cache_home /home/gradle/cache_home

COPY . /home/gradle/app/

RUN mkdir -p /home/gradle/app/resources/blog \
    && mkdir -p /home/gradle/app/resources/static/images

RUN gradle clean buildFatJar --no-daemon

FROM amazoncorretto:23-alpine AS runtime
EXPOSE 8080

RUN mkdir /app
COPY --from=build /home/gradle/app/build/libs/*.jar /app/kamp.jar

ENTRYPOINT ["java", "-jar", "/app/kamp.jar"]