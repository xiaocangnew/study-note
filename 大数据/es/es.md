### es VS solar
0. lucence 更像一个汽车引擎， 而es和solar是汽车；
1. solar和es都是企业级搜索应用；
2. 在已有数据查询时， solar高于es，但是属于统一数量级；
   在实时建立索引时，solar会产生io阻塞， 查询性能变差， es不会；
3. solar支持数据格式很多， es只支持json格式；


### es概念
1. cluster: 集群，es内部会有一个主节点，由选举产生；（使用自己的zen自动发现机制）
2. shards：索引分片，(类似kafka的分区) (客户端请求时，如果节点A没有数据，A会进行请求转发，并接受结果，a向客户端返回成功)
3. replicas： 索引副本，和kafka类似
4. 数据持久化



### es和mysql
1. index <-> database
2. type  <-> table
3. document <-> row
4. field <-> column


### [为什么这么快](https://blog.csdn.net/iyoly/article/details/101926831)
0. 多分片副本，提升高并发场景下的搜索速度；
1. 在底层采用了分段的存储模式，使它在读写时几乎完全避免了锁的出现，大大提升了读写性能。
2. 一旦索引被读入内核的文件系统缓存，便会留在哪里，由于其不变性。只要文件系统缓存中还有足够的空间，
   那么大部分读请求会直接请求内存，而不会命中磁盘。这提供了很大的性能提升。

### LSM树 (log structed merge 存储结构)[LSM](https://zhuanlan.zhihu.com/p/181498475)
1. LSM树的核心特点是利用顺序写来提高写性能，但因为分层的设计会稍微降低读性能，
2. B+树数据的更新会直接在原数据所在处修改对应的值，但是LSM数的数据更新是日志式的，当一条数据更新是直接append一条更新记录完成的。
3. LSM树会将所有的数据插入、修改、删除等操作记录(注意是操作记录)保存在内存之中，当此类操作达到一定的数据量后，再批量地顺序写入到磁盘当中。
   因此当MemTable达到一定大小flush到持久化存储变成SSTable后，在不同的SSTable中，可能存在相同Key的记录，当然最新的那条记录才是准确的。

LSM树有以下三个重要组成部分：
1. MemTable是在内存中的数据结构，用于保存最近更新的数据(当写数据到memtable中时，会先通过WAL的方式备份到磁盘中，以防数据因为内存掉电而丢失。)
2. Immutable MemTable是将转MemTable变为SSTable的一种中间状态（memtable满了后就会进行这个阶段）
3. SSTable(Sorted String Table)有序键值对集合，是LSM树组在磁盘中的数据结构。  (数据结构：k-v-k-v-k-v)

由于采用顺序写， 因此需要进行Compact操作(合并多个SSTable)来清除冗余的记录。
两种基本策略：size-tiered和leveled。
1. size-tiered策略具体内容是当某个规模的集合达到一定的数量时，将这些集合合并为一个大的集合。
     比如有5个50个数据的集合，那么就将他们合并为一个250个数据的集合。
     这种策略有一个缺点是当集合达到一定的数据量后，合并操作会变得十分的耗时。
2. leveled策略也是采用分层的思想，每一层限制总文件的大小。可能带来写放大(即层之间连续压缩)

### 倒排索引
1. 正排索引： id， name， sex； 倒排索引：name， id；  sex， [id, id];

### es中的倒排索引
1. 从上到下的结构：
     term index --> term dictionary --> posting list
2. es为每个field 都建立了一个倒排索引。Posting list就是一个int的数组，存储了所有符合某个term(field)的文档id
3. term dictionary， 对所有term进行排序，形成的字典；(方便二分查找)
4. term index。term dictionary 本身太大了，无法完整地放到内存里。于是就有了 term index，类似一个trie树



