package org.jmxtrans.embedded.discovery;

import javax.annotation.Nonnull;

public interface AttributeTypeInclusionStrategy {

    boolean shouldIncludeAttributeType(@Nonnull String attributeType);

}
