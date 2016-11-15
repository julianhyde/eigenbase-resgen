/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.resgen;

import java.io.PrintWriter;

/**
 * A generator converts a set of resource definitions to a piece of code.
 *
 * @author jhyde
 */
interface Generator
{
    /**
     * Configures whether this generator will output comments that may be
     * submitted to a source code management system.  In general, it
     * squelches comments indicating the file should not be checked in as
     * well as comments change with each generation of the file (thereby
     * avoiding merge conflicts).
     *
     * @param enabled Whether enabled
     */
    void setScmSafeComments(boolean enabled);

    /**
     * Generates a class containing a line for each resource.
     */
    void generateModule(
        ResourceGen generator,
        ResourceDef.ResourceBundle resourceList,
        PrintWriter pw);
}

// End Generator.java
