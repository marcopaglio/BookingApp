FROM eclipse-temurin:11

ARG jarToCopy

#ENV DISPLAY=host.docker.internal:0.0 #Windwos
#ENV DISPLAY=docker.for.mac.host.internal:0 #Macos

COPY /target/$jarToCopy /app/app.jar

RUN apt-get update
RUN apt-get install -y libxtst6 libxrender1 libxi6

CMD ["sh", "-c", "java -jar /app/app.jar --dbms=$DBMS --host=$HOST --port=$PORT --name=$DB_NAME --user=$USER --pswd=$PSWD"]