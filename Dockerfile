FROM openjdk:21
WORKDIR /home/project/work5
COPY demo_11-0.0.1-SNAPSHOT.jar /home/project/work5
ENV SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true
ENV TZ=Asia/Shanghai
CMD ["nohup", "java", "-jar", "demo_11-0.0.1-SNAPSHOT.jar", "&"]