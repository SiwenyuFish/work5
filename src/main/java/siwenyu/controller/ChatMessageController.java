package siwenyu.controller;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import siwenyu.config.NettyConfig;
import siwenyu.pojo.ChatMessage;
import siwenyu.pojo.Result;
import siwenyu.pojo.User;
import siwenyu.server.NettyServer;
import siwenyu.server.service.ChatMessageService;
import siwenyu.server.service.UserService;
import siwenyu.utils.AliOssUtil;
import siwenyu.utils.SnowFlakeUtil;
import siwenyu.utils.ThreadLocalUtil;

import java.util.Map;

@RestController
@RequestMapping("/message")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);


    @PostMapping("/picture")
    public Result upload(MultipartFile file,String to) throws Exception {

        Map<String,Object> map = ThreadLocalUtil.get();
        String from = (String) map.get("username");
        if(NettyConfig.getChannel(from)==null){
            return Result.error("请先连接websocket...");
        }

        //根据用户名查询用户
        User loginUser = userService.findByUserName(to);
        //判断该用户是否存在
        if (loginUser == null) {
            NettyConfig.getChannel(from).writeAndFlush(new TextWebSocketFrame("该用户不存在"));
            return Result.error("用户不存在");
        }

        //判断有没有被屏蔽
        try {
            if((int)redisTemplate.opsForValue().get(to+"block"+from)==1){
                NettyConfig.getChannel(from).writeAndFlush(new TextWebSocketFrame("用户"+to+"屏蔽了你,消息无法发送"));
                return Result.error("该用户屏蔽了你");
            }
        } catch (Exception e) {
            log.info("...");
        }


        String originalFilename = file.getOriginalFilename();

        //保证文件的名字是唯一的,从而防止文件覆盖
        String filename = SnowFlakeUtil.getSnowFlakeId() + originalFilename.substring(originalFilename.lastIndexOf("."));

        String url= aliOssUtil.uploadMultiFile(filename,file);

        Long uniqueChatMessageId=SnowFlakeUtil.getSnowFlakeId();


        chatMessageService.saveContent(uniqueChatMessageId,from,to,url);

        ChatMessage chatMessage=chatMessageService.getContent(uniqueChatMessageId);

        log.info(chatMessage.toString());

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


        if(NettyConfig.getChannelMap().containsKey(to)) {
            //在线消息
            NettyConfig.getChannel(to).writeAndFlush(new TextWebSocketFrame(url));
        }else {
            //离线消息发送
            redisTemplate.opsForList().leftPush(to+"离线record",uniqueChatMessageId+"离线聊天记录");
            redisTemplate.opsForValue().set(uniqueChatMessageId+"离线聊天记录",chatMessage);

            NettyConfig.getChannel(from).writeAndFlush(new TextWebSocketFrame(to+"未登录,发送离线消息"));
        }


        return Result.success();
    }


}
