### kafka优缺点
- 优点
   削峰、异步、解耦、可水平扩展
- kafka的缺点(中间件本身有可用性降低和复杂性升高的问题)
1. 由于是批量发送，数据并非真正的实时；
2. 仅支持统一分区内消息有序，无法实现全局消息有序；
3. 监控不完善，需要安装插件；
4. 依赖zookeeper进行元数据管理；
5*. 对于mqtt协议不支持；
6*. 不支持物联网传感数据直接接入；

### kafka为什么这么快
- 1.顺序写磁盘
- 2.零拷贝技(消费者在读取数据时，数据拷贝两次才发送给消费者)
     在Linux kernel2.2 之后出现了一种叫做”零拷贝(zero-copy)”系统调用机制，就是跳过“用户缓冲区”的拷贝，
     建立一个磁盘空间和内存的直接映射，数据不再复制到“用户态缓冲区”系统上下文切换减少为2次，可以提升一倍的性能
- 3.文件分段， 建立索引
- 4.批量发送
- 5.支持消息集合进行压缩。

- 2.大量使用内存页(这个怎么理解记忆？)
   - 利用(Memory-Mapped-Files,mmap技术)
       直接利用操作系统的Page来实现磁盘文件到物理内存的直接映射
       写到mmap中的数据并没有被真正的写到硬盘，操作系统会在程序主动调用flush的时候才把数据真正的写到硬盘。
   - producer.type 参数控制是不是主动flush
       同步(sync): Kafka写入到mmap之后就立即flush然后再返回Producer
       异步(async,默认):写入mmap之后立即返回Producer不调用flush

#### 与rabbitmq对比  
- 1. 应用场景上的差异
    - RabbitMQ是一个消息代理  
    - Kafka是一个分布式流式系统
- 2. 性能上
     2.1 吞吐量
       - kafka比rabbitmq更好，吞吐量更高。
     2.2 伸缩性(分布式扩展能力)
       - rabbitmq 只需要简单的增删消费者就可以了
       - kafka在增加流量时，由于一个topic只能被消费组中的一个消费者消费，只能拓展topic的分区数量，增加更多消费者。
         在减少流量后，topic的分区不能向下减少(只能增加不能减少)
- 3. 消息处理方式上：
     3.1 路由方式
       - rabbitmq支持复杂路由
       - kafka只支持简单的topic路由
     3.2. 消息持久化
       - rabbitmq作为一个消息代理，消息处理完毕就删除不保留，同时queue里消息过多影响性能
       - kafka 可以设置保存时间，重复消费，消息数量的多少也不影响性能；
     3.3. 消息容错处理(消费失败)
       - rabbitmq 提供了死信队列，同时一个消息出问题不影响其他消费者；
       - kafka和rabbitmq都支持重试；同时当topic一个消息出问题时，顺序在后面的消息无法消费；
     3.4. 消息顺序性上的差异
       - rabbitmq 生产时无法保证顺序；在多消费者时无法保证顺行性；
      - kafka 保证单partition上的生产顺序；由消费者组保证每个topic只有一个消费者，是顺序的；
  
### 如何选型
1.协议：AMQP、STOMP、MQTT、私有协议等。
2.消息是否需要持久化。
3.吞吐量。
4.高可用支持，是否单点。
5.分布式扩展能力。
6.消息堆积能力和重放能力。
7.开发便捷，易于维护。
8.社区成熟度。

### Kafka 判断一个节点是否还活着有那两个条件？
1. 节点必须可以维护和 ZooKeeper 的连接，Zookeeper 通过心跳机制检查每个节点的连接
2. 如果节点是个follower,他必须能及时的同步leader的写操作，延时不能太久

