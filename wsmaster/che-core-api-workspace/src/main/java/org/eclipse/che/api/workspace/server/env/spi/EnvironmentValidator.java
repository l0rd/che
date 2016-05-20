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
package org.eclipse.che.api.workspace.server.env.spi;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.model.workspace.Environment;

/**
 * author Alexander Garagatyi
 */
public interface EnvironmentValidator {
    String getType();

    void validate(Environment env) throws BadRequestException;
}
