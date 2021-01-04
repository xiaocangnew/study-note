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
  
1.镜像是程序和文件的集合(类似java类)，容器是镜像的运行实例(java对象实例)。
2.每个容器运行时都有自己的容器层，保存容器运行相关数据（写数据），因为镜像层是只读的，所以多个容器可以共享同一个镜像
3.删除容器时，Docker Daemon会删除容器层，保留镜像层

### 创建镜像
- 从已经创建的容器中更新镜像
    commit save
- 使用DockerFile来创建一个镜像,Dockerfile是一个用来构建镜像的文本文件文本内容包含了一条条构建镜像所需的指令和说明。
   - 基本点
      - #为注释，
      - 建议命令大些， 内容小写
      - 按顺序执行命令
      - FROM 指令为dockerfile的第一个执行指令，目的为指定基准镜像
   - 新建一个目录，创建DockerFile文件
      - FROM  java8/centos:6.7
      - (核心指令) RUN / CMD / ENTRYPOINT (运行命令,run是在build构建时，cmd 是在docker run时， entrypoint类似于CMD，但其不会被 docker run 的命令行参数指定的指令所覆盖)
      - (核心指令) ADD / COPY 复制文件或者目录到容器里指定路径
      - (核心指令)ENV /ARG  key1=value1 key2=value2 (ARG是在build构建时，ENV 时在docker run时)
      - VOLUME  挂载数据卷
      - (核心指令) WORKDIR 指定工作目录
      - (核心指令) EXPOSE 声明接口
      - ONBUILD 延时构建
   - 根据dockerFile构建镜像
      - docker build -t test . (.是当前所在目录，也可以指定DockerFile的绝对路径)

### 容器启动
run/start (run是初始化镜像并启动， start直接启动容器)
- 参数：
    - i 交互式操作
    - t 进入终端， -t tomcat /bash/shell
    - d 容器在后台启动(damon)， 如果要进入容器，使用 docker exec -it pid /bin/bash
    - P 端口映射 (大P是随机映射，小p是指定映射 5050：5000)
    - e 环境变量 key value；
    - v 容器外目录 容器内目录
    
### 容器迁移
- 导出容器   docker export pid > out.tar
- 倒入容器   cat out.tar | docker import - test:v1
- 删除容器   docker rm -f pid

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


### 网络基础
- 网络 = 网络号段+ 主机号段。
     为了提高利用率，增加子网掩码，24代表24个1，8个0。（共32位）
     124.3.0.0/24， 表示前面数字与24个1做与操作，得到的结果就是真实网络。

- Linux 虚拟网络的背后都是由一个个的虚拟设备构成的。
     1. 虚拟化技术没出现之前，计算机网络系统都只包含物理的网卡设备，通过网卡适配器，线缆介质，连接外部网络，构成庞大的 Internet。
     2. 相较于单一的物理网络，虚拟网络变得非常复杂，在一个主机系统里面，需要实现诸如交换、路由、隧道、隔离、聚合等多种网络功能。
     3. 常见的虚拟网络
          3.1 tap/tun
             1.tap/tun提供了一台主机内用户空间的数据传输机制。它虚拟了一套网络接口，这套接口和物理的接口无任何区别，可以配置 IP，可以路由流量，
                不同的是，它的流量只在主机内流通。
             2.tun只操作第三层的IP包，而tap操作第二层的以太网帧。
          3.2 veth-pair
             1.是成对出现的一种虚拟网络设备，一端连接着协议栈，一端连接着彼此，数据从一端出，从另一端进。
             2.它的这个特性常常用来连接不同的虚拟网络组件，构建大规模的虚拟网络拓扑，比如连接 Linux Bridge、OVS、LXC 容器等。
               一个很常见的案例就是它被用于 OpenStack Neutron，构建非常复杂的网络形态。
          3.3 Bridge 是一种虚拟网络设备，
             1.具备虚拟网络设备的所有特性，比如可以配置 IP、MAC 等。
             2.是一个交换机，具有交换机所有的功能。
             3.多个端口，数据可以从多个端口进，从多个端口出。
             4.可以接入其他的网络设备，比如物理设备、虚拟设备、VLAN 设备等。Bridge 通常充当主设备，其他设备为从设备，这样的效果就等同于物理交换机的端口连接了一根网线。
             5.可以完成同主机两台 VM 的之间的通信流程。
             6.Bridge设备通常就是结合 tap/tun、veth-pair 设备用于虚拟机、容器网络里面。


- [地址域映射NAT](https://blog.csdn.net/lmm0513/article/details/89472259)

- Lo网卡， local 本地环回接口。
    假如包是由一个本地进程为另一个本地进程产生的, 它们将通过外出链的’lo’接口,然后返回进入链的’lo’接口。其主要作用有两个：
      1. 一是测试本机的网络配置，能PING通127.0.0.1说明本机的网卡和IP协议安装都没有问题；
      2. 另一个作用是某些SERVER/CLIENT的应用程序在运行时需调用服务器上的资源


### Docker支持4种网络模式：
1. host模式
     容器不会获得一个独立的Network Namespace，而是和宿主机共用一个Network Namespace
     但是，容器的其他方面，如文件系统、进程列表等还是和宿主机隔离的。
     host最大的优势就是网络性能比较好，但是docker host上已经使用的端口就不能再用了，网络的隔离性不好。
2. container模式
    指定新创建的容器和已经存在的一个容器共享一个 Network Namespace，而不是和宿主机共享。
    两个容器的进程可以通过 lo 网卡设备通信。
3.none模式。
    Docker容器拥有自己的Network Namespace，但是，并不为Docker容器进行任何网络配置。
    这种网络模式下容器只有lo回环网络，没有其他网卡。
    这种类型的网络没有办法联网，封闭的网络能很好的保证容器的安全性。
4.bridge模式（默认）
    当Docker进程启动时，会在主机上创建一个名为docker0的虚拟网桥，此主机上启动的Docker容器会连接到这个虚拟网桥上。
    从docker0子网中分配一个IP给容器使用，并设置docker0的IP地址为容器的默认网关。
    （Bridge 设备通常结合 tap/tun、veth-pair 设备用于虚拟机、容器网络里面。）
    在主机上创建一对虚拟网卡veth pair设备，在docker0和新建容器中成一对。


- bridge host none 都是单机的。 overlay就是多机网络了
