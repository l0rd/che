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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * author Alexander Garagatyi
 */
public class LinksBasedCheEnvStartStrategy implements CheEnvStartStrategy {
    @Override
    public List<MachineConfig> order(List<MachineConfig> configs) throws IllegalArgumentException {
        configs = new ArrayList<>(configs);

        // move start of dependent machines after machines they depends on
        Map<String, Integer> weights = weightMachines(configs);

        configs = sortByWeight(configs, weights);

        return configs;
    }

    private Map<String, Integer> weightMachines(List<MachineConfig> configs) throws IllegalArgumentException {
        HashMap<String, Integer> weights = new HashMap<>();
        Set<String> machinesLeft = configs.stream()
                                          .map(MachineConfig::getName)
                                          .collect(Collectors.toSet());

        // should not happen if config was validated before usage in engine
        if (machinesLeft.size() != configs.size()) {
            throw new IllegalArgumentException("Configs contains machines with duplicate name");
        }

        boolean weightEvaluatedInCycleRun = true;
        while (weights.size() != configs.size() && weightEvaluatedInCycleRun) {
            weightEvaluatedInCycleRun = false;
            for (MachineConfig config : configs) {
                // todo if type is not `docker` put machine in the end of start queue
                // process not yet processed machines only
                if (machinesLeft.contains(config.getName())) {
                    if (config.getMachineLinks().size() == 0) {
                        // no links - smallest weight 0
                        weights.put(config.getName(), 0);
                        machinesLeft.remove(config.getName());
                        weightEvaluatedInCycleRun = true;
                    } else {
                        // machine has links - check if it has not weighted link
                        Optional<String> nonWeightedLink = config.getMachineLinks()
                                                                 .stream()
                                                                 .filter(machinesLeft::contains)
                                                                 .findAny();
                        if (!nonWeightedLink.isPresent()) {
                            // all links are weighted - lets evaluate current machine
                            Optional<String> maxWeight = config.getMachineLinks()
                                                               .stream()
                                                               .max((o1, o2) -> weights.get(o1).compareTo(weights.get(o2)));
                            // optional can't empty because size of the list is checked above
                            //noinspection OptionalGetWithoutIsPresent
                            weights.put(config.getName(), weights.get(maxWeight.get()) + 1);
                            machinesLeft.remove(config.getName());
                            weightEvaluatedInCycleRun = true;
                        }
                    }
                }
            }
        }

        if (weights.size() != configs.size()) {
            throw new IllegalArgumentException("Launch order of machines " + machinesLeft + " can't be evaluated");
        }

        return weights;
    }

    private List<MachineConfig> sortByWeight(List<MachineConfig> configs, Map<String, Integer> weights) {
        configs.sort((o1, o2) -> weights.get(o1.getName()).compareTo(weights.get(o2.getName())));
        return configs;
    }
}
