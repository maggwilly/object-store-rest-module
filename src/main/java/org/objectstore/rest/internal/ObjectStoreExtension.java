package org.objectstore.rest.internal;

import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Import;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.param.RefName;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "os-rest")
@Extension(name = "Objectstore-rest")
@Import(type = HttpRequestAuthentication.class)
@Configurations(ObjectStoreConfiguration.class)
@ConnectionProviders(ObjectStoreManagerProvider.class)
public class ObjectStoreExtension {
    @RefName
    private String name;

    public String getConfigName() {
        return name;
    }
}
