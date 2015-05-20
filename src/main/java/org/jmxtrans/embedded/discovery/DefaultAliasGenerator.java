package org.jmxtrans.embedded.discovery;

import javax.annotation.Nonnull;
import javax.management.ObjectName;

public class DefaultAliasGenerator implements AliasGenerator {

    private String lowerCaseOnFirstChar(String value) {
        if (value == null) {
            return null;
        }
        switch (value.length()) {
            case 0:
                return value;
            case 1:
                return value.toLowerCase();
            default:
                return ("" + value.charAt(0)).toLowerCase() + value.substring(1);
        }
    }

    @Override
    @Nonnull
    public String generateAlias(@Nonnull ObjectName objectName, @Nonnull String objectCanonicalName) {
        final String typeProperty = objectName.getKeyProperty("type");
        if (objectCanonicalName.startsWith("java.lang:")) {
            String type = typeProperty;
            return "jvm." + lowerCaseOnFirstChar(type);
        }
        final String nameProperty = objectName.getKeyProperty("name");
        if (nameProperty != null) {
            if (typeProperty != null) {
                // if the name already include the type, then don't append the type to the name
                if (nameProperty.endsWith(typeProperty)) {
                    return nameProperty;
                }
                if (nameProperty.equals(lowerCaseOnFirstChar(typeProperty))) {
                    return nameProperty;
                }
                return nameProperty + typeProperty;
            }
            return nameProperty;
        }
        return objectCanonicalName;
    }

}
