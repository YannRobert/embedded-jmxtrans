package org.jmxtrans.embedded.discovery;

import java.util.Comparator;

public class AttributeComparator implements Comparator<Object> {

    public static final AttributeComparator singleton = new AttributeComparator();

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 instanceof String && o2 instanceof String) {
            return ((String) o1).compareTo((String) o2);
        }
        if (o1 instanceof ConfigModel.CompositeAttribute && o2 instanceof ConfigModel.CompositeAttribute) {
            return CompositeAttributeComparator.singleton.compare((ConfigModel.CompositeAttribute) o1, (ConfigModel.CompositeAttribute) o2);
        }
        if (o1 instanceof ConfigModel.CompositeAttribute && o2 instanceof String) {
            return CompositeAttributeComparator.singleton.compare((ConfigModel.CompositeAttribute) o1, (String) o2);
        }
        if (o1 instanceof String && o2 instanceof ConfigModel.CompositeAttribute) {
            return CompositeAttributeComparator.singleton.compare((ConfigModel.CompositeAttribute) o2, (String) o1);
        }
        return 0;
    }
}
