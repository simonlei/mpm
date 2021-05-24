package org.mpm.server.pics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mpm.server.BaseTest;
import org.mpm.server.entity.EntityFile;
import org.nutz.boot.test.junit4.NbJUnit4Runner;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

@Slf4j
@IocBean
@RunWith(NbJUnit4Runner.class)
public class FilesDataSourceTest extends BaseTest {

    @Inject
    Dao dao;
    @Inject
    FilesDataSource filesDataSource;

    @Test
    public void testChangeParent() {
        EntityFile newParent = dao.insert(EntityFile.builder().name("newParent").path("/root/newParent").build());
        EntityFile node = dao.insert(EntityFile.builder().name("node").path("/root2/node").build());
        EntityFile nodeChild = dao
                .insert(EntityFile.builder().name("child").path("/root2/node/child").parentId(node.getId()).build());
        EntityFile nodeChild2 = dao
                .insert(EntityFile.builder().name("child2").path("/root2/node/child2").parentId(node.getId()).build());
        filesDataSource.resetParentTo(node, newParent);
        assertEquals(node.getParentId(), newParent.getId());
        assertEquals("/root/newParent/node", dao.fetch(EntityFile.class, node.getId()).getPath());
        assertEquals("/root/newParent/node/child", dao.fetch(EntityFile.class, nodeChild.getId()).getPath());
        assertEquals("/root/newParent/node/child2", dao.fetch(EntityFile.class, nodeChild2.getId()).getPath());

        // set to root
        filesDataSource.resetParentTo(node, null);
        assertEquals(node.getParentId(), null);
        String newPath = dao.fetch(EntityFile.class, node.getId()).getPath();
        assertNotEquals("/root/newParent/node", newPath);
        String newSubPath = dao.fetch(EntityFile.class, nodeChild.getId()).getPath();
        assertNotEquals("/root/newParent/node/child", newSubPath);
        assertNotEquals("/root/newParent/node/child2", dao.fetch(EntityFile.class, nodeChild2.getId()).getPath());
        assertTrue(newSubPath.startsWith(newPath));
    }
}