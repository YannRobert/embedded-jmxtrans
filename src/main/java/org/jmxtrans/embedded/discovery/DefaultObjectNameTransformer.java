package org.jmxtrans.embedded.discovery;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.management.ObjectName;

@Slf4j
public class DefaultObjectNameTransformer implements ObjectNameTransformer {

    @Override
    @Nonnull
    public String transformObjectName(@Nonnull ObjectName objectName, @Nonnull String objectCanonicalName) {
        String applicationKeyProperty = objectName.getKeyProperty("application");
        if (applicationKeyProperty != null) {
            String result = objectCanonicalName.replace("application=" + applicationKeyProperty + "", "application=*");
            log.debug("objectCanonicalName = {}, applicationKeyProperty = {}, result = {}", objectCanonicalName, applicationKeyProperty, result);
            return result;
        }
        return objectCanonicalName;
    }

}
