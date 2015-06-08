package org.jmxtrans.embedded.discovery;

import lombok.Setter;
import org.springframework.util.MultiValueMap;

import javax.annotation.Nonnull;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.util.List;

public class DefaultAttributeExclusionStrategy implements AttributeExclusionStrategy {

    @Setter
    private boolean excludeWritableAttributes = true;

    @Override
    public boolean shouldExcludeAttribute(@Nonnull MBeanAttributeInfo attributeInfo, @Nonnull MultiValueMap<String, MBeanOperationInfo> allOperationsNames) {
        if (excludeWritableAttributes) {
            if (attributeInfo.isWritable()) {
                return true;
            }
            List<MBeanOperationInfo> mBeanOperationInfos = allOperationsNames.get("set" + attributeInfo.getName());
            if (mBeanOperationInfos != null) {
                if (!mBeanOperationInfos.isEmpty()) {
                    // TODO : check this is exact signature
                    return true;
                }
            }
        }
        return false;
    }

}
