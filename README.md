🎓 Student Admission System

A desktop-based Student Admission Management System built using JavaFX, Hibernate (JPA), and MySQL.
This project was developed as part of the Advanced Java Programming Final Project (Summer 2025, Humber College).

🚀 Features
👤 Applicant

Sign up & log in securely

Update personal details and upload required documents

Select up to 3 programs of interest

Save draft application and submit when ready

View application status (Pending → Under Process → Accepted/Rejected/Conditional)

📝 Registrar

Add, update, or remove applicants

Review and evaluate applications

Change applicant status after evaluation

Generate and export reports (by program, date, applicant, etc.)

🔑 Administrator

Manage registrars (add/remove/update)

Perform all registrar operations

Generate system-wide reports

🛠️ Tech Stack

JavaFX – User Interface

Hibernate ORM (JPA) – Database persistence

MySQL – Relational database

JDBC – Database connectivity (where needed)

OOP Concepts – Encapsulation, Inheritance, Polymorphism, Abstraction

📂 Project Structure
src/
 ├── com.humber.model       # Entity classes (Applicant, Registrar, Admin, etc.)
 ├── com.humber.dao         # Data access layer (Hibernate/JDBC)
 ├── com.humber.service     # Business logic
 ├── com.humber.util        # Utility classes (HibernateUtil, DB connection, etc.)
 ├── com.humber.ui          # JavaFX views (forms, dashboards, reports)
 └── Main.java              # Application entry point
🗄️ Database Schema (MySQL)

Applicants Table → applicant_id, name, username, password, email, phone, status, selected_programs, etc.

Registrars Table → registrar_id, name, username, password, email

Admins Table → admin_id, name, username, password, email

Initial Data: 1 Admin, 2 Registrars, 5 Applicants

📜 License

This project is for educational purposes (Humber College, Advanced Java Programming).
