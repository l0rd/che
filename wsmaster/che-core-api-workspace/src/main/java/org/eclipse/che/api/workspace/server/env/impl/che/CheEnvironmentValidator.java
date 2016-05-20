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

import com.google.common.base.Joiner;
import com.google.gson.JsonSyntaxException;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.server.env.spi.EnvironmentValidator;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * author Alexander Garagatyi
 */
public class CheEnvironmentValidator implements EnvironmentValidator {
    private static final Pattern SERVER_PORT     = Pattern.compile("[1-9]+[0-9]*/(?:tcp|udp)");
    private static final Pattern SERVER_PROTOCOL = Pattern.compile("[a-z][a-z0-9-+.]*");

    private final MachineInstanceProviders machineInstanceProviders;

    @Inject
    public CheEnvironmentValidator(MachineInstanceProviders machineInstanceProviders) {
        this.machineInstanceProviders = machineInstanceProviders;
    }

    public String getType() {
        return CheEnvironmentEngine.ENVIRONMENT_TYPE;
    }
// todo validate links in the same way as machine name
    @Override
    public void validate(Environment env) throws BadRequestException {
        final String envName = env.getName();
        checkArgument(!isNullOrEmpty(envName), "Environment name should be neither null nor empty");

        List<? extends MachineConfig> machines;
        try {
            machines = parse(env);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getLocalizedMessage());
        }

        //machine configs
        checkArgument(!machines.isEmpty(), "Environment '%s' should contain at least 1 machine", envName);

        final long devCount = machines.stream()
                                      .filter(MachineConfig::isDev)
                                      .count();
        checkArgument(devCount == 1,
                      "Environment should contain exactly 1 dev machine, but '%s' contains '%d'",
                      envName,
                      devCount);
        for (MachineConfig machineCfg : machines) {
            validateMachine(machineCfg, envName);
        }
    }

    // todo change to new entity that contains network
    public List<? extends MachineConfig> parse(Environment env) throws IllegalArgumentException {
        List<? extends MachineConfig> machines;
        if (env.getConfig() != null) {
            // parse new format
            try {
                machines = DtoFactory.getInstance().createListDtoFromJson(env.getConfig(), MachineConfigDto.class);
            } catch (JsonSyntaxException e) {
                throw new IllegalArgumentException("Parsing of environment configuration failed. " + e.getLocalizedMessage());
            }
        } else {
            // old format
            machines = env.getMachineConfigs();
        }
        return machines;
    }

    private void validateMachine(MachineConfig machineCfg, String envName) throws BadRequestException {
        checkArgument(!isNullOrEmpty(machineCfg.getName()), "Environment %s contains machine with null or empty name", envName);
        checkNotNull(machineCfg.getSource(), "Environment " + envName + " contains machine without source");
        checkArgument(!(machineCfg.getSource().getContent() == null && machineCfg.getSource().getLocation() == null),
                      "Environment " + envName + " contains machine with source but this source doesn't define a location or content");
        checkArgument(machineInstanceProviders.hasProvider(machineCfg.getType()),
                      "Type %s of machine %s in environment %s is not supported. Supported values: %s.",
                      machineCfg.getType(),
                      machineCfg.getName(),
                      envName,
                      Joiner.on(", ").join(machineInstanceProviders.getProviderTypes()));

        for (ServerConf serverConf : machineCfg.getServers()) {
            checkArgument(serverConf.getPort() != null && SERVER_PORT.matcher(serverConf.getPort()).matches(),
                          "Machine %s contains server conf with invalid port %s",
                          machineCfg.getName(),
                          serverConf.getPort());
            checkArgument(serverConf.getProtocol() == null || SERVER_PROTOCOL.matcher(serverConf.getProtocol()).matches(),
                          "Machine %s contains server conf with invalid protocol %s",
                          machineCfg.getName(),
                          serverConf.getProtocol());
        }
        for (Map.Entry<String, String> envVariable : machineCfg.getEnvVariables().entrySet()) {
            checkArgument(!isNullOrEmpty(envVariable.getKey()), "Machine %s contains environment variable with null or empty name");
            checkNotNull(envVariable.getValue(), "Machine %s contains environment variable with null value");
        }
    }

    /**
     * Checks that object reference is not null, throws {@link BadRequestException}
     * in the case of null {@code object} with given {@code message}.
     */
    private static void checkNotNull(Object object, String message) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(message);
        }
    }

    /**
     * Checks that expression is true, throws {@link BadRequestException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    private static void checkArgument(boolean expression, String errorMessage) throws BadRequestException {
        if (!expression) {
            throw new BadRequestException(errorMessage);
        }
    }

    /**
     * Checks that expression is true, throws {@link BadRequestException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    private static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageParams)
            throws BadRequestException {
        if (!expression) {
            throw new BadRequestException(format(errorMessageTemplate, errorMessageParams));
        }
    }
}
