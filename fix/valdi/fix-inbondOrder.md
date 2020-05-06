### newOrderSingle
参数 | 必需 |字段类型 | 说明
---|--- |--- | ---
StandardHeader | Y |  | MsgType=D
ClOrdID | Y | S | 
ClientID | N | S | 
ExecBroker | N | S | 
Account | N | S |  个人账户
SettlmntTyp | N | C | 结算周期 0 = Regular [Regular], default; 1 = Cash [Cash]  2 = Next Day [NextDay] 3 = T+2 [TPlus2]  4 = T+3 [TPlus3] 5 = T+4 [TPlus4] 8 = Sellers Option [SellersOption] 9 = T+ 5 [TPlus5]
FutSettDate | N | S | SettlmntTyp = 6/8时需要， YYYYMMDD格式
HandlInst | Y | C | 订单交易说明  1=自动交易，不介入   2= 自动交易，可介入  3 人工订单，最优执行
ExecInst | N | S | 指令说明，可包含多个，以空格分割； 1 = Not held [NotHeld]  2 = Work [Work] 5 = Held [Held] E = Do not increase - DNI [DoNotIncrease] F = Do not reduce - DNR [DoNotReduce] G = All or none - AON [AllOrNone]  U = Customer Display Instruction (Rule11Ac1-1/4) [CustomerDisplayInstruction]
ExDestination | N | S |  
RouteParameters | N | S | ??
Symbol | N | S | 
SymbolSfx | N | S |  symbol额外信息
SecurityID | N | S |
IDSource | N | S | 1 = CUSIP [CUSIP] 2 = SEDOL [SEDOL] 4 = ISIN number [ISINNumber]
Side | Y | C | 1=buy, 2=sell (Equities only 5= sell short, 6=sell short exempt)
LocateReqd | N | c | 指示经纪人是否要结合卖空订单来定位股票 N/Y
TransactTime | Y | S | GMT YYYYMMDD-HH:MM:SS.sss
OrderQty | Y | D | 4.2协议以前是integer
OrdType | Y | c | 1=mkt, 2=lmt, 3=stop, 4 stop limit, r=stop on bid or offer; s= stop limit on bid or offer
OrderCapacity | N | c | 指定公司下单能力 A = Agency [Agency] G = Proprietary [Proprietary] I = Individual [Individual] P = Principal (Note for CMS purposes, Principal includes Proprietary) [Principal] R = Riskless Principal [RisklessPrincipal] W = Agent for Other Member [AgentForOtherMember]
Price | N | D | limit 类型的订单需要
StopPx | N | D |  stop/stop limit 类型订单需要
Currency | N | S |
ComplianceID | N | S | 出于合规目的用来表示此交易的ID（例如，OATS报告）
SolicitedFlag | N | C | 指示是否已请求订单 N/Y
TimeInForce | N | C | 订单时效性，默认为当日有效； 0 = Day [Day]  1 = Good Till Cancel (GTC) [GoodTillCancel] 2 = At the Opening (OPG) [AtTheOpening] 3 = Immediate or Cancel (IOC) [ImmediateOrCancel] 4 = Fill or Kill (FOK) [FillOrKill]   5 = Good Till Crossing (GTX) [GoodTillCrossing] 6 = Good Till Date [GoodTillDate]
EffectiveTime | N | S |  生效时间， GMT YYYYMMDD-HH:MM:SS.sss
ExpireDate | N | S | YYYYMMDD 当地市场决定
ExpireTime | N | S | YYYYMMDD-HH:MM:SS.sss
Commission | N | F | 费用，如果percentage,格式为小数0.05
CommType | N | S | 1 = for each share [PerUnit] 2 = percentage [Percent] 3 = absolute [Absolute] 4 = percentage waived cash discount 5 = percentage waived enhanced units 7 = basis points 8 = sales credit per unit 9 = sales credit basis points
Rule80A | N | C | 请注意，此字段的名称将更改为“ OrderCapacity”，因为Rule80A是一个非常特定于美国市场的术语。 其他世界市场也需要传达类似的信息，但通常只是美国价值的一部分。 有关此字段的特定于市场的用法，请参见按市场划分的Rule80A（称为OrderCapacity）用法。 A = Agency single order [AgencySingleOrder] I = Individual Investor, single order [IndividualInvestor]  P = Principal [Principal]
SettlCurrency | N | S |
Text | N | S |
CustomExecInst | N | S | 随便编写
CustomExecInst | N | S | 订单处理指令， 多指令之间用空格分割  D = Do not display Z = Cancel on replace
LocateID | C | S | Locate Info, to be specified if Tag-114=N
SecurityExchange | N | S | 用来帮助识别证券的市场
CountryOfIssue | N | S | 证券发行的国家/地区代码
IOIid | N | S | 利息消息的唯一标识符
MinQty | N | D | 最小执行数量
MaxFloor | N | D | 在任何给定时间要显示在交易大厅内的订单中的最大数量（例如，股票数量）。
MaturityMonthYear | N | S | 
PutOrCall | N | I |0 = Put 1 = Call
StrikePrice | N | S |
CoveredOrUncovered | N | I | 期权专用： 0 = Covered 1 = Uncovered
CustomerOrFirm | N | S |  用于在将订单交付到执行系统/交易所时用于选择，以指定订单是针对客户还是自己下订单的公司
MaturityDay | N | S |
OpenClose | N | C | 指示交易后的结果头寸应该是开仓还是平仓。 用于综合统计-帐户以总金额为基础，而不是汇总在一起
CFICode | N | S |表示期权和期货，无论是看涨期权还是看跌期权以及什么是到期类型。 FIX附录6-D CFICode用法-ISO 10962金融工具分类（CFI代码）中提供了标准定义。
MaturityDate | N | S | 
NoAllocs | N | I |
AllocAccount | N | S | Required if NoAllocs (78) > 0
AllocQty | C | D |
IndividualAllocID | C | S | 唯一allocationID
NoPartyIDs | N | I | partyID number
PartyID | Y | S | 
PartyRole | Y | I |4 = Clearing Firm  14 = Giveup Clearing Firm
PegOffsetValue | C | F |止损订单的TrailAmount / TrailPercentage值。 止损定单要求此字段，并且应大于零。
PegOffsetType | C | I | 0 = Price 1 = Percentage
LimitOffset | C | F | Limit Price for stop-loss limit orders.
StandardTrailer | Y | 




