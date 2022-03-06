FROM maven:3.6-jdk-8

COPY pom.xml /working_dir/

WORKDIR /working_dir

RUN mvn dependency:go-offline -P coverage-per-test

COPY src /working_dir/src/

RUN mvn -T1C clean install -P coverage-per-test
