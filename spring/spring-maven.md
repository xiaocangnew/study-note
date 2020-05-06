### 1. zcs.spring-boot-starter-parent（控制版本信息）
Spring Boot的版本仲裁中心，控制了所有依赖的版本号，
好处：以后我们导入依赖默认是不需要写版本；
### 2.zcs.spring-boot-starter
    Spring Boot的核心启动器，包含了自动配置、日志和YAML
### 3. zcs.spring-boot-starter-web
web的场景，自动帮我们引入了web模块开发需要的相关jar包
### 4. zcs.spring-boot-starter-test
springboot程序测试依赖，如果是自动创建项目默认添加




#### Unregistering JMX-exposed beans on shutdown 
报这个错误，因为springboot 内建的tomcat没有编译进项目，所以需要springboot-starter-tomcat模块， scope=compile;
或者直接引进springboot-starter-web模块；

### springboot打包报错repackage failed: Unable to find main class
使用spring-boot-maven-plugin插件， 则代码中必须有一个springboot的启动main类，如果没有main，则吧这个依赖删掉