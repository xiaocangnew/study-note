## TransactionSynchronizationManager
作用：用来管理线程相关的资源和同步

1. spring事务的实现原理：

2. 事务失效原因：
2.1. 由于代理导致的实效
      * 使用private 方法，导致无法被代理。 解决： 使用public方法
      * 同一个service中，a调用b， 解决办法： (Aop.currentProxy()).b();
2.2. 由于事务管理器导致的实效
      * 由于多个事务管理器，选择了默认导致失效； 多个事务管理器事务嵌套
      * 事务管理器使用不当导致
      * 
2.3. 由于外部导致的失效