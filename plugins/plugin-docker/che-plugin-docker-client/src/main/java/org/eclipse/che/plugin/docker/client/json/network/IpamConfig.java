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

/**
 * author Alexander Garagatyi
 */
public class IpamConfig {
    private String subnet;
    private String gateway;
    private String iPRange;

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public IpamConfig withSubnet(String subnet) {
        this.subnet = subnet;
        return this;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public IpamConfig withGateway(String gateway) {
        this.gateway = gateway;
        return this;
    }

    public String getIPRange() {
        return iPRange;
    }

    public void setIPRange(String iPRange) {
        this.iPRange = iPRange;
    }

    public IpamConfig withIPRange(String iPRange) {
        this.iPRange = iPRange;
        return this;
    }

    @Override
    public String toString() {
        return "IpamConfig{" +
               "subnet='" + subnet + '\'' +
               ", gateway='" + gateway + '\'' +
               ", iPRange='" + iPRange + '\'' +
               '}';
    }
}
