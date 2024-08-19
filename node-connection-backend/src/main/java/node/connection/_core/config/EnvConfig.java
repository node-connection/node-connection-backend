package node.connection._core.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;
import java.util.Properties;

public class EnvConfig implements PropertySourceFactory {
    @NotNull
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(resource.getResource());
        Properties properties = bean.getObject();
        assert properties != null;
        return new PropertiesPropertySource(Objects.requireNonNull(resource.getResource().getFilename()), properties);
    }
}
