### fix协议trailer
CheckSum，字节总数，总是fix协议的最后一部分

### 心跳
参数 | 必需 | 说明
---|--- | ---
StandardHeader | Y | MsgType=0
TestReqID | N  | Test Request message时必须
StandardTrailer | Y | 

### 登录
参数 | 必需 | 说明
---|--- | ---
StandardHeader | Y | MsgType=A
EncryptMethod | N  | Always unencrypted
HeartBtInt | Y |  心跳间隔
ResetSeqNumFlag | N |  当需要重置序列号时需要 N/Y
StandardTrailer | Y |  

### 测试连接
参数 | 必需 | 说明
---|--- | ---
StandardHeader | Y | MsgType=1
TestReqID | Y  | Test Request message时必须
StandardTrailer | Y |  

### 重新发送请求
当接收方发现序列号不连续时，要求发送方重新发送

参数 | 必需 | 说明
---|--- | ---
StandardHeader | Y | MsgType=2
BeginSeqNo | Y  | 重发序列号start
EndSeqNo | Y |  重发序列号end

### 拒绝消息
由于会话的原因，导致消息接收到了，但不能正确处理，例如消息经过校验，但数据是无效的

参数 | 必需 | 说明
---|--- | ---
StandardHeader | Y | MsgType=3
RefSeqNum | Y  | 
RefTagID | N   | 
RefMsgType | N  | 
SessionRejectReason | N  | 
Text | N  | 
StandardTrailer | Y |

SessionRejectReason:
0 = Invalid Tag Number
1 = Required Tag Missing
2 = Tag not defined for this message type
3 = Undefined Tag
4 = Tag Specified without a value
5 = Value is incorrect (out of range) for this tag 6 = Incorrect data format for value
7 = Decryption Problem
8 = Signature Problem
9 = CompID Problem
10 = SendingTime accuracy problem
11 = Invalid MsgType

### 序列号重置
参数 | 必需 | 说明
---|--- | ---
StandardHeader | Y | MsgType=4
GapFillFlag | Y  | Y/N  Y=Gap Fill message, MsgSeqNum field valid; N=Sequence Reset, ignore MsgSeqNum
NewSeqNo | Y  | 
StandardTrailer | Y | 

### 退出登录
参数 | 必需 | 说明
---|--- | ---
StandardHeader | Y | MsgType=5
Text | Y  | 随便写
StandardTrailer | Y | 






















  