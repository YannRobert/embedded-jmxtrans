package org.jmxtrans.embedded.discovery;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Nonnull;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.IOException;
import java.util.*;

@Slf4j
public class AutomaticDiscoveryConfigGenerator {

    private final MBeanServer mbeanServer;

    @Setter
    private AttributeTypeInclusionStrategy attributeTypeInclusionStrategy = new DefaultAttributeTypeInclusionStrategy();

    @Setter
    private AttributeExclusionStrategy attributeExclusionStrategy = new DefaultAttributeExclusionStrategy();

    @Setter
    private ObjectNameExclusionStrategy objectNameExclusionStrategy = new DefaultObjectNameExclusionStrategy();

    @Setter
    private AliasGenerator aliasGenerator = new DefaultAliasGenerator();

    @Setter
    private ObjectNameTransformer objectNameTransformer = new DefaultObjectNameTransformer();

    public AutomaticDiscoveryConfigGenerator(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    @Nonnull
    public ConfigModel.Config autoDiscovery() throws IntrospectionException, InstanceNotFoundException, ReflectionException, IOException, AttributeNotFoundException, MBeanException {
        Set<ObjectInstance> instances = mbeanServer.queryMBeans(null, null);

        final List<ConfigModel.Query> queries = new ArrayList<ConfigModel.Query>();
        for (ObjectInstance objectInstance : instances) {
            ObjectName objectName = objectInstance.getObjectName();
            String objectCanonicalName = objectName.getCanonicalName();
            if (!objectNameExclusionStrategy.excludeObjectName(objectCanonicalName)) {
                Collection<ConfigModel.Query> queriesForObjectName = processObjectName(objectInstance, objectName, objectCanonicalName);
                queries.addAll(queriesForObjectName);
            }
        }

        Collections.sort(queries, new QueryComparator());
        final ConfigModel.Config config = new ConfigModel.Config();
        config.setQueries(queries);
        return config;
    }

    @Nonnull
    protected Collection<ConfigModel.Query> processObjectName(ObjectInstance objectInstance, ObjectName objectName, String objectCanonicalName) throws InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException, AttributeNotFoundException {
        log.debug("className = " + objectInstance.getClassName());

        final MBeanInfo mBeanInfo = mbeanServer.getMBeanInfo(objectInstance.getObjectName());
        final MBeanAttributeInfo[] allAttributesInfo = mBeanInfo.getAttributes();
        final MBeanOperationInfo[] allOperationsInfo = mBeanInfo.getOperations();

        final MultiValueMap<String, MBeanOperationInfo> allOperationsNames = new LinkedMultiValueMap<String, MBeanOperationInfo>();
        for (MBeanOperationInfo operationInfo : allOperationsInfo) {
            allOperationsNames.add(operationInfo.getName(), operationInfo);
        }
        log.debug("allOperationsNames.keySet() = {}", allOperationsNames.keySet());

        final List<Object> attributes = new ArrayList<Object>();
        for (MBeanAttributeInfo attributeInfo : allAttributesInfo) {
            if (attributeInfo.isReadable()) {
                if (attributeExclusionStrategy.shouldExcludeAttribute(attributeInfo, allOperationsNames)) {
                    // attribute is writable and should not be exported
                    // most of the times, it is manageable attributes intended to be set by operations guys
                    // most of the times, manageable attributes that serves as system metrics are read-only
                } else {
                    if (attributeTypeInclusionStrategy.shouldIncludeAttributeType(attributeInfo.getType())) {
                        log.debug("attributeInfo.getType() = " + attributeInfo.getType());
                        attributes.add(attributeInfo.getName());
                    }
                    if (attributeInfo.getType().equals("javax.management.openmbean.CompositeData")) {
                        CompositeData cd = (CompositeData) mbeanServer.getAttribute(objectName, attributeInfo.getName());
                        if (cd != null) {
                            ConfigModel.CompositeAttribute compositeAttribute = new ConfigModel.CompositeAttribute();
                            compositeAttribute.setName(attributeInfo.getName());
                            CompositeType compositeType = cd.getCompositeType();
                            compositeAttribute.setKeys(compositeType.keySet());
                            attributes.add(compositeAttribute);
                        }
                    }
                }
            }
        }

        if (!attributes.isEmpty()) {
            Collections.sort(attributes, AttributeComparator.singleton);

            final ConfigModel.Query query = new ConfigModel.Query();
            query.setObjectName(objectNameTransformer.transformObjectName(objectName, objectCanonicalName));
            query.setResultAlias(aliasGenerator.generateAlias(objectName, objectCanonicalName));
            query.setAttributes(new LinkedHashSet<Object>(attributes));

            List<ConfigModel.Query> results = new ArrayList<ConfigModel.Query>();
            results.add(query);
            return results;
        } else {
            return Collections.EMPTY_LIST;
        }

    }

}
