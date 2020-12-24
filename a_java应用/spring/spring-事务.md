## TransactionSynchronizationManager
作用：用来管理线程相关的资源和同步

1. spring事务的实现原理：
TransactionAspectSupport： 辅助类，包含以下的类信息，和代理的核心实现方法； 
  TransactionDefinition： 定义事务的8个隔离级别，超时，名称等。
  TransactionAttribute， 在TransactionDefinition的基础上增加rollbackon配置；
  TransactionStatus： 保存当前事务的状态：是否是新事务的状态，是否完成的状态，是否回滚的状态，回滚点。 实现类都有具体的con。
  TransactionInfo 事务信息： 包含事务管理器，事务状态，事务隔离级别， 老得TransactionInfo等。

TransactionInterceptor 继承了TransactionAspectSupport， 是核心类；

https://www.jianshu.com/p/d4c3634447d0

http://www.itrensheng.com/archives/
spring_transactional_uneffect

https://www.jianshu.com/p/d4c3634447d0

https://blog.csdn.net/weixin_44366439/article/details/89030080

2. 事务失效原因：
2.1. 由于代理导致的实效
      * 使用private 方法，导致无法被代理。 解决： 使用public方法
      * 同一个service中，a调用b， 解决办法： (Aop.currentProxy()).b();
2.2. 由于事务管理器导致的实效
      * 由于多个事务管理器，选择了默认导致失效； 多个事务管理器事务嵌套
      * 事务管理器使用不当导致
      * 
2.3. 由于外部导致的失效

