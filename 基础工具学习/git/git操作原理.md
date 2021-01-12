### [git官方文档](https://git-scm.com/book/zh/v2)

###本地已经有了现成的项目，需要将此项目push到远端gitlab的服务器上。具体操作步骤如下：  
1.在gitlab上创建好对应的project  
2.初始化本地项目: git init  
3.关联远程仓库:git remote add origin git@xxx.xxx.xxx:xxx/xxx.git  
4.提交代码： git push origin master

### 分支管理
1.删除远程分支：git push origin --delete Chapater6  
2.从远端拉取分支 git checkout -b name origin/name
3.分支查看： git branch / git branch -a

### .ignore 文件
1.位置在父项目下面

### 更新远程分支列表
git remote update origin --prune


### 不同分支间的文件复制
 git checkout --patch master f.txt   将master上的f文件复制到当前分支上
 
 
### 代码回退
回退部分代码到特定分支
git reset commit-id 文件夹名称     git checkout 

### 切分支
git checkout -b commitId   基于某次提交新建一个分支


### 代码切片
- git rebase --onto A B C  / git rebase B C -- onto A
  A代表的是你实际想要将切片放到哪的分支，
  B代表切片开始分支（一定要特别注意B的开闭问题，这里rebase --onto的机制是左开右闭）

### ~和^  默认0是HEAD
- HEAD^主要是控制merge之后回退的方向
  git checkout HEAD^1~1	在主分支上回退一个快照，可以简写为git checkout HEAD^~
  git checkout HEAD^2~1	在merge的分支上回退一个快照
- HEAD~才是回退的步数
  一个"~"是一步， "～～"=="～2" 
  
  
G   H   I   J
 \ /     \ /
  D   E   F
   \  |  / \
    \ | /   |
     \|/    |
      B     C
       \   /
        \ /
         A
A = A^0
B = A^   = A^1     = A~1
C = A^2
D = A^^  = A^1^1   = A~2
E = B^2  = A^^2
F = B^3  = A^^3
G = A^^^ = A^1^1^1 = A~3
H = D^2  = B^^2    = A^^^2  = A~2^2
I = F^   = B^3^    = A^^3^
J = F^2  = B^3^2   = A^^3^2


### cherry-pick     -n no-commit   -e edit
- git cherry-pick commitId
   - 当修改的是不同的代码时，可以精确定位到commitId时提交的内容
       例如 commitA(a.txt) -> commitB(b.txt) -> commitC(c.txt),   那么pick commitB时 只会增加b.txt文件
   - 每次commit都会修改相同部分代码,这样cherry-pick中间某个提交时，会有全部内容，引起冲突，必须从第一次开始修改的commit处开始pick
       例如 commitA(a.txt) -> commitB(a.txt) -> commitC(a.txt), 那么pick commitB时 会增加A->B的修改，需要自己解决冲突
- git cherry-pick branch
   表示将该分支自顶端起全部提交进cherry-pick
- git cherry-pick ..branchname (两分支都是基于master的)
   这些提交是branchname的祖先但不是当前分支的祖先
   

### revert
- git revert commitId  
   每次commit都会修改相同部分代码, 例如 commitA(a.txt) -> commitB(a.txt) -> commitC(a.txt)  
   那么， revertA或revertB时，需要修改冲突，revertC时不需要。
### 问题
1. 删除某一次提交  git rebase -i head~2 ,使用drop参数


### reflog
类似与电脑的垃圾回收，可以找回重置的commit； Head{0}


### git stash

### git commit --amend  -e 

### git merge --squash branch 将branch的提交合为一次提交，merge到当前分支上

### git重命名
- 远程分支重命名 (已经推送远程-且本地分支和远程分支名称相同)
  a. 重命名远程分支对应的本地分支
      git branch -m oldName newName
  b. 删除远程分支
      git push --delete origin oldName
  c. 上传新命名的本地分支
      git push origin newName
  d.把修改后的本地分支与远程分支关联
      git branch --set-upstream-to origin/newName

### 更新远程分支列表
git remote update origin -p

### [rebase](https://baijiahao.baidu.com/s?id=1633418495146592435&wfr=spider&for=pc)
- git pull --rebase 代替git pull

### git reset –-soft/hard
有时候，进行了错误的提交，但是还没有push到远程分支，想要撤销本次提交，
git reset –-soft：
  回退到某个版本，只回退了commit的信息，不会恢复到index file一级。如果还要提交，直接commit即可；
git reset -–hard：
  彻底回退到某个版本，本地的源码也会变为上一个版本的内容，撤销的commit中所包含的更改被冲掉；