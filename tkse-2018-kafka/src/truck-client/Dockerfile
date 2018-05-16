FROM java:8-alpine
VOLUME /tmp

ENV MAVEN_VERSION 3.5.2
ENV MAVEN_HOME /usr/lib/mvn
ENV PATH $MAVEN_HOME/bin:$PATH

RUN wget http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  tar -zxvf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  rm apache-maven-$MAVEN_VERSION-bin.tar.gz && \
mv apache-maven-$MAVEN_VERSION /usr/lib/mvn

#RUN mvn clean install -DskipTests

ADD ./deploy/truck-client-1.0.0.jar /truck-client.jar
RUN sh -c 'touch /truck-client.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dexec.args='-s KAFKA -f JSON',"-jar","/truck-client.jar"]
