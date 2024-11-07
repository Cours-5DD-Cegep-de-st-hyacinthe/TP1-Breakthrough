FROM openjdk:21-jdk
COPY target/ /
# Trouver quel port exposer
EXPOSE 8080
EXPOSE 9092
EXPOSE 29092
EXPOSE 39092
ENTRYPOINT ["java","-jar","/scala-3.3.4/scalakafka-assembly-0.1.0-SNAPSHOT.jar"]