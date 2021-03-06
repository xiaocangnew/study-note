###ClientAbortException: java.io.IOException: Broken pipe

看报错信息，是tomcat的connector在执行写操作的时候发生了Broken pipe异常，connector是tomcat处理网络请求的

- 只要是连接断开，再往这个断开的连接上去执行写操作，都会出现这个异常，客户端超时断开是其中的一种情况

- 查看当前tcpip连接状态：netstat -n | awk '/^tcp/ {++state[$NF]} END {for(key in state) print key,"\t",state[key]}'
    CLOSE_WAIT        3853(太多，这说明是客户端先关闭了连接，服务器端没有执行关闭连接的操作，  )
    TIME_WAIT         40
    ESTABLISHED       285
    LAST_ACT          6

- sysctl -a |grep keepalive
    net.ipv4.tcp_keepalive_time = 7200 (客户端宕机， 服务端默认2小时后才close)
    net.ipv4.tcp_keepalive_probes = 9
    net.ipv4.tcp_keepalive_intvl = 75

- 看一下进程打开的文件句柄数 
   - cat /proc/sys/fs/file-nr 系统总句柄数,
   - ls -l /proc/<pid>/fd | wc -l  当前应用打开的文件句柄数
   - 分析句柄数：
      - 统计各进程打开句柄数：lsof -n|awk '{print $2}'|sort|uniq -c|sort -nr
      - 统计各用户打开句柄数：lsof -n|awk '{print $3}'|sort|uniq -c|sort -nr
      - 统计各命令打开句柄数：lsof -n|awk '{print $1}'|sort|uniq -c|sort -nr
     

- java.net.SocketException: Broken pipe

broken pipe只出现往对端已经关闭的管道里写数据的情况下，在调用write的时候。意思是对端的管道已经断开，往往发生在远端把这个读/写管道关闭了，你无法在对这个管道进行读写操作。
从tcp的四次挥手来讲，远端已经发送了FIN序号，告诉你我这个管道已经关闭，这时候，如果你继续往管道里写数据，
  第一次往管道里write数据，你会收到一个远端发送的RST信号，
  第二次往管道里write数据，操作系统就会给你发送SIGPIPE的信号，并且将errno置为Broken pipe（32），
  第三次往管道里write数据，就会出现这个错误；
  
   - 如果你的程序默认没有对SIGPIPE进行处理，那么程序会中断退出。
      signal(SIGPIPE,SIG_IGN)函数忽略SIGPIPE信号，write会返回-1并且将errno置为Broken pipe（32）。

 在短连接情况下还好，如果是长连接情况，对于连接状态的维护不当，则非常容易出现异常。基本上对长连接需要做的就是：
a) 检测对方的主动断连（对方调用了Socket的close方法）。
  因为对方主动断连，另一方如果在进行读操作，则此时的返回值是-1。所以一旦检测到对方断连，则主动关闭己方的连接（调用 Socket 的 close 方法）。
b) 检测对方的宕机、异常退出及网络不通,一般做法都是心跳检测。
  判断对方或者宕机或者异常退出或者网络不通，此时也需要主动关闭己方连接；
  虽然Socket有一个keep alive选项来维护连接，如果用该选项，一般需要两个小时才能发现对方的宕机、异常退出及网络不通。

# RST
- 终止一个TCP连接的正常方式是发送FIN。在发送缓冲区中所有排队数据都已发送之后才发送FIN，正常情况下没有任何数据丢失。
  但我们有时也可能发送一个RST报文段而不是FIN来中途关闭一个连接。这称为异常关闭。
  
- 出现RST报文的场景：
    - connect一个不存在的端口；
    - 向一个已经关掉的连接send数据；
    - 向一个已经崩溃的对端发送数据（连接之前已经被建立）；
    - close(sockfd)时，直接丢弃接收缓冲区未读取的数据，并给对方发一个RST。这个是由SO_LINGER选项来控制的；
    - a重启，收到b的保活探针，a发rst，通知b。

- SO_LINGER是用来设置函数close()关闭TCP连接时的行为。
    - 缺省close()的行为是，如果有数据残留在socket发送缓冲区中则系统将继续发送这些数据给对方，等待被确认，然后返回。
    - zcs.socket.setSoLinger(true,0),调用close()会立即关闭该连接，
     通过发送RST分组(而不是用正常的FIN|ACK|FIN|ACK四个分组)来关闭该连接。至于发送缓冲区中如果有未发送完的数据，则丢弃。


