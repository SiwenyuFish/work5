package siwenyu.listener;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import siwenyu.pojo.ChatMessage;
import siwenyu.server.NettyServer;
import siwenyu.server.service.ChatMessageService;

@Component
@RequiredArgsConstructor
public class RabbitMqListener {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "offline.queue"),
            exchange = @Exchange(name = "offline.topic",type = ExchangeTypes.TOPIC),key = "offline"))
    public void listenOfflineQueue(ChatMessage chatMessage){

        String to =chatMessage.getTo();
        Long uniqueChatMessageId=chatMessage.getId();

        //离线消息发送
        redisTemplate.opsForList().leftPush(to+"离线record",uniqueChatMessageId+"离线聊天记录");
        redisTemplate.opsForValue().set(uniqueChatMessageId+"离线聊天记录",chatMessage);
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "online.queue"),
            exchange = @Exchange(name = "online.topic",type = ExchangeTypes.TOPIC),key = "online"))
    public void listenOnlineQueue(ChatMessage chatMessage){

        //保存到数据库
        chatMessageService.saveContent(chatMessage);

        log.info(chatMessage.toString());

        String to =chatMessage.getTo();
        String from=chatMessage.getFrom();
        Long uniqueChatMessageId=chatMessage.getId();

        //用户1发送给用户2 和 用户2发送给用户1 的聊天记录id存放在同一个list
        try {
            if((int)(redisTemplate.opsForValue().get(to+from))==1) {
                redisTemplate.opsForList().leftPush(to + from+"record", uniqueChatMessageId + "聊天记录");
            }
        } catch (Exception e) {
            redisTemplate.opsForValue().set(from+to,1);
            redisTemplate.opsForList().leftPush(from+to+"record", uniqueChatMessageId+"聊天记录");
        }

        //存储聊天消息
        redisTemplate.opsForValue().set(uniqueChatMessageId+"聊天记录",chatMessage);
    }


    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "groupOnline.queue"),
            exchange = @Exchange(name = "groupOnline.topic",type = ExchangeTypes.TOPIC),key = "groupOnline"))
    public void listenGroupOnlineQueue(ChatMessage chatMessage){

        chatMessageService.saveContent(chatMessage);

        log.info(chatMessage.toString());


        String groupname=chatMessage.getTo();

        Long uniqueChatMessageId= chatMessage.getId();

        redisTemplate.opsForList().leftPush(groupname+"record",uniqueChatMessageId+"群聊天记录");
        redisTemplate.opsForValue().set(uniqueChatMessageId+"群聊天记录",chatMessage);

    }


}
