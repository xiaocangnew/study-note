### sentinel 和 ahas
Sentinel 是核心的熔断降级组件，
AHAS（Application High Availability Service） 是基于 Sentinel 构建的云原生高可用治理平台，
二者相辅相成，共同解决微服务架构下的限流、熔断、降级等稳定性问题。

### sentinel 能力
1. 限流
     - 原理：通过滑动窗口实时统计流量，基于 QPS / 线程数等阈值
     - 支持多种限流模式：直接拒绝、预热、排队等待、匀速器
2. 降级/熔断 (基于错误率（如 5 秒内错误率 > 50%）、响应时间（如 RT>500ms）触发熔断)
     - 原理：通过状态机（闭合→打开→半开）实现故障隔
     - 熔断状态：闭合（正常）→ 打开（拒绝请求）→ 半开（试探恢复）
3. 动态配置，可通过本地文件、Nacos/Apollo 配置中心、控制台实时修改阈值，无需重启，毫秒级生效


### sentinel VS rateLimiter
1. 轻量内部接口用 RateLimiter：
      1. 解决单机内的局部流量平滑，仅支持 “匀速通过”。
      2. 仅需简单的速率控制，且流量稳定，用 RateLimiter 更轻量，避免引入 Sentinel 的复杂度。
      3. 在代码中显式调用acquire()或tryAcquire()，直接侵入业务逻辑。
2. 核心业务接口用 Sentinel：
      1. 解决跨服务的全局稳定性， 支持4种模式 (支持单独定义限流降级方法（blockHandler）和业务异常降级方法（fallback）)
      2. 对外暴露的订单接口，需要多维度保护（QPS 限流 + 线程数控制 + 熔断降级），且需动态调整规则、实时监控，用 Sentinel 更合适。
      3. 支持注解（@SentinelResource）、AOP 切面等非侵入式接入，无需修改业务代码核心逻辑；也支持手动埋点（SphU.entry()），灵活度高。