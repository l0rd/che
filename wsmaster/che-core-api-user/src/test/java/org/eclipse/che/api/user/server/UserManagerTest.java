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
package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Tests for {@link UserManager}.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class UserManagerTest {

    @Mock
    private UserDao            userDao;
    @Mock
    private ProfileManager     profileManager;
    @Mock
    private PreferencesManager preferencesManager;
    @InjectMocks
    private UserManager        manager;

    @Test
    public void shouldCreateProfileAndPreferencesOnUserCreation() throws Exception {
        final UserImpl user = new UserImpl(null, "test@email.com", "testName", null, null);

        manager.create(user, false);

        verify(userDao).create(any(UserImpl.class));
        verify(profileManager).create(any(Profile.class));
        verify(preferencesManager).save(anyString(), anyMapOf(String.class, String.class));
    }

    @Test(dataProvider = "rollback")
    public void shouldTryToRollbackWhenEntityCreationFailed(Callable preAction) throws Exception {
        preAction.call();

        // Creating new user
        try {
            manager.create(new UserImpl(null, "test@email.com", "testName", null, null), false);
            fail("Had to throw Exception");
        } catch (Exception x) {
            // defined by userDao mock
        }

        // Capturing identifier
        final ArgumentCaptor<UserImpl> captor = ArgumentCaptor.forClass(UserImpl.class);
        verify(userDao).create(captor.capture());
        final String userId = captor.getValue().getId();

        // Verifying rollback
        verify(userDao).remove(userId);
        verify(preferencesManager).remove(userId);
        verify(profileManager).remove(userId);
    }

    @Test
    public void shouldGeneratePasswordWhenCreatingUserAndItIsMissing() throws Exception {
        final User user = new UserImpl(null, "test@email.com", "testName", null, null);

        manager.create(user, false);

        final ArgumentCaptor<UserImpl> userCaptor = ArgumentCaptor.forClass(UserImpl.class);
        verify(userDao).create(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getPassword());
    }

    @Test
    public void shouldGenerateIdentifierWhenCreatingUser() throws Exception {
        final User user = new UserImpl("identifier", "test@email.com", "testName", null, null);

        manager.create(user, false);

        final ArgumentCaptor<UserImpl> userCaptor = ArgumentCaptor.forClass(UserImpl.class);
        verify(userDao).create(userCaptor.capture());
        final String id = userCaptor.getValue().getId();
        assertNotNull(id);
        assertNotEquals(id, "identifier");
    }

    @DataProvider(name = "rollback")
    public Object[][] rollbackTestPreActions() throws Exception {
        return new Callable[][] {

                // User creation mocking
                {() -> {
                    doThrow(new ServerException("error"))
                            .when(userDao)
                            .create(any());
                    return null;
                }},

                // Preferences creation mocking
                {() -> {
                    doThrow(new ServerException("error"))
                            .when(preferencesManager)
                            .save(anyString(), any());
                    return null;
                }},

                // Profile creation mocking
                {() -> {
                    doThrow(new ServerException("error"))
                            .when(profileManager)
                            .create(any());
                    return null;
                }}
        };
    }
}
