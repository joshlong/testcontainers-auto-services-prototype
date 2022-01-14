package autoservices;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.Map;

public class MongoEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getProperty("spring.data.mongodb.uri") == null) {
            var mongoDBContainer = new MongoDBContainer("mongo:5.0.3");
            Startables.deepStart(mongoDBContainer).join();
            environment.getPropertySources().addLast(
                    new MapPropertySource("testcontainers",
                            Map.of("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl()))
            );
        }
    }
}
