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

import java.util.List;

/**
 * author Alexander Garagatyi
 */
public class EndpointConfig {
    private NewIpamConfig iPAMConfig;
    private List<String>  links;
    private List<String>  aliases;

    public NewIpamConfig getIPAMConfig() {
        return iPAMConfig;
    }

    public void setIPAMConfig(NewIpamConfig iPAMConfig) {
        this.iPAMConfig = iPAMConfig;
    }

    public EndpointConfig withIPAMConfig(NewIpamConfig iPAMConfig) {
        this.iPAMConfig = iPAMConfig;
        return this;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public EndpointConfig withLinks(List<String> links) {
        this.links = links;
        return this;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public EndpointConfig withAliases(List<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    @Override
    public String toString() {
        return "EndpointConfig{" +
               "iPAMConfig=" + iPAMConfig +
               ", links=" + links +
               ", aliases=" + aliases +
               '}';
    }
}
