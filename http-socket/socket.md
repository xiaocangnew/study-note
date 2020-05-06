### 零拷贝技术
- 基础知识点：
   - 户态和内核态
      处于用户态执行时，能访问的内存空间和对象受到限制，占有的处理器是可被抢占的
      处于内核态执行时，能访问所有的内存空间和对象，   占有的处理器是不允许被抢占的
   - DMA（Direct Memory Access）直接内存访问
      DMA允许外设组件将I/O数据直接传送到主存储器中；并且传输不需要CPU的参与，来释放CPU时间分片资源。
   
- 优化线
   - (用户在网上浏览信息)为例收到请求后的数据流：
      磁盘文件--(DMA copy)--> 内核态ReadBuffer --(cpu copy)-->用户态AppBuffer --(cpu copy)-->内核态socketBuffer--(DMA copy)--> 网络上NicBuffer
   - 痛点：
      1. 两次cpu copy占用cpu时间
      2. 需要从内核态和用户态切换
   - Linux2.1 内核优化：
        跳过AppBuffer步骤(mmap技术,实现数据共享)，直接从ReadBuffer--(cpu copy)-->socketBuffer
   - Linux2.4 内核优化: 
        操作系统提供scatter/gather这种DMA的方式，来从内核空间缓冲区中将数据直接读取到协议引擎,消除了最后一次cpu copy
        socketBuffer不再copy信息，里面只有数据的位置和长度的信息的描述符，
   
- 注意：零拷贝其实是根据内核状态划分的，
   1. 数据在用户态的状态下，经历了零次拷贝，所以才叫做零拷贝，
   2. 不是说不拷贝。