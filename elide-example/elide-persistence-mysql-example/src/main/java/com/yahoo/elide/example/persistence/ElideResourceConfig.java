package com.yahoo.elide.example.persistence;

import com.yahoo.elide.Elide;
import com.yahoo.elide.audit.Slf4jLogger;
import com.yahoo.elide.dbmanagers.hibernate5.PersistenceManager;
import com.yahoo.elide.resources.JsonApiEndpoint;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Example application for resource config
 */
public class ElideResourceConfig extends ResourceConfig {
    public ElideResourceConfig() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                JsonApiEndpoint.DefaultOpaqueUserFunction noUserFn = v -> null;
                bind(noUserFn)
                        .to(JsonApiEndpoint.DefaultOpaqueUserFunction.class)
                        .named("elideUserExtractionFunction");

                EntityManagerFactory entityManagerFactory =
                        Persistence.createEntityManagerFactory("com.yahoo.elide.example");
                bind(new Elide(new Slf4jLogger(), new PersistenceManager(entityManagerFactory)))
                        .to(Elide.class).named("elide");
            }
        });
    }
}
