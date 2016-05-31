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
public class DisconnectContainer {
    private String  container;
    private boolean force;

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public DisconnectContainer withContainer(String container) {
        this.container = container;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public DisconnectContainer withForce(boolean force) {
        this.force = force;
        return  this;
    }

    @Override
    public String toString() {
        return "DisconnectContainer{" +
               "container='" + container + '\'' +
               ", force=" + force +
               '}';
    }
}
