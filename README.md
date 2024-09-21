### 使用方式

如服务器实行邀请制，在游戏中使用：

`/invite <Username>` - 邀请一个指定玩家

`/whoinvite <Username>` - 查看指定玩家由谁邀请

`/ireload` - 重载插件

### 示例配置文件
```
# GZSZ Invitation插件由魔大可制作
# 开源于https://github.com/wq5794250/GZSZinvitation
bypass-invite:
  enabled: true # 是否启用OP绕过功能，默认启用
  message: "作为管理员，您已成功绕过邀请检测。" # 当OP绕过邀请检测时显示的消息

invite-messages:
  invite-success: "你已经成功邀请了 %player%。"
  not-invited: "%player% 尚未被邀请！"
  invited-by: "%player% 是由 %inviter% 邀请的！"
  reload-message: "邀请记录已重新加载。"
  kick-message: "您尚未被邀请加入此服务器。" # 踢出玩家时的提示消息
```

### 示例邀请记录文件
```
  modakeWuPo: modakeWuPo 
  redmoon5428: modakeWuPo 
  <被邀请人>: <邀请人>
```

> 该项目处于测试阶段，有问题请反馈
> 
> 此插件由魔大可单人完成制作，开源于[Github](https://github.com/wq5794250/GZSZinvitation/tree/master)
