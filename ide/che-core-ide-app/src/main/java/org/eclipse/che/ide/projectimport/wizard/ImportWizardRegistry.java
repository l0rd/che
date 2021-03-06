/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projectimport.wizard;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Picks-up all bounded {@link ImportWizardRegistrar}s to be able to return it for the particular
 * project importer ID.
 */
public class ImportWizardRegistry {

  private final Map<String, ImportWizardRegistrar> registrars = new HashMap<>();

  @Inject(optional = true)
  private void register(Set<ImportWizardRegistrar> registrars) {
    for (ImportWizardRegistrar registrar : registrars) {
      final String id = registrar.getImporterId();

      if (this.registrars.containsKey(id)) {
        Log.warn(
            ImportWizardRegistry.class,
            "Wizard for project importer " + id + " already registered.");
      } else {
        this.registrars.put(id, registrar);
      }
    }
  }

  /**
   * Get an {@link ImportWizardRegistrar} for the specified project importer.
   *
   * @param importerId the ID of the project importer to get an appropriate {@link
   *     ImportWizardRegistrar}
   * @return {@link ImportWizardRegistrar} for the specified project importer ID or {@code
   *     Optional#empty()} if none
   */
  public Optional<ImportWizardRegistrar> getWizardRegistrar(@NotNull String importerId) {
    return Optional.ofNullable(registrars.get(importerId));
  }
}
