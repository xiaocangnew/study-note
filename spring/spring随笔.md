### springboot-devtools 
这个依赖会进行热部署，
原理在于 Spring Boot 使用两个 classloader：不改变的类（如第三方jar）由 base 类加载器加载，
正在开发的类由 restart 类加载器加载。应用重启时，restart 类加载器被扔掉重建，而 base 类加载器不变，
这种方法意味着应用程序重新启动通常比“冷启动”快得多，因为 base 类加载器已经可用并已填充。
所以，当我们开启 devtools 后，classpath 中的文件变化会导致应用自动重启； 
所以，需要idea 开启自动编译，每当文件变化后classpath中的文件自动变化；

### 实现多语言的方式：
1。实现LocaleResolver接口，声明bean接口为"localResolver"，里面处理
2。LocaleContextHolder.getLocale()来获取具体的语言类

在进行request请求时， FrameworkServlet进行doGet()方法时，会处理LocaleContext；由dispatcherServlet进行buildContext；

### endPoint接口
endPoint 和TradeContext接口一样，里面封装了类， 然后具体的类再实现具体的类，还有helper类来实现endPoint        
RabbitListenerEndpoint接口


### [databinder](https://blog.csdn.net/f641385712/article/details/96450469)
自己debug看源码： @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")  hh是0-12小时制，23点会出错