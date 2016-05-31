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
package org.eclipse.che.api.workspace.server.env;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import org.eclipse.che.api.workspace.server.env.impl.che.CheEnvironmentEngine;
import org.eclipse.che.api.workspace.server.env.impl.che.CheEnvironmentValidator;
import org.eclipse.che.api.workspace.server.env.spi.EnvironmentEngine;
import org.eclipse.che.api.workspace.server.env.spi.EnvironmentValidator;

/**
 * author Alexander Garagatyi
 */
public class EnvironmentModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder<String, EnvironmentEngine> engines =
                MapBinder.newMapBinder(binder(), String.class, EnvironmentEngine.class);
        engines.addBinding(CheEnvironmentEngine.ENVIRONMENT_TYPE).to(CheEnvironmentEngine.class);

        MapBinder<String, EnvironmentValidator> validators =
                MapBinder.newMapBinder(binder(), String.class, EnvironmentValidator.class);
        validators.addBinding(CheEnvironmentEngine.ENVIRONMENT_TYPE).to(CheEnvironmentValidator.class);

        bind(org.eclipse.che.api.workspace.server.env.impl.che.CheEnvStartStrategy.class)
                .to(org.eclipse.che.api.workspace.server.env.impl.che.LinksBasedCheEnvStartStrategy.class);
    }
}
