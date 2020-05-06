### Execution Report
参数 | 必需 |字段类型 | 说明
---|--- |--- | ---
StandardHeader | Y |  | MsgType=D
OrderID | Y | S |  由broker指定的唯一标识符，在单个交易日内唯一；
SecondaryOrderID | N | S | 由订单接受方指定
ClOrdID | Y | S | 
OrigClOrdID |  | S | 
ClientID | N | S | 
ExecBroker | N | S | 
ExecID | Y | S | 
ExecTransType | Y | S | 
ExecRefID | Y | S | 
ExecType | Y | S | 
OrdStatus | Y | S | 
OrdRejReason | Y | S |  0 = Broker option [BrokerCredit] 1 = Unknown symbol [UnknownSymbol] 2 = Exchange closed [ExchangeClosed] 3 = Order exceeds limit [OrderExceedsLimit]  4 = Too late to enter [TooLateToEnter] 5 = Unknown Order [UnknownOrder] 6 = Duplicate Order (for example, dupe ClOrdID) [DuplicateOrder] 7 = Duplicate of a verbally communicated order [DuplicateOfAVerballyCommunicatedOrder]  8 = Stale Order [StaleOrder]
ExecRestatementReason | Y | S | 0 = GT Corporate action [GTCorporateAction] 1 = GT renewal / restatement (no corporate action) [GTRenewal] 2 = Verbal change [VerbalChange] 3 = Repricing of order [RepricingOfOrder] 4 = Broker option [BrokerOption] 5 = Partial decline of OrderQty
Account | N | S |  retail order account
Symbol | N | S | 
SymbolSfx | N | S |  symbol额外信息
SecurityID | N | S |
IDSource | N | S | 1 = CUSIP [CUSIP] 2 = SEDOL [SEDOL] 4 = ISIN number [ISINNumber]
Side | Y | C | 1=buy, 2=sell (Equities only 5= sell short, 6=sell short exempt)
OrderQty | Y | D | 4.2协议以前是integer
OrdType | Y | c | 1=mkt, 2=lmt, 3=stop, 4 stop limit, r=stop on bid or offer; s= stop limit on bid or offer
Price | N | D | limit 类型的订单需要
StopPx | N | D |  stop/stop limit 类型订单需要
Currency | N | S |
TimeInForce | N | C | 订单时效性，默认为当日有效； 0 = Day [Day]  1 = Good Till Cancel (GTC) [GoodTillCancel] 2 = At the Opening (OPG) [AtTheOpening] 3 = Immediate or Cancel (IOC) [ImmediateOrCancel] 4 = Fill or Kill (FOK) [FillOrKill]   5 = Good Till Crossing (GTX) [GoodTillCrossing] 6 = Good Till Date [GoodTillDate]
EffectiveTime | N | S |  生效时间， GMT YYYYMMDD-HH:MM:SS.sss
ExpireDate | N | S | YYYYMMDD 当地市场决定
ExpireTime | N | S | YYYYMMDD-HH:MM:SS.sss
ExecInst | N | S | 指令说明，可包含多个，以空格分割； 1 = Not held [NotHeld]  2 = Work [Work] 5 = Held [Held] E = Do not increase - DNI [DoNotIncrease] F = Do not reduce - DNR [DoNotReduce] G = All or none - AON [AllOrNone]  U = Customer Display Instruction (Rule11Ac1-1/4) [CustomerDisplayInstruction]
LastShares | N | D |  
LastPx | N | D |  
LastMkt | N | S |  
LastCapacity | N | C |  
LeavesQty | N | D |  
CumQty | N | D |  
AvgPx | N | D |  
TradeDate | N | S |  
TransactTime | N | S | 
CommType | N | C | 1 = for each share [PerUnit] 2 = percentage [Percent] 3 = absolute [Absolute]
Commission | N | D | 费用，如果percentage,格式为小数0.05
Rule80A | N | C | 请注意，此字段的名称将更改为“ OrderCapacity”，因为Rule80A是一个非常特定于美国市场的术语。 其他世界市场也需要传达类似的信息，但通常只是美国价值的一部分。 有关此字段的特定于市场的用法，请参见按市场划分的Rule80A（称为OrderCapacity）用法。 A = Agency single order [AgencySingleOrder] I = Individual Investor, single order [IndividualInvestor]  P = Principal [Principal]
HandlInst | Y | C | 订单交易说明  1=自动交易，不介入   2= 自动交易，可介入  3 人工订单，最优执行
Text | N | S |Added to explain reason for rejection
StrikePrice | N | S | 
CFICode | N | S | 表示期权和期货，无论是看涨期权还是看跌期权以及什么是到期类型。 FIX附录6-D CFICode用法-ISO 10962金融工具分类（CFI代码）中提供了标准定义。
MaturityDate | N | S | 
Position effect | N | S | Position effect enum values required.
NoLegs | N | D | Leg group 识别，当响应时为1
LegRefID | N | S | 
LegSymbol | N | S | 
LegStrikePrice | N | D | 
LegCFICode | N | S | 
LegMaturityDate | N | S | 
LegSide | N | S | 
LegRatioQty | N | D | 
LegPrice | N | D | 
LegLastPx | N | D | 
StandardTrailer | Y | 

