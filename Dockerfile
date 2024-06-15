FROM openjdk:11

WORKDIR /opt/play
RUN wget https://github.com/playframework/play1/releases/download/1.3.2/play-1.3.2.zip
RUN unzip play-1.3.2.zip
ENV PATH="${PATH}:/opt/play/play-1.3.2"

RUN apt-get update
RUN apt-get install -y python2

CMD ["python","-version"]
