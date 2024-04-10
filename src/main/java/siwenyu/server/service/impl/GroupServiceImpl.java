package siwenyu.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siwenyu.mapper.GroupMapper;
import siwenyu.server.service.GroupService;
import siwenyu.pojo.Group;
import siwenyu.utils.SnowFlakeUtil;

import java.util.HashSet;
import java.util.Set;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Override
    public Group getGroupByGroupName(String groupname) {
        String members = groupMapper.getMembersByGroupName(groupname);
        if (members == null)
            return null;
        String[] member = members.substring(1, members.length() - 1).split(",");
        Set<String> memberSet =new HashSet<>();
        for (String s : member) {
            memberSet.add(s.trim());
        }
        return new Group(groupname, memberSet);
    }

    @Override
    public void saveGroup(String groupname, Set<String> member) {
        Long snowFlakeId = SnowFlakeUtil.getSnowFlakeId();
        groupMapper.saveGroup(snowFlakeId,groupname,member.toString());

    }
}
