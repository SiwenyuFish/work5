package siwenyu.server.handler;


import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import siwenyu.config.NettyConfig;
import siwenyu.pojo.ChatMessage;
import siwenyu.pojo.User;
import siwenyu.server.NettyServer;
import siwenyu.server.service.ChatMessageService;
import siwenyu.server.service.GroupService;
import siwenyu.server.service.UserService;
import siwenyu.pojo.Group;
import siwenyu.utils.JwtUtil;
import siwenyu.utils.SnowFlakeUtil;


import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 一旦连接，第一个被执行
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的客户端链接：[{}]", ctx.channel().id().asLongText());

        // 添加到channelGroup 通道组
        NettyConfig.getChannelGroup().add(ctx.channel());
    }

    /**
     * 读取数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        //接收的消息
        Map map = JSON.parseObject(msg.text(), Map.class);
        String type = map.get("type").toString();
        switch (type){
            case "1":   // 登录
                websocketLogin(map,ctx);
                break;
            case "2": // 发送对话消息
                send(map,ctx);
                break;
            case "3"://查看和某个人的聊天记录
                searchChatRecord(map,ctx);
                break;
            case "4"://向指定群聊发送消息
                sendToGroup(map,ctx);
                break;
            case "5":
                searchGroupRecord(map,ctx);
                break;
        }
       log.info(String.format("收到客户端%s的数据：%s", ctx.channel().id(), msg.text()));
    }

    private void searchGroupRecord(Map map,ChannelHandlerContext ctx){

        String groupname =map.get("groupname").toString();

        //获取发送者的名称
        AttributeKey<String> key = AttributeKey.valueOf("username");
        String from = ctx.channel().attr(key).get();


        if (from==null){
            ctx.channel().writeAndFlush(new TextWebSocketFrame("请先认证"));
            return;
        }

        int pageNum;
        int pageSize;
        List list;
        try {
            pageNum = (int)map.get("pageNum");
            pageSize = (int)map.get("pageSize");
            list = redisTemplate.opsForList().range("[group]"+groupname + "record", pageNum, pageNum+pageSize-1);
        } catch (Exception e) {
            list = redisTemplate.opsForList().range("[group]"+groupname + "record", 0, -1);
        }

        List<ChatMessage> messages  = redisTemplate.opsForValue().multiGet(list);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(messages.toString()));
    }

    private void sendToGroup(Map map,ChannelHandlerContext ctx){

        String groupname = map.get("groupname").toString();

        Group group1=groupService.getGroupByGroupName(groupname);

        if (group1==null){
            ctx.channel().writeAndFlush(new TextWebSocketFrame("该群聊不存在"));
            return;
        }

        String content = map.get("content").toString();

        //获取发送者的名称
        AttributeKey<String> key = AttributeKey.valueOf("username");
        String from = ctx.channel().attr(key).get();

        if (from==null){
            ctx.channel().writeAndFlush(new TextWebSocketFrame("请先认证"));
            return;
        }
        Long uniqueChatMessageId=SnowFlakeUtil.getSnowFlakeId();

        ChatMessage chatMessage=new ChatMessage(uniqueChatMessageId,from,"[group]"+groupname,content,LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend("groupOnline.topic","groupOnline",chatMessage);
        } catch (AmqpException e) {
            log.error("群聊在线消息写入数据库或redis失败");
        }

        Set<String> members = group1.getMembers();
        for (Iterator<String> iterator = members.iterator(); iterator.hasNext(); ) {
            String next =  iterator.next();
            if(NettyConfig.getChannelMap().containsKey(next)){
                NettyConfig.getChannel(next).writeAndFlush(new TextWebSocketFrame(content));
            } else {
                //离线消息发送
                redisTemplate.opsForList().leftPush(next+"离线群聊record",next+uniqueChatMessageId+"离线群聊天记录");
                redisTemplate.opsForValue().set(next+uniqueChatMessageId+"离线群聊天记录",chatMessage);

                ctx.channel().writeAndFlush(new TextWebSocketFrame(next+"未登录,发送离线消息"));

            }
        }

    }

    private void searchChatRecord(Map map,ChannelHandlerContext ctx){

        String to =map.get("to").toString();

        //获取发送者的名称
        AttributeKey<String> key = AttributeKey.valueOf("username");
        String from = ctx.channel().attr(key).get();


        if (from==null){
            ctx.channel().writeAndFlush(new TextWebSocketFrame("请先认证"));
            return;
        }

        int pageNum;
        int pageSize;
        List list;
        try {
            pageNum = (int)map.get("pageNum");
            pageSize = (int)map.get("pageSize");
            list = redisTemplate.opsForList().range(to + from + "record", pageNum, pageNum+pageSize-1);
            if(list==null)
                list = redisTemplate.opsForList().range(from + to + "record", pageNum, pageNum+pageSize-1);
        } catch (Exception e) {
            list = redisTemplate.opsForList().range(from + to + "record", 0, -1);
            if(list==null)
                list = redisTemplate.opsForList().range(to + from + "record", 0, -1);
        }

        List<ChatMessage> messages  = redisTemplate.opsForValue().multiGet(list);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(messages.toString()));
    }

    private void send(Map map,ChannelHandlerContext ctx) {

        //获取发送者的名称
        AttributeKey<String> key = AttributeKey.valueOf("username");
        String from = ctx.channel().attr(key).get();

        if (from == null) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("请先认证"));
            return;
        }

        String to =map.get("to").toString();

        //根据用户名查询用户
        User loginUser = userService.findByUserName(to);
        //判断该用户是否存在
        if (loginUser == null) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("该用户不存在"));
            return;
        }

        //判断有没有被屏蔽
        try {
            if((int)redisTemplate.opsForValue().get(to+"block"+from)==1){
                ctx.channel().writeAndFlush(new TextWebSocketFrame("用户"+to+"屏蔽了你,消息无法发送"));
                return;
            }
        } catch (Exception e) {
           log.info("...");
        }

        String content = map.get("content").toString();

        Long uniqueChatMessageId=SnowFlakeUtil.getSnowFlakeId();

        ChatMessage chatMessage=new ChatMessage(uniqueChatMessageId,from,to,content, LocalDateTime.now());


        //通知消息写入mysql和redis
        try {
            rabbitTemplate.convertAndSend("online.topic","online",chatMessage);
        } catch (AmqpException e) {
            log.error("在线消息写入数据库或redis失败");
        }

        if(NettyConfig.getChannelMap().containsKey(to)) {
            //在线消息
            NettyConfig.getChannel(to).writeAndFlush(new TextWebSocketFrame(content));
        }else {
            //离线消息发送
            //通知离线消息写入redis
            try {
                rabbitTemplate.convertAndSend("offline.topic","offline",chatMessage);
            } catch (AmqpException e) {
                log.error("离线消息写入redis失败");
            }

            ctx.channel().writeAndFlush(new TextWebSocketFrame(to+"未登录,发送离线消息"));
        }

    }

    private void websocketLogin(Map map,ChannelHandlerContext ctx) {


        Map<String, Object> claims = null;
        try {
            String token = map.get("token").toString();
            claims = JwtUtil.parseToken(token);
        } catch (Exception e) {
            log.info("token过期");
            return;
        }
        String username = (String) claims.get("username");

        // 将用户ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
        AttributeKey<String> key = AttributeKey.valueOf("username");
        ctx.channel().attr(key).setIfAbsent(username);

        NettyConfig.getChannelMap().put(username,ctx.channel());
        NettyConfig.getChannelGroup().writeAndFlush(new TextWebSocketFrame("用户"+username+"认证成功"));


        //是否有离线消息
        //离线单聊记录获取
        List range = redisTemplate.opsForList().range(username + "离线record", 0, -1);
        List<ChatMessage> chatMessages = redisTemplate.opsForValue().multiGet(range);

        //离线群聊记录获取
        List range1 = redisTemplate.opsForList().range(username + "离线群聊record", 0, -1);
        List<ChatMessage> chatMessages1 = redisTemplate.opsForValue().multiGet(range1);


        if (chatMessages != null && !chatMessages.isEmpty())
            ctx.channel().writeAndFlush(new TextWebSocketFrame(chatMessages.toString()));

        if(chatMessages1!=null&&!chatMessages1.isEmpty())
            ctx.channel().writeAndFlush(new TextWebSocketFrame(chatMessages1.toString()));

        //删除离线消息
        redisTemplate.delete(range);
        redisTemplate.opsForList().leftPop(username + "离线record", redisTemplate.opsForList().size(username + "离线record"));

        redisTemplate.delete(range1);
        redisTemplate.opsForList().leftPop(username+"离线群聊record",redisTemplate.opsForList().size(username+"离线群聊record"));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("用户下线了:{}", ctx.channel().id().asLongText());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeUserId(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("异常：{}", cause.getMessage());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeUserId(ctx);
        ctx.close();
    }

    /**
     * 删除用户与channel的对应关系
     */
    private void removeUserId(ChannelHandlerContext ctx) {
        AttributeKey<String> key = AttributeKey.valueOf("username");
        String username = ctx.channel().attr(key).get();
        NettyConfig.getChannelMap().remove(username);
    }
}
