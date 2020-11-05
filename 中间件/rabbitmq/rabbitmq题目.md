### 消息队列的优缺点：
优点：
解耦、异步、削峰，水平扩展
缺点有：
1.系统可用性降低，依赖mq组件的高可用
2.系统复杂度提高，比如数据一致性问题、证消息不被重复消费、保证消息可靠性传输等

### 性能调优
[性能调优](https://www.cnblogs.com/purpleraintear/p/6033136.html)
Q：MQ 们为什么要做生产者流量控制？
A：麻烦就在于：像 Erlang 的虚拟机实现和设计上都没有阻止用户往一个进程的消息队列里扔消息，当消息的生产速度过快，超过进程的处理能力时，这些消息就堆积起来，占用越来愈多的内存，最终导致VM崩溃。』 
Q：我为什么要知道 MQ 在做生产者流量控制？
A：当你发现自家的 Producers 动辄被挂起或被阻塞时，你要知道该调 Consumer 的消费速率，还是调 Memory Threshold of MQ 。

### 什么是rabbitmq
使用erlang编写，一个基于AMQP协议的消息中间件

### 流控机制  
- RabbitMQ可以对内存和磁盘使用量设置阈值，当达到阈值后，生产者将被阻塞（block），直到对应项恢复正常。
    RabbitMQ在正常情况下也可以用流控（Flow Control）机制来确保稳定性。
- RabbitMQ的流量控制机制是基于信用证(Credit)的拥塞控制机制 
    1. 消息处理进程有一个信用组{InitialCredit，MoreCreditAfter}，默认值为{200, 50}。
    2. 消息发送者A向接收者B发消息，每发一条消息，Credit数量减1，直到为0，A被block住；
    3. 对于接收者B，每接收MoreCreditAfter条消息，会向A发送一条消息，给予A MoreCreditAfter个Credit，当A的Credit>0时，A可以继续向B发送消息
- 实质: 监控每个进程的mailbox，当某个进程负载过高来不及接收消息时，这个进程的mailbox就开始堆积消息。
       当堆积到一定量时，就会阻塞住上游进程，让其不得接收新消息。从而慢慢上游进程的mailbox也开始积压消息(类似多级水库，当下游水库压力过大时，上游水库就得关闭闸门，使得自己的压力也越来越大，需要关闭更上游的水库闸门，直到关闭最最上游的闸门

### 流量控制  2.8.0+引入了一个新特性“internal flow control”。至此 RabbitMQ 有三种：
- 1.1.面向每一个连接做的流量控制。 Per-Connection Flow Control 
    - 主动阻塞（Block）那些发布消息太快的连接（Connections），无需做任何配置。
    - 如果连接被阻塞了，那么它在rabbitmqctl 控制台上会显示一个blocked的状态。
   
- 1.2.面向内存做的流量控制。 Memory-Based Flow Control
    RabbitMQ 会在启动时检测机器的物理内存数值。
    默认当MQ占用40%以上内存时，MQ会主动抛出一个内存警告并阻塞所有连接
    默认值是0.4 :[{rabbit, [{vm_memory_high_watermark, 0.4}]}].

- 1.3.面向磁盘存储做的流量控制。 Disk-Based Flow Control
  默认:剩余磁盘空间<1GB，主动阻塞所有的生产者。这个阈值也是可调的。
  
#### 消息超时
- 为队列设置消息TTL：（x-expires参数）。
    - 1.队列在多长时间未被访问将被删除
       - 队列未被访问定义：1.队列没有被重新申明。2.没有basicGet操作发生.3.没有Consumer连接在队列上（哪怕队列一直没有消息）
       - 特别的：就算一直有消息进入队列，也不算队列在被使用。
   - 2. RabbitMQ保证死消息不会被消费者获得，同时会尽快删除死的消费者。
       - 消息不会在消费者的缓冲区中过期。只要队列在消息过期前将消息推送给消费者，消费者就一定能处理到这条消息。
       - 重新入队的消息不刷新过期时间。（例如被取消确认或者信道关闭或拒绝并重新入队）       
- 为单条消息设置TTL（x-message-ttl 参数）
    - 当队列消息的TTL和消息TTL都被设置，时间短的TTL设置生效。
    - 为消息设置TTL有一个问题：RabbitMQ只对处于队头的消息判断是否过期（即不会扫描队列），
      很可能队列中已存在死消息，但是队列并不知情。这会影响队列统计数据的正确性，妨碍队列及时释放资源。

### 死信队列 
- 进入死信的三种方式
1.消息被拒绝并且requeue=false (requeue=true时，消息会重新回到queue的头部，重新消费)
2.消息TTL过期 
3.队列达到最大长度 

- 死信队列配置
  x-dead-letter-exchange: x-dead-letter-exchange
  x-dead-letter-routing-key: task_queque.fail
  
#### 数据持久化
1.将数据写入磁盘上的一个持久化日志文件，Rabbit会在消息提交到日志文件后才发送响应。
2.消息消费后在持久化日志中标记为等待垃圾收集。
3.失败重启后重播持久化日志文件中的消息到合适的队列或者交换器上。

### MQ 的常见问题有：
- 消息的顺序问题
   - 1.producer保证发送消息的顺序性：
       1. channel内发送的消息是保证顺序性的， 自研确保channel与线程绑定。
       2. 继承AbstractRoutingConnectionFactory， 实现lookupkey方法，每个factory中只用一个channel。
   - 2.consumer端保证接受消息的顺序性
       1. 一个queue只用一个consumer消费(防止多消费者时，一个consumer消费失败，又传给别人消费)

- 消息的重复问题
   - 幂等
   
- 如何保证不丢消息
   - 1.producer引入事务机制或者Confirm机制
   - 2.消息队列进行消息持久化， 同时引入mirrored-queue镜像队列
       1.exchange持久化：channel.exchangeDeclare(exchangeName,"direct/topic/header/fanout",true);
       2.queue持久化：channel.queueDeclare(queueName,true,false,false,null);
       3.message持久化发送,设置BasicProperties的deliverayMode=2：
   - 3.consumer不自动ack，处理完成后再ack，
   - 4.消息补偿机制，发送消息前入库，缺失消息后可以重发；

## 集群
###普通模式-提高吞吐量
- 集群启动方式：
准备1. 准备3台虚拟机，均安装rabbitmq-server，分别对应node1，node2，node3
准备2. 修改hosts文件，添加host和ip映射： rmq-01 10.200.37.201
准备3. rabbitmq的集群是依附于erlang的集群来工作的，所以必须先构建起erlang的集群镜像。
      erlang集群内所有的设备要持有相同的erlang.cookie文件才允许彼此通信。复制rmq-01的cookie到剩余两台中。
1. 重启所有rmq. systemctl restart rabbitmq-server.service
2. 在rmq-02/03中执行： rabbitmqctl stop_app； rabbitmqctl reset； 
    rabbitmqctl join_cluster --ram rabbit@rmq-01； rabbitmqctl start_app。
备注. 
1.--ram 指定内存节点类型(消息都存储在内存中，重启服务器消息丢失，性能高于磁盘类型)，
  --disc指定磁盘节点类型(消息会存储到磁盘，为了高可用，至少两个以上的磁盘节点)； 
2. rabbitmqctl cluster_status 查看集群状态。   rabbitmqctl change_cluster_node_type disc 修改节点类型。
      

- 集群元数据的同步（不通过zookeeper，通过rabbitmq命令设置）集群中的每个节点都会始终同步四种类型的内部元数据（类似索引）：
    - queues：队列名称和它的属性；
    - exchanges：交换器名称、类型和属性；
    - bingdings：一张简单的表格展示了如何将消息路由到队列；
    - vhost：为vhost内的队列、交换器和绑定提供命名空间和安全属性；
1. 当用户访问其中任何一个RabbitMQ节点时，通过rabbitmqctl查询到的信息都是相同的。
      场景1、客户端直接连接队列所在节点
      场景2、客户端连接的是非队列数据所在节点（该节点充当路由器角色，进行转发）
   
- 存在问题
1. 单点失效后，exchange和binding是好的。
3. 普通集群中queue及其内容仅仅存储于单个节点之上，所以一个节点的失效表现为其对应的queue不可用


### 镜像队列（高可用）
- 集群启动方式：
  在cluster普通模式中中任意节点启用策略，策略会自动同步到集群节点，从而变为镜像模式。
  使用命令：rabbitmqctl set_policy -p my-vhosts my-policy"^" '{"ha-mode":"all"}'。
  
- 镜像队列支持两种机制。
  - 事务机制，只有当前事务在全部镜像queue中执行之后，客户端才会收到Tx.CommitOk的消息。
  - publish-confirm机制，message被发送给全部镜像，只有当都接受了，才会confirm。

- 队列中的master和slave
1. 通常针对每一个镜像队列都包含一个master和多个slave，分别对应于不同的节点
2. 所有动作都只会向master发送，然后由master将命令执行的结果广播给slave(publish-confirm机制除外，所以普通机制有丢失消息的风险)，
3. slave会准确地按照master执行命令的顺序进行命令执行，故slave与master上维护的状态应该是相同的。
4. 消息的发布和消费都是通过master队列完成， slave只负责备份
  - 节点的失效
    1. slave失效，系统只记录log。或者被通知slave失效。
    2. master失效
    2.1. 与master相连的客户端连接断开；
    2.2 选举最老的slave节点为master。若此时所有slave处于未同步状态，则未同步部分消息丢失；
    2.3 新master节点requeue所有unack消息，此时客户端可能有重复消息(因为无法区分消息的状态)
  
- 新节点加入镜像队列时， 
1. 之前保存的队列内容会被清空
2. 新节点加入镜像队列的消息同步
    1. ha-sync-mode=manual 为新节点加入镜像队列时的默认值，镜像队列中的消息不会主动同步到新节点
    2. 新加入节点调用同步命令后，队列开始阻塞，无法对其进行操作，直到同步完毕
    3. 不建议在生产的active队列（有生产消费消息）中设置ha-sync-mode=automatic.
    
- 镜像队列的启动和关闭
1. 启动时master先启动，slave后启动
如果slave先启动，它会有30s的等待时间，等待master的启动，然后加入cluster中
（如果30s内master没有启动，slave会自动停止）
2. 关闭时master最后关闭

- 镜像队列的恢复(只能先启动master，然后启动slave)
1. slave先死， master后死，但是slave无法恢复。
   master启动后，在master上调用rabbitmqctl forget_cluster_node slave解除关系，新加入slave即可
2. slave先死， master后死，但是master无法恢复。
    在slave节点上执行rabbitmqctl forget_cluster_node -offline master。(将master踢出cluster，然后slave就可以正常启动了)
3. slave先死， master后死，都无法恢复。
    将slave/master的数据库文件（$RabbitMQ_HOME/var/lib目录中）copy至新节点C的目录下，
    再将C的hostname改成master或者slave的hostname。按照1或者2进行重启。
    
- 集群中每个有客户端连接的节点都会启动若干个channel进程，
  channel进程中记录着镜像队列中master和所有slave进程的Pid，以便直接与队列进程通信
  
- 镜像参数：(吞吐量可能下降)
  rabbitmqctl set_policy [-p Vhost] Name Pattern Definition [Priority]
     -demo : rabbitmqctl set_policy ha-all "^rock.wechat" '{"ha-mode":"all","ha-sync-mode":"automatic"}'
     - Vhost： 可选参数，针对指定vhost下的queue进行设置
     - Name: policy的名称(自己定义)
     - Pattern: queue的匹配模式(正则表达式)
     - Definition：镜像定义，包括三个部分ha-mode, ha-params, ha-sync-mode
         ha-mode:指明镜像队列的模式，有效值为 all/exactly/nodes (all：表示在集群中所有的节点上进行镜像(我们现在线上就是all)
                                                   		exactly：表示在指定个数的节点上进行镜像，节点的个数由ha-params指定
                                                   		nodes：表示在指定的节点上进行镜像，节点名称通过ha-params指定)
         ha-params
         ha-sync-mode：automatic / manual
     - priority：可选参数，policy的优先

### rabbitmq 节点的类型有哪些
磁盘节点：(元数据，队列，消息)会存储到磁盘。
内存节点：都存储在内存中，重启服务器消息丢失，性能高于磁盘类型。

### rabbitmq 集群搭建需要注意哪些问题？
1. 整个集群中必须包含一个磁盘节点。
2. 各节点之间使用“–link”连接，此属性不能忽略。
3. 各节点使用的 erlang cookie 值必须相同，此值相当于“秘钥”的功能，用于各节点的认证。

### rabbitmq 每个节点是其他节点的完整拷贝吗？为什么？
不是完整拷贝，只同步其他节点的元数据。
存储空间的考虑：如果每个节点都拥有所有队列的完全拷贝，这样新增节点不但没有新增存储空间，反而增加了更多的冗余数据；
性能的考虑：如果每条消息都需要完整拷贝到每一个集群节点，那新增节点并没有提升处理消息的能力，最多是保持和单节点相同的性能甚至是更糟。

### rabbitmq 集群中唯一一个磁盘节点崩溃了会发生什么情况？
如果唯一磁盘的磁盘节点崩溃了，集群是可以保持运行的，但你不能更改任何东西。
1.不能添加和删除集群节点(所以至少两个磁盘节点)
2.不能创建队列,交换器,绑定
3.不能添加用户,更改权限

### rabbitmq 对集群节点停止顺序有要求吗？
应该先关闭内存节点，最后再关闭磁盘节点.
如果顺序恰好相反的话，可能会造成消息的丢失。

### 为什么不应该对所有的 message 都使用持久化机制？
1. 性能下降； 有10倍差距
2. message的持久化机制用在RabbitMQ的内置cluster方案时会出现问题
    2.1 message持久化，但queue未持久化.那么当queue的owner node出现异常后，在未重建该queue前，发往该queue的message将被阻塞；
    2. 2message持久化，但queue也持久化.那么当queue的owner node出现异常后，若无法重启的情况下，则该queue无法在其他node上重建，发消息也会阻塞；
    
### 消费者出问题
- 集群队列的磁盘要写满
- 导致消息过期，丢失了 
- 1. 大量消息堆积了几个小时的处理办法   
  - 1. 修复bug(大量积压导致原来consumer消费能力不足，需要扩容)
  - 2. 临时紧急扩容(需要考虑机器是否存在数据库竞争等情况，临时分发程序是否可以保证正确性，及时性)
    - 1.1 新建多个queue，将积压的消息轮询写入新建的多个queue中，进行快速消费；
    - 1.2 等消费完消息后，再去掉临时程序。
- 2. 堆积的消息要过期了(丢失消息)
    - 1. producer发消息前要落库，之后进行重发；
    - 2. 要能够找出丢失的消息，进行重发。

### 为什么一个普通的方法加上@RabbitListener注解就能接收消息了呢？
RabbitListenerEndpointRegistry 类是用来管理RabbitListenerEndPoint的，  
它实现了smartLiftcycle，在应用起来时自动调用container.start();

### RabbitMQ 上的一个 queue 中存放的 message 是否有数量限制？
可以认为是无限制，因为限制取决于机器的内存，但是消息过多会导致处理效率的下降。

### master queue失效
- 若cluster中拥有某个queue的owner node失效了，且该queue被声明具有durable属性，
  是否能够成功从其他node上重新声明该 queue ？
1. 若queue具有durable属性，如果该queue的owner node失效，只能等queue所属的node恢复后才能使用该queue。继续使用该queue将得到404错误。
2. 若queue不具有durable属性，则可在其他node上重新声明。
    
### Consumer Cancellation Notification 机制用于什么场景？
1.用于保证当镜像queue中master挂掉时，连接到slave上的consumer可以收到自
身consume被取消的通知，进而可以重新执行consume动作从新选出的master出获得消息。
2.若不采用该机制，连接到slave上的consumer将不会感知master挂掉这个事情，导致后续无法再收到新master广播出来的message。
3.另外，因为在镜像queue模式下，存在将message进行requeue的可能，所以实现consumer的逻辑时需要能够正确处理出现重复message的情况。

### consumer进行Basic.Reject 
1. 如果rabbitmq 设置了requeue=true，会发送给其他consumer
2. 如果没有设置，rabbitmq会直接扔掉该信息。

### 什么情况下会出现 blackholed 问题？
- blackholed问题是指，向exchange投递了message而由于各种原因导致该message丢失，但发送者却不知道。
可导致 blackholed 的情况：
1.向未绑定 queue 的exchange 发送 message；
2.发送消息时使用错误的routing_key

- 如何防止出现 blackholed 问题？
1.发消息时设置 mandatory=true，则在遇到可能出现blackholed情况
    时，服务器会通过返回Basic.Return告之当前message无法被正确投递（内含原因 312 NO_ROUTE）。
2.增加addReturnListener()回调方法，当收到服务器端的basic.return消息时，
   ReturnListener方法被调用，生产者端可以进行消息重传
(mandatory标志告诉服务器至少将该消息route到一个队列中，否则将消息返还给生产者；
immediate标志告诉服务器如果该消息关联的queue上有消费者，则马上将消息投递给它，
如果所有queue都没有消费者，直接把消息返还给生产者，不用将消息入队列等待消费者了。)

### HaProxy 负载均衡
1. cluster中每个节点都有集群所有queue信息，如果所有客户端都连接到同一台node上，网络负载必然会大大增加而显得难以承受 

### 消息如何分发？
若该队列至少有一个消费者订阅，消息将以循环（round-robin）的方式发送给消费者。
每条消息只会分发给一个订阅的消费者（前提是消费者能够正常处理消息并进行确认）。
通过路由可实现多消费的功能