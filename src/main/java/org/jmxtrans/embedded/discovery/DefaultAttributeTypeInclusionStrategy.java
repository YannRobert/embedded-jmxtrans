package org.jmxtrans.embedded.discovery;

import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class DefaultAttributeTypeInclusionStrategy implements AttributeTypeInclusionStrategy {

    private static Set<String> createIncludedAttributeTypesSet() {
        Set<String> result = new HashSet<String>();
        result.add("long");
        result.add("double");
        result.add("int");
        return result;
    }

    @Setter
    private Set<String> includedAttributeTypes = createIncludedAttributeTypesSet();

    @Override
    public boolean shouldIncludeAttributeType(@Nonnull String attributeType) {
        return includedAttributeTypes.contains(attributeType);
    }

}
