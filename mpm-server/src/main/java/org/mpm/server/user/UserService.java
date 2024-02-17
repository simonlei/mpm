package org.mpm.server.user;

import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityUser;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Lang;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final Dao dao;

    public UserService(Dao dao) {
        this.dao = dao;
    }

    public EntityUser checkPassword(String account, String passwd) {
        EntityUser user = dao.fetch(EntityUser.class, Cnd.where("account", "=", account));

        if (user != null) {
            String calcedPwd = Lang.sha256(passwd + user.getSalt()).toUpperCase();
            log.info("Calced pwd is {}", calcedPwd);
            if (Lang.equals(user.getPasswd(), calcedPwd)) {
                return user;
            }
        }
        return null;
    }
}
