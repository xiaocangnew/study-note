### [破解版方法](https://www.sojson.com/blog/283.html)
总结一句话：将charles.jar包覆盖原来的jar包，同时防止系统不识别，执行sudo spctl --master-disable命令

### 代理http：
1. 打开charles的help->local Ip address,记录ip
2. 打开charles的proxy-> proxy settings, 记录port
3. 打开手机wifi设置，代理栏设置为手动， 输入ip和port，保存
4. 开始手机联网， charles弹出是否拦截，点击allow；

### 代理https：
1.下载和安装电脑的Charles证书(help->ssl proxying-> install charles root certificate)，并设置为始终信任(钥匙串应用->左侧登陆栏->左下侧证书栏-> 找到证书并信任)
2.从Charles上获取需要在iPhone上安装的 证书网址(help->ssl proxying-> install charles root certificate on mobile device)
3.在iPhone上下载和安装证书(从步骤2中获取网址，iphone打开网址输入下载， 之后进入通用设置，进入描述符下载)，在iphone上要对证书进行信任设置（进入通用->关于本机->信任证书设置）
4.在Charles上进行 SSL代理设置(proxy->ssl proxy settings: 打开ssl proxying, 设置拦截网址：*：443，表示拦截所有网址)

