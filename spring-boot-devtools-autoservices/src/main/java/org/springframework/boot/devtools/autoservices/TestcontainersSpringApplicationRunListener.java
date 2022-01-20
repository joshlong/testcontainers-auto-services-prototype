package org.springframework.boot.devtools.autoservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.devtools.restart.Restarter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TestcontainersSpringApplicationRunListener implements SpringApplicationRunListener {

    public TestcontainersSpringApplicationRunListener(SpringApplication app, String[] args) {
        app.addInitializers(configurableApplicationContext -> {
            try {
                Restarter.getInstance();
            } catch (Throwable ignored) {
                // Restarter is not initialized, exiting
                return;
            }

            ConfigurableEnvironment environment = configurableApplicationContext.getEnvironment();
            environment.getPropertySources().addLast(new MongoDBPropertySource(environment));
            environment.getPropertySources().addLast(new PostgreSQLPropertySource(environment));
        });
    }

    private static class PostgreSQLPropertySource extends ContainerPropertySource<PostgreSQLContainer<?>> {

        private final ConfigurableEnvironment environment;

        public PostgreSQLPropertySource(ConfigurableEnvironment environment) {
            super("org.testcontainers.postgresql");
            this.environment = environment;

            addPropertyMapper("spring.r2dbc.url", it -> {
                return String.format(
                        "r2dbc:postgresql://%s:%d/test",
                        it.getHost(),
                        it.getMappedPort(5432)
                );
            });
            addPropertyMapper("spring.r2dbc.username", PostgreSQLContainer::getUsername);
            addPropertyMapper("spring.r2dbc.password", PostgreSQLContainer::getPassword);
            addPropertyMapper("spring.datasource.url", PostgreSQLContainer::getJdbcUrl);
            addPropertyMapper("spring.datasource.username", PostgreSQLContainer::getUsername);
            addPropertyMapper("spring.datasource.password", PostgreSQLContainer::getPassword);
        }

        @Override
        protected PostgreSQLContainer<?> createContainer() {
            String image = environment.getProperty("testcontainers.mongodb.postgres", "postgres:13.3");
            return new PostgreSQLContainer<>(image)
                    .withReuse(true);
        }
    }

    private static class MongoDBPropertySource extends ContainerPropertySource<MongoDBContainer> {
        private final Environment environment;

        public MongoDBPropertySource(Environment environment) {
            super("org.testcontainers.mongodb");
            this.environment = environment;
            addPropertyMapper("spring.data.mongodb.uri", MongoDBContainer::getReplicaSetUrl);
        }

        @Override
        protected MongoDBContainer createContainer() {
            String image = environment.getProperty("testcontainers.mongodb.image", "mongo:4.0.10");
            return new MongoDBContainer(image)
                    .withReuse(true);
        }
    }

    /**
     * Base class for Testcontainers-based property sources
     */
    private abstract static class ContainerPropertySource<T extends GenericContainer<?>> extends EnumerablePropertySource<T> {

        private final Map<String, Function<T, Object>> propertyMappers = new HashMap<>();

        public ContainerPropertySource(String name) {
            super(name);
        }

        protected void addPropertyMapper(String name, Function<T, Object> mapper) {
            propertyMappers.put(name, mapper);
        }

        protected abstract T createContainer();

        @Override
        public String[] getPropertyNames() {
            return propertyMappers.keySet().toArray(new String[0]);
        }

        @Override
        public boolean containsProperty(String name) {
            return propertyMappers.containsKey(name);
        }

        @Override
        public Object getProperty(String name) {
            Function<T, Object> mapper = propertyMappers.get(name);
            if (mapper == null) {
                return null;
            }

            return mapper.apply(
                    (T) Restarter.getInstance().getOrAddAttribute(
                            "ContainerPropertySource." + getName(),
                            () -> {
                                T result = createContainer();
                                result.start();
                                return result;
                            }
                    )
            );
        }
    }
}