### orderCancelReplaceRequest
参数 | 必需 |字段类型 | 说明
---|--- |--- | ---
StandardHeader | Y |  | MsgType=W0
ClientID | N | S | 
ExecBroker | N | S | 
OrigClOrdID |  | S | 
ClOrdID | Y | S | 
Account | N | S |  retail order account
SettlmntTyp | N | C | 结算周期 0 = Regular [Regular], default; 1 = Cash [Cash]  2 = Next Day [NextDay] 3 = T+2 [TPlus2]  4 = T+3 [TPlus3] 5 = T+4 [TPlus4] 8 = Sellers Option [SellersOption] 9 = T+ 5 [TPlus5]
FutSettDate | N | S | SettlmntTyp = 6/8时需要， YYYYMMDD格式
HandlInst | Y | C | 订单交易说明  1=自动交易，不介入   2= 自动交易，可介入  3 人工订单，最优执行
ExecInst | N | S | 指令说明，可包含多个，以空格分割； 1 = Not held [NotHeld]  2 = Work [Work] 5 = Held [Held] E = Do not increase - DNI [DoNotIncrease] F = Do not reduce - DNR [DoNotReduce] G = All or none - AON [AllOrNone]  U = Customer Display Instruction (Rule11Ac1-1/4) [CustomerDisplayInstruction]
ExDestination | N | S |  
RouteParameters | N | S | ??
Symbol | N | S | 
SymbolSfx | N | S |  symbol额外信息
SecurityID | N | S |
IDSource | N | S | 1 = CUSIP [CUSIP] 2 = SEDOL [SEDOL] 4 = ISIN number [ISINNumber]
Side | Y | C | 1=buy, 2=sell (Equities only 5= sell short, 6=sell short exempt)
TransactTime | Y | S | GMT YYYYMMDD-HH:MM:SS.sss
OrderQty | Y | D | 4.2协议以前是integer
OrdType | Y | c | 1=mkt, 2=lmt, 3=stop, 4 stop limit, r=stop on bid or offer; s= stop limit on bid or offer
Price | N | D | limit 类型的订单需要
StopPx | N | D |  stop/stop limit 类型订单需要
Currency | N | S |
TimeInForce | N | C | 订单时效性，默认为当日有效； 0 = Day [Day]  1 = Good Till Cancel (GTC) [GoodTillCancel] 2 = At the Opening (OPG) [AtTheOpening] 3 = Immediate or Cancel (IOC) [ImmediateOrCancel] 4 = Fill or Kill (FOK) [FillOrKill]   5 = Good Till Crossing (GTX) [GoodTillCrossing] 6 = Good Till Date [GoodTillDate]
EffectiveTime | N | S |  生效时间， GMT YYYYMMDD-HH:MM:SS.sss
ExpireDate | N | S | YYYYMMDD 当地市场决定
ExpireTime | N | S | YYYYMMDD-HH:MM:SS.sss
Commission | N | F | 费用，如果percentage,格式为小数0.05
CommType | N | S | 1 = for each share [PerUnit] 2 = percentage [Percent] 3 = absolute [Absolute] 4 = percentage waived cash discount 5 = percentage waived enhanced units 7 = basis points 8 = sales credit per unit 9 = sales credit basis points
Rule80A | N | C | 请注意，此字段的名称将更改为“ OrderCapacity”，因为Rule80A是一个非常特定于美国市场的术语。 其他世界市场也需要传达类似的信息，但通常只是美国价值的一部分。 有关此字段的特定于市场的用法，请参见按市场划分的Rule80A（称为OrderCapacity）用法。 A = Agency single order [AgencySingleOrder] I = Individual Investor, single order [IndividualInvestor]  P = Principal [Principal]
SettlCurrency | N | S |
Text | N | S |
LocateReqd | N | c | 指示经纪人是否要结合卖空订单来定位股票 N/Y
CustomExecInst | N | S | 多指令间使用空格区分 D = Do not display Z = Cancel on replace
LocateID | C | S | Locate Info, to be specified if Tag-114=N
SecurityType | N | S |  用来表明证券类型
SecurityExchange | N | S | 用来帮助识别证券的市场
CountryOfIssue | N | S | 证券发行的国家/地区代码
IOIid | N | S | 利息消息的唯一标识符
MinQty | N | D | 最小执行数量
MaxFloor | N | D | 在任何给定时间要显示在交易大厅内的订单中的最大数量（例如，股票数量）。
MaturityMonthYear | N | S | 
PutOrCall | N | I |0 = Put 1 = Call
StrikePrice | N | S |
CoveredOrUncovered | N | I | 期权专用： 0 = Covered 1 = Uncovered
CustomerOrFirm | N | S |  用于在将订单交付到执行系统/交易所时用于选择，以指定订单是针对客户还是自己下订单的公司
MaturityDay | N | S |
OpenClose | N | C | 指示交易后的结果头寸应该是开仓还是平仓。 用于综合统计-帐户以总金额为基础，而不是汇总在一起
StandardTrailer | Y | 



