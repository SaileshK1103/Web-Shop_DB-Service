package com.webshop;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SchemaMappingTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    // The context only starts when Hibernate's schema validation (ddl-auto: validate)
    // accepts every entity against sql/schema_no_data_clean.sql.
    @Test
    void entitiesMatchTheDatabaseSchema() {
        assertThat(entityManagerFactory.getMetamodel().getEntities()).hasSize(8);
    }
}
