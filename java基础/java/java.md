### 为什么String是final的
1.不可变性支持字符串常量池
   字符串池的实现可以在运行时节约很多heap空间，因为不同的字符串变量都指向池中的同一个字符串。
   但如果字符串是可变的，如果变量改变了它的值，那么其它指向这个值的变量的值也会一起改变。
2.为了线程安全： 
   不可变性支持线程安全。同一个字符串实例可以被多个线程共享。这样便不用因为线程安全问题而使用同步
3. String text="abc" 将"abc"放入常量池中， 然后text指向它；
4. String text1 = new String("abc"),
     1.首先在堆中（不是常量池）创建一个指定的对象"abc"，并让str引用指向该对象
     2.在字符串常量池中查看，是否存在内容为"abc"字符串对象
     3.若存在，则将new出来的字符串对象与字符串常量池中的对象联系起来
     4.若不存在，则在字符串常量池中创建一个内容为"abc"的字符串对象，并将堆中的对象与之联系起来

### boolean字节数
boolean类型没有给出精确的定义，《Java虚拟机规范》给出了4个字节，和boolean数组1个字节的定义；
具体还要看虚拟机实现是否按照规范来，所以1个字节、4个字节都是有可能的。这其实是运算效率和存储空间之间的博弈，两者都非常的重要。

### 8种基本类型
- 整型： byte, short, int, long
- 浮点： float， double
- 字符： char
- 布尔： boolean

### final关键字的作用
1. 用来修饰一个引用
    如果引用为基本数据类型，则该引用为常量，该值无法修改；
    如果引用为引用数据类型，比如对象、数组，则该对象、数组本身可以修改，但指向该对象或数组的地址的引用不能修改。
    如果引用时类的成员变量，则必须当场赋值，否则编译会报错。
2. 当使用final修饰方法时，这个方法将成为最终方法，无法被子类重写。但是，该方法仍然可以被继承。
3. 当用final修改类时，该类成为最终类，无法被继承。简称为“断子绝孙类”。


### jdk和jre区别
JDK = JRE+Java开发工具
JRE = JVM+核心类库（libs）
JVM是用来执行字节码文件的


### [延时队列实现](https://github.com/chengxy-nds/delayqueue)
1. delayQueue；
2. rabbitmq
    - 实现方式
       1. 设置过期时间和死信队列转发；
       2. 在rabbit3.5.7提供了延时插件。开启延时插件参数后，
           通过声明一个x-delayed-message类型的exchange来使用delayed-messaging特性；
           通过在消息header中声明"x-delay"参数，延时投递。
    - 优缺点：
       1. 优点：消息持久化，分布式
       2. 缺点：1. 每一种延时就需要建立一个队列
               2. 后面的消息比前面的消息先过期，还是只能等待前面的消息过期
3. redis的Zset
     - 消息持久化，消息至少被消费一次
       实时性：存在一定的时间误差（定时任务间隔）
       支持指定消息 remove
       高可用性
       ack方案： 维护一个消息记录表，存贮消息的消费记录，用于失败时回滚消息
                定时任务轮询该消息表，处理消费记录表中消费状态未成功的记录，重新放入等待队列
       消息重试
       失败通知
4. 时间轮HashedWheelTimer = 底层数据结构使用DelayedQueue + 做时间轮的算法
     - 缺点：
          数据是保存在内存，需要自己实现持久化
          不具备分布式能力，需要自己实现高可用
          延迟任务过期时间受时间轮总间隔限制


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
   ThreadLocalMap的key是弱引用，即threadLocal是弱引用，在每次gc时，如果threadLocal =null后， 会被回收，但此时线程还没结束(例如线程池中的核心线程)，
   导致value无法被回收，内存泄漏。解决办法：用完后手动回收。
- threadLocal为什么使用弱引用
   当创建threadLocal后，会在

   
   
   
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

### ConcurrentHashMap是怎么做到线程安全的？
- 使用volatile保证当Node中的值变化时对于其他线程是可见的
- 使用table数组的头结点作为synchronized的锁来保证写操作的安全
- 当头结点为null时，使用CAS操作来保证数据能正确的写入。

