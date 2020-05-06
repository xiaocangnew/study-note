基础
基本概念
Cluster （集群）
一个Cluster是由一个或者多个Node组成。
Node（节点）
一个Node一般表示一个机器，一个Node会有多个Index。
Index（索引）
一个索引可以有多个shards（分片），默认5个。
Shards
数据存储的基本单位。
Replicas
Shards的备份称为Replicas。
 
集群状态
Cluster有三种状态，Red、 Yellow、Green：
RED:Some or all of (primary) shards are not ready.
YELLOW: Elasticsearch has allocated all of the primary shards, but some/all of the replicas have not been allocated.
GREEN:  cluster is fully operational. Elasticsearch is able to allocate all shards and replicas to machines within the cluster.
通过http请求可以查看集群状态：
curl -XGET http://localhost:9200/_cluster/health?pretty=true
#response
{
  "cluster_name" : "elasticsearch",
  "status" : "yellow",
  "timed_out" : false,
  "number_of_nodes" : 1,
  "number_of_data_nodes" : 1,
  "active_primary_shards" : 1,
  "active_shards" : 1,
  "relocating_shards" : 0,
  "initializing_shards" : 0,
  "unassigned_shards" : 1,
  "delayed_unassigned_shards" : 0,
  "number_of_pending_tasks" : 0,
  "number_of_in_flight_fetch" : 0,
  "task_max_waiting_in_queue_millis" : 0,
  "active_shards_percent_as_number" : 50.0
}
Elasticsearch Head
Elasticsearch在5.X之前是Elasticsearch的一个插件，在5.4版本中，需要搭建一个单独的服务。
可以通过Elasticsearch Head服务查看集群状态。

集群搭建
配置
配置（机器）：
#cluster.name must to be unique
cluster.name: elasticsearch
node.name: ${HOSTNAME}
transport.host: localhost
transport.tcp.port: 9300
http.port: 9200
network.host: 0.0.0.0
http.cors.enabled: true
http.cors.allow-origin: "*"
bootstrap.system_call_filter: false
discovery.zen.ping.unicast.hosts: [localhost]
node.master: true
node.data: true
配置（机器）
cluster.name: elasticsearch
node.name: ${HOSTNAME}
transport.host: localhost
transport.tcp.port: 9300
http.port: 9200
network.host: 0.0.0.0
http.cors.enabled: true
http.cors.allow-origin: "*"
bootstrap.system_call_filter: false
discovery.zen.ping.unicast.hosts: [localhost]
node.data: true
node.master: false


启动时可能会有报错：
[1]: max file descriptors [65535] for elasticsearch process is too low, increase to at least [65536]
[2]: max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
针对1,vim /etc/security/limits.conf， 设置nofile >= 65536
*       -   nofile  65536
 
针对2, vim  /etc/sysctl.conf, 添加
vm.max_map_count=262144
重启机器生效（不知道有没有其他方式）。
 
Replicas策略
查看集群状态：当前两个机器，所有的shards都有两份拷贝，分别部署在两台机器上。

加入一台新的机器：
分片自动转移，任何一台机器宕机，数据依然完备。

删除最后加入的机器后，集群又变为之前的状态。
也可以提前告知elasticsearch某个ip不再使用，elasticsearch会调整数据：
curl -XPUT localhost:9200/_cluster/settings -d '{
  "transient" :{
      "cluster.routing.allocation.exclude._ip" : "10.0.0.1"
   }
}';
设置Replicas数量
对已有索引有效
curl -XPUT http://127.0.0.1:9200/_settings -d ' { "index" : { "number_of_replicas" : 0 } } '
设置模板，对所有新创建索引有效：
curl -XPUT 'localhost:9200/_template/template_1?pretty' -H 'Content-Type: application/json' -d'
{
  "template": "*",
  "settings": {
    "number_of_replicas": 0
  }
}
'
设置后的结果：

 
结论
日志不需要特别高的可用性，为了节省磁盘空间，不使用replicas。