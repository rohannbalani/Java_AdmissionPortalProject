package com.humber.tools;

import com.humber.util.PasswordUtil;
import com.humber.model.Admin;
import com.humber.model.Applicant;
import com.humber.model.Registrar;
import com.humber.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class MigratePasswords {
    private static boolean isHashed(String s) {
        return s != null && (s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$"));
    }

    public static void main(String[] args) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();


            List<Applicant> apps = s.createQuery("from Applicant", Applicant.class).list();
            for (Applicant a : apps) {
                if (!isHashed(a.getPasswordHash())) {
                    a.setPasswordHash(PasswordUtil.hash(a.getPasswordHash()));
                    System.out.println("Hashed applicant: " + a.getUsername());
                }
            }


            List<Registrar> regs = s.createQuery("from Registrar", Registrar.class).list();
            for (Registrar r : regs) {
                if (!isHashed(r.getPasswordHash())) {
                    r.setPasswordHash(PasswordUtil.hash(r.getPasswordHash()));
                    System.out.println("Hashed registrar: " + r.getUsername());
                }
            }

            List<Admin> admins = s.createQuery("from Admin", Admin.class).list();
            for (Admin a : admins) {
                if (!isHashed(a.getPasswordHash())) {
                    a.setPasswordHash(PasswordUtil.hash(a.getPasswordHash()));
                    System.out.println("Hashed admin: " + a.getUsername());
                }
            }

            tx.commit();
            System.out.println("Done.");
        }
        HibernateUtil.shutdown();
    }
}
