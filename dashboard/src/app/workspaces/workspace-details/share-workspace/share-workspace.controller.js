/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc controller
 * @name workspace.details.controller:ShareWorkspaceController
 * @description This class is handling the controller sharing workspace
 * @author Ann Shumilova
 */
export class ShareWorkspaceController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheWorkspace, $mdConstant) {
    "ngInject";

    this.cheWorkspace = cheWorkspace;

    this.separators = [$mdConstant.KEY_CODE.ENTER, $mdConstant.KEY_CODE.COMMA, $mdConstant.KEY_CODE.SPACE];
    this.emails = [];
    this.users = [];

    this.actions = ['read', 'use'];
  }

  shareWorkspace() {
    //getUser by alias

    //form permissions

    //store permissions
  }
}
