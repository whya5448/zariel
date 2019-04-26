#!/usr/bin/env bash

docker run --rm --name nginx -p 80:80 -v nginx.conf:/etc/nginx/nginx.conf:ro -v $PWD/dc-eso-kr:/usr/share/nginx/html:ro -d nginx:mainline