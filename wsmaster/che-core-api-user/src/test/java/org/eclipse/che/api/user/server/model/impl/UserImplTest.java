/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server.model.impl;

import org.testng.annotations.Test;

import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link UserImpl}.
 *
 * @author Yevhenii Voevodin
 */
public class UserImplTest {

    @Test
    public void testCreation() {
        final UserImpl user = new UserImpl("user123",
                                           "user@company.com",
                                           "user_name",
                                           "password",
                                           singletonList("google:id"));

        assertEquals(user.getId(), "user123");
        assertEquals(user.getEmail(), "user@company.com");
        assertEquals(user.getName(), "user_name");
        assertEquals(user.getPassword(), "password");
        assertEquals(user.getAliases(), singletonList("google:id"));
    }

    @Test(dependsOnMethods = "testCreation")
    public void testModification() throws Exception {
        final UserImpl user = new UserImpl("user123",
                                           "user@company.com",
                                           "user_name",
                                           "password",
                                           singletonList("google:id"));

        user.setName("new_name");
        user.setEmail("new_email@company.com");
        user.setPassword("new-password");
        user.setAliases(singletonList("new-alias"));

        assertEquals(user.getName(), "new_name");
        assertEquals(user.getEmail(), "new_email@company.com");
        assertEquals(user.getPassword(), "new-password");
        assertEquals(user.getAliases(), singletonList("new-alias"));
    }

    @Test(dependsOnMethods = "testCreation")
    @SuppressWarnings("all")
    public void testReflexiveness() throws Exception {
        final UserImpl user = new UserImpl("user123",
                                           "user@company.com",
                                           "user_name",
                                           "password",
                                           singletonList("google:id"));

        assertTrue(user.equals(user));
    }

    @Test(dependsOnMethods = "testCreation")
    public void testSymmetry() throws Exception {
        final UserImpl user1 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            singletonList("google:id"));
        final UserImpl user2 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            singletonList("google:id"));

        assertTrue(user1.equals(user2));
        assertTrue(user2.equals(user1));
    }

    @Test(dependsOnMethods = "testCreation")
    public void testTransitivity() {
        final UserImpl user1 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            singletonList("google:id"));
        final UserImpl user2 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            singletonList("google:id"));
        final UserImpl user3 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            singletonList("google:id"));

        assertTrue(user1.equals(user2));
        assertTrue(user2.equals(user3));
    }

    @Test(dependsOnMethods = "testCreation")
    public void testConsistency() {
        final UserImpl user1 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            null);
        final UserImpl user2 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            new ArrayList<>());

        assertTrue(user1.equals(user2));
    }

    @Test(dependsOnMethods = "testCreation")
    @SuppressWarnings("all")
    public void testNotEqualityToNull() throws Exception {
        final UserImpl user1 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            null);

        assertFalse(user1.equals(null));
    }

    @Test(dependsOnMethods = {"testReflexiveness", "testSymmetry", "testTransitivity", "testConsistency", "testNotEqualityToNull"})
    public void testHashCodeContract() throws Exception {
        final UserImpl user1 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            null);
        final UserImpl user2 = new UserImpl("user123",
                                            "user@company.com",
                                            "user_name",
                                            "password",
                                            new ArrayList<>());

        assertEquals(user1.hashCode(), user2.hashCode());
    }
}
