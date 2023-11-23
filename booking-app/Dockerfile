FROM eclipse-temurin:11

ARG jarToCopy

ENV DISPLAY=docker.for.mac.host.internal:0

COPY /target/$jarToCopy /app/app.jar

RUN apt-get update
RUN apt-get install -y libxtst6 libxrender1 libxi6

CMD ["sh", "-c", "java -jar /app/app.jar --dbms=$DBMS --host=$HOST --port=$PORT --name=$DB_NAME --user=$USER --pswd=$PSWD"]