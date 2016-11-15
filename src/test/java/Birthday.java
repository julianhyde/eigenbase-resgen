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
// ResGen example code.
*/
import happy.BirthdayResource;

import java.util.Locale;

public class Birthday {
    static void wishHappyBirthday(String name, int age) {
        if (age < 0) {
            throw BirthdayResource.instance().newTooYoung(name);
        }
        System.out.println(BirthdayResource.instance().getHappyBirthday(name, new Integer(age)));
    }
    public static void main(String[] args) {
        runTest();
    }

    public static void runTest()
    {
        wishHappyBirthday("Fred", 33);
        try {
            wishHappyBirthday("Wilma", -3);
        } catch (Throwable e) {
            System.out.println("Received " + e);
        }
        BirthdayResource.setThreadLocale(Locale.FRANCE);
        wishHappyBirthday("Pierre", 22);
    }
}

// End Birthday.java
