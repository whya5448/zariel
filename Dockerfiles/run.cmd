mkdir dc-eso-kr
docker run --name dc-eso-kr-dev -ti -v C:\Users\whya5\IdeaProjects\zariel:/root/zariel -v %cd%\dc-eso-kr:/root/eso_server whya5448/dc-eso-kr
docker start dc-eso-kr-dev -i