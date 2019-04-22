#!/usr/bin/env bash
sz=2048m

mkdir -p /var/vm
fallocate -l ${sz} /var/vm/swapfile1
chmod 600 /var/vm/swapfile1
mkswap /var/vm/swapfile1

loc=/etc/systemd/system/var-vm-swapfile1.swap
touch ${loc}

cat > ${loc} <<- EOM
[Unit]
Description=Turn on swap

[Swap]
What=/var/vm/swapfile1

[Install]
WantedBy=multi-user.target
EOM

systemctl enable --now var-vm-swapfile1.swap
# Optionally
echo 'vm.swappiness=10' | sudo tee /etc/sysctl.d/80-swappiness.conf
systemctl restart systemd-sysctl