package org.mpm.server.user;

import java.security.SecureRandom;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityUser;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private static final Random RANDOM = new SecureRandom();
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
                user.setSalt("");
                user.setPasswd("");
                return user;
            }
        }
        return null;
    }

    public Boolean createOrUpdateUser(EntityUser user) {
        EntityUser existUser = user.getId() == null ? null : dao.fetch(EntityUser.class, user.getId());
        if (existUser == null) {
            user.setSalt(generateSalt());
            user.setPasswd(Lang.sha256(user.getPasswd() + user.getSalt()).toUpperCase());
            dao.insert(user, true, false, false);
        } else {
            existUser.setName(user.getName());
            existUser.setIsAdmin(user.getIsAdmin());
            existUser.setFaceId(user.getFaceId());
            if (Strings.isNotBlank(user.getPasswd())) {
                existUser.setPasswd(Lang.sha256(user.getPasswd() + existUser.getSalt()).toUpperCase());
            }
            dao.updateIgnoreNull(existUser);
        }
        return true;
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Lang.fixedHexString(salt);
    }
}
