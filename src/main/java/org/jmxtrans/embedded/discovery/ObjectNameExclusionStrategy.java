package org.jmxtrans.embedded.discovery;

import javax.annotation.Nonnull;

public interface ObjectNameExclusionStrategy {

    boolean excludeObjectName(@Nonnull String objectCanonicalName);

}
