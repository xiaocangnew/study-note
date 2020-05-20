### 常见问题
1. text字段， 最大65535字节， 超过报错
2. 唯一索引，字段可以为null

### 编码问题：   show variables like '%char%';   查看数据库编码
- 其中与服务器端相关：database、server、system（永远无法修改，就是utf-8）；
  - 查看数据表的编码，也设置成utf-8；
- 与客户端相关：connection、client、results ，也需要设置成utf-8；
  - 设置url连接的编码：characterEncoding=utf8
  
  
### [经典面试题](https://www.cnblogs.com/panwenbin-logs/p/8366940.html)

- 主从复制的过程：binlog->relaylog
    - master/slave主从复制线程的交互：     
       - slave上的io线程，主动连接Master，并且请求某个bin-log，position之后的内容。
       - master上的io线程，将bin-log内容、position返回给Slave IO线程
       - slave接收后存储为relay-log
       - slave上的sql线程，负责读取relay-log并执行
    - 一主多从
        master为了将二进制日志只给某一slave，其他slave只从这一个slave上获取；
       
- 保证复制过程中数据一致性及减少数据同步延时
   * MySQL5.5 以及之前
      一直采用的是异步复制的方式。主库的事务执行不会管备库的同步进度，如果备库落后，主库不幸crash，那么就会导致数据丢失
   * MySQL 5.6
      - GTID复制 (Global Transaction ID)，
         GTID复制不像传统的复制方式（异步复制、半同步复制）需要找到binlog和POSITION点，只需要知道master的IP、端口、账号、密码即可。因为复制是自动的，MySQL会通过内部机制GTID自动找点同步。
      - 半同步复制， after_commit模式
         * master在应答客户端提交的事务前：1.commit事务但是不返回客户端成功；2.master收到至少slave的ack; 3返回客户端ok；
         * slave只有在接收到某一个事务的所有Binlog，将其写入并Flush到Relay Log文件之后，才会通知master进行ack。
         * 如果在等待过程中，等待时间已经超过了配置的超时时间，没有任何slave通知当前事务，那么此时master会自动转换为异步复制，当至少一个slave点赶上来时，master便会自动转换为半同步方式的复制。
         * 半同步复制必须是在master和slave两端都开启时才行，否则master都会使用异步方式复制。
   * MySQL 5.7 无损半同步复制，
     引入参数rpl_semi_sync_master_wait_point，默认after_sync，指的是master事务不提交，而是接收到slave的ACK确认之后才提交该事务，复制真正可以做到无损的了。
     - 无损复制可能导致master还没commit，在slave收到后还没ack，master就挂了，此时slave数据> master数据，无碍，因为不丢失数据；
     - 无损复制情况下，master意外宕机，重启后发现有binlog没传到slave上面，这部分binlog怎么办
- 复制延时 基本是ms级别
   - 5.5 是单线程复制，
   - 5.6 是多库复制（对于单库或者单表的并发操作是没用的）， 
   - 5.7 是真正意义的多线程复制，它的原理是基于group commit，只要master上面的事务是group commit的，那slave上面也可以通过多个worker线程去并发执行。
- 主从同步延时产生的原因及解决
    - 常见原因： master负载过高， slave负载过高， 网络延时， 机器性能过低，MySQL配置不合理；
    - 排查方法：show slave status命令输出的Seconds_Behind_Master参数的值来判断：Null为故障；0为没有延时； 正值表示延时时间
    
    
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

### insert on duplicate key update 死锁问题
这条sql语句执行时包括：
1.innodb引擎会先判断插入的行是否产生重复key错误，如果存在，在对该现有的行加上S（共享锁）锁，返回该行数据给mysql,
2.mysql在引擎内部执行完duplicate后的update操作，然后对该记录加上X（排他锁），最后进行update写入.

注意：
1. 多个s锁可以同时存在； 但x锁只能单独存在，不能和s锁和x锁同时存在；
2.多个线程同时操作时，可能发生死锁。

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

### redo log和 undo log
- redo log 是重做日志，提供“前滚”操作；undo log是回退日志，提供“回滚”操作。
- Undo Log的原理(保证ACID中的 C和I)
    1. 为了满足事务的原子性，在操作任何数据之前，首先将数据备份到一个地方 （这个存储数据备份的地方称为Undo Log）。
     然后进行数据的修改。如果出现了错误或者用户执行了 ROLLBACK语句，系统可以利用Undo Log中的备份将数据恢复到事务开始之前的状态。
    2. undo log用来回滚行记录到某个版本。undo log一般是逻辑日志，根据每行记录进行记录。
- Redo Log的原理(保证ACID中的A和D)
    1. 在事务提交前，只要将Redo Log持久化即可，不需要将数据持久化。当系统崩溃时，虽然数据没有持久化，但是Redo Log已经持久化。
    系统可以根据 Redo Log的内容，将所有数据恢复到最新的状态。
    2. redo log 通常是 物理 日志，记录的是数据页的物理修改，而不是某一行或某几行修改成怎样怎样，
    它用来恢复提交后的物理数据页(恢复数据页，且只能恢复到最后一次提交的位置)。
- 如果不用Redo log，只用undo log来保证ACID中的A和D
    1.事务提交前
       需要将Undo Log写磁盘（提供可回滚功能，保证原子性），这会造成多次磁盘 IO（不考虑各种优化例如 SQL 解析优化等），这些 IO 算是顺序 IO
    2.事务提交后
       需要将数据立即更新到数据库中，这又会造成至少一次磁盘 IO，这是一次随机 IO。
    
undo log和binlog的区别  
1. redo/undo log是属于innoDB层面，作为异常宕机或者介质故障后的数据恢复使用；
     binlog属于MySQL Server层面的，作为恢复数据使用，主从复制搭建。
2. redo log是循环写，日志空间大小固定；binlog是追加写。
