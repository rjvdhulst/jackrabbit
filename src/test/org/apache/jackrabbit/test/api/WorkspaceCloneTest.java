/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.test.api;

import javax.jcr.AccessDeniedException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.lock.LockException;

/**
 * <code>WorkspaceCloneTest</code> contains tests for cloning nodes between
 * workspace.
 *
 * @test
 * @sources WorkspaceCloneTest.java
 * @executeClass org.apache.jackrabbit.test.api.WorkspaceCloneTest
 * @keywords level2
 */
public class WorkspaceCloneTest extends AbstractWorkspaceCopyBetweenTest {

    /**
     * If successful, the changes are persisted immediately, there is no need to
     * call save.
     */
    public void testCloneNodes() throws RepositoryException {
        // clone referenceable node below non-referenceable node
        String dstAbsPath = node2W2.getPath() + "/" + node1.getName();
        workspaceW2.clone(workspace.getName(), node1.getPath(), dstAbsPath, true);

        // there should not be any pending changes after clone
        assertFalse(superuserW2.hasPendingChanges());
    }

    /**
     * A NoSuchWorkspaceException is thrown if srcWorkspace does not exist.
     */
    public void testCloneNodesUnvalidWorkspace() throws RepositoryException {
        // clone a node to a non-existing workspace
        String dstAbsPath = node2W2.getPath() + "/" + node1.getName();
        try {
            workspaceW2.clone(getNonExistingWorkspaceName(superuser), node1.getPath(), dstAbsPath, true);
            fail("Unvalid Source Workspace should throw NoSuchWorkspaceException.");
        } catch (NoSuchWorkspaceException e) {
            // successful
        }
    }

    /**
     * The destAbsPath provided must not have an index on its final element. If
     * it does, then a RepositoryException is thrown. Strictly speaking, the
     * destAbsPath parameter is actually an absolute path to the parent node of
     * the new location, appended with the new name desired for the copied node.
     * It does not specify a position within the child node ordering.
     */
    public void testCloneNodesAbsolutePath() {
        try {
            // copy referenceable node to an absolute path containing index
            String dstAbsPath = node2W2.getPath() + "/" + node1.getName() + "[2]";
            workspaceW2.clone(workspace.getName(), node1.getPath(), dstAbsPath, true);
            fail("Cloning a node to an absolute path containing index should not be possible.");
        } catch (RepositoryException e) {
            // successful
        }
    }

    /**
     * A ConstraintViolationException is thrown if the operation would violate a
     * node-type or other implementation-specific constraint.
     */
    public void testCloneNodesConstraintViolationException() throws RepositoryException {
        // if parent node is nt:base then no sub nodes can be created
        Node subNodesNotAllowedNode = testRootNodeW2.addNode(nodeName3, ntBase);
        testRootNodeW2.save();
        try {
            String dstAbsPath = subNodesNotAllowedNode.getPath() + "/" + node2.getName();
            workspaceW2.clone(workspace.getName(), node2.getPath(), dstAbsPath, true);
            fail("Cloning a node below a node which can not have any sub nodes should throw a ConstraintViolationException.");
        } catch (ConstraintViolationException e) {
            // successful
        }
    }


    /**
     * An AccessDeniedException is thrown if the current session (i.e., the
     * session that was used to acquire this Workspace object) does not have
     * sufficient access permissions to complete the operation.
     */
    public void testCloneNodesAccessDenied() throws RepositoryException {
        // logout and get read only session
        superuser.logout();
        Session readOnlySuperuser = helper.getReadOnlySession();

        String dstAbsPath = node2.getPath() + "/" + node1.getName();
        try {
            readOnlySuperuser.getWorkspace().clone(workspaceW2.getName(), node1.getPath(), dstAbsPath, true);
            fail("Cloning in a read-only session should throw an AccessDeniedException.");
        } catch (AccessDeniedException e) {
            // successful
        }
    }

    /**
     * A PathNotFoundException is thrown if the node at srcAbsPath or the parent
     * of the new node at destAbsPath does not exist.
     */
    public void testCloneNodesPathNotExisting() throws RepositoryException {

        String srcAbsPath = node1.getPath();
        String dstAbsPath = node2W2.getPath() + "/" + node1.getName();

        // srcAbsPath is not existing
        String unvalidSrcPath = srcAbsPath + "unvalid";
        try {
            workspaceW2.clone(workspace.getName(), unvalidSrcPath, dstAbsPath, true);
            fail("Not existing source path '" + unvalidSrcPath + "' should throw PathNotFoundException.");
        } catch (PathNotFoundException e) {
            // successful
        }

        // dstAbsPath parent is not existing
        String unvalidDstParentPath = node2W2.getPath() + "unvalid/" + node1.getName();
        try {
            workspaceW2.clone(workspace.getName(), srcAbsPath, unvalidDstParentPath, true);
            fail("Not existing destination parent path '" + unvalidDstParentPath + "' should throw PathNotFoundException.");
        } catch (PathNotFoundException e) {
            // successful
        }
    }

    /**
     * A LockException is thrown if a lock prevents the copy.
     */
    public void testCloneNodesLocked() throws RepositoryException {
        // we assume repository supports locking
        String dstAbsPath = node2W2.getPath() + "/" + node1.getName();

        // add mixin "lockable" to be able to lock the node
        if (!node2W2.getPrimaryNodeType().isNodeType(mixLockable)) {
            node2W2.addMixin(mixLockable);
            testRootNodeW2.save();
        }

        // lock dst parent node
        node2W2.lock(true, true);

        try {
            workspaceW2.clone(workspace.getName(), node1.getPath(), dstAbsPath, true);
            fail("LockException was expected.");
        } catch (LockException e) {
            // successful
        }
    }
}