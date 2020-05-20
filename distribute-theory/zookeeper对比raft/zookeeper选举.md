### zab协议
- ZXID通常由64位组成。
    高32位表示Epoch值，翻译过来指朝代。就是每次换了Leader之后，Epoch值都会增加1
    低32位表示事务值，每次加1。当更换leader后，置为0；       
- zab协议由4个阶段组成
1. leader选举
   在FLE算法(fastLeaderElecttion)中通过筛选具有最大LastZxid(history中最后一个提案的ZXID编号)的节点作为候选Leader，
因为具有最大LastZxid的节点肯定具有最全的提交历史。
   在FLE算法中，每个节点都只能投一张选票，只有这样才能确定过半选票的统计值，其思路就是在投票的过程中，节点之间互相交换信息，
然后更新自己手中的选票，更新的标准就是具有更新的提案号：要么具有更新的epoch，或者在相同epoch下具有更大的机器编号。
   那么这个迭代更新的过程什么时候结束呢？
首先，每一轮的选取会有一个递增的round number作为标识，这个值越高优先级越高；
其次，每一个节点都有一个状态标识自己：election/leading/fellowing，同时每个节点都知道集群中其他节点的个数，以及和他们通信的方式。
   选举刚刚开始的时候，每个节点在没有先验信息的情况下都把选票投向自己，并把这个消息发送给所有的节点，然后等待其他节点们的响应，
节点再收到这个消息的时候：
(1). 如果选票的round number比较旧，则忽略；
(2). 如果选票的round number比自己新，则更新自己的round number，并清空上一轮相关的陈旧信息，开始广播自己新的选票；
(3). 如果是在同一轮投票中：
如果收到的选票角色是election，并且该消息附带更新的提案号，则更新自己的选票并继续广播自己的选票；
如果收到的选票角色是election，但是消息的提案号比自己旧或者跟自己一样，则记录这张选票，而检查发现自己得到针对某个节点超过集群半数的选票，自己切换为leading/fellowing状态，并转入Phase Recovery；
(4). 任何时候一旦收到leading/fellowing的选票，都指明当前集群中已有有效的候选Leader了，直接更新自己切换入Phase Recovery阶段；

2. 发现全局最新事务
   这个阶段选出的准Leader(prospective leader)肯定是集群中过半数机器的投票选出的leader。
2.1 此时所有节点会把自己的F:acceptedEpoch通过followerInfo发送给自己的prospective leader，
2.2 当prospective Leader得到过半的followerInfo消息时候，会在收到消息中取出所见最大的epoch并将其递增，这样之前的Leader就不能再提交新的提案了，
2.3 然后prospective Leader再将这个新epoch通过NEWEPOCH消息发回给这些节点并等待确认。
2.4 在Fellower节点收到候选Leader发送NEWEPOCH后，将其与自己本地的acceptedEpoch对比，
如果比他们大就更新自己acceptedEpoch，并返回ACKEPOCH消息后进入同步阶段，否则切换会leader选举状态。
2.5 prospective Leader也只能在收到过半数目的ACKEPOCH才会进入同步阶段。
需要注意的是这里Fellower发送的ACKEPOCH包含了额外的重要信息——自己最新提交日志，这样候选Leader在收集ACKEPOCH的同时就知道哪个Fellower具有最新提交了，选定到这个具有最新提交的Fellower后向其同步日志.
3. 各个follower同步最新事务
    进入这个阶段后，prospective Leader已经确立了最新任期号和最新提交日志，然后他会把自己的history通过
新epoch作为NEWLEADER消息发送给所有的集群成员，集群成员更新自己currentEpoch 并按需同步history信息。
完成这个步骤后候选Fellower向Leader发送ackNewLeader消息，而候选Leader得到过半数目的ackNewLeader消息后，
会向所有的Fellower发送COMMIT并进入广播流程，而Fellower接收到COMMIT命令而完成提交后，也会切换到广播流程。
4. 正常工作流程(广播)
   到达这个阶段后，所有节点检查自己的prospective leader，如果发现它是自己，就切换到正式Leader的状态，不是这种情况的节点切换到正式Fellower的状态，
而一致性协议保证此时只可能会有一个Leader。这是整个集群稳定工作状态，稳定之后的基本流程也类似于上面提到的Propose-ACK-COMMIT的伪
2PC(与标准的2PC有区别，只需要半数同意)操作。其中2PC是指参与者将操作成败通知协调者，再由协调者根据所有参与者的反馈情报决定各参与者是否要提交操作还是中止操作。



