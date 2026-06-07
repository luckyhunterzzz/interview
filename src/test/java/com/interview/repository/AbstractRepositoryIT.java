package com.interview.repository;

import com.interview.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
abstract class AbstractRepositoryIT {

    @Container
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("user_service_test_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @BeforeAll
    static void beforeAll() {
        System.setProperty("DB_HOST", POSTGRESQL_CONTAINER.getHost());
        System.setProperty("DB_PORT", String.valueOf(POSTGRESQL_CONTAINER.getMappedPort(5432)));
        System.setProperty("POSTGRES_DB", POSTGRESQL_CONTAINER.getDatabaseName());
        System.setProperty("POSTGRES_USER", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("POSTGRES_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
        HibernateUtil.shutdown();
    }

    @BeforeEach
    void cleanDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY").executeUpdate();
            transaction.commit();
        }
    }

    @AfterAll
    static void afterAll() {
        HibernateUtil.shutdown();
        System.clearProperty("DB_HOST");
        System.clearProperty("DB_PORT");
        System.clearProperty("POSTGRES_DB");
        System.clearProperty("POSTGRES_USER");
        System.clearProperty("POSTGRES_PASSWORD");
    }
}
