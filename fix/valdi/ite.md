#### 2.start
### 2.1 Allocation Configuration 分配
### 2.1 
### 2.1 公司行动调整
### 2.1 用户配置
### 2.1 创建组
### 2.1 vender group
### 2.1 级别检查
### 2.1 购买力限制
### 2.1  逐步配置
### 2.1  搜索标的
可以根据Symbol/SEDOL/CUSIP/ISIN来搜索
SEDOL(Stock Exchange Daily Official List)同一个证券在每个不同的交易所都有不同的代码，因此如果我们看到交易报告里的SEDOL代码，就知道是在哪个交易所交易的
ISIN是国际合作的一套证券代码系统,ISIN有三部非组成，前两位字母是两位字母的ISO国家代码，后面九位是证券代码，最后一位是数字校验位
CUSIP(Committee onUniform Security Identification Procedures)，即统一证券识别程序委员会。CUSIP系统的拥有者是美国银行家协会，具体的运营公司是标准普尔,CUSIP码就是北美当地使用的证券识别码
FIGI(FinancialInstrument Global Identifier),相对于CUSIP和ISIN来说，FIGI的覆盖范围更广，对于没有标准识别码的工具而言是不可或缺的，例如贷款、场外衍生品、大宗商品等
#### 3.config ite
#### 4.creating order
### 常用名词
股票信息

参数 | 说明
---|--- 
Bid | 最高买价
Ask | 最低卖价
Hs Ask | highest sell
open ask |lowest sell
ETB | (Easy to Borrow) status Y/N
HTB | (HARD to Borrow) status Y/N
Pos | position
Post | post number of security
Panel | panel number of security
Noop Price| 纳斯达克官方开盘价
AMQ | 可用加价数量
NASDAQ Volatility Guard in effect | 
LULD Indicator | 个股动态涨跌幅限制机制（Limit-Up/Limit Down，LULD，用于替代个股熔断机制) ，A~I
Tick Pilot Group | 指定了证券所属的Tick Pilot Group。

订单信息

参数 | 说明
---|--- 
Send | 订单发送目的地
Broker | broker识别码
CstAcc | 客户账号
Accumulated Method | 执行报告是每次都发送还是汇总成一条发送
Booking unit | 指示执行是累积还是逐个打印  0=每次， 1=订单汇总，trade是每次记录 2=全部汇总
Handle Inst | manual/auto
Solicited Flag  | 带有SC和UN选项的多选组合框。
OE Trd#  | 进入trade 的trader
Pre-Post Manning  | 开盘前和开盘后有无人值守
ExecInst  | 订单的执行说明
DayBookingInst  | 
Memo  | 订单备忘录
Exch  | exchange name
Clr Bkr   | 清算方的code
Qind   | 订单的行情指标
Inst   | 订单的一些说明，大部分是关于时间的
AON   | 订单是否属于类别 Y/N
Trigger Inst | 订单止损触发说明
Retail Investor | 与Tick Pilot 证券相关的行情和交易豁免
Wic | 在电汇代码中输入客户分支目的地
Corr | 在电汇代码中输入客户分支目的地
Comm Type | 佣金类型
DNR/DNI | 
BI | blotter code
Bnet Cpty | 
Flr Bkr | floor broker code 场内经纪人code
PFD Mkt Mkr | 指示订单是否已路由到首选做市商。 可以根据订单的大小和安全性自动设置此路线
MarkUpDown | 输入加价或减价
Acc Rst | 订单中的公司信息
Bunchind | 订单上的切换按钮
E Min Qty | 订单执行的最小数量
Execute Timer | 自动刷新间隔
R<->M | R：订单执行自动刷新 M：禁止自动刷新
Spl Inst | 订单的特殊指令
Send Qty | 发送到外部的订单数量
Stop Ind | 设置为Y，可以启动订单止损数量和价格
Aggregate Desk ID  | 将几个id和并起来
Trail Amt | amount other than price
Investment Officer  | 项目负责人
Rebate | 减少，如果不符合标准则退款
Reference Reporting facility | 参考报告工具， 因为TRF中涉及的组织不同
targetSubID | 用于标识要接收消息的个人或单位的分配值。 保留“ ADMIN”用于非用户的管理消息。
Max Floor | 在交易大厅中显示的最大数量
Verbal Order | 口头命令
###4.1 总览
###4.2 安全信息
###4.3 订单信息 93
###4.4 快速面板 99
###4.4 action buttion 100
###4.5 智能订单 102
###4.6 期权
###4.7 现金订单
现金订单即不指定数量和价格，只指定总金额；使用时设置普通订单的type=money；


###4.8 orderType
###4.9 buy/sell
买卖订单可以是客户买卖订单，机构购买订单或经销商买卖订单。
Customer Buy Order/ Institutional Buy Order / Dealer Buy Order
###4.10 short sell order
###4.11 cross order
允许创建交叉订单， 在系统中输入交叉订单后，将创建相同数量和价格的一个买入和卖出订单。
###4.12 sell short exempt
类似与sell short订单，但是不受卖空规则约束；
###4.13 Institutional Cross
###4.14 stop order
###4.15 net order
net order是一种交易类型，在这，价格是包含了佣金的；
Cstprice 是包含佣金的价格
###4.16 Ticket Entry window
提供了很多特性，可以减少重复工作，加速订单的创建
#### 5.executing order
#### 6.viewing trading activity
#### 7.DMA trading
#### 8.advanced 
#### 9.IOI
#### 10. options