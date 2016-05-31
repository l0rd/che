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
package org.eclipse.che.api.workspace.server.env.impl.che;

import org.eclipse.che.api.core.model.machine.MachineConfig;

import java.util.List;

/**
 * author Alexander Garagatyi
 */
public interface CheEnvStartStrategy {

    // todo can return list of list of configs to implement parallel start
    /**
     * Returns configs in order this machines should be started.
     *
     * @param configs origin machines order
     * @throws IllegalArgumentException when config is illegal and it is not possible to find correct order
     */
    List<MachineConfig> order(List<MachineConfig> configs) throws IllegalArgumentException;
}
