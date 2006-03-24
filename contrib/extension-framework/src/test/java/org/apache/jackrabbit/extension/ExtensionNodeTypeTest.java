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
package org.apache.jackrabbit.extension;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeManager;

/**
 * The <code>ExtensionNodeTypeTest</code> TODO
 *
 * @author Felix Meschberger
 */
public class ExtensionNodeTypeTest extends ExtensionFrameworkTestBase {

    public void testNodeType() throws ExtensionException, RepositoryException {
        NodeTypeManager ntm= session.getWorkspace().getNodeTypeManager();

        // check whether the node type is available
        ExtensionManager.checkNodeType(session);

        ntm.getNodeType(ExtensionManager.NODE_EXTENSION_TYPE);
    }
}
