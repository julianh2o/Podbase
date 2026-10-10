FROM openjdk:11

WORKDIR /opt/play
RUN wget https://github.com/playframework/play1/releases/download/1.3.2/play-1.3.2.zip
RUN unzip play-1.3.2.zip
ENV PATH="${PATH}:/opt/play/play-1.3.2"

# Replace the vulnerable bundled log4j 1.2.17 with reload4j, a drop-in fork
# that keeps the org.apache.log4j.* API (see issue #158).
ARG RELOAD4J_VERSION=1.2.25
RUN cd /opt/play/play-1.3.2/framework/lib \
    && rm log4j-1.2.17.jar \
    && wget https://repo1.maven.org/maven2/ch/qos/reload4j/reload4j/${RELOAD4J_VERSION}/reload4j-${RELOAD4J_VERSION}.jar \
    && wget https://repo1.maven.org/maven2/ch/qos/reload4j/reload4j/${RELOAD4J_VERSION}/reload4j-${RELOAD4J_VERSION}.jar.sha1 \
    && echo "$(cat reload4j-${RELOAD4J_VERSION}.jar.sha1)  reload4j-${RELOAD4J_VERSION}.jar" | sha1sum -c - \
    && rm reload4j-${RELOAD4J_VERSION}.jar.sha1

RUN apt-get update
RUN apt-get install -y python2

CMD ["python","-version"]
