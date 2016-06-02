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
package org.eclipse.che.plugin.docker.client.parser;

import org.eclipse.che.plugin.docker.client.DockerFileException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse docker image reference.
 * <p>
 * For example reference used in FROM instruction of Dockerfile.<br>
 * This class doesn't validate all components as Docker do.<br>
 * It was designed to extract base docker image reference from dockerfile.
 *
 * @author Alexander Garagatyi
 */
public class DockerImageIdentifierParser {

    // Some of the rules are taken from https://github.com/docker/distribution/blob/master/reference/regexp.go
    // But dot is removed from SEPARATOR part as it is hard to parse some simple identifiers
    // E.g. 'codenvy/ubuntu_jdk8'
    private static final String HOSTNAME_COMPONENT  = "(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9])";
    private static final String REGISTRY            = HOSTNAME_COMPONENT + "(?:\\." + HOSTNAME_COMPONENT + ")*(:[0-9]+)?";
    private static final String SEPARATOR           = "(?:[._]|__|[-]*)";
    private static final String ALPHA_NUMERIC       = "[a-z0-9]+";
    private static final String NAME_COMPONENT      = ALPHA_NUMERIC + "(?:" + SEPARATOR + ALPHA_NUMERIC + ")*";
    private static final String REPOSITORY          = NAME_COMPONENT + "(?:/" + NAME_COMPONENT + ")*";
    private static final String NAME                = "(?:" + REGISTRY + "/)?" + REPOSITORY;

    private static final String ss = "(" + REGISTRY + "/)?" + REPOSITORY;
    private static final Pattern sspa = Pattern.compile(ss);

    private static final Pattern IMAGE_PATTERN = Pattern.compile(NAME);

// todo
    // add method to evaluate default host and repo


    /**
     *
     * @param image
     * @return
     * @throws DockerFileException
     * @throws IllegalArgumentException
     */
    public static DockerImageIdentifier parse(final String image) throws DockerFileException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Null and empty argument value is forbidden");
        }

        DockerImageIdentifier.DockerImageIdentifierBuilder identifierBuilder = DockerImageIdentifier.builder();
        String workingCopyOfImage = image;

        // find digest
        int index = workingCopyOfImage.lastIndexOf('@');
        if (index != -1) {
            String digest = workingCopyOfImage.substring(index + 1);
            if (!digest.isEmpty()) {
                workingCopyOfImage = workingCopyOfImage.substring(0, index);
                identifierBuilder.setDigest(digest);
            }
        }

        // find tag
        index = workingCopyOfImage.lastIndexOf(':');
        if (index != -1) {
            if (workingCopyOfImage.lastIndexOf('/') < index) {
                String tag = workingCopyOfImage.substring(index + 1);
                if (!tag.isEmpty()) {
                    workingCopyOfImage = workingCopyOfImage.substring(0, index);
                    identifierBuilder.setTag(tag);
                }
            }
        }

        Matcher matcher = IMAGE_PATTERN.matcher(workingCopyOfImage);
        if (!matcher.matches()) {
            throw new DockerFileException("Provided image reference is invalid");
        }

        index = workingCopyOfImage.indexOf('/');
        String beforeSlash = index > -1 ? workingCopyOfImage.substring(0, index) : "";
        if (beforeSlash.isEmpty() || (!beforeSlash.contains(".") &&
                                      !beforeSlash.contains(":") &&
                                      !"localhost".equals(beforeSlash))) {

            identifierBuilder.setRepository(workingCopyOfImage);
        } else {
            identifierBuilder.setRegistry(beforeSlash)
                             .setRepository(workingCopyOfImage.substring(index + 1));
        }

        return identifierBuilder.build();
    }

    private DockerImageIdentifierParser() {}
}
