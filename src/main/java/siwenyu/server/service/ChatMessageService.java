package siwenyu.server.service;

import siwenyu.pojo.ChatMessage;

public interface ChatMessageService {
    void saveContent(Long uniqueChatMessageId,String from, String to, String content);

    ChatMessage getContent(Long uniqueChatMessageId);
}
