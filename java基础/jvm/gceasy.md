### 关键指标(3个指标是trade-off， 只能3选2)
1. 吞吐量 Throughput
      = 1- gc时间/应用总时间
2. 延时 lantency
      三个方面考量：1. 平均gc时间； 2. 最大gc时间； 3. gc时间分布；
3. gc使用的cpu占比 Footprint
      1和2指标可以通过gceasy来分析，footprint需要通过其他监控工具








### 监控中关注jvm指标
1. 