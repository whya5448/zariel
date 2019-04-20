FROM gradle:jdk11

USER root
WORKDIR /root

RUN echo org.gradle.daemon=true > /home/gradle/.gradle/gradle.properties; \
echo org.gradle.parallel=true >> /home/gradle/.gradle/gradle.properties; \
echo org.gradle.jvmargs=-Xmx2G >> /home/gradle/.gradle/gradle.properties; \
apt-get update && apt-get install -y locales p7zip p7zip-full tzdata; \
ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime; \
localedef -f UTF-8 -i ko_KR ko_KR.UTF-8;

ENV LC_ALL ko_KR.UTF-8
ENV ESO_SERVER_DIR /root/eso_server

EXPOSE 5005

CMD /bin/bash