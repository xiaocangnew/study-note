##[log日志框架](https://cloud.tencent.com/developer/information/java设置日志级别)

###logback加载过程，
当我们使用logback-classic.jar时，应用启动，那么logback会按照如下顺序进行扫描:  
1.在系统配置文件System Properties中寻找是否有logback.configurationFile对应的value
2.在classpath下寻找是否有logback.groovy（即logback支持groovy与xml两种配置方式）
3.在classpath下寻找是否有logback-test.xml
4.在classpath下寻找是否有logback.xml
  以上任何一项找到了，就不进行后续扫描，按照对应的配置进行logback的初始化，具体代码实现可见ch.qos.logback.classic.util.ContextInitializer类的findURLOfDefaultConfigurationFile方法。
当所有以上四项都找不到的情况下，logback会调用ch.qos.logback.classic.BasicConfigurator的configure方法，构造一个ConsoleAppender用于向控制台输出日志，默认日志输出格式为"%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"。

### logback.xml中的元素
####<logger>
1。用来设置某一个包或者具体某一个类的日志打印级别、以及指定<appender>：<logger name="com.zcs.alpha.site" level="DEBUG" additivity="true"/> （additivity：是否向上级logger传递打印信息，默认为true）
2。可以包含零个或者多个<appender-ref>元素，标识这个appender将会添加到这个logger
3。<root>也是<logger>元素，但是它是根logger，只有一个level属性

###<appender>
<appender>是<configuration>的子节点，是负责写日志的组件。  
<appender>有两个必要属性name和class：1。name指定<appender>的名称 2。class指定<appender>的全限定名

### <filter>
是<appender>的一个子节点，表示在当前给到的日志级别下再进行一次过滤，  
最基本的Filter有ch.qos.logback.classic.filter.LevelFilter和ch.qos.logback.classic.filter.ThresholdFilter

### 异步写日志
<!-- 异步输出 -->  
     <appender name ="ASYNC" class= "ch.qos.logback.classic.AsyncAppender">  
         <!-- 0为不丢失日志。默认值为20:如果队列的80%已满,则会丢弃TRACT、DEBUG、INFO级别的日志 -->  
         <discardingThreshold>0</discardingThreshold>  
         <!-- 更改默认的队列的深度,该值会影响性能.默认值为256 -->  
         <queueSize>256</queueSize>  
         <!-- 添加附加的appender,最多只能添加一个 -->  
         <appender-ref ref ="STDOUT"/>  
     </appender>
     
原理：
1.当我们配置了AsyncAppender，系统启动时会初始化一条名为"AsyncAppender-Worker-ASYNC"的线程
2.当Logging Event进入AsyncAppender后，AsyncAppender会调用appender方法，appender方法中再将event填入Buffer  
（使用的Buffer为BlockingQueue，具体实现为ArrayBlockingQueye）前，会先判断当前Buffer的容量以及丢弃日志特性是否开启，  
当消费能力不如生产能力时，AsyncAppender会将超出Buffer容量的Logging Event的级别进行丢弃，  
作为消费速度一旦跟不上生产速度导致Buffer溢出处理的一种方式。
3.上面的线程的作用，就是从Buffer中取出Event，交给对应的appender进行后面的日志推送
4.从上面的描述我们可以看出，AsyncAppender并不处理日志，只是将日志缓冲到一个BlockingQueue里面去，  
并在内部创建一个工作线程从队列头部获取日志，之后将获取的日志循环记录到附加的其他appender上去，  
从而达到不阻塞主线程的效果。因此AsyncAppender仅仅充当的是事件转发器，必须引用另外一个appender来做事。
