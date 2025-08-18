package com.humber.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stu_applicants")
public class Applicant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicant_id") private Integer id;

    @Column(name = "full_name", nullable = false, length = 100) private String fullName;
    @Column(name = "username",  nullable = false, unique = true, length = 50) private String username;
    @Column(name = "password",  nullable = false, length = 255) private String passwordHash;
    @Column(name = "phone_number", length = 20) private String phoneNumber;
    @Column(name = "email", length = 100) private String email;

    // Additional personal information
    @Column(name = "address", length = 200) private String address;
    @Column(name = "city", length = 50) private String city;
    @Column(name = "province", length = 50) private String province;
    @Column(name = "postal_code", length = 10) private String postalCode;
    @Column(name = "date_of_birth") private LocalDateTime dateOfBirth;

    // Document paths
    @Column(name = "resume_path", length = 255) private String resumePath;
    @Column(name = "transcript_path", length = 255) private String transcriptPath;
    @Column(name = "id_document_path", length = 255) private String idDocumentPath;
    @Column(name = "other_document_path", length = 255) private String otherDocumentPath;

    @Column(name = "status", nullable = false, length = 255) private String status = "Pending";
    @Column(name = "program_1", length = 100) private String program1;
    @Column(name = "program_2", length = 100) private String program2;
    @Column(name = "program_3", length = 100) private String program3;

    @Column(name = "is_submitted", nullable = false) private Boolean submitted = false;
    @Column(name = "is_saved", nullable = false) private Boolean saved = false;

    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;


    public Integer getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { fullName = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { passwordHash = v; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String v) { phoneNumber = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }

    // Additional personal information getters/setters
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }
    public String getCity() { return city; }
    public void setCity(String v) { city = v; }
    public String getProvince() { return province; }
    public void setProvince(String v) { province = v; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String v) { postalCode = v; }
    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime v) { dateOfBirth = v; }

    // Document paths getters/setters
    public String getResumePath() { return resumePath; }
    public void setResumePath(String v) { resumePath = v; }
    public String getTranscriptPath() { return transcriptPath; }
    public void setTranscriptPath(String v) { transcriptPath = v; }
    public String getIdDocumentPath() { return idDocumentPath; }
    public void setIdDocumentPath(String v) { idDocumentPath = v; }
    public String getOtherDocumentPath() { return otherDocumentPath; }
    public void setOtherDocumentPath(String v) { otherDocumentPath = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public String getProgram1() { return program1; }
    public void setProgram1(String v) { program1 = v; }
    public String getProgram2() { return program2; }
    public void setProgram2(String v) { program2 = v; }
    public String getProgram3() { return program3; }
    public void setProgram3(String v) { program3 = v; }
    public Boolean getSubmitted() { return submitted; }
    public void setSubmitted(Boolean v) { submitted = v; }
    public Boolean getSaved() { return saved; }
    public void setSaved(Boolean v) { saved = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
}
