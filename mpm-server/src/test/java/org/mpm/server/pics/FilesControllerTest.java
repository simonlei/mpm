package org.mpm.server.pics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class FilesControllerTest extends BaseTest {

    @Autowired
    Dao dao;
    @Autowired
    FilesController filesController;
    private EntityFile newParent;
    private EntityFile newParentNode;
    private EntityFile node;
    private EntityFile node2;
    private EntityFile nodeChild;
    private EntityFile node2Child;
    private EntityFile node2ChildFile;
    private EntityFile nodeChild2;

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        newParent = dao.insert(EntityFile.builder().name("newParent").path("/root/newParent").isFolder(true).build());
        newParentNode = dao.insert(EntityFile.builder().name("node2").path("/root/newParent/node2").isFolder(true)
                .parentId(newParent.getId()).build());
        dao.insert(EntityFile.builder().name("node2file").path("/root/newParent/node2/node2file").isFolder(false)
                .parentId(newParentNode.getId()).build());

        node = dao.insert(EntityFile.builder().name("node").path("/root2/node").isFolder(true).build());
        nodeChild = dao.insert(EntityFile.builder().name("child").path("/root2/node/child").isFolder(true)
                .parentId(node.getId()).build());
        nodeChild2 = dao.insert(EntityFile.builder().name("child2").path("/root2/node/child2").isFolder(true)
                .parentId(node.getId()).build());

        node2 = dao.insert(EntityFile.builder().name("node2").path("/root/newParent/node2").isFolder(true)
                .parentId(newParent.getId()).build());
        node2Child = dao.insert(EntityFile.builder().name("node2child").path("/root/newParent/node2/node2child")
                .isFolder(true).parentId(node2.getId()).build());
        node2ChildFile = dao.insert(EntityFile.builder().name("node2file").path("/root/newParent/node2/node2file")
                .isFolder(false).parentId(node2.getId()).build());

    }

    @Test
    public void testChangeParent() {
        filesController.moveFolderTo(node, newParent);
        assertEquals(node.getParentId(), newParent.getId());
        assertEquals("/root/newParent/node", dao.fetch(EntityFile.class, node.getId()).getPath());
        assertEquals("/root/newParent/node/child", dao.fetch(EntityFile.class, nodeChild.getId()).getPath());
        assertEquals("/root/newParent/node/child2", dao.fetch(EntityFile.class, nodeChild2.getId()).getPath());

        filesController.moveFolderTo(node2, newParent);
        assertNull(dao.fetch(EntityFile.class, node2.getId()));
        assertEquals(newParentNode.getId(), dao.fetch(EntityFile.class, node2Child.getId()).getParentId());
        assertNotNull(dao.fetch(EntityFile.class, node2ChildFile.getId()));
        assertEquals(newParentNode.getId(), dao.fetch(EntityFile.class, node2ChildFile.getId()).getParentId());
    }

    @Test
    public void testChangeToRoot() {
        // set to root
        filesController.moveFolderTo(node, null);
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
        filesController.mergeTo(node, newParent);
        assertNull(dao.fetch(EntityFile.class, id));
        assertEquals(newParent.getId(), dao.fetch(EntityFile.class, nodeChild.getId()).getParentId());
        assertEquals("/root/newParent/child", dao.fetch(EntityFile.class, nodeChild.getId()).getPath());
        assertEquals(newParent.getId(), dao.fetch(EntityFile.class, nodeChild2.getId()).getParentId());
        assertEquals("/root/newParent/child2", dao.fetch(EntityFile.class, nodeChild2.getId()).getPath());
    }

    @Test
    public void testMergeToRoot() {
        assertThrows(Exception.class, () -> filesController.mergeTo(node, null));
    }
}