package org.jmxtrans.embedded.discovery;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Set;

public class ConfigModel {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class Config {
        private List<Query> queries;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class Query {
        private String objectName;
        private String resultAlias;
        private Set<Object> attributes;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class CompositeAttribute {
        private String name;
        private Set<String> keys;
    }

}
