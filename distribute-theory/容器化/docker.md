### docker VS 虚拟机
1. 隔离级别： docker 进程级；虚拟机是操作系统级。
2. 系统资源： docker 站5%以下； 虚拟机站5%-15%。
3. 镜像存储： docker是MB级别，虚拟机时GB级别；
4. 集群规模： docker是上万， 虚拟机是上百。

### docker是什么
Docker提供容器的生命周期管理和 Docker 镜像构建运行时容器


### 镜像和容器
- 直观感受
  镜像：比如alpha.jar 就类似一个镜像，是一个打包的易于传输的东西
  容器： 就是镜像解压后部署的东西；
  
1.镜像是程序和文件的集合，容器是镜像的运行实例。
2.每个容器运行时都有自己的容器层，保存容器运行相关数据（写数据），因为镜像层是只读的，所以多个容器可以共享同一个镜像
3.删除容器时，Docker Daemon会删除容器层，保留镜像层
4.创建镜像两种方式： 1.使用makefile从头开始创建； 2.使用容器commit save命令把当前容器生成镜像。

### 创建镜像
- 从已经创建的容器中更新镜像
- 使用DockerFile来创建一个镜像,Dockerfile是一个用来构建镜像的文本文件文本内容包含了一条条构建镜像所需的指令和说明。
   - 新建一个目录，创建DockerFile文件
      - FROM  java8/centos:6.7 (基础)
      - RUN / CMD / ENTRYPOINT (运行命令,run是在build构建时，cmd 是在docker run时， entrypoint 不会被)
      - COPY  源路径 目标路径
      - ENV /ARG  key1=value1 key2=value2 (ARG是在build构建时，ENV 时在docker run时)
      - VOLUME  挂载数据卷
      - WORKDIR  指定工作目录
      - EXPOSE  22 (声明接口)
      - ONBUILD 延时构建
   - docker build -t test . (.是当前所在目录，也可以指定DockerFile的绝对路径)

### 容器启动
run/start (run是初始化镜像并启动， start直接启动容器)
- 参数：
    - i 交互式操作
    - t 进入终端， -t tomcat /bash/shell
    - d 容器在后台启动， 如果要进入容器，使用 docker exec -it pid /bin/bash
    - P 端口映射 (P 是随机映射， p是指定映射 5050：5000)
    
### 容器迁移
- 导出容器   docker export pid > out.tar
- 倒入容器   cat out.tar | docker import - test:v1
- 删除容器   docker rm -f pid
s
### 容器互联
- 端口映射把docker连接到另一个容器的方法。
- docker有一个连接系统允许将多个容器连接在一起，共享连接信息。
- docker连接会创建一个父子关系，其中父容器可以看到子容器的信息。




### 运维
- docker port pid 端口查看
- docker logs -f pid 看日志
- docker top pid 看进程
- docker inspect pid 查看 Docker的底层信息。它会返回容器的配置和状态信息(json)


###LXC(linux container)所实现的隔离性
主要是来自内核的命名空间, 其中pid、net、ipc、mnt、uts 等命名空间将容器的进程、网络、消息、文件系统和hostname 隔离开。



### mac VS docker
1.  在macOS中，docker是安装在linux虚拟机中：/Users/<YourUserName>/Library/Containers/com.docker.docker/Data/com.docker.driver.amd64-linux/
2. 下载的景象存储在linux下的Docker.qcow2文件中
3.  在linux目录下， 使用screen tty命令进入linux的终端中。
4.  Docker宿主机与macOS操作系统的目录共享。将业务数据写入到宿主机中。如果容器故障，只需将容器删除，重新启用一个容器即可，这样就不会丢失原来的数据。



### docker目录：

aufs ：docker后端文件存储系统
    diff：存放docker image的subimage，每个目录中存放了subimage的真实文件和目录
    layers：存放docker image的layer文件，每个layer文件都记录了其祖先image列表
    mnt：每个容器实例的文件layer的目录挂载点
containers：用于存储容器信息
image：用来存储镜像中间件及本身信息，大小，依赖信息
tmp：docker临时目录
trust：docker信任目录
volumes：docker卷目录


### docker 创建网络： 
  docker network create --subnet=172.18.0.0/24 net1
  docker network inspect net1


### Docker数据卷。 -v 宿主机目录：容器目录
Docker 创建卷： docker volume create —name v1
               docker volume inspect v1
1）volumes：Docker管理宿主机文件系统的一部分，默认位于 /var/lib/docker/volumes 目录中。
     目前所有Container的数据都保存在了这个目录下边，由于没有在创建时指定卷，所以Docker帮我们默认创建许多匿名（启动时使用相对路径）
2）bind mounts：意为着可以存储在宿主机系统的任意位置；但是，bind mount在不同的宿主机系统时不可移植的。
     bind mount不能出现在Dockerfile中的原因（创建时使用绝对路径）
3）tmpfs：挂载存储在宿主机系统的内存中，而不会写入宿主机的文件系统。不推荐