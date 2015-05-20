package org.jmxtrans.embedded.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.Writer;

public class ConfigJsonSerializer {

    public void serializeConfig(ConfigModel.Config config, Writer w) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(w, config);
    }

}
