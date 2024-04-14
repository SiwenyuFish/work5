# Bonus

* ## 使用RabbitMq

* ## Netty实现Websocket

#项目结构

    ├─src
    │  ├─main
    │  │  ├─java
    │  │  │  └─siwenyu
    │  │  │      │  Demo11Application.java
    │  │  │      │
    │  │  │      ├─config
    │  │  │      │      NettyConfig.java
    │  │  │      │      ProjectInitializer.java
    │  │  │      │      RedisConfig.java
    │  │  │      │      WebConfig.java
    │  │  │      │
    │  │  │      ├─controller
    │  │  │      │      ChatMessageController.java
    │  │  │      │      GroupController.java
    │  │  │      │      UserController.java
    │  │  │      │
    │  │  │      ├─interceptor
    │  │  │      │      LoginInterceptor.java
    │  │  │      │
    │  │  │      ├─listener
    │  │  │      │      RabbitMqListener.java
    │  │  │      │
    │  │  │      ├─mapper
    │  │  │      │      ChatMessageMapper.java
    │  │  │      │      GroupMapper.java
    │  │  │      │      UserMapper.java
    │  │  │      │
    │  │  │      ├─pojo
    │  │  │      │      ChatMessage.java
    │  │  │      │      Group.java
    │  │  │      │      Result.java
    │  │  │      │      User.java
    │  │  │      │
    │  │  │      ├─server
    │  │  │      │  │  NettyServer.java
    │  │  │      │  │
    │  │  │      │  ├─handler
    │  │  │      │  │      WebSocketHandler.java
    │  │  │      │  │
    │  │  │      │  └─service
    │  │  │      │      │  ChatMessageService.java
    │  │  │      │      │  GroupService.java
    │  │  │      │      │  UserService.java
    │  │  │      │      │
    │  │  │      │      └─impl
    │  │  │      │              ChatMessageServiceImpl.java
    │  │  │      │              GroupServiceImpl.java
    │  │  │      │              UserServiceImpl.java
    │  │  │      │
    │  │  │      └─utils
    │  │  │              AliOssUtil.java
    │  │  │              JwtUtil.java
    │  │  │              SnowFlakeUtil.java
    │  │  │              ThreadLocalUtil.java
    │  │  │
    │  │  └─resources
    │  │      │  application.yml
    │  │      │
    │  │      ├─mybatis
    │  │      │  └─mapper
    │  │      │          UserMapper.xml
