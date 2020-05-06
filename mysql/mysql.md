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
       - master上的dump线程，负责将binlog event传入slave；    
       - slave上的io线程，主动去master上读取binglog, 写入自己的relay-log；
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
         * slave会在连接到主库时告诉主库，它是不是配置了半同步。
         * slave只有在接收到某一个事务的所有Binlog，将其写入并Flush到Relay Log文件之后，才会通知master进行ack。
         * 如果在等待过程中，等待时间已经超过了配置的超时时间，没有任何slave通知当前事务，那么此时master会自动转换为异步复制，当至少一个slave点赶上来时，master便会自动转换为半同步方式的复制。
         * 半同步复制必须是在master和slave两端都开启时才行，否则master都会使用异步方式复制。
   * MySQL 5.7 无损半同步复制，
     引入参数rpl_semi_sync_master_wait_point，默认after_sync，指的是master事务不提交，而是接收到slave的ACK确认之后才提交该事务，复制真正可以做到无损的了。
     - 无损复制可能导致master还没commit，在slave收到后还没ack，master就挂了，此时slave数据> master数据，无碍，因为不丢失数据；
     - 无损复制情况下，master意外宕机，重启后发现有binlog没传到slave上面，这部分binlog怎么办？？？
       - 1 宕机时已经切成异步了， 
       - 2 是宕机时还没切成异步？
- 复制延时 基本是ms级别
   - 5.5 是单线程复制，
   - 5.6 是多库复制（对于单库或者单表的并发操作是没用的）， 
   - 5.7 是真正意义的多线程复制，它的原理是基于group commit，只要master上面的事务是group commit的，那slave上面也可以通过多个worker线程去并发执行。



- cpu飙到500%，排查问题：
  - 先top看是不是mysql导致的，
  - show processlist，执行时间长的线程
      - 显示的信息时来自information_schema.processlist表，所以这个Id就是这个表的主键。
  - explain sql看看执行计划是否准确，index是否消失。



- mysql使用自增主键的可能问题


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

### mysql 的两阶段提交



