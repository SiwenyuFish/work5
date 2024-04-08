package siwenyu.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import siwenyu.pojo.Result;
import siwenyu.pojo.User;
import siwenyu.server.service.UserService;
import siwenyu.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 用户注册 用户名必须为2-16个字符，密码必须为6-16个字符
     * 用户名唯一
     */
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{2,16}$") String username, @Pattern(regexp = "^\\S{6,16}$") String password){
        User user=userService.findByUserName(username);
        if(user==null){
            userService.register(username,password);
            return Result.success();
        }else {
            return Result.error("用户名已被占用");
        }
    }

    /**
     * 登录 使用JwtUtil获取token
     */

    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{2,16}$") String username, @Pattern(regexp = "^\\S{6,16}$") String password) {
        //根据用户名查询用户
        User loginUser = userService.findByUserName(username);
        //判断该用户是否存在
        if (loginUser == null) {
            return Result.error("用户名错误");
        }
        //判断密码是否正确  loginUser对象中的password是密文
        if (password.equals(loginUser.getPassword())) {
            //登录成功
            StpUtil.login(loginUser.getUsername());

            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            String token = JwtUtil.genToken(claims);



            return Result.success(token);
        }
        return Result.error("密码错误");
    }

}