### kafka常见问题
- 消息的顺序性：
    1. 在同一个partition内有序
       1.1. producer.send(record1, partition1)；之后producer.send(record2, partition1);
       1.2. Kafka配置了重试机制和max.in.flight.requests.per.connection大于1(默认值是5，本来就是大于1的)，
            瞬时的网络抖动导致record1没有成功发送，record2发送成功了；那么重试record1后，record1就在record2之后了。
       1.3 如果要保证单partition内有序，需要设置max.in.flight.requests.per.connection=1.
    2. 在同一个partition内有序的另一种方式：
       使用幂等+重试：幂等保证record1没有成功前，record2会被broker抛弃； 重试机制保证了record2可以在record1成功后也成功。
    3. 在多个partition内无法保证有序

- 保证数据可靠性，不丢失消息
1. broker级别：关闭不完全的Leader选举，即 unclean.leader.election.enable=false；
      (即不允许非ISR中的副本被选举为leader，以避免数据丢失)
2. topic级别：设置 replication.factor>=3，并且 min.insync.replicas>=2；
3. producer级别：
     1.acks=all（或者 request.required.acks=-1），
     2.发送模式为同步 producer.type=sync (异步时发送后先保存在缓冲区中，如果宕机则丢失全部消息)
     3.关闭自动提交：enable.auto.commit=false
     4.提交缓冲区满后一直阻塞不抛异常：block.on.buffer.full = true  尽管该参数在0.9.0.0已经被标记为“deprecated”，
      但鉴于它的含义非常直观，所以这里还是显式设置它为true，使得producer将一直等待缓冲区直至其变为可用。
      否则如果producer生产速度过快耗尽了缓冲区，producer将抛出异常。缓冲区满了就阻塞在那，不要抛异常，也不要丢失数据       
       
- 保证数据的一致性
 数据可靠性是由ISR中HW来控制的。

### 如何处理kafka所有Replica都不工作
 - 如果某个Partition的所有Replica都宕机了，就无法保证数据不丢失了。这种情况下有两种可行的方案：
   - 1.等待ISR中的任一个Replica“活”过来，并且选它作为Lea der(如果ISR中的所有Replica都无法“活”过来了，或者数据都丢失了，这个Partition将永远不可用)
   - 2.（不一定是ISR中的）第一个“活”过来的Replica作为Leader

- unclean.leader.election.enable 参数决定使用哪种方案，默认是true，采用第二种方案

### kafka客户端使用缓冲池的机制
在客户端发送消息给kafka服务器的时候，一定是有一个内存缓冲机制的，消息会先写入一个内存缓冲中，然后直到多条消息组成了一个Batch，
才会一次网络通信把Batch发送过去。避免了一条消息一次网络请求。从而提升了吞吐量。
每个Batch底层都对应一块内存空间，使用完毕内存空间不交给JVM去垃圾回收，而是把这块内存空间给放入一个缓冲池里。

### Kafka 分区的目的？
 1. 对于Kafka集群的好处是：实现负载均衡。
 2. 对于消费者来说，可以提高并发度，提高效率
 
### Kafka 消费者是否可以消费指定分区消息？
- kafka消费者有两种模式, 订阅模式和分配模式
    订阅模式subscribe： 使用Kafka Group管理，自动进行rebalance操作
    分配模式assign： 用户自己进行相关的处理。consumer.assign(partitionList);   consumer.seek(partition, offset); 指定分区和位移
    一个consumer只能处于两种模式之一。

### Kafka消息是采用Pull模式，还是Push模式
Kafka遵循了一种大部分消息系统共同的传统的设计：producer将消息推送到broker，consumer从broker拉取消息。
Kafka有个参数可以让consumer阻塞知道新消息到达(防止brokers没消息时consuemr空轮询)

### kafka中的选举机制
见distribute-theory -> kafka选举

### kafka为什么不支持读写分离
1. 数据一致性和延时问题（对延时敏感的应用而言，主写从读的功能并不太适用）
     Kafka中，主从同步会比Redis更加耗时，它需要经历网络→主节点内存→主节点磁盘→网络→从节点内存→从节点磁盘这几个阶段。
2. kafka优秀的负载均衡设计不需要读写分裂。使用分区把一个topic的partition放到不同broker上

