# MShell
一个[Mirai](https://github.com/mamoe/mirai)机器人插件，用来把QQ聊天窗口变成Shell透传来远程控制电脑或者服务器

可以利用QQ的聊天功能，远程开启一个Shell窗口，然后就可以做任何事情了

可以用来日常简单维护一下文件什么的，或者打几个指令什么的

适合内网的机器使用，这样就不用单独设置内网穿透了

也可以借助Linux的screen命令来管理Minecraft后台（没有实测过，大概是可以的）

因为关闭QQ聊天窗口不会断开与服务器Shell的连接，所以你可以写个Python程序，然后用MShell做个定时提醒什么的

![preview.png](https://i.loli.net/2021/09/15/Sq8yQR7tiTW3B6a.png)

支持会话共享，多人之间可以分享控制权（请看下面的GIF演示）

![shared-session.gif](https://i.loli.net/2021/09/14/FE5xp2juLkKVIlG.gif)

## 概念

在开始使用之前，有一些很简单的概念需要明白。

### 进程(Process)

进程是操作系统的概念，简而言之就是正在运行的程序的实例

每个进程会有一个对应的`pid`，并且不会重复也不会冲突，通过这个`pid`，我们就可以定位到任何一个进程

### 会话(Session)

在MShell中，每一个启动的子进程，都会被封装成一个个会话进行管理，会话会负责管理子进程的标准输入输出(stdin, stdout)管道，并将管道的输出通过qq消息转发给对应的qq用户，然后将qq用户发过来的文字消息，转发到输入管道中以传递给子进程。

### 连接(Connection)

每当有人连接到一个会话上时，就会产生一个与之对应的连接，当从一个会话上断开时，这个连接也会被随之销毁

每一个会话都支持多人同时连接，也就是说，多个qq可以同时操控同一个会话，连接上的所有人会共享输出与输入，但每个人同一时间只能连接到一个会话上。

### 环境预设(Preset)

环境预设的作用很像Linux中的bashrc或者profile文件，用来初始化子程序的运行环境

环境预设包含了子程序启动所必要的东西，比如要启动子程序文件名，工作目录，环境变量等等

> 子程序文件名通常是cmd.exe或者bash、sh。因为这样就可以利用这些shell程序执行一些其它命令啦（当然你也可以换成其它的程序）

### 用户(User)

MShell中的用户指的是QQ好友，如果和机器人不是好友关系，则不能算是用户（后文中的用户均代指QQ好友），MShell也不会响应任何非QQ好友发来的任何消息（包括陌生人、群聊、临时会话等）

不同机器人间的共同好友，也会被视作是同一用户（这点和Mirai内部的机制有区别）

## 插件指令

MShell有3个大指令，分别是：

1. `/mshell`：负责与MShell的主要功能进行交互（指令简写`/ms`）
2. `/mshelle`：负责管理MShell的环境预设（指令简写`/mse`或者`/me`）
3. `/mshella`：负责管理MShell的授权，也就是管理可以使用MShell的管理员（指令简写`/msa`或者`/ma`）

## 使用教程

### 基本教程

1. 首先将插件放到Mirai的插件目录里，然后重新启动Mirai，使其加载插件
2. 首先需要在后台创建一个环境预设，输入下面的指令
   1. 如果是Windows：`/mse add def gbk cmd`
      1. 意思是：添加一个环境预设，这个预设的名字叫`def`（名字随便取，只是我喜欢叫`def`罢了）
      2. 编码为`gbk`，Windows一般是这个，如果填其它的值可能会导致中文乱码
      3. 要运行的程序是`cmd`，通过`cmd`，我们就可以执行其它更多的指令啦，当然也可以换成`powershell`
   2. 如果是Linux：`/mse add def utf-8 bash`
      1. 意思是：添加一个环境预设，这个预设的名字叫`def`（名字随便取，只是我喜欢叫`def`罢了）
      2. 编码为`utf-8`，Linux一般是这个，如果填其它的值我也不知道会发生什么
      3. 要运行的程序是`bash`，通过`bash`，我们就可以执行其它更多的指令啦，当然也可以换成`sh`或者`zsh`
3. 这样就创建好了一个预设，接下来我们要启动一个会话（Session）或者说启动一个Shell也可以
4. 在启动会话之前，我们首先要给自己权限，没有权限的话，插件可是不会理会你的消息的
5. 首先加机器人为QQ好友，MShell只能在好友间使用，临时消息和群聊是没有用的
6. 在后台使用`/msa add <qq号>`给自己添加权限，使自己变成能使用MShell的管理员
7. 然后我们就可以启动一个会话了
    1. 一般不建议在后台里直接启动，因为后台只能输入指令，不能直接输入文字（除非使用`/ms write`指令）
8. 我们对机器人发送QQ消息`/ms open def `来启动一个新的会话
    1. 这里的`def`就是刚才创建的环境预设名了，意思是加载`def`这一套预设
    2. 如果`def`这个参数被省略掉了，也就是直接使用`/ms open`，则会使用默认的配置
    3. 第一次创建环境预设时，创建的环境预设会被自动设为默认
    4. 默认配置可以用指令或者在配置文件里修改，这样就不用每次都打一遍`def`了
9. 不出意外的话机器人就会给我们返回这些消息
    1. `Process created with pid(18844)`：表示会话启动成功了，并跟着会话的pid（后面会用到）
    2. `Connected to pid(18844)`：已经连接到这个会话上了，接下来你发送的QQ消息就会被转发给程序了（进入透传模式了）
10. 我们可以给机器人发送`dir`(Win)或者`ls`(Linux)消息，输入好后点击发送按钮，机器人很快就会响应我们输入，然后返回给我们对应的消息
11. 用完之后，直接关闭聊天窗口并不能结束这个会话，而是要给机器人发送`exit`(Win或者Linux通用)消息来退出这个Shell，如果遇到卡死无法退出的情况，可以使用后面的指令来强制结束这个会话的运行

### 快速断开和重连

直接关闭聊天窗口后，与会话的连接并没有切断，再次打开聊天窗口后，还是能继续刚才的操作的。

可有时候，我就是想要临时切出来，再开别的会话进行操作怎么办，或者说程序卡死了，无法通过`exit`正常退出怎么办，其实也有办法，只需要给机器人发送一个戳一戳消息（PC端叫窗口抖动），马上就能断开与这个会话连接，然后就可以输入其它指令新开一个会话或者强制干掉卡死的会话了

如果在断开会话时使用戳一戳消息（PC端叫窗口抖动）的话，会快速重连回刚刚断开的会话（但是断开期间的消息你是收不到的，相当于漏掉了），如果刚刚的会话已经结束或者退出了，也就是说没法再重连了的话，就会快速新开一个会话供你使用（以默认环境预设），相当于使用`/ms open`指令一样

>  小技巧：可以一上来就对机器人使用戳一戳消息（PC端叫窗口抖动）来快速启动一个会话（这样就不用输入`/ms open`了），但是如果你想要启动非默认环境预设的话，那么还是要手动用使用指令`/ms open <preset>`来启动的

### 共享会话

同一个会话，可以在多个QQ用户之间共享，也就是说你可以和朋友连接到同一个会话上，并给他演示如何如何操作（和`screen -x`有着异曲同工之妙）

要连接到朋友的会话，需要知道那个会话的pid，如果不知道也不要紧，可以使用`/ms list`指令列出当前所有的会话和对应的pid，以及当前都有谁连接到了哪个会话上

拿到pid后，对机器人使用`/ms connect <pid>`就能连接过去了，连接后，你和朋友会共同拥有对整个会话的控制权（共享控制权），如果需要自己单独断开与这个会话的连接而不影响其他人的连接的话，使用戳一戳消息（PC端叫窗口抖动）即可，如果你用`exit`的话，会导致整个会话结束退出，你和其它所有连接上来的人都会被断开链接（因为会话结束了，与之相关的所有人的连接也会被一同断开）

### 会话管理

断开与会话的连接并不会导致会话终止，会话会转入后台继续运行，你也可以随时使用消息或者指令来恢复连接

如果遇到会话卡死无法使用`exit`退出，可以使用戳一戳消息（PC端叫窗口抖动）先断开与会话的路连接，然后使用`/ms kill <pid>`来强制结束正在运行的会话，其中pid会在断开会话的提示里中出现一次，很容易找到

每一个会话其实都是一个子进程，每一个子进程都是要占用系统资源的，所以不要接连不断地开新会话。如果不用的话，记得关掉不用会话，可以使用指令`/ms list`查看当前都有哪些会话，以及会话的pid，和当前都有哪些人连接到了哪个会话上。所有的会话，不管有没有人连接，都可以使用`/ms kill <pid>`指令强行干掉

> 注：多个机器人（Bot）之间，会话是相互共享，并没有单独做隔离。如果你作为两个机器人（指部署到同一个Mirai进程上的两个机器人）的共同好友，分别给两个机器人发送消息，你会发现两个窗口是互通的，消息是同步的（在以后的版本中可能会做隔离）

### 权限机制

MShell插件只会响应信任的用户的消息，所谓被信任的用户，在MShell里叫管理员（后文中的管理员均代指MShell管理员，并不是其它概念中的管理员）

MShell的权限机制是依赖[Mirai-Console](https://github.com/mamoe/mirai-console)的权限系统的，我们可以用Mirai-Console的权限指令去给指定用户授权使其变成管理员。虽然这样可行，但是多多少少不太方便，因为要查具体的插件id和权限名。

MShell提供了一个方便的方式来设置管理员和取消管理员，那就是使用`/mshella`指令（别名`/msa`）

1. 使用`/msa add <qq号>`：可以添加一个管理员
2. 使用`/msa remove <qq号>`：可以移除一个管理员
3. 使用`/msa list：`可以查看管理员列表

> 注：拥有`*:*`（根权限）或者`com.github.asforest.mshell:*`（MShell插件的根权限）的用户会被视作为管理员，但是不会显示在管理员列表里

当用户的管理员权限被移除时，如果用户已经连接到了一个会话，那么用户会被立即断开与会话的连接

### 使用指令发送消息

在之前的例子中，我们连接到了一个会话后，可以直接在聊天窗口里发送对应信息给会话，那么有没有办法在不连接时就往会话里发送消息呢，也是有办法的，我们可以利用指令去手动发送指令

比如使用指令`/ms writeto <pid> <text>`，就可以把`text`发送到`pid`对应的会话中啦

还有一个与之相似的指令`/ms writeto2 <pid> <text>`，这个指令的用法和上面的一样，唯一的不同就是`writeto`会在`<text>`的末尾自动跟上一个换行符，相当于按了下回车。但`writeto2 `不会自动附加换行，而是按原本的样子不做任何改动地发送到`pid`对应的会话中

一般情况下操作shell时(`cmd.exe`、`bash`、`sh`、`zsh`)，用`writeto`就很好，特殊情况下可以使用不带换行的`writeto2`

如果想仅发送一个回车怎么办呢，直接使用`/ms writeto <pid>`就好，注意后面没有`<text>`，这样发出去就是单个回车了

### Mirai控制台

除了普通QQ用户可以连接/创建会话，Mirai控制台也是可以连接/创建会话的，想不到吧。

但是Mirai控制台使用起来不太方便，一般只是特殊情况下才会使用

具体使用方式和普通用户一样，使用`/ms open [preset]`来创建，`/ms connect [pid]`来连接等等

输入控制台的每一个消息，都会被当做指令处理，也就是说没法直接给控制台绑定的会话发送消息，只能使用指令进行发送。

可以使用上面的`/ms writeto <pid> <text>`指令或者`/ms writeto2 <pid> <text>`指令进行发送，如果你不想输入`pid`的话，也可以使用`/ms write <text>`和`/ms write2 <text>`进行发送，这两个是控制台的专属指令，可以给控制台当前连接的会话发送消息，如果没有连接的话，那就只能使用`/ms writeto <pid> <text>`以手动指定pid的方式发送消息了

##  指令参考

### 主指令(/mshell)

主指令用于实现与MShell插件的大部分常规操作

```bash
# 连接到一个会话，会话使用pid指定
/mshell connect <pid>

# 断开当前会话
/mshell disconnect

# 强制断开一个会话的所有连接
/mshell disconnect <pid>

# 强制结束一个会话
/mshell kill <pid>

# 显示所有会话，输出格式：
# [0] pid: 4432: [四月浅森(123456789)]
# 0是编号不用管；4432是pid；后面括号里是当前连接的用户列表
/mshell list    

# 开启一个会话并立即连接上去
# 如果preset被省略了，则使用默认的环境预设（默认预设可以修改）
# 否则使用指定的环境预设
/mshell open [preset]

# 向当前连接的会话stdin里输出内容并换行
# 如果text被省略，那么就只发送一个换行
/mshell write [text]

# 向当前连接的会话stdin里输出内容但不换行
/mshell write2 <text>

# 向目标会话的stdin里输出内容并换行
# 如果text被省略，那么就只发送一个换行
/mshell writeto <pid> [text]

# 向目标会话的stdin里输出内容但不换行
/mshell writeto2 <pid> <text>

# 显示资源消耗情况
# Active代表正在运行的线程，一般是  会话数x2
# Queued代表正在等待的线程，一般是  0
# PoolSize代表总线程数量，一般是  会话数x2 + 1
/mshell status
```

### 环境预设指令(/mshelle)

环境预设指令用于配置环境预设

注意：所有路径分隔符均使用正斜线，不要使用反斜线

```bash
# 创建一个环境预设
# preset: 预设的名字
# charset: 字符集（Win选择gbk或者gb2312，Linux选择utf-8）
# shell：具体启动的子程序，一般是cmd.exe或者bash、sh
/mshelle add <preset> <charset> <shell>

# 设置一个环境的编码方式
# 如果charset被省略，charset就会被清空
# 清空后这个环境就不能正常启动了，需要重新设置一次charset才行
/mshelle charset <preset> [charset]

# 设置环境的工作目录
# 工作目录可以保持默认的空状态
# 如果为空，工作目录默认就是mirai的目录
/mshelle cwd <preset> [cwd]

# 切换默认的环境预设方案
# 如果preset被省略，就会输出当前使用的默认环境预设名
# 如果preset没有省略，就会设置默认环境预设名（preset必须是已存在的预设）
/mshelle def [preset]

# 设置环境的环境变量
# 如果key被省略，会输出整个env的值
# 如果value被省略，则会删除对应的key-value
/mshelle env <preset> [key] [value]

# 设置环境的初始化命令
# exec是一个指令或者说一个预先设置好的文字
# shell启动之后，就会立即发送给shell的stdin
# 可以在会话启动后自动执行某些程序什么的
# 如果exec被省略，则会禁用这个功能
/mshelle exec <preset> [exec]

# 设置会话(子进程)的入口程序(一般是shell程序)
# 如果shell被省略，shell就会被清空
# 清空后这个环境就不能正常启动了，需要重新设置一次shell才行
/mshelle shell <preset> [shell]

# 列出所有环境预设配置
# 列出当前都有哪些环境预设方案
# 如果preset被省略，会显示所有环境预设方案
# 如果preset没被省略，会显示预设名中包含preset的所有方案（可以理解为搜索）
/mshelle list [preset] 

# 从配置文件重新加载环境预设方案
# 如果你手动改了配置文件presets.yml，可以使用这个指令来强制重载
# 一般不建议直接改配置文件，很容易出错
/mshelle reload

# 删除一个环境预设
/mshelle remove <preset>
```

### 权限管理指令(/mshella)

权限管理指令用来添加、删除管理员

管理员就是有权限使用MShell插件的QQ用户

```bash
# 添加管理员
/mshella add <qq号>

# 列出所有管理员
/mshella list

# 删除管理员
/mshella remove <qq号>
```

所谓添加管理员，就是指给某个用户赋予`com.github.asforest.mshell:all`权限，因为手动给权限比较麻烦，建议使用`/mshella`及其子指令来替代这一操作

拥有`*:*`（根权限）或者`com.github.asforest.mshell:*`（MShell插件的根权限）的用户拥有比管理员更高的权限，即使没有给他单独添加管理员权限，也是能够使用MShell插件的，同时不会显示在管理员列表里！（`/mshella list`）

不建议手动给某个用户单独赋予`com.github.asforest.mshell:*`（MShell插件的根权限），建议要么给用户赋予`*:*`（根权限），要么给用户赋予`com.github.asforest.mshell:all`权限

---

`*:*`（根权限）拥有至高无上的的权利，相当于超级管理员。除了自己，不要轻易给任何人！详情可以参考[Mirai-Console的相关文档](https://github.com/mamoe/mirai-console/blob/master/docs/Permissions.md#%E6%A0%B9%E6%9D%83%E9%99%90)

另外MShell管理员权限也不要轻易给陌生人，因为MShell和系统Shell相连，权限风险远大于普通插件，如果被恶意利用，会发生更严重的后果，请严格控制权限的分配

## 配置文件参考

### presets.yml

`presets.yml`是保存着环境预设方案的配置文件，一般不建议手动修改，因为很容易出错，建议使用`/mse`系列指令来完成修改

如果一定要手动修改，可以在修改完成后，使用`/mse reload`来立即重新加载

### config.yml

`config.yml`是保存着一些MShell设置信息的文件，因为没有重载指令，如果需要手动修改的话，建议先停止Mirai再修改，然后在重新启动Mirai

此文件一般不需要修改，各项属性保持默认就好

```yaml
# stdout合批最大上限，单位为字符数（不是字节）
# 超过这个值后消息会被拆分成2条发出来
stdoutputTruncationThreshold: 512

# stdout合批等等时间，如果两个消息之间的间隔在300ms以下
# 就会被合批成一条消息发出来
# 如果消息太多以至于超过stdoutputTruncationThreshold的值
# 还是会被拆分成两条发出来
stdoutputBatchingTimeoutInMs: 300
```

