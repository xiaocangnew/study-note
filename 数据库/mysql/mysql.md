### 常见问题
1. text字段， 最大65535字节， 超过报错
2. 唯一索引，字段可以为null

### 编码问题：   show variables like '%char%';   查看数据库编码
- 其中与服务器端相关：database、server、system（永远无法修改，就是utf-8）；
  - 查看数据表的编码，也设置成utf-8；
- 与客户端相关：connection、client、results ，也需要设置成utf-8；
  - 设置url连接的编码：characterEncoding=utf8
  
  
### [经典面试题](https://www.cnblogs.com/panwenbin-logs/p/8366940.html)

### [MySQL事务两阶段提交(解决binlog与redo-log的一致性与协同问题)](https://sq.163yun.com/blog/article/165554812476866560)
- 两阶段流程通用流程
   1. 准备阶段
         1.协调者记录事务开始日志
         2.协调者向所有参与者发送prepare消息，询问是否可以执行事务提交，并等待参与者响应
         3.参与者收到prepare消息后，根据自身情况，进行事务预处理，执行询问发起为止的所有事务操作
             3.1 如果能够提交该事务，将undo信息和redo信息写入日志，进入预提交状态
             3.2 如果不能提交该事务，撤销所做的变更，并记录日志
             3.3 参与者响应协调者发起的询问。如果事务预处理成功返回commit，否者返回abort。
             准备阶段只保留了最后一步耗时短暂的正式提交操作给第二阶段执行。
   2. 提交阶段
         1.协调者等待所有参与者准备阶段的反馈
         1.1失败
             如果收到某个参与者发来的abort消息或者迟迟未收到某个参与者发来的消息
             标识该事务不能提交，协调者记录abort日志
             向所有参与者发送abort消息，让所有参与者撤销准备阶段的预处理
         1.2成功
             如果协调者收到所有参与者发来的commit消息
             标识着该事务可以提交，协调者记录commit日志
             向所有参与者发送commit消息，让所有参与者提交事务
         2.参与者等待协调者的指令
         2.1如果参与者收到的是abort消息
            中止事务，利用之前写入的undo日志执行回滚，释放准备阶段锁定的资源
            记录abort日志
            向协调者发送rollback done消息
         2.2如果参与者收到的是commit消息
            提交事务，释放准备阶段锁定的资源
            记录commit日志
            向协调者发送commit done消息
            协调者等待所有参与者提交阶段的反馈
         3.如果协调者收到所有参与者发来的commit done消息
           完成事务，记录事务完成日志
         3.1如果协调者收到所有参与者发来的rollback done消息
            取消事务，记录事务取消日志


- mysql 的两阶段提交
1. 在开启binlog后，binlog会被当做事务协调者，binlog event会被当做协调者日志，MySQL内部会自动将普通事务当做一个XA事务来处理。
2. 事务参与者InnoDB引擎来执行prepare，commit或者rollback。
3. MySQL对binlog做了优化,prepare不写binlog日志，commit才写日志
4. 事务提交的整个过程如下：
     1. 准备阶段
         通知InnoDB prepare：更改事务状态，将undo、redo log落盘
     2. 提交阶段
         记录协调者日志(binlog日志)，并通过fsync()永久落盘
         通知InnoDB commit
5. 内部XA异常恢复
     1.准备阶段redo log落盘前宕机
          InnoDB中还没prepare，binlog中也没有该事务的events。通知InnoDB回滚事务
     2.准备阶段redo log落盘后宕机(binlog落盘前)
          InnoDB中是prepared状态，binlog中没有该事务的events。通知InnoDB回滚事务
     3.提交阶段binlog落盘后宕机
          InnoDB中是prepared状态，binlog中有该事务的events。通知InnoDB提交事务

### 组提交（多个并发需要提交的事务共享一次fsync操作来进行数据的持久化)
- 在没有开启binlog时，Redo log的刷盘操作将会是最终影响MySQL TPS的瓶颈所在。
    MySQL使用了redo-log组提交，将多个刷盘操作合并成一个
