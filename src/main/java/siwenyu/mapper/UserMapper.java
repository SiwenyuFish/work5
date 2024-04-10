package siwenyu.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import siwenyu.pojo.User;

import java.util.HashMap;

@Mapper
@Repository
public interface UserMapper {

    @Select("select user.id,user.username,user.password from user where username =#{username}")
    User findByUserName(String username);

    void add(HashMap<String, Object> map);

    @Insert("insert into block (user, blockeduser) VALUES (#{user},#{username})")
    void saveBlockUser(String user, String username);

    @Delete("delete from block where user=#{user} and blockeduser=#{username}")
    void removeBlockUser(String user, String username);
}
