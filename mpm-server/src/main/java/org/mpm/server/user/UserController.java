package org.mpm.server.user;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.mpm.server.entity.EntityUser;
import org.mpm.server.util.IdParam;
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
    public EntityUser checkPassword(@RequestBody EntityUser checkUser, HttpServletResponse resp) {
        return userService.checkPassword(resp, checkUser.getAccount(), checkUser.getPasswd());
    }

    @PostMapping("/api/createOrUpdateUser")
    public Boolean createOrUpdateUser(@RequestBody EntityUser user) {
        return userService.createOrUpdateUser(user);
    }

    @PostMapping("/api/loadUsers")
    public List<EntityUser> loadUsers() {
        return userService.loadUsers();
    }

    @PostMapping("/api/deleteUser")
    public Boolean deleteUser(@RequestBody IdParam idParam) {
        return userService.deleteUser(idParam.getId());
    }

    @PostMapping("/api/loadUser")
    public EntityUser loadUser(@RequestBody IdParam idParam) {
        return userService.loadUser(idParam.getId());
    }
}
