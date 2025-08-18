package com.humber.service;

import com.humber.model.Admin;
import com.humber.model.Applicant;
import com.humber.model.Registrar;
import com.humber.util.HibernateUtil;
import com.humber.util.PasswordUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;


public class AuthService {


    public Applicant registerApplicant(String fullName, String username, String plainPw,
                                       String phone, String email) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            if (findApplicantByUsername(s, username) != null)
                throw new IllegalArgumentException("Username already exists.");

            Transaction tx = s.beginTransaction();
            Applicant a = new Applicant();
            a.setFullName(fullName);
            a.setUsername(username);
            a.setPasswordHash(PasswordUtil.hash(plainPw));
            a.setPhoneNumber(phone);
            a.setEmail(email);
            try {
                var setCreated = a.getClass().getDeclaredMethod("setCreatedAt", java.time.LocalDateTime.class);
                var setUpdated = a.getClass().getDeclaredMethod("setUpdatedAt", java.time.LocalDateTime.class);
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                setCreated.invoke(a, now);
                setUpdated.invoke(a, now);
            } catch (Exception ignored) {}
            s.persist(a);
            tx.commit();
            return a;
        }
    }

    public Applicant loginApplicant(String username, String plainPw) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Applicant a = findApplicantByUsername(s, username);
            if (a == null) return null;
            return PasswordUtil.hash(plainPw).equals(a.getPasswordHash()) ? a : null;
        }
    }

    private Applicant findApplicantByUsername(Session s, String username) {
        return s.createQuery("from Applicant a where a.username=:u", Applicant.class)
                .setParameter("u", username).uniqueResult();
    }

    public Registrar addRegistrar(String fullName, String username, String plainPw,
                                  String phone, String email) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            if (findRegistrarByUsername(s, username) != null)
                throw new IllegalArgumentException("Registrar username exists.");
            Transaction tx = s.beginTransaction();
            Registrar r = new Registrar();
            r.setFullName(fullName);
            r.setUsername(username);
            r.setPasswordHash(PasswordUtil.hash(plainPw));
            r.setPhoneNumber(phone);
            r.setEmail(email);
            s.persist(r);
            tx.commit();
            return r;
        }
    }

    public Registrar loginRegistrar(String username, String plainPw) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Registrar r = findRegistrarByUsername(s, username);
            if (r == null) return null;
            return PasswordUtil.hash(plainPw).equals(r.getPasswordHash()) ? r : null;
        }
    }

    private Registrar findRegistrarByUsername(Session s, String username) {
        return s.createQuery("from Registrar r where r.username=:u", Registrar.class)
                .setParameter("u", username).uniqueResult();
    }


    public Admin loginAdmin(String username, String plainPw) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Admin a = findAdminByUsername(s, username);
            if (a == null) return null;
            return PasswordUtil.hash(plainPw).equals(a.getPasswordHash()) ? a : null;
        }
    }

    private Admin findAdminByUsername(Session s, String username) {
        return s.createQuery("from Admin a where a.username=:u", Admin.class)
                .setParameter("u", username).uniqueResult();
    }
}
