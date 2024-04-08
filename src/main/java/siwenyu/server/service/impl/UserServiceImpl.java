package siwenyu.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siwenyu.mapper.UserMapper;
import siwenyu.pojo.User;
import siwenyu.server.service.UserService;
import siwenyu.utils.SnowFlakeUtil;

import java.util.HashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    /**
     * 实现输出登录用户信息
     */
    @Override
    public User findByUserName(String username) {
        User user = userMapper.findByUserName(username);
        return user;
    }


    /**
     * 实现用户注册 将用户信息保存到数据库
     */
    @Override
    public void register(String username, String password) {
        HashMap<String, Object> map=new HashMap<String,Object>();
        map.put("id", SnowFlakeUtil.getSnowFlakeId());
        map.put("username",username);
        map.put("password",password);
        userMapper.add(map);
    }


}
