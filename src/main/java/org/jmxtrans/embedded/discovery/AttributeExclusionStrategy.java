package org.jmxtrans.embedded.discovery;

import org.springframework.util.MultiValueMap;

import javax.annotation.Nonnull;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

public interface AttributeExclusionStrategy {

    boolean shouldExcludeAttribute(@Nonnull MBeanAttributeInfo attributeInfo, @Nonnull MultiValueMap<String, MBeanOperationInfo> allOperationsNames);

}
