package node.connection._core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import node.connection.hyperledger.fabric.ca.CAEnrollment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean("CustomModule")
    public SimpleModule simpleModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(CAEnrollment.class, new CAEnrollment.Serializer());
        simpleModule.addDeserializer(CAEnrollment.class, new CAEnrollment.DeSerializer());

        return simpleModule;
    }

    @Bean
    public ObjectMapper objectMapper(@Qualifier("CustomModule") SimpleModule simpleModule) {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(timeModule);
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }
}
