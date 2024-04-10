package siwenyu.controller;


import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import siwenyu.pojo.Result;
import siwenyu.server.service.GroupService;
import siwenyu.server.service.UserService;
import siwenyu.pojo.Group;
import siwenyu.utils.ThreadLocalUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/group")
@Validated
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public Result groupRegister(@Pattern(regexp = "^\\S{2,16}$") String groupname, @RequestParam Set<String>member){
        Group group =groupService.getGroupByGroupName(groupname);
        if(group==null){

            //加入自己
            Map<String,Object> map = ThreadLocalUtil.get();
            String username = (String) map.get("username");
            member.add(username);

            //遍历用户是否都存在
            for (Iterator<String> iterator = member.iterator(); iterator.hasNext(); ) {
                String next =  iterator.next();
                if (userService.findByUserName(next)==null){
                    return Result.error("用户"+next+"不存在");
                }
            }

            groupService.saveGroup(groupname,member);
            return Result.success();

        }else {
            return Result.error("群聊名已被占用");
        }
    }

}