####  java.net.SocketException: Too many open files
- files不单是文件的意思，也包括打开的通讯链接(比如socket)，正在监听的端口等等，所以有时候也可以叫做句柄(handle)，这个错误通常也可以叫做句柄数超出系统限制。
- 产生的原因：
    大多数情况是由于程序没有正常关闭一些资源引起的，所以出现这种情况，请检查io读写，socket通讯等是否正常关闭。

- 解决方式：
    - 尽量把类打成jar包，因为一个jar包只消耗一个文件句柄，如果不打包，一个类就消耗一个文件句柄。
    - java的GC不能关闭网络连接打开的文件句柄，如果没有执行 close()则文件句柄将一直存在，而不能被关闭。
    - 考虑设置socket的最大打开数来控制这个问题。对操作系统做相关的设置，增加最大文件句柄数量。
       ulimit -a 可以查看系统目前资源限制，ulimit -n 10240 则可以修改，这个修改只对当前窗口有效。

### tcp / ip 常见的异常

- java.net.SocketTimeoutException . 
  socket超时。一般有2个地方会抛出这个
    - connect时，这个超时参数由connect(SocketAddress endpoint,int timeout) 中的后者来决定，
    - 还有就是setSoTimeout(int timeout)，这个是设定读取的超时时间。它们设置成 0 均表示无限大。

- java.net.BindException:Address already in use: JVM_Bind 
   原因:port被占用。此时用 netstat –an 命令，可以看到一个 Listending 状态的端口。只需要找一个没有被占用的端口就能解决这个问题。

- java.net.ConnectException: Connection refused: connect
   该异常发生在客户端进行new Socket(ip, port)或者 zcs.socket.connect(address,timeout)操作时，
   原因:指定ip地址的机器不能找到,或者找不到指定的端口进行监听。
     首先检查客户端的ip和port是否写错了，其次从客户端ping一下服务器，最后看在服务器端的监听指定端口的程序是否启动。

- java.net.SocketException: Socket is closed 
  该异常在客户端和服务器均可能发生。异常的原因是己方主动关闭了连接后再对网络连接进行读写操作。

- java.net.SocketException: Connection reset 或者Connect reset by peer:Socket write error
connection reset by peer在调用write或者read的时候都会出现。按照glibc的说法，是such as by the remote machine rebooting or an unrecoverable protocol violation。从字面意义上来看，是表示远端机器重启或者发生不可恢复的错误。从我的测试来看，目前只出现在对端直接kill掉进程的情况。这两种情况有什么不同呢？对比tcpdump的截包图来看，直接kill掉远端进程的话，远端并没有发送FIN序号，来告诉对方，我已经关闭管道，而是直接发送了RST序号，而远端如果调用close或者shutdown的话，是会发送FIN序号的。按照TCP的四次挥手来看，是需要FIN这个序号的。个人猜测，如果在本端没有收到对方的FIN序号而直接收到了RST序号的话，表明对端出现了machine rebooting or an unrecoverable protocol violation，这时候对这个管道的IO操作，就会出现connection reset by peer错误。

该异常在客户端和服务器端均有可能发生，引起该异常的原因有两个，第一个就是假如一端的 Socket 被关闭（或主动关闭或者因为异常退出而引起的关闭）， 另一端仍发送数据，发送的第一个数据包引发该异常(Connect reset by peer)。另一个是一端退出，但退出时并未关闭该连接，另 一 端 假 如 在 从 连 接 中 读 数 据 则 抛 出 该 异 常（Connection reset）。简单的说就是在连接断开后的读和写操作引起的。

还有一种情况，如果一端发送RST数据包中断了TCP连接，另外一端也会出现这个异常，如果是tomcat，异常如下：

- Cannot assign requested address                                    
    - 端口号被占用,导致地址无法绑定：
    - java.net.BindException: Cannot assign requested address: bind：是由于IP地址变化导致的；
    - 服务器网络配置异常：/etc/hosts  中配置的地址错误;
    - 还有一种情况是执行ipconfig 发现没有环路地址，这是因为环路地址配置文件丢失了;