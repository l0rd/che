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

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link PreferencesManager}.
 *
 * @author Yevhenii Voevodin.
 */
@Listeners(MockitoTestNGListener.class)
public class PreferencesManagerTest {

    @Mock
    private PreferenceDao preferenceDao;

    @InjectMocks
    private PreferencesManager preferencesManager;

    @Captor
    private ArgumentCaptor<Map<String, String>> preferencesCaptor;

    @Test
    public void shouldUseMergeStrategyForPreferencesUpdate() throws Exception {
        // Preparing preferences
        final Map<String, String> existingPreferences = new HashMap<>();
        existingPreferences.put("pKey1", "pValue1");
        existingPreferences.put("pKey2", "pValue2");
        existingPreferences.put("pKey3", "pValue3");
        existingPreferences.put("pKey4", "pValue4");
        when(preferenceDao.getPreferences(any())).thenReturn(existingPreferences);

        // Updating preferences
        final Map<String, String> newPreferences = new HashMap<>();
        newPreferences.put("pKey5", "pValue5");
        newPreferences.put("pKey1", "new-value");
        preferencesManager.update("user123", newPreferences);

        // Checking
        verify(preferenceDao).setPreferences(anyString(), preferencesCaptor.capture());
        assertEquals(preferencesCaptor.getValue(), ImmutableMap.of("pKey1", "new-value",
                                                                   "pKey2", "pValue2",
                                                                   "pKey3", "pValue3",
                                                                   "pKey4", "pValue4",
                                                                   "pKey5", "pValue5"));

    }

    @Test
    public void shouldRemoveSpecifiedPreferences() throws Exception {
        // Preparing preferences
        final Map<String, String> existingPreferences = new HashMap<>();
        existingPreferences.put("pKey1", "pValue1");
        existingPreferences.put("pKey2", "pValue2");
        existingPreferences.put("pKey3", "pValue3");
        existingPreferences.put("pKey4", "pValue4");
        when(preferenceDao.getPreferences(any())).thenReturn(existingPreferences);

        // Removing
        preferencesManager.remove("user123", asList("pKey1", "pKey5", "odd-pref-name"));

        // Checking
        verify(preferenceDao).setPreferences(anyString(), preferencesCaptor.capture());
        assertEquals(preferencesCaptor.getValue(), ImmutableMap.of("pKey2", "pValue2",
                                                                   "pKey3", "pValue3",
                                                                   "pKey4", "pValue4"));
    }
}
