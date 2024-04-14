package siwenyu.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siwenyu.mapper.ChatMessageMapper;
import siwenyu.pojo.ChatMessage;
import siwenyu.server.service.ChatMessageService;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public void saveContent(Long uniqueChatMessageId, String from, String to, String content) {
        chatMessageMapper.saveContent(uniqueChatMessageId,from,to,content);
    }


    @Override
    public void saveContent(ChatMessage chatMessage) {
        chatMessageMapper.saveContentMq(chatMessage.getId(), chatMessage.getFrom(), chatMessage.getTo(),chatMessage.getContent(),chatMessage.getCreatedAt());
    }

    @Override
    public ChatMessage getContent(Long uniqueChatMessageId) {
        return chatMessageMapper.getContent(uniqueChatMessageId);
    }
}
