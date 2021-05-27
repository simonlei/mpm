package org.mpm.server.pics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mpm.server.BaseTest;
import org.mpm.server.entity.EntityFile;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class FilesDataSourceTest extends BaseTest {

    @Autowired
    Dao dao;
    @Autowired
    FilesDataSource filesDataSource;
    private EntityFile newParent;
    private EntityFile node;
    private EntityFile nodeChild;
    private EntityFile nodeChild2;

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        newParent = dao.insert(EntityFile.builder().name("newParent").path("/root/newParent").build());
        node = dao.insert(EntityFile.builder().name("node").path("/root2/node").build());
        nodeChild = dao.insert(
                EntityFile.builder().name("child").path("/root2/node/child").parentId(node.getId()).build());
        nodeChild2 = dao.insert(
                EntityFile.builder().name("child2").path("/root2/node/child2").parentId(node.getId()).build());
    }

    @Test
    public void testChangeParent() {
        filesDataSource.resetParentTo(node, newParent);
        assertEquals(node.getParentId(), newParent.getId());
        assertEquals("/root/newParent/node", dao.fetch(EntityFile.class, node.getId()).getPath());
        assertEquals("/root/newParent/node/child", dao.fetch(EntityFile.class, nodeChild.getId()).getPath());
        assertEquals("/root/newParent/node/child2", dao.fetch(EntityFile.class, nodeChild2.getId()).getPath());
    }

    @Test
    public void testChangeToRoot() {
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

    @Test
    public void testMergeFolder() {
        Long id = node.getId();
        filesDataSource.mergeTo(node, newParent);
        assertNull(dao.fetch(EntityFile.class, id));
        assertEquals(newParent.getId(), dao.fetch(EntityFile.class, nodeChild.getId()).getParentId());
        assertEquals("/root/newParent/child", dao.fetch(EntityFile.class, nodeChild.getId()).getPath());
        assertEquals(newParent.getId(), dao.fetch(EntityFile.class, nodeChild2.getId()).getParentId());
        assertEquals("/root/newParent/child2", dao.fetch(EntityFile.class, nodeChild2.getId()).getPath());
    }

    @Test
    public void testMergeToRoot() {
        assertThrows(Exception.class, () -> filesDataSource.mergeTo(node, null));
    }
}