package org.jmxtrans.embedded.discovery;

import java.io.Serializable;
import java.util.Comparator;

public class QueryComparator implements Comparator<ConfigModel.Query>, Serializable {
    @Override
    public int compare(ConfigModel.Query o1, ConfigModel.Query o2) {
        return o1.getObjectName().compareTo(o2.getObjectName());
    }
}
