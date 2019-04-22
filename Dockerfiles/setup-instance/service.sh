#!/usr/bin/env bash
scloc=/etc/systemd/system/
scnm=dc-eso-kr

touch ${scloc}${scnm}.service
touch ${scloc}${scnm}.timer

cat > ${scloc}${scnm}.timer <<- EOM
[Unit]
Description=Run eso-eso-kr-server daily 03:00

[Timer]
OnCalendar=*-*-* 18:00:00

[Install]
WantedBy=timers.target
EOM

cat > ${scloc}${scnm}.service <<- EOM
[Unit]
Description=make TESO-Korean Language sfx

[Service]
WorkingDirectory=/root/
Type=oneshot
ExecStart=/root/run-product.sh

[Install]
WantedBy=${scnm}.timer
EOM

systemctl enable ${scnm}
systemctl enable ${scnm}.timer
systemctl start ${scnm}.timer