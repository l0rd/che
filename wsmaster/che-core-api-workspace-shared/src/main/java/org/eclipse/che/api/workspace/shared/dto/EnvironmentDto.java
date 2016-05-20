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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;
import static org.eclipse.che.api.core.factory.FactoryParameter.Version.V4_0;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface EnvironmentDto extends Environment {

    EnvironmentDto withName(String name);

    void setName(String name);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    @Deprecated
    RecipeDto getRecipe();

    @Deprecated
    void setRecipe(RecipeDto recipe);

    @Deprecated
    EnvironmentDto withRecipe(RecipeDto recipe);

    @Override
    @FactoryParameter(obligation = MANDATORY, deprecatedSince = V4_0)
    @Deprecated
    List<MachineConfigDto> getMachineConfigs();

    @Deprecated
    EnvironmentDto withMachineConfigs(List<MachineConfigDto> machineConfigs);

    @Deprecated
    void setMachineConfigs(List<MachineConfigDto> machineConfigs);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getType();

    void setType(String type);

    EnvironmentDto withType(String type);

    // todo added since?
    // todo custom validator?
    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getConfig();

    void setConfig(String config);

    EnvironmentDto withConfig(String config);
}
