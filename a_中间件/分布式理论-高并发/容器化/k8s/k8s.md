https://www.cnblogs.com/cheyunhua/p/14498014.html


### docker VS docker-compose VS docker swarm VS k8s
Docker是容器技术的核心、基础，
Docker Compose是一个基于Docker的单主机容器编排工具.
Docker Swarm也跨主机的集群部署，现在不行了。
k8s是一个跨主机的集群部署工具，功能丰富

### keburnetes (导航员， 省略中间8个字符：k8s)
- 功能： 
  1. 基于容器的集群管理平台。管理操作包括：部署，调度和节点集群间扩展。
  2. k8s需要提供的是网关、水平拓展、监控、备份、灾难恢复等一系列运维能力。
     并且在任务与任务之间的关系处理，才是作业编排和系统管理最困难的地方。
 
- 一个K8S系统称为一个K8S集群（Cluster）。
   - 一个Master节点（主节点，管理控制）
      - 1. API Server 
             整个系统的对外接口，供客户端和其它组件调用，相当于“营业厅”。
             并提供认证、授权、访问控制、API注册和发现等机制
      - 2. Scheduler
             负责节点资源管理，接收来自kube-apiserver创建Pods任务，并分配到某个节点。
      - 3. Controller manager
             执行整个系统的后台任务，包括节点状态状况、Pod个数、Pods和Service的关联等
   - 一群Node节点（工作负载节点，硬件单元）
       - 1. Container
              k8s中的容器支持不仅仅包含了docker，还支持一些其他的容器标准
       - 2. Fluentd
             主要负责日志收集、存储与查询。
       - 3. pod
            1. Kubernetes最基本的操作单元。pod内部封装了一个或多个紧密相关的容器。
            2. 一个Pod代表着集群中运行的一个进程，被Master调度到一个Node上运行(pod相当与逻辑主机，每个pod都有自己的IP地址)
            3. pod中的所有容器都共享一个网络namespace，所有的容器可以共享存储。
               - pod有两种使用方式：      
                  1. 运行单一容器：
                       one-container-per-Pod 是kubernetes最常见的模型，这种情况下，只是将单个容器简单封装成pod。
                       即使只有一个容器，kubernetes管理的也是pod而不是直接管理容器。
                  2. 运行多个容器:
                       运行在同一个pod的的多个容器必须联系紧密，而且直接共享资源           
            4. Pod的生命周期通过Replication Controller来管理；
       - 4. Kube-proxy，
             主要负责为Pod对象提供代理。
             负责为Service提供cluster内部的服务发现和负载均衡；
       - 5. Kubelet
             主要负责监视指派到它所在Node上的Pod，包括创建、修改、监控、删除

### 重要概念
- Namespace
    如果有多个用户或者项目组共同使用k8s集群，如果将他们创建的Pod等资源分开呢，就是通过Namespace进行隔离。
- 副本集(Replica Sets)，
    是指在扩容时产生的Pod的复制
- deployment
    deployment层是“过程无关”的，你只需要声明你所期望的最终状态，k8s将会自动为你调度pod并保证它们满足你的预期。
- etcd
   保存了整个集群的状态，是个分布式存储；
   负责节点间的服务发现和配置共享。
- service

###  kubectl:客户端命令行工具，作为整个系统的操作入口。

### 组件工作流程
①运维人员向kube-apiserver发出指令（我想干什么，我期望事情是什么状态）
②api响应命令,通过一系列认证授权,把pod数据存储到etcd,创建deployment资源并初始化。(期望状态）
③controller通过list-watch机制,监测发现新的deployment,将该资源加入到内部工作队列,
   发现该资源没有关联的pod和replicaset,启用deployment controller创建replicaset资源,
   再启用replicaset controller创建pod。
④所有controller被创建完成后.将deployment,replicaset,pod资源更新存储到etcd。
⑤scheduler通过list-watch机制,监测发现新的pod,经过主机过滤、主机打分规则,将pod绑定(binding)到合适的主机。
⑥将绑定结果存储到etcd。
⑦kubelet每隔 20s(可以自定义)向apiserver通过NodeName获取自身Node上所要运行的pod清单.
  通过与自己的内部缓存进行比较,新增加pod。
⑧kubelet创建pod。
⑨kube-proxy为新创建的pod注册动态DNS到CoreOS。给pod的service添加iptables/ipvs规则，用于服务发现和负载均衡。
⑩controller通过control loop（控制循环）将当前pod状态与用户所期望的状态做对比，
 如果当前状态与用户期望状态不同，则controller会将pod修改为用户期望状态，实在不行会将此pod删掉，然后重新创建pod。
 
 ### 两地三中心
   两地三中心包括本地生产中心、本地灾备中心、异地灾备中心。
   
   
###一个Pod中的应用容器共享五种资源：
1. PID命名空间：Pod中的不同应用程序可以看到其他应用程序的进程ID。
2. 网络命名空间：Pod中的多个容器能够访问同一个IP和端口范围。
3. IPC命名空间：Pod中的多个容器能够使用SystemV IPC或POSIX消息队列进行通信 (ipc,进程间通信)
4. UTS命名空间：Pod中的多个容器共享一个主机名hostname。 (UNIX Time-sharing System)
5. Volumes(共享存储卷)：Pod中的各个容器可以访问在Pod级别定义的Volumes。

### hello world入门
1. 安装环境。
2. 创建一个deployment： kubectl run helloworld --port=8080(部署一个应用)
3. 访问应用(创建后还需要暴露service才能访问)：
   kubectl expose deployment hello-world --type=LoadBalancer --name=my-service
   kubectl get services my-service
   使用ip访问。

### 缺点
 优点：
1、在基础层提供了抽象，对代码无侵入
 缺点：
1、对微服务治理比较弱，如熔断限流等，当然这也不应该是k8s做的。

