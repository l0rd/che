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
package org.eclipse.che.plugin.docker.client.json.network;

import java.util.Map;

/**
 * author Alexander Garagatyi
 */
public class Network {
    private String                          name;
    private String                          id;
    private String                          scope;
    private String                          driver;
    private boolean                         enableIPv6;
    private boolean                         internal;
    private Ipam                            iPAM;
    private Map<String, ContainerInNetwork> containers;
    private Map<String, String>             options;
    private Map<String, String>             labels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Network withName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Network withId(String id) {
        this.id = id;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Network withScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Network withDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public boolean isEnableIPv6() {
        return enableIPv6;
    }

    public void setEnableIPv6(boolean enableIPv6) {
        this.enableIPv6 = enableIPv6;
    }

    public Network withEnableIPv6(boolean enableIPv6) {
        this.enableIPv6 = enableIPv6;
        return this;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public Network withInternal(boolean internal) {
        this.internal = internal;
        return this;
    }

    public Ipam getIPAM() {
        return iPAM;
    }

    public void setIPAM(Ipam iPAM) {
        this.iPAM = iPAM;
    }

    public Network withIPAM(Ipam iPAM) {
        this.iPAM = iPAM;
        return this;
    }

    public Map<String, ContainerInNetwork> getContainers() {
        return containers;
    }

    public void setContainers(Map<String, ContainerInNetwork> containers) {
        this.containers = containers;
    }

    public Network withContainers(Map<String, ContainerInNetwork> containers) {
        this.containers = containers;
        return this;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public Network withOptions(Map<String, String> options) {
        this.options = options;
        return this;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Network withLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    @Override
    public String toString() {
        return "Network{" +
               "name='" + name + '\'' +
               ", id='" + id + '\'' +
               ", scope='" + scope + '\'' +
               ", driver='" + driver + '\'' +
               ", enableIPv6=" + enableIPv6 +
               ", internal=" + internal +
               ", iPAM=" + iPAM +
               ", containers=" + containers +
               ", options=" + options +
               ", labels=" + labels +
               '}';
    }
}
