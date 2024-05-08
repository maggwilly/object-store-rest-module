package org.objectstore.rest.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.PriorityQueue;

public class RedisObjectStoreExpiryTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RedisObjectStoreExpiryTask.class);
    private final RestObjectStore objectStore;

    public RedisObjectStoreExpiryTask(RestObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    public void run() {
        this.expire();
    }

    public void expire() {
        try {
            List<String> keys = this.objectStore.allKeys();
            int excess = this.objectStore.getMaxEntries() != null ? keys.size() - this.objectStore.getMaxEntries() : 0;
            PriorityQueue<String> sortedMaxEntriesKeys = null;
            if (excess > 0) {
                sortedMaxEntriesKeys = new PriorityQueue<>(excess);
                for (String key : keys) {
                    sortedMaxEntriesKeys.offer(key);
                }
            }

            if (sortedMaxEntriesKeys != null) {
                for(String key = sortedMaxEntriesKeys.poll(); key != null && excess > 0; key = sortedMaxEntriesKeys.poll()) {
                    this.objectStore.remove(key);
                    --excess;
                }
            }
        } catch (Exception var6) {
            logger.warn("Running expiry on Custom  Object Store threw {} : {}", var6, var6.getMessage(), var6);
        }

    }
}