### [kafka常见的监控指标](https://www.cnblogs.com/xinxiucan/p/12666967.html)
- broker端
    - kafka本身的指标
        - BytesInPerSec/BytesOutPerSec: 通常磁盘的吞吐量往往是决定kafka性能的瓶颈，但也不是说网络就不会成为瓶颈
        - UnderReplicatedPartitions(isr副本数量)
        - ActiveControllerCount(controller数量):  当为0时有问题
        - OfflinePartitionsCount (只有controller有)
        - LeaderElectionRateAndTimeMs(报告了两点：leader选举的频率（每秒钟多少次）和集群中无leader状态的时长（)
        - UncleanLeaderElectionsPerSec(不完全leader选举，会丢数据)
        - PurgatorySize(请求炼狱, 关注炼狱的大小有助于判断导致延迟的原因是什么)
           是一个临时存放的区域，生产(produce)和消费(fetch)的请求在那里等待直到被需要的时候
           当fetch.wait.max.ms定义的时间已到，还没有足够的数据来填充请求，获取消息的请求就会被扔到炼狱中。
           当request.required.acks=-1，所有的生产请求都会被暂时放到炼狱中，直到partition leader收到follower的确认消息。
    - 主机层面的指标
        1. 主要的瓶颈通常是内存。kafka在设计最初的时候，通过内核中的页缓存，来达到沟通可靠性（基于磁盘）和高效性（基于内存）之间的桥梁。
         page cache read ratio（可理解为页缓存读取率）< 80%, 说明要增加broker了。
        2. 磁盘使用情况。
    - JVM垃圾回收指标 
        （垃圾回收造成的）暂停对kafka最大的影响就是会造成大量废弃的zookeeper session（因为session超时了）。
        当发现垃圾回收造成了过度的暂停，你可以考虑升级JDK版本或者垃圾回收器。另外，可以调节java runtime参数来最小化垃圾回收
- producer端
    - Outgoing byte rate(网络吞吐量)
    - IO wait time 当producer产生了超越他发送能力的数据量，那结果就是只能等待网络资源
    - Request rate(请求的速率)
        数据从producer发送到broker的速率。请求的速率变化是否健康是由使用的场景所决定的。
        关注速率走势的上和下，对于保证服务的可用性非常关键。当流量高峰来临时，broker就将变得很慢
    - Request latency average(平均请求延迟)
    
- consumer端
    - BytesPerSec(网络吞吐量)
    - MessagesPerSec(消息消费速度): 正常下应该是稳定的，突然下降可能是因为消费失败
    - MinFetchRate(消费者拉取的速率)
         反映了消费者的整体健康状况,通常都是非0的。如果发现这个值在下降，往往就是消费者失败的标志。
    - ConsumerLag/MaxLag(关键指标)
        ConsumerLag是指consumer当前的日志偏移量相对生产者的日志偏移量，MaxLag是观察到的ConsumerLag的最大值
        如果lag值一直居高不下，那就说明消费者有些过载，需要加机器或切分topic更多分片    
- zookeeper端
   - Bytes sent/received: 
         zookeeper是串行处理请求的。根据时间变化跟踪发送和接受数据比特大小可以帮助诊断性能问题。
         如果zookeeper集群需要连续不断处理大流量，那么就需要为集群提供更多节点，来适应更大数据量。
   - Usable memory: 
          zookeeper是用来保存状态的，需要加载大量数据到内存。zookeeper的性能下降将会导致整个kafka集群的性能下降。
          作为zookeeper节点的主机都需要拥有较大的内存，来应对负载的高峰。
   - Swap usage: 
         如果发现内存不够了，将会用到swap，频繁进行内存交换，降低性能
   - Disk latency:
         zookeeper主要是使用内存，但也会用到文件系统，定时保存事务日志和当前状态快照。
         在update发生以后，zookeeper必须要把事务写到磁盘，磁盘的读写存在潜在瓶颈，
         磁盘延迟的突增，会导致所有与zookeeper通信的服务器响应变慢，时刻关注磁盘延迟。