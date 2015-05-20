package org.jmxtrans.embedded.discovery;

import javax.annotation.Nonnull;
import java.util.Comparator;

public class CompositeAttributeComparator implements Comparator<ConfigModel.CompositeAttribute> {

    public static final CompositeAttributeComparator singleton = new CompositeAttributeComparator();

    @Override
    public int compare(ConfigModel.CompositeAttribute o1, ConfigModel.CompositeAttribute o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return o1.getName().compareTo(o2.getName());
    }

    public int compare(@Nonnull ConfigModel.CompositeAttribute o1, @Nonnull String o2) {
        return o1.getName().compareTo(o2);
    }

}
