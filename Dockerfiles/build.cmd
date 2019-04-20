@echo off
echo docker build -t whya5448/dc-eso-kr .
docker build -t whya5448/dc-eso-kr .
echo docker build -f Dockerfile-product -t whya5448/dc-eso-kr:product .
docker build -f Dockerfile-product -t whya5448/dc-eso-kr:product .
pause