### jdk1.7 <-> jdk1.8
- 结构上的变化:
1.取消原先的Segment设计，取而代之的是使用与HashMap同样的数据结构，但其内部仍然有Segment定义，但仅仅是为了保证序列化时的兼容性而已，不再有任何结构上的用处
2.哈希表+红黑树
    - 2.1. 在链表长度超过8时会将链表结构转换为红黑树，Node对象：
        为什么要是8？是一个trade-off问题，红黑树的TreeNode的空间占用是Node的二倍，是空间和时间的平衡
        链表长度达到8就转成红黑树，当长度降到6就转成普通bin。
    - 2.2. JDK1.7一上来就初始化，JDK1.8 在第一次put时才初始化
3.引入了懒加载机制。会等到第一次put方法调用时才初始化Node[]

- 线程安全方面的变化:
1.锁粒度更细:由原来的锁Segment一片区域到锁桶的头结点
2.由原先的ReentrantLock替换为Sychronized+CAS
   - 2.1. 性能上差不多(当前版本synchronized不断优化后)
   - 2.2. 使用Sychronized可以节省大量内存空间（原来ReentrantLock下的segment都得加入同步队列，
   都得继承AQS下的Node，而synchronized只是锁住头结点，头结点下边的节点都不会加入同步队列里，所以节省了空间），这是非常大的优势所在。


### 底层数据结构：
- jdk1.7
    - 其本质是一个segment[]数组,分段锁就是对segment进行，每个segment维护一个HashEntry<K,V>[]数组，扩容时只能扩单个segment下的容，整体不能扩容，在new时就订好了；
    - 这个链表是final类型不可更改。1.新增数据只能加到链表头。2.删除时只能复制删除节点前面的部分，并使得next指向删除节点的下一个节点。
- jdk1.8
    与hashMap一样

### 初始化数据结构时的线程安全
- 在jdk1.7中
1.新建时就初始化；
- 在JDK1.8中
1.初始化ConcurrentHashMap的时候这个Node[]数组是还未初始化的，会等到第一次put方法调用时才初始化：
2.如果多个线程同时调用initTable初始化Node数组会有并发问题, 初始化数组使用了乐观锁CAS操作来决定到底是哪个线程有资格进行初始化，其他线程均只能等待。
    - 用到的并发技巧：
      1.volatile变量（sizeCtl）：它是一个标记位，用来告诉其他线程这个坑位有没有人在，其线程间的可见性由volatile保证。
      2.CAS操作：CAS操作保证了设置sizeCtl标记位的原子性，保证了只有一个线程能设置成功

### put操作的线程安全
- jdk1.8
   减小锁粒度：将Node链表的头节点作为锁，若在默认大小16情况下，将有16把锁，大大减小了锁竞争，
   将串行的部分最大化缩小，在理想情况下线程的put操作都为并行操作。同时直接synchronized锁住头节点，保证了线程安全

### get操作 
- jdk1.7 不需要进行加锁
   通过Unsafe.getObjectVolatile()方法提供的原子读语义，来获得Segment以及对应的链表，然后对链表遍历判断是否存在key相同的节点以及获得该节点的value。
   由于遍历过程中其他线程可能对链表结构做了调整(调整时，由于final,所以修改会复制原来的数组，同时并行两个数组)，因此get和containsKey返回的可能是过时的数据，这一点是ConcurrentHashMap在弱一致性上的体现。如果要求强一致性，那么必须整个map加锁
- jdk1.8
   对于get操作，其实没有线程安全的问题，只有可见性的问题，只需要确保get的数据是线程之间可见的即可：
   使用了tabAt方法Unsafe类volatile的方式去获取Node数组中的Node，保证获得到的Node是最新的
   
