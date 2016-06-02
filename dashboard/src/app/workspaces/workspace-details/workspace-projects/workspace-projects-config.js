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
 * @name workspaces:WorkspacesConfig
 * @description This class is used for configuring all workspaces stuff.
 * @author Ann Shumilova
 */
export class WorkspacesProjectsConfig {

  constructor(register) {

    new CreateProjectStackLibrarySelectedStackFilter(register);

    register.controller('ListWorkspacesCtrl', ListWorkspacesCtrl);
    register.controller('CreateWorkspaceCtrl', CreateWorkspaceCtrl);
    register.controller('CreateWorkspaceAddMemberCtrl', CreateWorkspaceAddMemberCtrl);

    register.directive('cheWorkspaceItem', CheWorkspaceItem);
    register.controller('WorkspaceItemCtrl', WorkspaceItemCtrl);
    register.directive('usageChart', UsageChart);

    register.controller('WorkspaceDetailsCtrl', WorkspaceDetailsCtrl);

    register.controller('WorkspaceDetailsProjectsCtrl', WorkspaceDetailsProjectsCtrl);
    register.directive('workspaceDetailsProjects', WorkspaceDetailsProjects);
    register.service('workspaceDetailsService', WorkspaceDetailsService);

    register.controller('ExportWorkspaceDialogController', ExportWorkspaceDialogController);
    register.controller('ExportWorkspaceController', ExportWorkspaceController);
    register.directive('exportWorkspace', ExportWorkspace);

    register.controller('WorkspaceRecipeCtrl', WorkspaceRecipeCtrl);
    register.directive('cheWorkspaceRecipe', WorkspaceRecipe);

    register.controller('CheWorkspaceRamAllocationSliderCtrl', CheWorkspaceRamAllocationSliderCtrl);
    register.directive('cheWorkspaceRamAllocationSlider', CheWorkspaceRamAllocationSlider);

    register.directive('workspaceStatusIndicator', WorkspaceStatusIndicator);

    register.controller('ReadyToGoStacksCtrl', ReadyToGoStacksCtrl);
    register.directive('readyToGoStacks', ReadyToGoStacks);

    register.controller('CreateProjectStackLibraryCtrl', CreateProjectStackLibraryCtrl);

    register.directive('createProjectStackLibrary', CreateProjectStackLibrary);
    register.directive('cheStackLibrarySelecter', CheStackLibrarySelecter);

    register.controller('WorkspaceSelectStackCtrl', WorkspaceSelectStackCtrl);
    register.directive('cheWorkspaceSelectStack', WorkspaceSelectStack);

    register.controller('CheStackLibraryFilterCtrl', CheStackLibraryFilterCtrl);
    register.directive('cheStackLibraryFilter', CheStackLibraryFilter);

    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/workspaces', {
        templateUrl: 'app/workspaces/list-workspaces/list-workspaces.html',
        controller: 'ListWorkspacesCtrl',
        controllerAs: 'listWorkspacesCtrl'
      })
      .accessWhen('/workspace/:workspaceId', {
          templateUrl: 'app/workspaces/workspace-details/workspace-details.html',
          controller: 'WorkspaceDetailsCtrl',
          controllerAs: 'workspaceDetailsCtrl'
        })
      .accessWhen('/create-workspace', {
          templateUrl: 'app/workspaces/create-workspace/create-workspace.html',
          controller: 'CreateWorkspaceCtrl',
          controllerAs: 'createWorkspaceCtrl'
        });
    });
  }
}
