package siwenyu.server.service;

import siwenyu.pojo.User;

public interface UserService {

    User findByUserName(String username);

    void register(String username, String password);

    void saveBlockUser(String user, String username);

    void removeBlockUser(String user, String username);
}
