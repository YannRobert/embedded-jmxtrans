package org.jmxtrans.embedded.discovery;

import javax.annotation.Nonnull;
import javax.management.ObjectName;

public interface ObjectNameTransformer {

    @Nonnull
    String transformObjectName(@Nonnull ObjectName objectName, @Nonnull String objectCanonicalName);

}
