package org.mpm.server.user;

import org.mpm.server.entity.EntityUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/checkPassword")
    public EntityUser checkPassword(@RequestBody EntityUser checkUser) {
        return userService.checkPassword(checkUser.getAccount(), checkUser.getPasswd());
    }

    @PostMapping("/api/createOrUpdateUser")
    public Boolean createOrUpdateUser(@RequestBody EntityUser user) {
        return userService.createOrUpdateUser(user);
    }
}
