set nm=eso-product
docker create -m 600m --memory-swap=1G --name %nm% --rm -ti -v C:\Users\Whya5\IdeaProjects\zariel\server\temp:/root/eso_server whya5448/dc-eso-kr:product
docker cp ./ssh/id_rsa %nm%:/root/.ssh/
docker start -ai %nm%