### 反射

### [延时队列实现](https://github.com/chengxy-nds/delayqueue)
1. delayQueue；
2. rabbitmq
    设置过期时间和死信队列转发；
3. redis的Zset
4. 时间轮HashedWheelTimer = 底层数据结构使用DelayedQueue + 做时间轮的算法


### [反射效率为什么低](http://www.imooc.com/article/293679)
1.Method#invoke方法会对参数做封装和解封操作
    在进入invoke时，先将参数封装成object[]; 底层真实调用时，又需要将解封为包装前的样子；
2.需要检查方法可见性
3.需要校验参数
    反射时也必须检查每个实际参数与形式参数的类型匹配性(在NativeMethodAccessorImpl.invoke0()里或者Java版MethodAccessor.invoke 里）；
4.反射方法难以内联
    1.方法内联：JVM在运行时将调用次数达到一定阈值的方法调用替换为方法体本身，从而消除调用成本，并为接下来进一步的代码性能优化提供基础，是JVM的一个重要优化手段之一
    2.Method#invoke()就像是个独木桥一样，各处的反射调用都要挤过去，在调用点上收集到的类型信息就会很乱，
      影响内联程序的判断，使得 Method.invoke() 自身难以被内联到调用方
5.JIT(just-in-time complie)无法优化。java doc中说反射涉及到动态加载的类型，所以无法进行优化


``````
Class clazz = Class.forName("com.zy.java.RefTest");
Object refTest = clazz.newInstance();
Method method = clazz.getDeclaredMethod("refMethod");
method.invoke(refTest);
``````

- 第三步中获取getDeclaredMethod()方法：
1. 检查方法权限
2. 获取方法 Method 对象 
    2.1 流程 
           getMethod() -> getMethod0() -> getMethodsRecursive() -> privateGetDeclaredMethods()
           getDeclaredMethod ->privateGetDeclaredMethods()
    2.2 getMethodsRecursive找到对应的methodList，是一个链表结点，其 next 指向下一个结点
           1.通过 privateGetDeclaredMethods 获取自己所有的 public 方法
               - relectionData 通过缓存获取
                   在 Class 中会维护一个 ReflectionData 的软引用，作为反射数据的缓存.ReflectionData中包含了class的属性和方法；
               - 如果缓存没有命中的话，通过 getDeclaredMethods0() 从jvm中获取方法
           2.通过 MethodList#filter 查找方法名，参数相同的方法，如果找到，直接返回
           3.如果自己没有实现对应的方法，就去父类中查找对应的方法
           4.查找接口中对应的方法
    2.3 最终返回方法时会通过 MethodList#getMostSpecific 进行返回值的筛选，筛选出返回值类型最具体的方法
        
3. 返回方法的拷贝。 在copy()方法里:会 new 一个 Method 实例并返回
    这里有两点要注意：
     1.设置 root = this
     2.会给 Method 设置 MethodAccessor，用于后面方法调用。也就是所有的 Method 的拷贝都会使用同一份 methodAccessor。
                      
- 第四步中invoke()方法   
1.检查是否有权限调用方法
    这里对 override 变量进行判断，如果 override == true，就跳过检查
    我们通常在 Method#invoke 之前，会调用 Method#setAccessible(true)，就是设置 override 值为 true。
2.获取MethodAccessor对象。一共有三种MethodAccessor对象
      1.MethodAccessorImpl, 这种是java版本，通过动态字节码进行方法调用，
         效率是NativeMethodAccessorImpl的20倍以上，但是加载时消耗资源也是要高好几倍；
      2.NativeMethodAccessorImpl是Native版本的MethodAccessor实现(默认使用这个)。实现中有一个numInvocations调用次数控制，
         numInvocations大于15那么就使用Java版本的 MethodAccessorImpl。                                                            
      3.DelegatingMethodAccessorImpl 就是单纯的代理
3.调用 MethodAccessor#invoke 实现方法的调用

### threadlocal
由于ThreadLocalMap使用线性探测法来解决散列冲突，所以实际上Entry[]数组在程序逻辑上是作为一个环形存在的,否则可能导致找不到位置。
``````
//在同一个线程里
ThreadLocal threadLocal1 = new ThreadLocal();
threadLocal1.set(connection1);
threadLocal1.get();

ThreadLocal threadLocal2 = new ThreadLocal();
threadLocal2.set(connection2);
threadLocal2.get();
``````
- 底层实现原理
   1. ThreadLocalMap确实是一个map, 通过它的属性Entry[] table实现；
       set时，底层实现时set(T object) --> set(Thread t, T object) --> set(ThreadLocal local, T object)
       get时， 底层实现是get() --> get(Thread t) --> get(ThreadLocal local);
   2. ThreadLocalMap 由每个Thread保存； Thread.threadLocalMap；
   3. threadLocal就是key，有一个static AtomicInteger()变量， 每次new 一个threadLocal， 就会加1，该变量使用*0x61c88647 
      产生hashcode，使得threadLocal1 和 threadLocal2的hashcode不一样。
- 内存泄漏：
   ThreadLocalMap的key是弱引用，即threadLocal是弱引用，在每次gc时会被回收，但此时线程还没结束(例如线程池中的核心线程)，
    导致value无法被回收，内存泄漏。解决办法：用完后手动回收。
   
   
   
### hash算法
- 常用的hash算法：
1. 直接定址法： 
     取关键字的某个线性函数为散列地址：Hash（Key）= A*Key + B。 
2. 除留余数法 :  p的选取非常关键
     如果知道Hash表的最大长度为m，可以取不大于m的最大质数p，然后对关键字进行取余运算，Hash(key)=key%p。 
3. 平方取中法 
     对关键字进行平方运算，然后取结果的中间几位作为Hash地址。假如有以下关键字序列{421，423，436}，平方之后的结果为{177241，178929，190096}，那么可以取{72，89，00}作为Hash地址。
4. 折叠法 
     将关键字拆分成几部分，然后将这几部分组合在一起，以特定的方式进行转化形成Hash地址。假如知道图书的ISBN号为8903-241-23，可以将Hash(key)=89+03+24+12+3作为Hash地址。
     
- hash冲突时使用的方法：
1. 开放定址法
    1.1 线性探测。 遇到冲突后直接探测后面的地址。 (会造成元素聚集现象，降低查找效率)
    1.2 平方探测。
    1.3 双散列探测。
2. 链地址法