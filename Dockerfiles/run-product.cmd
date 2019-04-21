set nm=eso-product
mkdir temp
docker create -m 600m --memory-swap=1G --name %nm% --rm -ti -v %cd%\dc-eso-kr:/root/eso_server whya5448/dc-eso-kr:product
docker cp ./ssh/id_rsa %nm%:/root/.ssh/
docker start -ai %nm%