### orderCancelRequest   
Order Cancel/Replace Request must be used to partially cancel (reduce) an order).

参数 | 必需 |字段类型 | 说明
---|--- |--- | ---
StandardHeader | Y |  | MsgType=SD
OrigClOrdID |  | S | 
ClOrdID | Y | S | 
ClientID | N | S | 
ExecBroker | N | S | 
Account | N | S |  retail order account
Symbol | N | S | 
SymbolSfx | N | S |  symbol额外信息
SecurityID | N | S |
IDSource | N | S | 1 = CUSIP [CUSIP] 2 = SEDOL [SEDOL] 4 = ISIN number [ISINNumber]
Side | Y | C | 1=buy, 2=sell (Equities only 5= sell short, 6=sell short exempt)
TransactTime | Y | S | GMT YYYYMMDD-HH:MM:SS.sss
OrderQty | Y | D | 4.2协议以前是integer
SecurityType | N | S |  用来表明证券类型
Text | N | S | 拒绝的额外原因
StandardTrailer | Y | 



### OrderStatusRequest  

参数 | 必需 |字段类型 | 说明
---|--- |--- | ---
StandardHeader | Y |  | MsgType=8
ClOrdID | Y | S | 
Symbol | N | S | 
SymbolSfx | N | S |  symbol额外信息
SecurityID | N | S |
IDSource | N | S | 1 = CUSIP [CUSIP] 2 = SEDOL [SEDOL] 4 = ISIN number [ISINNumber]
Side | Y | C | 1=buy, 2=sell (Equities only 5= sell short, 6=sell short exempt)
StandardTrailer | Y | 