package org.jmxtrans.embedded.discovery;

import javax.annotation.Nonnull;
import javax.management.ObjectName;

public interface AliasGenerator {

    @Nonnull
    String generateAlias(@Nonnull ObjectName objectName, @Nonnull String objectCanonicalName);

}
