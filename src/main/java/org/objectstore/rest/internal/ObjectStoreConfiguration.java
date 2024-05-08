package org.objectstore.rest.internal;

import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@ConnectionProviders(ObjectStoreManagerProvider.class)
public class ObjectStoreConfiguration {

}
