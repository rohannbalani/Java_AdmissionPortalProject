package com.humber.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory FACTORY = build();
    private static SessionFactory build() {
        try { return new Configuration().configure().buildSessionFactory(); }
        catch (Throwable ex) { throw new ExceptionInInitializerError("SessionFactory init failed: " + ex); }
    }
    public static SessionFactory getSessionFactory() { return FACTORY; }
    public static void shutdown() { FACTORY.close(); }
}
