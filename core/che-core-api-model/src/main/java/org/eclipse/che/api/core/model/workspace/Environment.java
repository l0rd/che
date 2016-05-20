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
package org.eclipse.che.api.core.model.workspace;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.Recipe;

import java.util.List;

/**
 * Defines environment for machines network.
 *
 * @author gazarenkov
 */
public interface Environment {

    /**
     * Returns environment display name. It is mandatory and unique per workspace
     */
    String getName();

    /**
     * Returns the recipe (the main script) to define this environment (compose, kubernetes pod).
     * Type of this recipe defines engine for composing machines network runtime
     */
    @Deprecated
    Recipe getRecipe();

    /**
     * Returns list of Machine configs defined by this environment
     * Note: it may happen that we are not able to provide this info for particular environment type
     * or for particular time (for example this information may be reasonable accessible only when we start network or so)
     * to investigate
     */
    @Deprecated
    List<? extends MachineConfig> getMachineConfigs();

    /**
     * Returns type of environment, e.g. che, compose, opencompose, etc.
     * It is mandatory and case insensitive.
     */
    String getType();

    /**
     * Returns configuration of environment.
     * Content is implementation specific and depends on {@link #getType()}
     */
    String getConfig();

    // TODO consider url types of environment, so we are able to provide an url to the env script instead of env script
}
