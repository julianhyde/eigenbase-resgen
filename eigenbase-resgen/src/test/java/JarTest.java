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
//
// ResGen test code.
*/

import java.lang.reflect.*;
import java.net.*;

public class JarTest {
    public static void main(String [] args) throws Exception {
        URL [] urls = new URL[1];
        urls[0] = new URL(args[0]);
        URLClassLoader classLoader = new URLClassLoader(urls);
        Class c = classLoader.loadClass("Birthday");
        Method m = c.getMethod("runTest", new Class[0]);
        m.invoke(null, new Object[0]);
    }
}

// End JarTest.java
