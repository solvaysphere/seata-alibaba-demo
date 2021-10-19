FROM openjdk:8-jdk-alpine
MAINTAINER SolvaySphere <hpuswl@126.com>
VOLUME /tmp
ENV APP_VERSION 0.0.1-SNAPSHOT
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN mkdir /app
ADD target/seata-account-service-$APP_VERSION.jar /app/app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Duser.timezone=GMT+08", "-jar","/app.jar"]
