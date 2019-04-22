#!/usr/bin/env bash
scnm=dc-eso-kr

touch /etc/systemd/system/${scnm}.service
touch /etc/systemd/system/${scnm}.timer

cat > ${scnm}.timer <<- EOM
[Unit]
Description=Run eso-eso-kr-server daily 03:00

[Timer]
OnCalendar=*-*-* 03:00:00 Asia/Seoul

[Install]
WantedBy=timers.target
EOM

cat > ${scnm}.service <<- EOM
[Unit]
Description=make TESO-Korean Language sfx

[Service]
Type=oneshot
ExecStart=/root/run-product.sh

[Install]
WantedBy=${scnm}.timer
EOM

systemctl enable ${scnm}
systemctl start ${scnm}