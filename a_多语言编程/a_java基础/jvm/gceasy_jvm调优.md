### 关键指标(3个指标是trade-off， 只能3选2)
1. 吞吐量 Throughput
      = 1- gc时间/应用总时间
2. 延时 lantency
      三个方面考量：1. 平均gc时间； 2. 最大gc时间； 3. gc时间分布；
3. gc使用的cpu占比 Footprint
      1和2指标可以通过gceasy来分析，footprint需要通过其他监控工具

### gceasy中的指标
1. jvm空间占比
2. gc时间统计，平均/最大，时间分布
3. cms各个阶段(初始标记，并发标记，重新标记等)时间统计
4. cms 的gc时间统计
5. 生成对象统计
6. 内存泄漏分析
7. 连续full gc分析
8. 长停顿
9. 安全点stw
    除了gc会stw外，有其他情况也会stw：
       1. 卸载classes文件
       2. 整理内存碎片
       3. Code Deoptimization
       4. Flushing code cache
       5. Class redefinition
    为了暂停应用，需要让所有工作线程在安全点处停顿；
    -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime // 打印停顿时间
    -XX:+PrintSafepointStatistics  -XX:PrintSafepointStatisticsCount=1  //原因分析
10. gc原因分析，占比
     0. System.gc()： 手动触发GC操作。
     1. allocation failure: 新生代内存不足导致
     2. CMS： CMS GC在执行过程中的一些动作，重点关注 CMS Initial Mark 和 CMS Final Remark 两个 STW 阶段。
     3.Promotion Failure： Old区没有足够的空间分配给 Young 区晋升的对象（即使总可用内存足够大）。
     4.Concurrent Mode Failure： CMS GC运行期间，Old区预留的空间不足以分配给新的对象，此时收集器会发生退化，严重影响 GC 性能
     5.GCLocker Initiated GC： 如果线程执行在 JNI 临界区时，刚好需要进行 GC，此时 GC Locker 将会阻止GC的发生，同时阻止其他线程进入JNI 临界区，直到最后一个线程退出临界区时触发一次 GC。




### [Java中9种常见的CMS GC问题分析与解决](https://juejin.cn/post/6894500808583610382/#heading-32)
1. 频繁young gc
     1. 产生了太多朝生夕灭的对象导致需要频繁minor gc
          查看gceasy中的内存晋升速率，优化代码；
     2. 新生代空间设置的比较小
          设置old 区大小为一次major gc后存活大小的3倍即可；

2. 频繁major gc

3. 单次major gc时间过长
     1. 查看cms的6个阶段时间占比，一般是两个stw阶段时间比较长
        Final Remark的开始阶段与Init Mark处理的流程相同，但是后续多了Card Table遍历、Reference实例的清理并将其加入到 Reference 维护的 pend_list 中，
        如果要收集元数据信息，还要清理 SystemDictionary、CodeCache、SymbolTable、StringTable 等组件中不再使用的资源。
     2. [concurrent-abortable-preclean LongGC的问题](https://blog.csdn.net/flysqrlboy/article/details/88679457)

