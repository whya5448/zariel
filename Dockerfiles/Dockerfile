FROM gradle:jdk13

USER root
WORKDIR /root

RUN sed -i -re "s/[a-Z]+.ubuntu.com/mirror.kakao.com/g" /etc/apt/sources.list

RUN apt-get update; \
apt-get install -y locales p7zip p7zip-full git
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y tzdata; \
ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime; \
echo Asia/Seoul > /etc/timezone; \
localedef -f UTF-8 -i ko_KR ko_KR.UTF-8; \
git config --global user.email "whya5448@gmail.com"; \
git config --global user.name "작은게이"

RUN echo org.gradle.parallel=true >> /home/gradle/.gradle/gradle.properties

ADD ./ssh/known_hosts /root/.ssh/

ENV LC_ALL ko_KR.UTF-8
ENV ESO_SERVER_DIR /root/eso_server

WORKDIR /root/zariel
CMD /bin/bash