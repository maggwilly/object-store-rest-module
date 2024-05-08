package org.objectstore.rest.internal;

import org.objectstore.rest.http.HttpConnection;
import org.objectstore.rest.internal.exception.UnableToSendRequestException;
import org.mule.runtime.api.serialization.ObjectSerializer;
import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.ObjectStoreException;
import org.mule.runtime.api.store.ObjectStoreManager;
import org.mule.runtime.api.store.ObjectStoreSettings;
import org.mule.runtime.api.scheduler.Scheduler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RestObjectStoreManager implements ObjectStoreManager {
    private final Map<String, RestObjectStore> objectStores = new HashMap<>();
    private final HttpConnection httpConnection;
    private final Scheduler scheduler;
    public RestObjectStoreManager(HttpConnection httpConnection, Scheduler scheduler) {
        this.httpConnection = httpConnection;
        this.scheduler = scheduler;
    }


    public <T extends ObjectStore<? extends Serializable>> T getObjectStore(String name) {
        synchronized(this) {
            return (T)Optional.ofNullable(this.objectStores.get(name)).orElseThrow(() -> {
                return new IllegalArgumentException("An Object Store was not defined for name " + name);
            });
        }
    }

    @Override
    public <T extends ObjectStore<? extends Serializable>> T createObjectStore(String name, ObjectStoreSettings objectStoreSettings) {
        synchronized(this) {
            if (this.objectStores.containsKey(name)) {
                throw new IllegalArgumentException("An Object Store was already defined for name " + name);
            } else {
                RestObjectStore store = new RestObjectStore(httpConnection, objectStoreSettings, this.scheduler,name );
                try {
                    store.open();
                } catch (ObjectStoreException var7) {
                    throw new UnableToSendRequestException(var7.getMessage(), var7);
                }
                this.objectStores.put(name, store);
                return (T) store;
            }
        }
    }

    @Override
    public <T extends ObjectStore<? extends Serializable>> T getOrCreateObjectStore(String name, ObjectStoreSettings objectStoreSettings) {
        synchronized(this) {
            RestObjectStore store;
            if (this.objectStores.containsKey(name)) {
                store = this.objectStores.get(name);
            } else {
                try {
                    store = new RestObjectStore(httpConnection, objectStoreSettings, this.scheduler,name );
                    store.open();
                } catch (ObjectStoreException var7) {
                    throw new UnableToSendRequestException(var7.getMessage(), var7);
                }
                this.objectStores.put(name, store);
            }
            return (T) store;
        }
    }

    public void disposeStore(String name) throws ObjectStoreException {
        synchronized(this) {
            if (this.objectStores.containsKey(name)) {
                this.objectStores.get(name).clear();
                this.objectStores.remove(name);
            }
        }
    }

    public void disconnect() {
        this.objectStores.values().forEach(RestObjectStore::close);
    }

}
