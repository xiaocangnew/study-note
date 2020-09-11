###Hystrix的设计原则是什么
1)资源隔离（线程池隔离和信号量隔离）机制：
   限制调用分布式服务的资源使用，某一个调用的服务出现问题不会影响其它服务调用。
2)限流机制：
   限流机制主要是提前对各个类型的请求设置最高的QPS阈值，若高于设置的阈值则对该请求直接返回，不再调用后续资源
3)降级
   当失败率达到阀值自动触发降级（如因网络故障、超时造成的失败率真高），熔断器触发的快速失败会进行快速恢复
   超时降级、资源不足时（线程或信号量）降级 、运行异常降级等
4)缓存支持：
   提供了请求缓存、请求合并实现

### 初步使用
- 引入maven  spring-cloud-starter-hystrix
- main类上添加 @EnableHystrix
- 在service上添加 @HystrixCommand，注解的参数有
    - fallbackMethod指定fallback函数
    - commandProperties = { @HystrixProperty(name, value)}; property参数有：
       - execution.isolation.thread.timeoutInMilliseconds 
          指定超时时间
       - circuitBreaker.requestVolumeThreshold 
          设置在一个滚动窗口中，打开断路器的最少请求数，只有超过这个请求才会开启熔断
       - circuitBreaker.sleepWindowInMilliseconds
          熔断器工作时间，超过这个时间，先放一个请求进去，成功的话就关闭熔断，失败就再等一段时间
        

### 功能介绍
- 线程隔离
   - 线程池隔离 VS 信号量隔离   
      1. 如果不涉及远程RPC调用（没有网络开销）则使用信号量来隔离，更为轻量，开销更小。
      2. 当依赖的服务是极低延迟的，比如访问内存缓存，就没有必要使用线程池的方式，那样的话开销得不偿失，而是推荐使用信号量这种方式。
      3. 信号量不支持异步调用，且不支持超时。而线程池都支持     
- 熔断
   - Hystrix在运行过程中会向每个commandKey对应的熔断器报告成功、失败、超时和拒绝的状态，熔断器维护计算统计的数据；
     根据这些统计的信息来确定熔断器是否打开。如果打开，后续的请求都会被截断。然后会隔一段时间默认是5s，尝试半开，放入一部分流量请求进来，相当于对依赖服务进行一次健康检查，如果恢复，熔断器关闭，随后完全恢复调用
- 降级
   - 概念
      指在在Hystrix执行非核心链路功能失败的情况下，我们如何处理等。如果我们要回退或者降级处理，代码上需要实现HystrixCommand.getFallback()。
### 在项目中的使用
- quote行情模块，调用了上游csp的行情rpc服务。
    当omnibus交易相关应用以rpc形式调用quote时，quote使用了hystrix；
    
### hystrix 执行时内部流程
1. 创建commond
    一个 HystrixCommand或HystrixObservableCommand对象，代表了对某个依赖服务发起的一次请求或者调用
     1. 返回一个结果。HystrixCommand hystrixCommand = new HystrixCommand(arg1, arg2);
     2. 返回多个结果.HystrixObservableCommand command = new HystrixObservableCommand(arg1, arg2);
2. 调用 command 执行方法
     1. 执行时有同步和异步方式，底层都是通过异步，同步是通过future.get()来阻塞。
3. 检查是否开启缓存(从这一步开始进入底层)
     如果command开启了请求缓存，而且这个调用的结果在缓存中存在，那么直接从缓存中返回结果。否则，继续往后的步骤。
4. 检查是否开启了断路器
     如果断路器被打开了，就不会执行这个command，而是直接去执行fallback降级机制，返回降级结果。
        - 几种降级回退模式：
            - Fail Fast 快速失败   失败抛出异常，没有回退函数。
            - Fail Silent 无声失败
            - Static 返回默认值
            - Stubbed 自己组装一个值返回
            - Cache via Network 利用远程缓存
            - Primary + Secondary with Fallback 主次方式回退（主要和次要）
5. 检查线程池/队列/信号量是否已满
     如果满了，不会执行command，而是直接去调用fallback降级机制，同时发送reject信息给断路器统计。
6. 执行command
     1. HystrixCommand.run() 返回单条结果，或者抛出异常。
     2. HystrixObservableCommand.construct()返回一个Observable对象，可以获取多条结果。
7. 断路健康检查--断路器
   Hystrix会把每一个依赖服务的调用成功、失败、Reject、Timeout等事件发送给circuit breaker断路器。
   断路器就会对这些事件的次数进行统计，根据异常事件发生的比例来决定是否要进行断路（熔断）。
   如果打开了断路器，那么在接下来一段时间内，会直接断路，返回降级结果。
   如果在之后，断路器尝试执行command，调用没有出错，返回了正常结果，那么 Hystrix 就会把断路器关闭。

### 源码介绍
- 断路器源码
   - 关键变量
     AtomicBoolean circuitOpen // 断路器状态
     AtomicLong circuitOpenedOrLastTestedTime  //记录着断路恢复计时器的初始时间，用于Open状态向Close状态的转换
     Metrics metrics // 统计请求情况
   - singleTest()方法
      circuitOpen.get() && System.currentTimeMillis() > timeCircuitOpenedOrWasLastTested + properties.circuitBreakerSleepWindowInMilliseconds().get()
   - isOpen()方法
       断路器是否开启 -> 请求总数(totalCount)是否小于属性中的请求容量阈值 -> 检查错误比率是否小于属性中的错误百分比阈
   
   

