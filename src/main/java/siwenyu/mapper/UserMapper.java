package siwenyu.mapper;

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
}
