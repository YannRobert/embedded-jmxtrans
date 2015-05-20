package org.jmxtrans.embedded.discovery;

import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class DefaultObjectNameExclusionStrategy implements ObjectNameExclusionStrategy {

    public static Set<String> createExcludedObjectNamePrefixes() {
        Set<String> result = new HashSet<String>();
        result.add("org.jmxtrans.");
        result.add("java.lang:");
        result.add("Tomcat:");
        return result;
    }

    @Setter
    private Set<String> excludedObjectNamePrefixes = createExcludedObjectNamePrefixes();

    @Override
    public boolean excludeObjectName(@Nonnull String objectCanonicalName) {
        for (String prefixToExclude : excludedObjectNamePrefixes) {
            if (objectCanonicalName.startsWith(prefixToExclude)) {
                return true;
            }
        }
        return false;
    }

}
