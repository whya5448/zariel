#!/usr/bin/env bash
# for CoreOS

if [[ `id -u` -ne 0 ]] ; then echo "Please run as root" ; exit 1 ; fi

wget https://raw.githubusercontent.com/Whya5448/zariel/master/Dockerfiles/run-product.sh
wget https://raw.githubusercontent.com/Whya5448/zariel/master/Dockerfiles/setup-instance/service.sh
wget https://raw.githubusercontent.com/Whya5448/zariel/master/Dockerfiles/setup-instance/swap.sh
chmod 700 *.sh -R

./swap.sh
./service.sh