### raft : https://zhuanlan.zhihu.com/p/91288179
- 复制状态机(Raft协议可以使得一个集群的服务器组成复制状态机)
1. 一个分布式的复制状态机系统由多个复制状态机组成；
2. 复制状态机的状态保存在一组状态变量中，状态机的变量只能通过外部命令来改变
3. 每一个状态机存储一个包含一系列指令的日志，严格按照顺序逐条执行日志中的指令，如果所有的状态机都能按照相同的日志执行指令，
那么它们最终将达到相同的状态。因此，在复制状态机模型下，只要保证了操作日志的一致性，我们就能保证该分布式系统状态的一致性。

- 为了让一致性协议变得简单可理解，Raft协议主要使用了两种策略。
1. 将复杂问题进行分解，在Raft协议中，一致性问题被分解为：leader election、log replication、safety三个简单问题；
2. 减少状态空间中的状态数目。


- 基本概念
   - 角色
        有follower， candidate， leader。 启动时所有节点都是follower。
   - time out信号 
       集群刚启动时，所有节点都是follower，之后在time out信号的驱使下，follower会转变成candidate去拉取选票，
       获得大多数选票后就会成为leader，这时候如果其他候选人发现了新的leader已经诞生，就会自动转变为follower；
       而如果另一个time out信号发出时，还没有选举出leader，将会重新开始一次新的选举。
       可见，time out信号是促使角色转换得关键因素，类似于操作系统中得中断信号。
   - term
       1.在Raft协议中，将时间分成了一些任意长度的时间片，称为term，term使用连续递增的编号的进行识别
       2.每一个term都从新的选举开始，candidate们会努力争取称为leader。一旦获胜，它就会在剩余的term时间内保持leader状态，
       在某些情况下(如term3)选票可能被多个candidate瓜分，形不成多数派，因此term可能直至结束都没有leader，
       下一个term很快就会到来重新发起选举。
       3.每一个server都存储了当前term编号，在server之间进行交流的时候就会带有该编号，如果一个server的编号小于另一个的，
       那么它会将自己的编号更新为较大的那一个；如果leader或者candidate发现自己的编号不是最新的了，就会自动转变为follower；
       如果接收到的请求的term编号小于自己的当前term将会拒绝执行。  
   - rpc
       server之间的交流是通过RPC进行的。只需要实现两种RPC就能构建一个基本的Raft集群：
         RequestVote RPC：它由选举过程中的candidate发起，用于拉取选票
         AppendEntries RPC：它由leader发起，用于复制日志或者发送心跳信号。


- 2）leader选举过程。 Raft通过心跳机制发起leader选举。
1. 节点都是从follower状态开始的，如果收到了来自leader或candidate的RPC，那它就保持follower状态，避免争抢成为candidate。
Leader会发送空的AppendEntries RPC作为心跳信号来确立自己的地位，如果follower一段时间(election timeout)没有收到心跳，
它就会认为leader已经挂了，发起新的一轮选举。
2. 选举发起后，一个follower会增加自己的当前term编号并转变为candidate。它会首先投自己一票，
然后向其他所有节点并行发起RequestVote RPC，之后candidate状态将可能发生如下三种变化:
  2.1 赢得选举,称为leader: 如果它在一个term内收到了大多数的选票，将会在接下的剩余term时间内称为leader，
       然后就可以通过发送心跳确立自己的地位。
  2.2 其他server成为leader：在等待投票时，可能会收到其他server发出AppendEntries RPC心跳信号，说明其他leader已经产生了。
       这时通过比较自己的term编号和RPC过来的term编号，如果比对方大，说明leader的term过期了，就会拒绝该RPC,并继续保持候选人身份; 
       如果对方编号不比自己小,则承认对方的地位,转为follower.
  2.3 选票被瓜分,选举失败: 如果没有candidate获取大多数选票, 则没有leader产生, candidate们等待超时后发起另一轮选举.
       为了防止下一次选票还被瓜分,必须采取一些额外的措施, raft采用随机election timeout的机制防止选票被持续瓜分.
       通过将timeout随机设为一段区间上的某个值, 因此很大概率会有某个candidate率先超时然后赢得大部分选票.

















### zookeeper VS raft  : https://blog.csdn.net/weixin_36145588/article/details/78477159