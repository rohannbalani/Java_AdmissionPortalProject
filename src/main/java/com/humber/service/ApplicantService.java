package com.humber.service;

import com.humber.model.Applicant;
import com.humber.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApplicantService {

    public Applicant createApplicant(String fullName, String username, String plainPw,
                                     String phone, String email) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            // Ensure unique username
            Applicant existing = s.createQuery("from Applicant a where a.username=:u", Applicant.class)
                    .setParameter("u", username).uniqueResult();
            if (existing != null) {
                tx.rollback();
                throw new IllegalArgumentException("Username already exists");
            }
            Applicant a = new Applicant();
            a.setFullName(fullName);
            a.setUsername(username);
            a.setPasswordHash(com.humber.util.PasswordUtil.hash(plainPw != null ? plainPw : "changeme"));
            a.setPhoneNumber(phone);
            a.setEmail(email);
            a.setStatus("Pending");
            LocalDateTime now = LocalDateTime.now();
            try {

                a.getClass().getDeclaredMethod("getCreatedAt");
                var setCreated = a.getClass().getDeclaredMethod("setCreatedAt", LocalDateTime.class);
                var setUpdated = a.getClass().getDeclaredMethod("setUpdatedAt", LocalDateTime.class);
                setCreated.invoke(a, now);
                setUpdated.invoke(a, now);
            } catch (NoSuchMethodException ignored) {}
            s.persist(a);
            tx.commit();
            return a;
        } catch (RuntimeException re) { throw re; }
        catch (Exception e) { throw new RuntimeException(e); }
    }


    public Applicant updateApplicant(Applicant updated) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            try {
                var setUpdated = updated.getClass().getDeclaredMethod("setUpdatedAt", LocalDateTime.class);
                setUpdated.invoke(updated, LocalDateTime.now());
            } catch (Exception ignored) {}
            Applicant merged = (Applicant) s.merge(updated);
            tx.commit();
            return merged;
        }
    }


    public boolean removeApplicant(int applicantId) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            Applicant a = s.get(Applicant.class, applicantId);
            if (a == null) { tx.rollback(); return false; }
            s.remove(a);
            tx.commit();
            return true;
        }
    }







    public List<Applicant> search(String namePart, String program, LocalDate from, LocalDate to) {
        StringBuilder hql = new StringBuilder("from Applicant a where 1=1");

        boolean hasName = namePart != null && !namePart.isBlank();
        boolean hasProgram = program != null && !program.isBlank();
        boolean hasDate = from != null || to != null;

        if (hasName) {
            hql.append(" and lower(a.fullName) like :name");
        }
        if (hasProgram) {
            hql.append(" and (a.program1 = :program or a.program2 = :program or a.program3 = :program)");
        }

        LocalDateTime start = null, end = null;
        if (hasDate) {
            start = from != null ? from.atStartOfDay() : LocalDate.MIN.atStartOfDay();
            end = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.MAX.atTime(23,59,59);
            hql.append(" and a.createdAt between :start and :end");
        }

        hql.append(" order by a.fullName");

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            var q = s.createQuery(hql.toString(), Applicant.class);
            if (hasName) {
                q.setParameter("name", "%" + namePart.toLowerCase() + "%");
            }
            if (hasProgram) {
                q.setParameter("program", program);
            }
            if (hasDate) {
                q.setParameter("start", start);
                q.setParameter("end", end);
            }
            return q.list();
        }
    }


    public Applicant updateFinalStatus(int applicantId, String status, String conditionalMessage) {
        String normalized = normalizeStatus(status);
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            Applicant a = s.get(Applicant.class, applicantId);
            if (a == null) { tx.rollback(); throw new IllegalArgumentException("Applicant not found"); }
            if (normalized.equals("Conditionally Accepted")) {
                if (conditionalMessage != null && !conditionalMessage.isBlank()) {
                    a.setStatus("Conditionally Accepted: " + conditionalMessage.trim());
                } else {
                    a.setStatus("Conditionally Accepted");
                }
            } else {
                a.setStatus(normalized);
            }
            try { a.getClass().getDeclaredMethod("setUpdatedAt", LocalDateTime.class)
                    .invoke(a, LocalDateTime.now()); } catch (Exception ignored) {}
            s.merge(a);
            tx.commit();
            return a;
        }
    }

    private String normalizeStatus(String status) {
        if (status == null) throw new IllegalArgumentException("Status required");
        String s = status.trim().toLowerCase();
        return switch (s) {
            case "accepted" -> "Accepted";
            case "rejected" -> "Rejected";
            case "conditionally accepted", "conditional", "conditionally" -> "Conditionally Accepted";
            default -> throw new IllegalArgumentException("Invalid status: " + status);
        };
    }


    public String generateReport(List<Applicant> applicants, String title) {
        if (applicants == null) applicants = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(title == null ? "Applicants Report" : title).append(" ===\n");
        sb.append("Count: ").append(applicants.size()).append("\n\n");
        for (Applicant a : applicants) {
            sb.append("ID: ").append(a.getId()).append(" | ")
              .append(a.getFullName()).append(" | Email: ").append(nullToEmpty(a.getEmail()))
              .append(" | Phone: ").append(nullToEmpty(a.getPhoneNumber()))
              .append(" | Status: ").append(nullToEmpty(a.getStatus()))
              .append(" | Programs: ")
              .append(nullToEmpty(a.getProgram1()));
            if (a.getProgram2() != null) sb.append(", ").append(a.getProgram2());
            if (a.getProgram3() != null) sb.append(", ").append(a.getProgram3());
            sb.append("\n");
        }
        return sb.toString();
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
