1. 部署mysql
  docker run -d \
   -e MYSQL_ROOT_PASSWORD=123456 \
   --name mysql \
   -p 3306:3306 \
   mysql