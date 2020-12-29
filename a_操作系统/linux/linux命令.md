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

### 配置文件的区别
- /etc/profile: 
     此文件为系统的每个用户设置环境信息,当用户第一次登录时,该文件被执行.并从/etc/profile.d目录的配置文件中搜集shell的设置.
        export PATH=$PATH:/usr/local/mysql/bin，   source  /etc/profile
- /etc/bashrc:  
    为每一个运行bash shell的用户执行此文件.当bash shell被打开时,该文件被读取.
- ~/.bash_profile: 
    每个用户都可使用该文件输入专用于自己使用的shell信息,当用户登录时,该文件仅仅执行一次!
    默认情况下,他设置一些环境变量,执行用户的.bashrc文件.  export PATH=$PATH:/usr/local/mysql/bin    source .bashrc
- ~/.bashrc: 
    该文件包含专用于你的bash shell的bash信息,当登录时以及每次打开新的shell时,该该文件被读取.
- ~/.bash_logout: 
    当每次退出系统(退出bash shell)时,执行该文件.
    
- 如果你想对bash的功能进行设置或者是定义一些别名，推荐你修改~/.bashrc文件，这样无论你以何种方式打开shell，你的配置都会生效。
而如果你要更改一些环境变量，推荐你修改~/.bash_profile文件，因为考虑到shell的继承特性，这些更改确实只应该被执行一次（而不是多次）。

### mac端设置快捷键(与Linux不太一样)：
方法一.  在.bash_profile文件末尾追加 alias redis-cli-connect = ' redis-cli -h xxx -p xxx -a "xxx" ',   source .bash_profile
方法二. .bashrc文件中追加alias命令， 在 .bash_profile文件末尾追加如下代码：if [ -f ~/.bashrc ]; then   source ~/.bashrc  fi
- 如果使用的终端是zsh，可能会有重启终端后别名无法使用的问题， 需要在~/.zshrc文件内，追加 source ~/.bash_profile

### curl命令
- get请求： 
       curl -X GET http://localhost:8080/get -d "age=12&sex=1"
- post请求   
   1.参数在head： 
        curl  -d "age=12&sex=1" http://localhost:8080/ge
   2.参数在body
        curl -H "Content-Type:application/json" -X POST --data '{"startTime":"2019-11-25 10:00:00", "endTime":"2020-05-30 10:00:00"}' http://127.0.0.1:8082/test
   
   3. 多header：
        curl --request POST 'http://10.96.93.151:8388/base/api/group/leader/info' \
        --header 'appid: wujie_mid' \
        --header 'ticket: F9ipCl0vbepuSOzjHW2+z0Ub+LQAmuM7KuZMxGUjiSw=' \
        --header 'Content-Type: application/json' \
        --data-binary '{
            "uids": [639264856623032064]
        }'

### cut 管道命令
1. -d 分割符， -d " "；
2. -c 字符， 只要选中位置的字符， -c 14-18，
3. -f 字段， 只要选中的列字段， -f 1；


### awk命令
 awk -F "," '{ if($1="abc"){print $0}else{print $1} }' access.log

### head命令
 head -n 5， 默认显示前5行 (head -n -5， 从头到最后5行)
 tail -n 5， 默认显示最后5行 (tail -n +5, 从第5行到末尾)

### wc命令
 统计， -l 统计行数， -w 统计字数， -c 字节数