### 扩容时的线程安全 
- [扩容操作](https://blog.csdn.net/ZOKEKAI/article/details/90051567)
    ConcurrentHashMap支持多线程并发扩容，在扩容过程中同时支持get查数据，若有线程put数据，还会帮助一起扩容，这种无阻塞算法，将并行最大化的设计
- 基础概念
   - 变量:sizeCtl
      1)map未初始化时记录的是初始容量大小
      2)在初始化过程中将sizeCtl= -1,其他线程发现该值为 -1 时会让出CPU资源以便初始化操作尽快完成
      3)正常后sizeCtl 用于记录触发集合扩容的极限值
      4)在扩容过程中记录当前扩容的并发线程数
   - 变量transferIndex
       数组上hash桶的迁移工作已经'分配到'的位置(从右到左分配)
   - 线程内迁移
      1)stride：当前线程需要迁移hash桶的个数
      2)i:当前线程迁移任务开始下标； bond：结束下标， 从右到左迁移；
      3）每个线程承担不小于 16 个槽中的元素的扩容，然后从右向左划分16个槽给当前线程去迁移，
      每当开始迁移一个槽中的元素的时候，线程会锁住当前槽中列表的头元素
   - forwardingNode
      1）标识该hash桶的数据迁移已经完成
      2）转发，遇到get操作时转发到扩容后的新数组上
   - 迁移过程中的ln（低位Node）、hn（高位Node）
      1)在put值的时候，首先会计算hash值，再散列到指定的Node数组下标中(1.得到key的hashCode; 2.使用(n - 1) & hash 运算，定位Node数组中下标值)
      2)低位链表放入原下标处，而高位链表则需要加上原Node数组长度

- 常见问题
   - 扩容如何保证线程安全        
   - 触发扩容条件：
      - 在put值时，发现Node为占位Node（fwd）时，会协助扩容
      - 在新增节点后，检测到链表长度大于8时，同时数组长度小于64。
      - 超过总容量后扩容
   - 扩容时put
      - 插入的位置扩容线程还未迁移到，直接插入
      - 只要，当迁移到该插入的位置时，就会阻塞等待插入操作完成再继续迁移 
   - 扩容时get
      - 扩容过程期间形成的hn和ln链是使用的复制引用的方式，原来 hash 桶上的链表并没有受到影响
      - 从迁移开始到迁移结束这段时间都是可以正常访问原数组hash桶上面的链表
      - 迁移结束后放置上fwd，往后的访问请求就直接转发到扩容后的数组去了
   - 扩容完成后为什么要再检查一遍
      - 因为扩容是分段进行的，当前线程扩容完，其他线程可能还没有扩容完成，需要再检查一次，而且会帮忙一起扩容

