package com.example.profiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.devtools.restart.Restarter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.function.Function;

public class TestcontainersSpringApplicationRunListener implements SpringApplicationRunListener {

    public TestcontainersSpringApplicationRunListener(SpringApplication app, String[] args) {
        app.addInitializers(configurableApplicationContext -> {
            var environment = configurableApplicationContext.getEnvironment();
//            environment.getPropertySources().addLast(new MongoDBPropertySource(environment));
            environment.getPropertySources().addLast(new PostgreSQLPropertySource(environment));
        });
    }

    private static class PostgreSQLPropertySource extends ContainerPropertySource<PostgreSQLContainer<?>> {

        private final ConfigurableEnvironment environment;

        public PostgreSQLPropertySource(ConfigurableEnvironment environment) {
            super("org.testcontainers.postgresql", Map.of(
                    "spring.r2dbc.url", it -> {
                        return String.format(
                                "r2dbc:postgresql://%s:%d/test",
                                it.getHost(),
                                it.getMappedPort(5432)
                        );
                    },
                    "spring.r2dbc.username", PostgreSQLContainer::getUsername,
                    "spring.r2dbc.password", PostgreSQLContainer::getPassword,
                    "spring.datasource.url", PostgreSQLContainer::getJdbcUrl,
                    "spring.datasource.username", PostgreSQLContainer::getUsername,
                    "spring.datasource.password", PostgreSQLContainer::getPassword
            ));
            this.environment = environment;
        }

        @Override
        protected PostgreSQLContainer<?> createContainer() {
            return new PostgreSQLContainer<>(
                    environment.getProperty("testcontainers.mongodb.postgres", "postgres:13.3")
            );
        }
    }

  /*  private static class MongoDBPropertySource extends ContainerPropertySource<MongoDBContainer> {
        private final Environment environment;

        public MongoDBPropertySource(Environment environment) {
            super("org.testcontainers.mongodb", Map.of(
                    "spring.data.mongodb.uri", MongoDBContainer::getReplicaSetUrl
            ));
            this.environment = environment;
        }

        @Override
        protected MongoDBContainer createContainer() {
            return new MongoDBContainer(
                    environment.getProperty("testcontainers.mongodb.image", "mongo:4.0.10")
            );
        }
    }
*/
    /**
     * Base class for Testcontainers-based property sources
     */
    private abstract static class ContainerPropertySource<T extends GenericContainer> extends EnumerablePropertySource<T> {

        private final Map<String, Function<T, Object>> propertyMappers;

        public ContainerPropertySource(String name, Map<String, Function<T, Object>> propertyMappers) {
            super(name);
            this.propertyMappers = propertyMappers;
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
            var mapper = propertyMappers.get(name);
            if (mapper == null) {
                return null;
            }

            return mapper.apply(
                    (T) Restarter.getInstance().getOrAddAttribute(
                            "ContainerPropertySource." + getName(),
                            () -> {
                                var result = createContainer();
                                result.start();
                                return result;
                            }
                    )
            );
        }
    }
}
