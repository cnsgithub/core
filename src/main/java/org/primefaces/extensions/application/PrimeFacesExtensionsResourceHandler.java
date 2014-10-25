/*
 * Copyright 2011-2012 PrimeFaces Extensions.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */

package org.primefaces.extensions.application;

import org.primefaces.extensions.config.ConfigContainer;
import org.primefaces.extensions.config.ConfigProvider;
import org.primefaces.extensions.util.Constants;

import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.context.FacesContext;

/**
 * {@link ResourceHandlerWrapper} which wraps PrimeFaces Extensions resources and
 * appends the version of PrimeFaces Extensions in the {@link PrimeFacesExtensionsResource}.
 *
 * @author Thomas Andraschko / last modified by $Author$
 * @version $Revision$
 * @since 0.1
 */
public class PrimeFacesExtensionsResourceHandler extends ResourceHandlerWrapper {

    public static final String[] UNCOMPRESSED_EXCLUDES = new String[]{"ckeditor/"};

    private final ResourceHandler wrapped;

    public PrimeFacesExtensionsResourceHandler(final ResourceHandler resourceHandler) {
        super();

        wrapped = resourceHandler;
    }

    @Override
    public ResourceHandler getWrapped() {
        return wrapped;
    }

    @Override
    public Resource createResource(final String resourceName, final String libraryName) {
        Resource resource = super.createResource(resourceName, libraryName);
        return wrapIfNecessary(resource, resourceName, libraryName);
    }

    public Resource createResource(String resourceName, String libraryName, String contentType) {
        Resource resource = super.createResource(resourceName, libraryName, contentType);
        return wrapIfNecessary(resource, resourceName, libraryName);
    }

    protected Resource wrapIfNecessary(Resource resource, final String resourceName, final String libraryName) {
        if (resource == null || libraryName == null || !libraryName.equalsIgnoreCase(Constants.LIBRARY)) {
            return resource;
        }

        // handle PrimeFaces Extensions resources
        Resource wrappedResource;
        final FacesContext context = FacesContext.getCurrentInstance();
        if (deliverUncompressedFile(resourceName, ConfigProvider.getConfig(context), context)) {
            // get uncompressed resource if the project stage == development
            wrappedResource = super.createResource(resourceName, Constants.LIBRARY_UNCOMPRESSED);
            if (wrappedResource != null) {
                wrappedResource = new PrimeFacesExtensionsResource(wrappedResource);
            }
        } else {
            wrappedResource = new PrimeFacesExtensionsResource(resource);
        }

        return wrappedResource;
    }

    protected boolean deliverUncompressedFile(final String resourceName,
                                              final ConfigContainer config, final FacesContext context) {

        if (config.isDeliverUncompressedResources()
                && context.isProjectStage(ProjectStage.Development)) {

            if (resourceName.endsWith(Constants.EXTENSION_CSS) || resourceName.endsWith(Constants.EXTENSION_JS)) {
                for (final String exclude : UNCOMPRESSED_EXCLUDES) {
                    if (resourceName.contains(exclude)) {
                        return false;
                    }
                }

                return config.isDeliverUncompressedResources();
            }
        }

        return false;
    }
}
