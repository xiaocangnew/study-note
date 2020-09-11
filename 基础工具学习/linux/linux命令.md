### nohup命令
在关掉终端窗口或断开链接时，系统会发送SIGHUP信号给会话控制进程，该进程会转发SIGHUP信号给前台运行进程，该信号的默认动作是终止程序，从而导致任务被kill掉。
解决方式1:  nohup命令，nohup忽略SIGHUP信号，&将任务放在后台运行
解决方式2: 使用screen命令




常用screen参数
screen -S yourname -> 新建一个叫yourname的session
screen -ls -> 列出当前所有的session
screen -r yourname -> 回到yourname这个session
screen -d yourname -> 远程detach某个session
screen -d -r yourname -> 结束当前session并回到yourname这个session