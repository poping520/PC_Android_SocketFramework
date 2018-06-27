# PC_Android_SocketFramework
PC Android 交互工作通信框架，用于某些需要借助Android系统来处理数据的工作。
* 客户端（PC）工作流程</br>
发送连接请求 > 收到连接成功消息 > PUSH数据 > 发送PUSH完成消息 > 收到任务完成消息 > PULL结果 > 发送PULL完成消息 > 结束程序
* 服务端（Android）工作流程</br>
收到连接请求 > 发送连接成功消息 > 收到PUSH完成消息 > 开始任务 > 发送任务完成消息 > 收到PULL完成消息 > 结束程序
