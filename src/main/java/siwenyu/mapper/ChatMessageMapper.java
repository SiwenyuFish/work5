package siwenyu.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import siwenyu.pojo.ChatMessage;

@Mapper
@Repository
public interface ChatMessageMapper {

    @Insert("insert into chatmessage(`id`,`from`, `to`, content, created_at) VALUES (#{uniqueChatMessageId},#{from},#{to},#{content},now())")
    void saveContent(Long uniqueChatMessageId, String from, String to, String content);

    @Select("select chatmessage.id,chatmessage.`from`,chatmessage.`to`,chatmessage.content,chatmessage.created_at from chatmessage where chatmessage.id=#{uniqueChatMessageId}")
    ChatMessage getContent(Long uniqueChatMessageId);
}
