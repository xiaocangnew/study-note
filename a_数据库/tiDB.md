### TiDB VS MySQL
特性	TiDB	MySQL
架构	分布式 HTAP 架构（水平扩展）	单机/主从架构（垂直扩展）
数据模型	支持关系型、半关系型（JSON）	纯关系型
事务模型	分布式事务（Percolator 模型）	ACID 本地事务
扩展性	水平扩展（无上限，按需添加节点）	垂直扩展（依赖单机硬件性能）
一致性	最终一致性（通过异步复制）	强一致性（同步复制需配置半数以上节点）
HTAP 能力	支持 OLTP + OLAP 混合负载	需依赖外部工具（如 ETL 或读写分离）
运维复杂度	较高（分布式系统调优、监控）	简单（成熟工具链，社区支持完善）


### 基础知识
TiDB基础的组件有TiDB-server、PD、TiKV三个，其他组件可以按需扩展，如TiFlash、TiCDC、TiSpark等。还有一些周边工具，如数据同步工具DM等，具体可参考官网：https://docs.pingcap.com/zh/tidb/dev/ecosystem-tool-user-guide
TiDB Server：SQL 层，对外暴露 MySQL 协议的连接 endpoint，负责接受客户端的连接，执行 SQL 解析和优化，最终生成分布式执行计划。TiDB 层本身是无状态的，实践中可以启动多个 TiDB 实例，通过负载均衡组件（如 LVS、HAProxy 或 F5）对外提供统一的接入地址，客户端的连接可以均匀地分摊在多个 TiDB 实例上以达到负载均衡的效果。TiDB Server 本身并不存储数据，只是解析 SQL，将实际的数据读取请求转发给底层的存储节点 TiKV（或 TiFlash）。
PD (Placement Driver) Server：整个 TiDB 集群的元信息管理模块，负责存储每个 TiKV 节点实时的数据分布情况和集群的整体拓扑结构，提供 TiDB Dashboard 管控界面，并为分布式事务分配事务 ID。PD 不仅存储元信息，同时还会根据 TiKV 节点实时上报的数据分布状态，下发数据调度命令给具体的 TiKV 节点，可以说是整个集群的“大脑”。此外，PD 本身也是由至少 3 个节点构成，拥有高可用的能力。建议部署奇数个 PD 节点。
存储节点
TiKV Server：负责存储数据，从外部看 TiKV 是一个分布式的提供事务的 Key-Value 存储引擎。存储数据的基本单位是 Region，每个 Region 负责存储一个 Key Range（从 StartKey 到 EndKey 的左闭右开区间）的数据，每个 TiKV 节点会负责多个 Region。TiKV 的 API 在 KV 键值对层面提供对分布式事务的原生支持，默认提供了 SI (Snapshot Isolation) 的隔离级别，这也是 TiDB 在 SQL 层面支持分布式事务的核心。TiDB 的 SQL 层做完 SQL 解析后，会将 SQL 的执行计划转换为对 TiKV API 的实际调用。所以，数据都存储在 TiKV 中。另外，TiKV 中的数据都会自动维护多副本（默认为三副本），天然支持高可用和自动故障转移。
TiFlash：TiFlash 是一类特殊的存储节点。和普通 TiKV 节点不一样的是，在 TiFlash 内部，数据是以列式的形式进行存储，主要的功能是为分析型的场景加速。
