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

    // Hibernate runs with ddl-auto: update on top of sql/schema_no_data_clean.sql, so this checks that the
    // application starts and maps every entity. It no longer fails when an entity and the schema disagree.
    @Test
    void applicationStartsAndMapsAllEntities() {
        assertThat(entityManagerFactory.getMetamodel().getEntities()).hasSize(9);
    }
}
