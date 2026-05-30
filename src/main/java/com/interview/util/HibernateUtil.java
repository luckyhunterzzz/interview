package com.interview.util;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Slf4j
public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        try {
            applyEnvironmentOverrides();
            return new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();
        } catch (Exception e) {
            log.error("Failed to create SessionFactory", e);
            throw new ExceptionInInitializerError("Failed to create SessionFactory: " + e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    private static void applyEnvironmentOverrides() {
        System.setProperty("DB_HOST", EnvUtil.getOptional("DB_HOST", "localhost"));
        System.setProperty("DB_PORT", EnvUtil.getOptional("DB_PORT", "5432"));
        System.setProperty("POSTGRES_DB", EnvUtil.getOptional("POSTGRES_DB", "user_service_db"));
        System.setProperty("POSTGRES_USER", EnvUtil.getRequired("POSTGRES_USER"));
        System.setProperty("POSTGRES_PASSWORD", EnvUtil.getRequired("POSTGRES_PASSWORD"));
    }

    public static void shutdown() {
        if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
            log.info("SessionFactory closed");
        }
    }
}
