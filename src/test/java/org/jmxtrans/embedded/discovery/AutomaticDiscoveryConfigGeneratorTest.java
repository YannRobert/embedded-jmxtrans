package org.jmxtrans.embedded.discovery;

import org.junit.Test;

import javax.management.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AutomaticDiscoveryConfigGeneratorTest {

    ConfigJsonSerializer configJsonSerializer = new ConfigJsonSerializer();

    @Test
    public void shouldAutoDiscoverConfig() throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException, AttributeNotFoundException, MBeanException {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        AutomaticDiscoveryConfigGenerator service = new AutomaticDiscoveryConfigGenerator(mbeanServer);
        ConfigModel.Config config = service.autoDiscovery();
        assertNotNull(config);
        configJsonSerializer.serializeConfig(config, new PrintWriter(System.out));
    }

    @Test
    public void shouldReplaceApplicationKeyWithWildcard() {
        ObjectName objectName = mock(ObjectName.class);
        doReturn("\"My Application Display Name\"").when(objectName).getKeyProperty("application");

        DefaultObjectNameTransformer service = new DefaultObjectNameTransformer();

        String result = service.transformObjectName(objectName, "com.mycompany.package:application=\"My Application Display Name\",bean=\"myBean\",name=myBean,type=Bean");
        assertEquals("com.mycompany.package:application=*,bean=\"myBean\",name=myBean,type=Bean", result);
    }

}
