### springboot-devtools 
这个依赖会进行热部署，
原理在于 Spring Boot 使用两个 classloader：不改变的类（如第三方jar）由 base 类加载器加载，
正在开发的类由 restart 类加载器加载。应用重启时，restart 类加载器被扔掉重建，而 base 类加载器不变，
这种方法意味着应用程序重新启动通常比“冷启动”快得多，因为 base 类加载器已经可用并已填充。
所以，当我们开启 devtools 后，classpath 中的文件变化会导致应用自动重启； 
所以，需要idea 开启自动编译，每当文件变化后classpath中的文件自动变化；

              