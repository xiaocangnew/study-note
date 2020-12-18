### Kafka 判断一个节点是否还活着有那两个条件？
1. 节点必须可以维护和 ZooKeeper 的连接，Zookeeper 通过心跳机制检查每个节点的连接
2. 如果节点是个follower,他必须能及时的同步leader的写操作，延时不能太久

### kafka中的 zookeeper 起到什么作用，可以不用zookeeper么？
1. zookeeper是一个分布式的协调组件，
2. 早期版本的kafka用zk做meta信息存储，consumer的消费状态，group的管理以及 offset的值。
3. 新版本中逐渐弱化了zookeeper的作用。新的consumer使用了kafka内部的group coordination协议，也减少了对zookeeper的依赖，
4. 新版本中broker依然依赖于ZK，zookeeper在kafka中还用来选举controller 和 检测broker是否存活等等。

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