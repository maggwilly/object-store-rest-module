package org.objectstore.rest.internal.stereotype;

import org.mule.runtime.extension.api.stereotype.MuleStereotypes;
import org.mule.runtime.extension.api.stereotype.StereotypeDefinition;

import java.util.Optional;

public class ObjectStoreConnectionStereotype implements StereotypeDefinition {
    public ObjectStoreConnectionStereotype() {
    }

    public String getName() {
        return MuleStereotypes.CONNECTION_DEFINITION.getName();
    }

    public Optional<StereotypeDefinition> getParent() {
        return Optional.of(new StereotypeDefinition() {
            public String getName() {
                return MuleStereotypes.CONNECTION_DEFINITION.getName();
            }

            public String getNamespace() {
                return "OS";
            }

            public Optional<StereotypeDefinition> getParent() {
                return Optional.of(MuleStereotypes.CONNECTION_DEFINITION);
            }
        });
    }
}
