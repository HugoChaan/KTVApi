# KTV场景化Api sample demo

> 本文档主要介绍如何快速跑通 <mark>KTV场景化Api</mark> 示例工程，支持通过声网内容中心版权音乐和本地文件两种方式。
> 
> k 歌流程
>  ![ktvapi_liucheng.png](image%2Fktvapi_liucheng.png)
> Demo 效果:
![Screenshot_20230814-114931.png](image/Screenshot_20230814-114931.png)
![Screenshot_20230814-115006.png](image/Screenshot_20230814-115006.png)
---

## 1. 环境准备

- <mark>最低兼容 Android 7.0</mark>（SDK API Level 24）
- Android Studio 3.5及以上版本。
- Android 7.0 及以上的手机设备。

---

## 2. 运行示例

- 获取声网 App ID -------- [声网Agora - 文档中心 - 如何获取 App ID](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)
  > - 点击创建应用
      >
      >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)
  >
  > - 选择你要创建的应用类型
      >
      >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)
  >
  > - 得到 App ID 与 App 证书
      >
      >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/get_app_id.jpg)

- 获取 App 证书 ----- [声网Agora - 文档中心 - 获取 App 证书](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

- <mark>联系销售给 AppID 开通 K 歌权限</mark>(如果您没有销售人员的联系方式可通过智能客服联系销售人员 [Agora 支持](https://agora-ticket.agora.io/))

    - 注: 拉取榜单、歌单、歌词等功能是需要开通权限的

- 在项目的 [**local.properties**](../../local.properties) 里填写需要的声网 App ID 和 App 证书，服务器地址可以联系销售人员获取
- 
![local_config.png](image%2Flocal_config.png)

  ```
  TOOLBOX_SERVER_HOST：获取 rtc token，rtm token
  AGORA_APP_ID：声网appid
  AGORA_APP_CERTIFICATE：声网Certificate
  ```
- 用 Android Studio 运行项目即可开始您的体验

---