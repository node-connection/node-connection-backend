package node.connection;

import node.connection._core.config.EnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:properties/env.yaml",
                factory = EnvConfig.class)
public class NodeConnectionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeConnectionBackendApplication.class, args);
    }

}
