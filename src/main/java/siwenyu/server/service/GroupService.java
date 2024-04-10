package siwenyu.server.service;

import siwenyu.pojo.Group;

import java.util.Set;

public interface GroupService {
    Group getGroupByGroupName(String groupname);

    void saveGroup(String groupname, Set<String> member);
}