### size()的线程安全
- 基于整个ConcurrentHashMap操作,原理：
    - 首先不加锁循环执行以下操作：循环所有的Segment(通过Unsafe的getObjectVolatile()以保证原子读语义），获得对应的值以及所有Segment的modcount之和。
      如果连续两次所有Segment的modcount和相等，则过程中没有发生其他线程修改ConcurrentHashMap的情况，返回获得的值。
      当循环次数超过预定义的值时，这时需要对所有的Segment依次进行加锁，获取返回值后再依次解锁。值得注意的是，加锁过程中要强制创建所有的Segment，否则容易出现其他线程创建Segment并进行put，remove等操作。

### jdk1.7 有并发度概念，  1.8 没有
- 就是ConcurrentHashMap中的分段锁个数，即Segment[]的数组长度。
   ConcurrentHashMap默认的并发度为16。当用户设置并发度时，ConcurrentHashMap会使用大于等于该值的最小2幂指数作为实际并发度（假如用户设置并发度为17，实际并发度则为32）。
- 并发度设置的过小，会带来严重的锁竞争问题；
- 并发度设置的过大，原本位于同一个Segment内的访问会扩散到不同的Segment中，CPU cache命中率会下降，从而引起程序性能下降。

### putIfAbent() 
- ConcurrentHashMap本身是一个线程安全的容器，putIfAbent()也没有问题。
- 但是其不适合保存创建计算机资源的场景。因为计算机资源诸如：线程池、IO等都是有限的，如果还不涉及到自动回收的话就更宝贵了。
putIfAbent()每次都会创建成功，但不一定被存放到Map中，就不会被使用到，就造成了浪费。

### linkedList和arrayList线程不安全， copyOnWriteArrayList是线程安全的。
操作不是原子性的，也没有锁来保护，所以是线程不安全的。


### HashMap 的线程不安全
- 扩容在jdk1.7中会导致死循环
    扩容过程中使用头插法将oldTable中的单链表中的节点插入到newTable的单链表中(put时使用的也是头插法，最近经常使用的放在最上面)
    所以newTable中的单链表会倒置oldTable中的单链表。
    那么在多个线程同时扩容的情况下就可能导致扩容后的HashMap中存在一个有环的单链表，
   从而导致后续执行get操作的时候，会触发死循环，引起CPU的100%问题。
- 在jdk1.8 时可能发生put覆盖；
   1. 正好hash到同一个位置上，正常会产生链表，但是多线程时直接丢失。
   2. 在扩容时使用尾插法， 不会存在死锁问题；

- [jdk1.7头插法进行扩容实现死锁](https://my.oschina.net/u/4305379/blog/4529330)
   两个线程都进行扩容， 一个线程扩容完毕后，另一个线程再从中断处开始扩容，会形成环；
void transfer(Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
        for (Entry<K,V> e : table) {
            while(null != e) {
            	//1， 获取旧表的下一个元素
                Entry<K,V> next = e.next; //提前保存e的下一个节点，下面e.next要更改；
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key);
                }
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];  // e的下一个节点为newTable[i]， e为新的头节点；
                newTable[i] = e;   //把新的头节点放到数组位置上；
                e = next;     //循环
            }
        }
    }


### hashMap的resize()
- 1. 什么时候扩容
       当前元素个数 > 数组容量 * loadFactor
- 2. 扩容后容量翻倍，长度为什么是2的n次方
       1.当数组长度为2的n次幂的时候，不同的key算得得index相同的几率较小，那么数据在数组上分布就比较均匀，也就是说碰撞的几率小，
       相对的，查询的时候就不用遍历某个位置上的链表，这样查询效率也就较高了
       2.空间浪费，如果不是16，而是15， 那么 e&(n-1)时，15-1的二进制位上有0出现，导致有几个位置永远不能放元素。
- 3. 扩容的时机
       初始化后首次插入数据时，先发生resize扩容再插入数据，
       之后每当插入的数据时，是先插入数据再resize。
- 4. 元素迁移
    - jdk1.7
      1. 在准备好新的数组后，map会遍历数组的每个“桶”，然后遍历桶中的每个Entity，重新计算其hash值（也有可能不计算），
      2. 找到新数组中的对应位置，以头插法插入新的链表， 链表会逆序。(多线程可能死循环)
    - jdk1.8(性能提升很大)
       1. 经过rehash之后，元素的位置要么是在原位置，要么是在(原位置+oldCap)位置。。
          因此，在扩容时，不需要重新计算元素的hash了，只需要hash&(oldCap-1)，判断最高位是1还是0就好了，0不变，1相加
       2. JDK8在迁移元素时是正序的，不会出现链表转置的发生。
- 5. 在resize过程中put操作，结果未知，多线程不安全
 
### 请简单谈谈TreeMap 与 HashMap 的区别？
1. TreeMap实现了SortMap接口，其能够根据键排序，默认是按键的升序排序，也可以指定排序的比较器，
     Iterator遍历TreeMap时得到的记录是排过序的，
     在插入和删除操作上会有些性能损耗，
     TreeMap的键和值都不能为空，
     为非并发安全Map，
     TreeMap基于红黑树实现。
2. HashMap是最常用的Map，其基于哈希散列表实现，主要根据键的hashCode值存储数据，根据键可以直接获取它的值，具有很快的访问速度，
     Iterator遍历HashMap时得到的记录顺序是随机的，
     HashMap允许键和值均为空，
     为非并发安全 Map。


### 同步异步， 阻塞非阻塞
1. 同步异步是指线程之间的
2. 阻塞非阻塞是指线程内部的。
