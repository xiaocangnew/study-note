https://blog.csdn.net/abc86319253/article/details/53020432
https://blog.csdn.net/zhushuai1221/article/details/79635505
https://www.cnblogs.com/jasonZh/p/8735649.html

- expireAfterAccess: 当缓存项在指定的时间段内没有被读或写就会被回收。
  

- expireAfterWrite：当缓存项在指定的时间段内没有更新就会被回收。
  - 每次更新之后的指定时间让缓存失效，然后重新加载缓存。
  - guava cache会严格限制只有1个加载操作，这样会很好地防止缓存失效的瞬间大量请求穿透到后端引起雪崩效应
  - 限制只有1个加载操作时进行加锁，其他请求必须阻塞等待这个加载操作完成；
   在加载完成之后，其他请求的线程会逐一获得锁，去判断是否已被加载完成，
   每个线程必须轮流地走一个“”获得锁，获得值，释放锁“”的过程，这样性能会有一些损耗。

- refreshAfterWrite：当缓存项上一次更新操作之后的多久会被刷新。
  - refreshAfterWrite会比expireAfterWrite性能好。
     在refresh的过程中，严格限制只有1个重新加载操作，而其他查询先返回旧值，这样有效地可以减少等待和锁争用
  - 到达指定时间后，它不能严格保证所有的查询都获取到新值。
     guava cache并没使用额外的线程去做定时清理和加载的功能，而是依赖于查询请求。
     在吞吐量很低的情况下，如很长一段时间内没有查询之后，发生的查询有可能会得到一个旧值，这将会引发问题。


- 折中方案：  
    控制缓存每1s进行refresh，如果超过2s没有访问，那么则让缓存失效，下次访问时不会得到旧值，而是必须得待新值加载

- 淘汰缓存  
  guava cache 没有使用专门的线程来进行缓存淘汰，而是在进行查询是才去判断是否过期，然后剔除;

- 使用ReferenceEntry接口来封装一个键值对，用ValueReference来封装Value值;
  之所以用Reference命令，是因为Cache要支持WeakReference Key和SoftReference、WeakReference value。


- WriteQueue和AccessQueue ：为了实现最近最少使用算法，Guava Cache在Segment中添加了两条链：write链（writeQueue）和access链（accessQueue）;
  这两条链都是一个双向链表，通过ReferenceEntry中的previousInWriteQueue、nextInWriteQueue和previousInAccessQueue、nextInAccessQueue链接而成，但是以Queue的形式表达。
  WriteQueue和AccessQueue都是自定义了offer、add（直接调用offer）、remove、poll等操作的逻辑，对offer（add）操作，如果是新加的节点，则直接加入到该链的结尾，如果是已存在的节点，则将该节点链接的链尾；对remove操作，直接从该链中移除该节点；对poll操作，将头节点的下一个节点移除，并返回。

- 在命中时： recordRead(): latencyQueueAdd; load/put/replace时： recordLockedRead(): accessQueueAdd

在没有命中时：立即进行writeQueue.remove(e);  accessQueue.remove(e);

在过期时: expireEntries(now), 同时会进行writeQueue.remove(e);  accessQueue.remove(e);

如果自己load数据，进行 storeLoadedValue()：evictEntries, writeQueueAdd， accessQueueAdd