- 在开启binlog时，增加了binlog的组提交，分为三个阶段(Flush 阶段、Sync 阶段、Commit 阶段)完成事务
       1. Flush阶段 (作用是提供了Redo log的组提交)
              1. 首先获取队列中的事务组
              2. 将Redo log中prepare阶段的数据刷盘
              3. 将binlog数据写入文件(其实是os buffer）
              4. 这一步完成后数据库崩溃，由于没有bin log，所以MySQL可能会在重启后回滚该组事务
       2. Sync阶段(作用是支持binlog的组提交)
              binlog_group_commit_sync_delay=N：在等待N μs后，开始事务刷盘
              binlog_group_commit_sync_no_delay_count=N
       3. commit阶段(作用是完成最后的引擎提交，使得Sync可以尽早的处理下一组事务，最大化组提交的效率)
            Commit阶段不用刷盘，Flush阶段中的Redo log刷盘已经足够保证数据库崩溃时的数据安全了
- 在MySQL中每个阶段都有一个队列，每个队列都有一把锁保护，第一个进入队列的事务会成为leader，
   leader领导所在队列的所有事务，全权负责整队的操作，完成后通知队内其他事务操作结束。

### 一条更新语句执行的顺序
update T set c=c+1 where ID=2;
按照上面的redo log和组提交相关知识，可以解决了。

### select * from T where A and B, [a和b都有索引，怎么选择](https://www.cnblogs.com/zx125/p/11749860.html)
1.直接force index直接强制指定查询使用的索引
2.analyze table zx重新计算预估的扫描行
3.引导sql的索引选择，比如order by
4.合理设置索引

- 增量复制
   主从同步是增量同步，通过binlog进行；原有数据需要mysqldump先把数据dump出来，导入到slave中去。
   不停机的开启主从同步，可以尝试在mysqldump添加--master-data的参数，这样导入从库之后会自动设置binlog的位点。

- 推 VS 拉 (类似命令广播)
  master主动推送新变化到slave上(减少slave的负担)。

- 主从复制的过程：binlog->relaylog
    - master/slave主从复制线程的交互：     
       - slave上的io线程，主动连接Master，并且请求bin-log，position之后的内容。
       - master上的Binlog dump线程，将bin-log内容、position位置后内容逐条返给Slave IO线程
       - slave接收后存储为relay-log
       - slave上的sql线程，负责读取relay-log并执行
    - 一主多从
        1.master为了将二进制日志只给某一slave，其他slave只从这一个slave上获取；
        2. 无损半同步，只要有一个slave进行ack就可以了。
       
- 保证复制过程中数据一致性及减少数据同步延时
   1. MySQL5.5 以及之前
      一直采用的是异步复制的方式。主库的事务执行不会管备库的同步进度，如果备库落后，主库不幸crash，那么就会导致数据丢失
   2. 同步复制 VS 半同步复制
       同步所有的slave都要commit之后事务才会成功。
       半同步指只有一个slave进行commit后就成功。
   3. MySQL 5.6
      - GTID复制 (Global Transaction ID)，
         GTID复制不像传统的复制方式（异步复制、半同步复制）需要找到binlog和POSITION点，只需要知道master的IP、端口、账号、密码即可。
         因为复制是自动的，MySQL会通过内部机制GTID自动找点同步。
           - 优缺点：
               1. 可以在集群全局范围标识事务，用于取代过去通过binlog文件偏移量定位复制位置的传统方式。
               2. 借助GTID，在发生主备切换的情况下，MySQL的其它Slave可以自动在新主上找到正确的复制位置,简化了复杂复制拓扑下集群的维护，减少了人为设置复制位置发生误操作的风险。
               3. 基于GTID的复制可以忽略已经执行过的事务,减少了数据发生不一致的风险。
      - 半同步复制， after_commit模式
         * master在应答客户端提交的事务前：1.commit事务但是不返回客户端成功；2.master收到至少一个slave的ack、; 3返回客户端ok；
         * slave写入到Relay Log文件之后，就会通知master进行ack。
         * 如果master在等待slave的ack信号超时时，那么master会自动转换为异步复制，当至少一个slave点赶上来时，master便会自动转换为半同步方式的复制。
         * 半同步复制必须是在master和slave两端都开启时才行，否则master都会使用异步方式复制。
      - 缺点：
         1. 会阻塞master session，性能差，非常依赖网络。
         2. 由于master是在三段提交的最后commit阶段完成后才等待，所以master的其他session是可以看到这个提交事务的，
            因此，如果在等待Slave ACK的时候crash了，那么会对其他事务出现幻读，数据丢失。
         3. master crash后，slave数据丢失。
   4. MySQL 5.7 无损半同步复制，
     引入参数rpl_semi_sync_master_wait_point，默认after_sync，指的是master事务不提交，而是接收到slave的(保存relay-log，然后返回ack)ACK确认之后才提交该事务，复制真正可以做到无损的了。
     - 无损复制可能导致master还没commit，在slave收到后还没ack，master就挂了，此时slave数据> master数据，无碍，因为不丢失数据；
     - 无损复制情况下，master意外宕机，重启后发现有binlog没传到slave上面，这部分binlog怎么办，按道理这部分binlog无效
     - 优点：数据零丢失 性能好
       缺点：
         1. ack数量可配，为了性能考虑，一般单机房配置ack = 1，以保证至少有一个从库收到最新的数据，但这样无法做到单机房容灾；
            于是，又发展出了分组半同步（每个机房的副本分为一个组，组内只需回复1个ack即可），以保证各个机房至少有一个从库持有最新的数据
         2. 主库在发送binlog时，不同的sync binlog时机将可能导致数据不一致（sync_binlog参数）
         3. sync_binlog > 1，主库依次flush binlog，update binlog position，send binlog events to slave，sync binlog
            sync_binlog = 1，主库依次flush binlog，sync binlog，update binlog position，send binlog events to slave
         4. 从库接收到binlog后，是否能及时将relay log及时落盘，也可能导致数据不一致（sync_relay_log != 1）
         5. 在超时后，会退化为异步复制，仍然存在脑裂问题
         6. 从库异常时，主库已写入binlog的无法回滚，在主库重启后会多数据
   5. [MySQL 5.7 MGR(MySQL Group Replication)组复制](https://database.51cto.com/art/202004/615706.htm)
       - MGR集群中每个MYSQL Server都有完整的副本，它是基于ROW格式的二进制日志文件和 GTID 特性
       - MGR集群是多个MySQL Server节点共同组成的分布式集群，使用paxos协议, 多数接受即成功，成功后更新relay-log，应用到 binlog，完成数据的同步
          (数据是有延迟的，但很小,但性能好)
       - 特点：
            MGR 是基于 Paxos 协议和原生复制的分布式集群，大多数节点同意即可以通过议题的模式，数据一致性高。
            具备高可用、自动故障检测功能，可自动切换。
            可弹性扩展，集群自动的新增和移除节点，集群最多接入 9 个节点。
            有单主和多主模式。支持多节点写入，具备冲突检测机制，可以适应多种应用场景需求。
            行级别并行复制，多线程复制，保证slave与master同步很快；
       - MGR实现了flow control限流措施，作用就是协调各个节点，保证所有节点执行事务的速度大于队列增长速度，从而避免丢失事务。
            实现原理：整个Group Replication集群中，同时只有一个节点可以广播消息（数据），每个节点都会获得广播消息的机会（获得机会后也可以不广播），
                     当慢节点的待执行队列超过一定长度后，它会广播一个FC_PAUSE消息，所以节点收到消息后都会暂缓广播消息并不提供写操作，
                     直到该慢节点的待执行队列长度减小到一定长度后，Group Replication数据同步又开始恢复。
       
- 复制延时 基本是ms级别  
   - 5.5 是单线程复制，
   - 5.6 是多库复制（对于单库或者单表的并发操作是没用的）， 
   - 5.7 是真正意义的多线程复制，它的原理是基于group commit，只要master上面的事务是group commit的，那slave上面也可以通过多个worker线程去并发执行。
- 主从同步延时产生的原因及解决
    - 常见原因： master负载过高， slave负载过高， 网络延时， 机器性能过低，MySQL配置不合理；
    - 排查方法：show slave status命令输出的Seconds_Behind_Master参数的值来判断：Null为故障；0为没有延时； 正值表示延时时间
              slave_net_timeout 在多少秒没收到主库传来的Binary Logs events之后,slave认为网络超时,Slave IO线程会重新连接主库
    
- cpu飙到500%，排查问题：
  - 先top看是不是mysql导致的，
  - show processlist，执行时间长的线程
      - 显示的信息时来自information_schema.processlist表，所以这个Id就是这个表的主键。
  - explain sql看看执行计划是否准确，index是否消失。

- 删除表中的数据:delete table-- truncate table
   - 不同之处在于：
      1.truncate 更快；
      2.truncate 删全表
      3.truncate强制删除，但delete会检查外键约束
      4.truncate不能回滚，为ddl语句，命令不放到日志文件中
      
### 主备切换
使用MySQL+keepalived是一种非常好的解决方案，在MySQL-HA环境中，MySQL互为主从关系，
这样就保证了两台 MySQL数据的一致性，然后用keepalived实现虚拟IP，
通过keepalived自带的服务监控功能来实现MySQL故障时自动切换

### insert on duplicate key update 死锁问题
1. innodb引擎会先判断插入的行是否已经存在，如果存在，在对该现有的行加上S（共享锁）锁，返回该行数据给mysql引擎内部,
2. mysql在引擎内部执行完duplicate后的update操作，然后对该记录加上X（排他锁），最后进行update写入.
3. 如果正好有两个事务，都获取到s锁后：T1准备获取x锁，需要等待T2释放s锁； T2准备获取x锁，等待T1释放s锁，然后死锁产生了。


### select for update + insert死锁
1.T1，T2事务里select for update 查询时如果没有命中，会加gap锁
2.T1没命中，准备插入insert，insert需要使用插入意向锁， 插入意向锁与gap锁冲突；
3.T2也没命中，准备插入insert，此时就会死锁。


### mysql 快照读和当前读
- 快照读：MySQL使用MVCC (Multiversion Concurrency Control)机制来保证被读取到数据的一致性，读取数据时不需要对数据进行加锁，且快照读不会被其他事物阻塞。
- 当前读：也称锁定读(locking read),通过对读取到的数据(索引记录)加锁来保证数据一致性，当前读会对所有扫描到的索引记录进行加锁，无论该记录是否满足WHERE条件都会被加锁。
- 在读提交和可重复读两种事务隔离级别下：
    - 快照读
        普通的select操作，不会对数据加锁，也不会被事务阻塞
    - 当前读：
        1、SELECT LOCK IN SHARE MODE
        2、SELECT FOR UPDATE
        3、DELETE / UPDATE / INSERT INTO

### redo log和 undo log  [详细参考](https://www.cnblogs.com/vana/p/10254885.html)
- redo log 是重做日志，提供“前滚”操作；undo log是回退日志，提供“回滚”操作。

- Undo Log的原理(保证事务的原子性和持久性，MVCC保证)
* 0. undo log是逻辑日志(可以认为当delete一条记录时，会记录一条对应的insert记录，反之亦然）
     redo log记录物理日志(记录的是数据页的物理修改，而不是某一行或某几行修改成怎样怎样)
     undo log也会产生redo log，因为undo log也要实现持久性保护。
          * 3.1 MVCC 需要。
              事务提交前会覆盖写的db，都需要存储被覆盖的旧值供“读已提交”事务隔离时使用
          * 3.2 crash recovery
              对于未提交的事务需要把被覆盖的旧值还原为新值所以旧值需要落盘。
              考虑到恢复需要逻辑线性序，所以需要把旧值组织成log。
* 1.事务的原子性(Atomicity)
   事务中的所有操作，要么全部完成，要么不做任何操作，不能只做部分操作。
如果在执行的过程中发生了错误，要回滚(Rollback)到事务开始前的状态，就像这个事务从来没有执行过。
   * 1.1原理
    Undo Log的原理很简单，为了满足事务的原子性，在操作任何数据之前，首先将数据备份到一个地方（这个存储数据备份的地方称为Undo Log）。然后进行数据的修改。
    如果出现了错误或者用户执行了ROLLBACK语句，系统可以利用Undo Log中的备份将数据恢复到事务开始之前的状态。
    除了可以保证事务的原子性，Undo Log也可以用来辅助完成事务的持久化。
* 2. 事务的持久性(undo来辅助持久性，redo来做持久性的)
   事务一旦完成，该事务对数据库所做的所有修改都会持久的保存到数据库中。
为了保证持久性，数据库系统会将修改后的数据完全的记录到持久的存储上。
   * 2.1  用Undo Log实现原子性和持久化的事务的简化过程
      A.事务开始. 
      B.记录A=1到undo log.  C.修改A=3. 
      D.记录B=2到undo log.  E.修改B=4. 
      F.将undo log写到磁盘。 (A-F之间系统崩溃,因为数据没有持久化到磁盘,实在内存中修改的，没问题)
      G.将数据写到磁盘。 (顺序要求，先F，然后G)
      H.事务提交 (G,H之间系统崩溃，undo log是完整的,可以用来回滚事务)
   * 2.2 
      前提条件：数据都是先读到内存中，然后修改内存中的数据，最后将数据写回磁盘。
      缺陷：每个事务提交前将数据和Undo-Log写入磁盘，这样会导致两次磁盘IO，因此性能很低。

- Redo Log的原理(保证事务的持久性)
* 1. 在事务提交前，只要将Redo Log持久化即可，不需要将数据持久化。当系统崩溃时，虽然数据没有持久化，但是Redo Log已经持久化。
   系统可以根据 Redo Log的内容，将所有数据恢复到最新的状态。
* 2. Undo + Redo事务的简化过程
  A.事务开始.
  B.记录A=1到undo log. C.修改A=3. D.记录A=3到redo log. 
  E.记录B=2到undo log. F.修改B=4. G.记录B=4到redo log.
  H.将redo log写入磁盘。(必须在事务提交前将Redo Log持久化。)
  I.事务提交(H—I之间崩溃，没事，如果bin log有了，继续事务提交，如果没有bin log，回滚，因为数据的修改还没写入磁盘)
  G.数据写入磁盘(I-G之间崩溃，没事，已经有redo log)
    
undo log和binlog的区别  
1. redo/undo log是属于innoDB层面，作为异常宕机或者介质故障后的数据恢复使用；
     binlog属于MySQL Server层面的，作为恢复数据使用，主从复制搭建。
2. redo log是循环写，日志空间大小固定；binlog是追加写。
3. 二进制日志只在每次事务提交的时候一次性写入缓存中的日志"文件"，
4. redolog先于binlog写入
           
### MySQL写日志时的参数
- 任何log写入磁盘的过程：
   log_buff -> mysql写 -> log_file(本质时内核的 OS buffer) -> OS刷新(fsync) -> disk

- innodb_flush_log_at_trx_commit 参数解释：
     * 0（延迟写，实时刷）： log_buff -- 每隔1秒 > log_file -- 实时fsync > disk
     * 1（实时写，实时刷）： log_buff -- 实时 > log_file -- 实时fsync > disk
     * 2（实时写，延迟刷）： log_buff -- 实时 > log_file -- 每隔1秒 > disk
     
- sync_binlog参数 二进制日志写入磁盘的过程。
  0：默认值。事务提交后，将二进制日志从缓冲写入磁盘，但是不进行刷新操作（fsync()），此时只是写入了操作系统缓冲，若操作系统宕机则会丢失部分二进制日志。
  1：事务提交后，将二进制文件写入磁盘并立即执行刷新操作，相当于是同步写入磁盘，不经过操作系统的缓存。
  N：每写N次操作系统缓冲就执行一次刷新操作。
