package com.humber;

import com.humber.model.Applicant;
import com.humber.model.Registrar;
import com.humber.service.ApplicantService;
import com.humber.service.AuthService;
import com.humber.util.HibernateUtil;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class App extends Application {
    private AuthService authService;
    private ApplicantService applicantService;
    private com.humber.model.Admin currentAdmin;

    @Override
    public void start(Stage primaryStage) {
        authService = new AuthService();
        applicantService = new ApplicantService();

        VBox mainMenu = createMainMenu(primaryStage);


        Scene scene = new Scene(mainMenu, 400, 500);
        primaryStage.setTitle("Humber Admission System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void testDatabaseConnection() {

        try {
            HibernateUtil.getSessionFactory().openSession().close();
        } catch (Exception e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    private VBox createMainMenu(Stage primaryStage) {
        Label titleLabel = new Label("Humber Admission System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button applicantLoginBtn = new Button("Applicant Login");
        Button registrarLoginBtn = new Button("Registrar Login");
        Button adminLoginBtn = new Button("Admin Login");


        applicantLoginBtn.setOnAction(e -> showApplicantLoginForm(primaryStage));
        registrarLoginBtn.setOnAction(e -> showRegistrarLoginForm(primaryStage));
        adminLoginBtn.setOnAction(e -> showAdminLoginForm(primaryStage));


        applicantLoginBtn.setPrefWidth(200);
        registrarLoginBtn.setPrefWidth(200);
        adminLoginBtn.setPrefWidth(200);


        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(
            titleLabel,
            applicantLoginBtn,
            registrarLoginBtn,
            adminLoginBtn

        );

        return vbox;
    }

    private void showApplicantRegistrationForm(Stage primaryStage) {
        Label titleLabel = new Label("Applicant Registration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Button registerButton = new Button("Register");
        Button backButton = new Button("Back to Main Menu");

        registerButton.setOnAction(e -> {
            try {
                String fullName = nameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();


                if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    showAlert("Error", "All fields except phone are required.");
                    return;
                }


                authService.registerApplicant(fullName, username, password, phone, email);
                showAlert("Success", "Registration successful! You can now log in.");


                showApplicantLoginForm(primaryStage);
            } catch (IllegalArgumentException ex) {
                showAlert("Registration Error", "Registration failed: " + ex.getMessage());
            } catch (Exception ex) {
                showAlert("Database Error", "Database operation failed: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> {
            VBox mainMenu = createMainMenu(primaryStage);
            Scene scene = new Scene(mainMenu, 400, 500);
            primaryStage.setScene(scene);
        });


        VBox formLayout = new VBox(10);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));
        formLayout.getChildren().addAll(
            titleLabel,
            nameLabel, nameField,
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            phoneLabel, phoneField,
            emailLabel, emailField,
            registerButton,
            backButton
        );


        Scene scene = new Scene(formLayout, 400, 500);
        primaryStage.setScene(scene);
    }

    private void showApplicantLoginForm(Stage primaryStage) {


        Label titleLabel = new Label("Applicant Login");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register New Account");
        Button backButton = new Button("Back to Main Menu");



        loginButton.setOnAction(e -> {
            try {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();


                if (username.isEmpty() || password.isEmpty()) {
                    showAlert("Error", "Username and password are required.");
                    return;
                }


                var applicant = authService.loginApplicant(username, password);
                if (applicant != null) {
                    showAlert("Success", "Welcome, " + applicant.getFullName() + "!");


                    showApplicantDashboard(primaryStage, applicant);
                } else {
                    showAlert("Error", "Invalid username or password.");
                }
            } catch (IllegalArgumentException ex) {
                showAlert("Login Error", "Login failed: " + ex.getMessage());
            } catch (Exception ex) {
                showAlert("Database Error", "Database operation failed: " + ex.getMessage());
            }
        });

        registerButton.setOnAction(e -> showApplicantRegistrationForm(primaryStage));

        backButton.setOnAction(e -> {
            VBox mainMenu = createMainMenu(primaryStage);
            Scene scene = new Scene(mainMenu, 400, 500);
            primaryStage.setScene(scene);
        });



        VBox formLayout = new VBox(10);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));
        formLayout.getChildren().addAll(
            titleLabel,
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            loginButton,
            registerButton,
            backButton
        );



        Scene scene = new Scene(formLayout, 400, 500);
        primaryStage.setScene(scene);
    }

    private void showRegistrarLoginForm(Stage primaryStage) {


        Label titleLabel = new Label("Registrar Login");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register New Account");
        Button backButton = new Button("Back to Main Menu");



        loginButton.setOnAction(e -> {
            try {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();



                if (username.isEmpty() || password.isEmpty()) {
                    showAlert("Error", "Username and password are required.");
                    return;
                }



                var registrar = authService.loginRegistrar(username, password);
                if (registrar != null) {
                    showAlert("Success", "Welcome, Registrar " + registrar.getFullName() + "!");


                    showRegistrarDashboard(primaryStage, registrar);
                } else {
                    showAlert("Error", "Invalid username or password.");
                }
            } catch (IllegalArgumentException ex) {
                showAlert("Login Error", "Login failed: " + ex.getMessage());
            } catch (Exception ex) {
                showAlert("Database Error", "Database operation failed: " + ex.getMessage());
            }
        });

        registerButton.setOnAction(e -> showRegistrarRegistrationForm(primaryStage));

        backButton.setOnAction(e -> {
            VBox mainMenu = createMainMenu(primaryStage);
            Scene scene = new Scene(mainMenu, 400, 500);
            primaryStage.setScene(scene);
        });



        VBox formLayout = new VBox(10);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));
        formLayout.getChildren().addAll(
            titleLabel,
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            loginButton,
            registerButton,
            backButton
        );



        Scene scene = new Scene(formLayout, 400, 500);
        primaryStage.setScene(scene);
    }

    private void showAdminLoginForm(Stage primaryStage) {


        Label titleLabel = new Label("Admin Login");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        Button backButton = new Button("Back to Main Menu");


        loginButton.setOnAction(e -> {
            try {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();


                if (username.isEmpty() || password.isEmpty()) {
                    showAlert("Error", "Username and password are required.");
                    return;
                }


                var admin = authService.loginAdmin(username, password);
                if (admin != null) {
                    showAlert("Success", "Welcome, Admin " + admin.getFullName() + "!");


                    currentAdmin = admin;
                    showAdminDashboard(primaryStage, admin);
                } else {
                    showAlert("Error", "Invalid username or password.");
                }
            } catch (IllegalArgumentException ex) {
                showAlert("Login Error", "Login failed: " + ex.getMessage());
            } catch (Exception ex) {
                showAlert("Database Error", "Database operation failed: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> {
            VBox mainMenu = createMainMenu(primaryStage);
            Scene scene = new Scene(mainMenu, 400, 500);
            primaryStage.setScene(scene);
        });


        VBox formLayout = new VBox(10);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));
        formLayout.getChildren().addAll(
            titleLabel,
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            loginButton,
            backButton
        );



        Scene scene = new Scene(formLayout, 400, 500);
        primaryStage.setScene(scene);
    }



    private void showApplicantDashboard(Stage primaryStage, com.humber.model.Applicant applicant) {


        Label titleLabel = new Label("Applicant Dashboard");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label welcomeLabel = new Label("Welcome, " + applicant.getFullName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 14px;");

        Label statusLabel = new Label("Application Status: " + applicant.getStatus());


        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


        Tab personalInfoTab = new Tab("Personal Information");
        VBox personalInfoContent = createPersonalInfoForm(applicant);
        personalInfoTab.setContent(personalInfoContent);


        Tab programSelectionTab = new Tab("Program Selection");
        VBox programSelectionContent = createProgramSelectionForm(primaryStage, applicant);
        programSelectionTab.setContent(programSelectionContent);


        Tab documentUploadTab = new Tab("Document Upload");
        VBox documentUploadContent = createDocumentUploadForm(applicant);
        documentUploadTab.setContent(documentUploadContent);


        tabPane.getTabs().addAll(personalInfoTab, programSelectionTab, documentUploadTab);


        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Save Application");
        saveButton.setOnAction(e -> saveApplication(applicant, primaryStage));

        Button submitButton = new Button("Submit Application");
        submitButton.setOnAction(e -> submitApplication(applicant, primaryStage));

        Button printButton = new Button("Print Application");
        printButton.setOnAction(e -> printApplication(applicant));

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            VBox mainMenu = createMainMenu(primaryStage);
            Scene scene = new Scene(mainMenu, 600, 700);
            primaryStage.setScene(scene);
        });


        if (applicant.getSubmitted()) {
            submitButton.setDisable(true);
            saveButton.setDisable(true);
        }

        actionButtons.getChildren().addAll(saveButton, submitButton, printButton, logoutButton);



        VBox dashboardLayout = new VBox(15);
        dashboardLayout.setAlignment(Pos.CENTER);
        dashboardLayout.setPadding(new Insets(20));
        dashboardLayout.getChildren().addAll(
            titleLabel,
            welcomeLabel,
            statusLabel,
            tabPane,
            actionButtons
        );



        ScrollPane scrollPane = new ScrollPane(dashboardLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);


        Scene scene = new Scene(scrollPane, 600, 700);
        primaryStage.setScene(scene);
    }

    private VBox createPersonalInfoForm(com.humber.model.Applicant applicant) {
        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.CENTER);

        Label sectionTitle = new Label("Update Your Personal Information");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);



        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField(applicant.getFullName());
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);



        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField(applicant.getPhoneNumber());
        grid.add(phoneLabel, 0, 1);
        grid.add(phoneField, 1, 1);


        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(applicant.getEmail());
        grid.add(emailLabel, 0, 2);
        grid.add(emailField, 1, 2);


        Label addressLabel = new Label("Address:");
        TextField addressField = new TextField(applicant.getAddress());
        grid.add(addressLabel, 0, 3);
        grid.add(addressField, 1, 3);



        Label cityLabel = new Label("City:");
        TextField cityField = new TextField(applicant.getCity());
        grid.add(cityLabel, 0, 4);
        grid.add(cityField, 1, 4);


        Label provinceLabel = new Label("Province:");
        TextField provinceField = new TextField(applicant.getProvince());
        grid.add(provinceLabel, 0, 5);
        grid.add(provinceField, 1, 5);


        Label postalCodeLabel = new Label("Postal Code:");
        TextField postalCodeField = new TextField(applicant.getPostalCode());
        grid.add(postalCodeLabel, 0, 6);
        grid.add(postalCodeField, 1, 6);



        Label dobLabel = new Label("Date of Birth:");
        DatePicker dobPicker = new DatePicker();
        if (applicant.getDateOfBirth() != null) {
            dobPicker.setValue(applicant.getDateOfBirth().toLocalDate());
        }
        grid.add(dobLabel, 0, 7);
        grid.add(dobPicker, 1, 7);

        Button updateButton = new Button("Update Information");
        updateButton.setOnAction(e -> {
            try {


                applicant.setFullName(nameField.getText().trim());
                applicant.setPhoneNumber(phoneField.getText().trim());
                applicant.setEmail(emailField.getText().trim());
                applicant.setAddress(addressField.getText().trim());
                applicant.setCity(cityField.getText().trim());
                applicant.setProvince(provinceField.getText().trim());
                applicant.setPostalCode(postalCodeField.getText().trim());

                if (dobPicker.getValue() != null) {
                    applicant.setDateOfBirth(LocalDateTime.of(dobPicker.getValue(), LocalDateTime.now().toLocalTime()));
                }


                updateApplicant(applicant);

                showAlert("Success", "Personal information updated successfully!");
            } catch (Exception ex) {
                showAlert("Error", "Failed to update personal information: " + ex.getMessage());
            }
        });



        if (applicant.getSubmitted()) {
            nameField.setDisable(true);
            phoneField.setDisable(true);
            emailField.setDisable(true);
            addressField.setDisable(true);
            cityField.setDisable(true);
            provinceField.setDisable(true);
            postalCodeField.setDisable(true);
            dobPicker.setDisable(true);
            updateButton.setDisable(true);
        }

        formLayout.getChildren().addAll(sectionTitle, grid, updateButton);
        return formLayout;
    }

    private VBox createProgramSelectionForm(Stage primaryStage, com.humber.model.Applicant applicant) {
        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.CENTER);

        Label sectionTitle = new Label("Select Your Programs");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");



        Label program1Label = new Label("First Choice Program:");
        ComboBox<String> program1ComboBox = new ComboBox<>();
        program1ComboBox.setPromptText("Select First Choice Program");
        program1ComboBox.setPrefWidth(300);

        Label program2Label = new Label("Second Choice Program (Optional):");
        ComboBox<String> program2ComboBox = new ComboBox<>();
        program2ComboBox.setPromptText("Select Second Choice Program");
        program2ComboBox.setPrefWidth(300);

        Label program3Label = new Label("Third Choice Program (Optional):");
        ComboBox<String> program3ComboBox = new ComboBox<>();
        program3ComboBox.setPromptText("Select Third Choice Program");
        program3ComboBox.setPrefWidth(300);



        String[] programOptions = {
                "c++ programming",
                "computer engineering",
                "computer science",
                "information technology solutions",
                "web development",
                "C programming",
                "cybersecurity",
                "DSA",
                "AI"
        };

        program1ComboBox.getItems().addAll(programOptions);
        program2ComboBox.getItems().addAll(programOptions);
        program3ComboBox.getItems().addAll(programOptions);


        if (applicant.getProgram1() != null) {
            program1ComboBox.setValue(applicant.getProgram1());
        }
        if (applicant.getProgram2() != null) {
            program2ComboBox.setValue(applicant.getProgram2());
        }
        if (applicant.getProgram3() != null) {
            program3ComboBox.setValue(applicant.getProgram3());
        }

        Button updateButton = new Button("Update Program Selections");
        updateButton.setOnAction(e -> {
            try {


                String program1 = program1ComboBox.getValue();
                String program2 = program2ComboBox.getValue();
                String program3 = program3ComboBox.getValue();


                if (program1 == null || program1.isEmpty()) {
                    showAlert("Error", "Please select at least your first choice program.");
                    return;
                }


                applicant.setProgram1(program1);
                applicant.setProgram2(program2);
                applicant.setProgram3(program3);

                updateApplicant(applicant);

                showAlert("Success", "Program selections updated successfully!");
            } catch (Exception ex) {
                showAlert("Error", "Failed to update program selections: " + ex.getMessage());
            }
        });


        if (applicant.getSubmitted()) {
            program1ComboBox.setDisable(true);
            program2ComboBox.setDisable(true);
            program3ComboBox.setDisable(true);
            updateButton.setDisable(true);


            Label submittedLabel = new Label("Your application has been submitted with the following programs:");
            Label program1InfoLabel = new Label("First Choice: " + 
                (applicant.getProgram1() != null ? applicant.getProgram1() : "None"));
            Label program2InfoLabel = new Label("Second Choice: " + 
                (applicant.getProgram2() != null ? applicant.getProgram2() : "None"));
            Label program3InfoLabel = new Label("Third Choice: " + 
                (applicant.getProgram3() != null ? applicant.getProgram3() : "None"));

            formLayout.getChildren().addAll(
                sectionTitle,
                submittedLabel,
                program1InfoLabel,
                program2InfoLabel,
                program3InfoLabel
            );
        } else {
            formLayout.getChildren().addAll(
                sectionTitle,
                program1Label, program1ComboBox,
                program2Label, program2ComboBox,
                program3Label, program3ComboBox,
                updateButton
            );
        }

        return formLayout;
    }

    private VBox createDocumentUploadForm(com.humber.model.Applicant applicant) {
        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.CENTER);

        Label sectionTitle = new Label("Upload Required Documents");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox resumeBox = new HBox(10);
        resumeBox.setAlignment(Pos.CENTER_LEFT);
        Label resumeLabel = new Label("Resume:");
        Label resumeStatus = new Label(applicant.getResumePath() != null ? "Uploaded" : "Not Uploaded");
        Button resumeUploadBtn = new Button("Upload Resume");
        resumeUploadBtn.setOnAction(e -> uploadDocument(applicant, "resume"));
        resumeBox.getChildren().addAll(resumeLabel, resumeStatus, resumeUploadBtn);

        HBox transcriptBox = new HBox(10);
        transcriptBox.setAlignment(Pos.CENTER_LEFT);
        Label transcriptLabel = new Label("Transcript:");
        Label transcriptStatus = new Label(applicant.getTranscriptPath() != null ? "Uploaded" : "Not Uploaded");
        Button transcriptUploadBtn = new Button("Upload Transcript");
        transcriptUploadBtn.setOnAction(e -> uploadDocument(applicant, "transcript"));
        transcriptBox.getChildren().addAll(transcriptLabel, transcriptStatus, transcriptUploadBtn);

        HBox idDocBox = new HBox(10);
        idDocBox.setAlignment(Pos.CENTER_LEFT);
        Label idDocLabel = new Label("ID Document:");
        Label idDocStatus = new Label(applicant.getIdDocumentPath() != null ? "Uploaded" : "Not Uploaded");
        Button idDocUploadBtn = new Button("Upload ID Document");
        idDocUploadBtn.setOnAction(e -> uploadDocument(applicant, "id"));
        idDocBox.getChildren().addAll(idDocLabel, idDocStatus, idDocUploadBtn);

        HBox otherDocBox = new HBox(10);
        otherDocBox.setAlignment(Pos.CENTER_LEFT);
        Label otherDocLabel = new Label("Other Document:");
        Label otherDocStatus = new Label(applicant.getOtherDocumentPath() != null ? "Uploaded" : "Not Uploaded");
        Button otherDocUploadBtn = new Button("Upload Other Document");
        otherDocUploadBtn.setOnAction(e -> uploadDocument(applicant, "other"));
        otherDocBox.getChildren().addAll(otherDocLabel, otherDocStatus, otherDocUploadBtn);

        if (applicant.getSubmitted()) {
            resumeUploadBtn.setDisable(true);
            transcriptUploadBtn.setDisable(true);
            idDocUploadBtn.setDisable(true);
            otherDocUploadBtn.setDisable(true);
        }

        formLayout.getChildren().addAll(
            sectionTitle,
            resumeBox,
            transcriptBox,
            idDocBox,
            otherDocBox
        );

        return formLayout;
    }

    private void uploadDocument(com.humber.model.Applicant applicant, String documentType) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Document");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Document Files", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
            );

            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                Path documentsDir = Paths.get("documents", String.valueOf(applicant.getId()));
                Files.createDirectories(documentsDir);

                String fileName = documentType + "_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = documentsDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                String relativePath = "documents/" + applicant.getId() + "/" + fileName;
                switch (documentType) {
                    case "resume":
                        applicant.setResumePath(relativePath);
                        break;
                    case "transcript":
                        applicant.setTranscriptPath(relativePath);
                        break;
                    case "id":
                        applicant.setIdDocumentPath(relativePath);
                        break;
                    case "other":
                        applicant.setOtherDocumentPath(relativePath);
                        break;
                }

                updateApplicant(applicant);

                showAlert("Success", "Document uploaded successfully!");
            }
        } catch (Exception ex) {
            showAlert("Error", "Failed to upload document: " + ex.getMessage());
        }
    }

    private void saveApplication(com.humber.model.Applicant applicant, Stage primaryStage) {
        try {
            applicant.setSaved(true);

            updateApplicant(applicant);

            showAlert("Success", "Application saved successfully! You can continue later.");

            showApplicantDashboard(primaryStage, applicant);
        } catch (Exception ex) {
            showAlert("Error", "Failed to save application: " + ex.getMessage());
        }
    }

    private void submitApplication(com.humber.model.Applicant applicant, Stage primaryStage) {
        try {
            if (applicant.getProgram1() == null || applicant.getProgram1().isEmpty()) {
                showAlert("Error", "Please select at least your first choice program before submitting.");
                return;
            }

            applicant.setSubmitted(true);
            applicant.setSaved(true);

            updateApplicant(applicant);

            showAlert("Success", "Application submitted successfully!");

            showApplicantDashboard(primaryStage, applicant);
        } catch (Exception ex) {
            showAlert("Error", "Failed to submit application: " + ex.getMessage());
        }
    }

    private void printApplication(com.humber.model.Applicant applicant) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print Application");
        alert.setHeaderText("Application Details");

        StringBuilder content = new StringBuilder();
        content.append("Applicant ID: ").append(applicant.getId()).append("\n");
        content.append("Full Name: ").append(applicant.getFullName()).append("\n");
        content.append("Email: ").append(applicant.getEmail()).append("\n");
        content.append("Phone: ").append(applicant.getPhoneNumber()).append("\n");
        content.append("Address: ").append(applicant.getAddress()).append("\n");
        content.append("City: ").append(applicant.getCity()).append("\n");
        content.append("Province: ").append(applicant.getProvince()).append("\n");
        content.append("Postal Code: ").append(applicant.getPostalCode()).append("\n");
        content.append("\nProgram Selections:\n");
        content.append("First Choice: ").append(applicant.getProgram1() != null ? applicant.getProgram1() : "None").append("\n");
        content.append("Second Choice: ").append(applicant.getProgram2() != null ? applicant.getProgram2() : "None").append("\n");
        content.append("Third Choice: ").append(applicant.getProgram3() != null ? applicant.getProgram3() : "None").append("\n");
        content.append("\nApplication Status: ").append(applicant.getStatus()).append("\n");
        content.append("Submitted: ").append(applicant.getSubmitted() ? "Yes" : "No").append("\n");

        alert.setContentText(content.toString());
        alert.showAndWait();

    }

    private void showRegistrarDashboard(Stage primaryStage, com.humber.model.Registrar registrar) {
        Label titleLabel = new Label("Registrar Dashboard");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label welcomeLabel = new Label("Welcome, " + registrar.getFullName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 14px;");

        TextField nameFilter = new TextField();
        nameFilter.setPromptText("Search by name...");

        ComboBox<String> programFilter = new ComboBox<>();
        programFilter.setPromptText("Program (optional)");
        String[] programOptions = {
            "c++ programming",
            "computer engineering",
            "computer science",
            "information technology solutions",
            "web development",
            "C programming",
            "cybersecurity",
            "DSA",
            "AI"
        };
        programFilter.getItems().addAll(programOptions);

        DatePicker fromPicker = new DatePicker();
        fromPicker.setPromptText("From date");
        DatePicker toPicker = new DatePicker();
        toPicker.setPromptText("To date");

        Button searchBtn = new Button("Search");
        Button clearBtn = new Button("Clear");

        HBox filters = new HBox(10, new Label("Name:"), nameFilter,
                new Label("Program:"), programFilter,
                new Label("From:"), fromPicker, new Label("To:"), toPicker,
                searchBtn, clearBtn);
        filters.setAlignment(Pos.CENTER_LEFT);

        TableView<Applicant> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Applicant, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Applicant, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        TableColumn<Applicant, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Applicant, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        TableColumn<Applicant, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<Applicant, String> colProg1 = new TableColumn<>("Program 1");
        colProg1.setCellValueFactory(new PropertyValueFactory<>("program1"));
        table.getColumns().addAll(colId, colName, colEmail, colPhone, colStatus, colProg1);

        ObservableList<Applicant> data = FXCollections.observableArrayList(loadAllApplicants());
        table.setItems(data);

        Button addBtn = new Button("Add Applicant");
        Button editBtn = new Button("Edit Selected");
        Button delBtn = new Button("Delete Selected");
        Button statusBtn = new Button("Update Status");
        Button reportBtn = new Button("Generate Report");
        Button printBtn = new Button("Print Report");
        Button logoutButton = new Button("Logout");

        HBox actions = new HBox(10, addBtn, editBtn, delBtn, statusBtn, reportBtn, printBtn, logoutButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        searchBtn.setOnAction(e -> {
            try {
                String name = nameFilter.getText() != null ? nameFilter.getText().trim() : null;
                String program = programFilter.getValue();
                LocalDate from = fromPicker.getValue();
                LocalDate to = toPicker.getValue();


                List<Applicant> results;
                if ((name == null || name.isBlank()) && (program == null || program.isBlank()) && from == null && to == null) {
                    results = loadAllApplicants();
                } else {
                    results = applicantService.search(name, program, from, to);
                }
                table.setItems(FXCollections.observableArrayList(results));
            } catch (Exception ex) {
                showAlert("Error", "Search failed: " + ex.getMessage());
            }
        });
        clearBtn.setOnAction(e -> {
            nameFilter.clear(); programFilter.setValue(null); fromPicker.setValue(null); toPicker.setValue(null);
            table.setItems(FXCollections.observableArrayList(loadAllApplicants()));
        });

        addBtn.setOnAction(e -> {
            Stage dlg = new Stage();
            dlg.setTitle("Add Applicant");
            TextField fullName = new TextField(); fullName.setPromptText("Full Name");
            TextField username = new TextField(); username.setPromptText("Username");
            PasswordField password = new PasswordField(); password.setPromptText("Password");
            TextField phone = new TextField(); phone.setPromptText("Phone");
            TextField email = new TextField(); email.setPromptText("Email");
            Button save = new Button("Save");
            Button cancel = new Button("Cancel");
            save.setOnAction(ev -> {
                try {
                    if (fullName.getText().isBlank() || username.getText().isBlank() || password.getText().isBlank() || email.getText().isBlank()) {
                        showAlert("Error", "Full name, username, password and email are required.");
                        return;
                    }
                    applicantService.createApplicant(fullName.getText().trim(), username.getText().trim(), password.getText(), phone.getText().trim(), email.getText().trim());
                    table.setItems(FXCollections.observableArrayList(loadAllApplicants()));
                    dlg.close();
                } catch (Exception ex) {
                    showAlert("Error", "Failed to add applicant: " + ex.getMessage());
                }
            });
            cancel.setOnAction(ev -> dlg.close());
            VBox box = new VBox(10, new Label("Full Name"), fullName,
                    new Label("Username"), username,
                    new Label("Password"), password,
                    new Label("Phone"), phone,
                    new Label("Email"), email,
                    new HBox(10, save, cancel));
            box.setPadding(new Insets(15));
            dlg.setScene(new Scene(box, 350, 350));
            dlg.showAndWait();
        });

        editBtn.setOnAction(e -> {
            Applicant sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert("Error", "Please select an applicant."); return; }
            Stage dlg = new Stage();
            dlg.setTitle("Edit Applicant");
            TextField fullName = new TextField(sel.getFullName());
            TextField phone = new TextField(sel.getPhoneNumber());
            TextField email = new TextField(sel.getEmail());
            Button save = new Button("Save");
            Button cancel = new Button("Cancel");
            save.setOnAction(ev -> {
                try {
                    sel.setFullName(fullName.getText().trim());
                    sel.setPhoneNumber(phone.getText().trim());
                    sel.setEmail(email.getText().trim());
                    applicantService.updateApplicant(sel);
                    table.setItems(FXCollections.observableArrayList(loadAllApplicants()));
                    dlg.close();
                } catch (Exception ex) {
                    showAlert("Error", "Failed to update applicant: " + ex.getMessage());
                }
            });
            cancel.setOnAction(ev -> dlg.close());
            VBox box = new VBox(10, new Label("Full Name"), fullName,
                    new Label("Phone"), phone,
                    new Label("Email"), email,
                    new HBox(10, save, cancel));
            box.setPadding(new Insets(15));
            dlg.setScene(new Scene(box, 300, 250));
            dlg.showAndWait();
        });

        delBtn.setOnAction(e -> {
            Applicant sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert("Error", "Please select an applicant."); return; }
            try {
                if (applicantService.removeApplicant(sel.getId())) {
                    table.getItems().remove(sel);
                    showAlert("Success", "Applicant removed.");
                } else {
                    showAlert("Error", "Applicant not found.");
                }
            } catch (Exception ex) { showAlert("Error", "Failed to remove: " + ex.getMessage()); }
        });

        statusBtn.setOnAction(e -> {
            Applicant sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert("Error", "Please select an applicant."); return; }
            Stage dlg = new Stage();
            dlg.setTitle("Update Status");
            ComboBox<String> statusBox = new ComboBox<>();
            statusBox.getItems().addAll("Accepted", "Rejected", "Conditionally Accepted");
            TextField condMsg = new TextField();
            condMsg.setPromptText("Conditional message (optional)");
            Button save = new Button("Apply");
            Button cancel = new Button("Cancel");
            save.setOnAction(ev -> {
                try {
                    String st = statusBox.getValue();
                    if (st == null || st.isBlank()) { showAlert("Error", "Please choose a status."); return; }
                    applicantService.updateFinalStatus(sel.getId(), st, condMsg.getText());
                    table.setItems(FXCollections.observableArrayList(loadAllApplicants()));
                    dlg.close();
                } catch (Exception ex) { showAlert("Error", "Failed to update status: " + ex.getMessage()); }
            });
            cancel.setOnAction(ev -> dlg.close());
            VBox box = new VBox(10, new Label("Status"), statusBox, condMsg, new HBox(10, save, cancel));
            box.setPadding(new Insets(15));
            dlg.setScene(new Scene(box, 320, 180));
            dlg.showAndWait();
        });

        reportBtn.setOnAction(e -> {
            try {
                String report = applicantService.generateReport(table.getItems(), "Applicants Report");
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Report");
                a.setHeaderText("Generated Report");
                a.setContentText(report);
                a.getDialogPane().setPrefWidth(700);
                a.showAndWait();
            } catch (Exception ex) { showAlert("Error", "Failed to generate report: " + ex.getMessage()); }
        });
        printBtn.setOnAction(e -> {
            String report = applicantService.generateReport(table.getItems(), "Applicants Report");
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Print Report");
            a.setHeaderText("Printable Report");
            a.setContentText(report);
            a.getDialogPane().setPrefWidth(700);
            a.showAndWait();
        });

        logoutButton.setOnAction(e -> {
            VBox mainMenu = createMainMenu(primaryStage);
            Scene scene = new Scene(mainMenu, 400, 500);
            primaryStage.setScene(scene);
        });

        VBox dashboardLayout = new VBox(12);
        if (currentAdmin != null && "Administrator Tools".equals(registrar.getFullName())) {
            Button backBtn = new Button("Back to Admin Dashboard");
            backBtn.setOnAction(ev -> showAdminDashboard(primaryStage, currentAdmin));
            dashboardLayout.getChildren().add(backBtn);
        }
        dashboardLayout.getChildren().addAll(titleLabel, welcomeLabel, filters, table, actions);
        dashboardLayout.setAlignment(Pos.TOP_LEFT);
        dashboardLayout.setPadding(new Insets(15));

        Scene scene = new Scene(dashboardLayout, 900, 600);
        primaryStage.setScene(scene);
    }

    private List<Applicant> loadAllApplicants() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Applicant", Applicant.class).list();
        }
    }

    private List<Registrar> loadAllRegistrars() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Registrar", Registrar.class).list();
        }
    }

    private void updateRegistrar(Registrar registrar) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            s.merge(registrar);
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update registrar: " + e.getMessage(), e);
        }
    }

    private boolean removeRegistrar(int registrarId) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.getTransaction();
            tx.begin();
            Registrar r = s.get(Registrar.class, registrarId);
            if (r == null) { tx.rollback(); return false; }
            s.remove(r);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            return false;
        }
    }

    private void showAdminDashboard(Stage primaryStage, com.humber.model.Admin admin) {
        Label titleLabel = new Label("Admin Dashboard");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label welcomeLabel = new Label("Welcome, " + admin.getFullName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 14px;");

        Label applicantsSection = new Label("Applicants Management");
        applicantsSection.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label applicantsHint = new Label("Open full applicants management to search, report, and update statuses.");
        Button openApplicantsBtn = new Button("Open Applicants Management");
        openApplicantsBtn.setOnAction(e -> {
            Registrar pseudo = new Registrar();
            pseudo.setFullName("Administrator Tools");
            showRegistrarDashboard(primaryStage, pseudo);
        });

        Label registrarSection = new Label("Registrars Management");
        registrarSection.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableView<Registrar> regTable = new TableView<>();
        regTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Registrar, Integer> rId = new TableColumn<>("ID");
        rId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Registrar, String> rName = new TableColumn<>("Full Name");
        rName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        TableColumn<Registrar, String> rUser = new TableColumn<>("Username");
        rUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<Registrar, String> rEmail = new TableColumn<>("Email");
        rEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Registrar, String> rPhone = new TableColumn<>("Phone");
        rPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        regTable.getColumns().addAll(rId, rName, rUser, rEmail, rPhone);
        regTable.setItems(FXCollections.observableArrayList(loadAllRegistrars()));

        Button addRegBtn = new Button("Add Registrar");
        Button editRegBtn = new Button("Edit Selected");
        Button delRegBtn = new Button("Delete Selected");
        HBox regActions = new HBox(10, addRegBtn, editRegBtn, delRegBtn);
        regActions.setAlignment(Pos.CENTER_LEFT);

        addRegBtn.setOnAction(e -> {
            Stage dlg = new Stage();
            dlg.setTitle("Add Registrar");
            TextField fullName = new TextField(); fullName.setPromptText("Full Name");
            TextField username = new TextField(); username.setPromptText("Username");
            PasswordField password = new PasswordField(); password.setPromptText("Password");
            TextField phone = new TextField(); phone.setPromptText("Phone");
            TextField email = new TextField(); email.setPromptText("Email");
            Button save = new Button("Save");
            Button cancel = new Button("Cancel");
            save.setOnAction(ev -> {
                try {
                    if (fullName.getText().isBlank() || username.getText().isBlank() || password.getText().isBlank() || email.getText().isBlank()) {
                        showAlert("Error", "Full name, username, password and email are required.");
                        return;
                    }
                    authService.addRegistrar(fullName.getText().trim(), username.getText().trim(), password.getText(), phone.getText().trim(), email.getText().trim());
                    regTable.setItems(FXCollections.observableArrayList(loadAllRegistrars()));
                    dlg.close();
                } catch (Exception ex) { showAlert("Error", "Failed to add registrar: " + ex.getMessage()); }
            });
            cancel.setOnAction(ev -> dlg.close());
            VBox box = new VBox(10,
                    new Label("Full Name"), fullName,
                    new Label("Username"), username,
                    new Label("Password"), password,
                    new Label("Phone"), phone,
                    new Label("Email"), email,
                    new HBox(10, save, cancel)
            );
            box.setPadding(new Insets(15));
            dlg.setScene(new Scene(box, 350, 350));
            dlg.showAndWait();
        });

        editRegBtn.setOnAction(e -> {
            Registrar sel = regTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert("Error", "Please select a registrar."); return; }
            Stage dlg = new Stage();
            dlg.setTitle("Edit Registrar");
            TextField fullName = new TextField(sel.getFullName());
            TextField phone = new TextField(sel.getPhoneNumber());
            TextField email = new TextField(sel.getEmail());
            Button save = new Button("Save");
            Button cancel = new Button("Cancel");
            save.setOnAction(ev -> {
                try {
                    sel.setFullName(fullName.getText().trim());
                    sel.setPhoneNumber(phone.getText().trim());
                    sel.setEmail(email.getText().trim());
                    updateRegistrar(sel);
                    regTable.setItems(FXCollections.observableArrayList(loadAllRegistrars()));
                    dlg.close();
                } catch (Exception ex) { showAlert("Error", "Failed to update registrar: " + ex.getMessage()); }
            });
            cancel.setOnAction(ev -> dlg.close());
            VBox box = new VBox(10,
                    new Label("Full Name"), fullName,
                    new Label("Phone"), phone,
                    new Label("Email"), email,
                    new HBox(10, save, cancel)
            );
            box.setPadding(new Insets(15));
            dlg.setScene(new Scene(box, 300, 250));
            dlg.showAndWait();
        });

        delRegBtn.setOnAction(e -> {
            Registrar sel = regTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert("Error", "Please select a registrar."); return; }
            try {
                if (removeRegistrar(sel.getId())) {
                    regTable.getItems().remove(sel);
                    showAlert("Success", "Registrar removed.");
                } else {
                    showAlert("Error", "Registrar not found.");
                }
            } catch (Exception ex) { showAlert("Error", "Failed to remove registrar: " + ex.getMessage()); }
        });

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            currentAdmin = null;
            VBox mainMenu = createMainMenu(primaryStage);
            Scene scene = new Scene(mainMenu, 400, 500);
            primaryStage.setScene(scene);
        });

        VBox dashboardLayout = new VBox(12,
                titleLabel,
                welcomeLabel,
                applicantsSection,
                applicantsHint,
                openApplicantsBtn,
                registrarSection,
                regTable,
                regActions,
                logoutButton);
        dashboardLayout.setAlignment(Pos.TOP_LEFT);
        dashboardLayout.setPadding(new Insets(15));

        Scene scene = new Scene(dashboardLayout, 900, 600);
        primaryStage.setScene(scene);
    }

    private void showRegistrarRegistrationForm(Stage primaryStage) {
        Label titleLabel = new Label("Registrar Registration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Button registerButton = new Button("Register");
        Button backButton = new Button("Back to Login");

        registerButton.setOnAction(e -> {
            try {
                String fullName = nameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();

                if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    showAlert("Error", "All fields except phone are required.");
                    return;
                }


                authService.addRegistrar(fullName, username, password, phone, email);
                showAlert("Success", "Registration successful! You can now log in.");

                showRegistrarLoginForm(primaryStage);
            } catch (IllegalArgumentException ex) {
                showAlert("Registration Error", "Registration failed: " + ex.getMessage());
            } catch (Exception ex) {
                showAlert("Database Error", "Database operation failed: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> {
            showRegistrarLoginForm(primaryStage);
        });


        VBox formLayout = new VBox(10);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));
        formLayout.getChildren().addAll(
            titleLabel,
            nameLabel, nameField,
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            phoneLabel, phoneField,
            emailLabel, emailField,
            registerButton,
            backButton
        );

        Scene scene = new Scene(formLayout, 400, 500);
        primaryStage.setScene(scene);
    }

    private void updateApplicant(com.humber.model.Applicant applicant) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(applicant);
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update applicant: " + e.getMessage(), e);
        }
    }

    private void showAlert(String title, String message) {
        Alert.AlertType alertType;

        if (title.startsWith("Error") || title.startsWith("Database Error")) {
            alertType = Alert.AlertType.ERROR;
        } else if (title.startsWith("Warning")) {
            alertType = Alert.AlertType.WARNING;
        } else if (title.startsWith("Info")) {
            alertType = Alert.AlertType.INFORMATION;
        } else if (title.startsWith("Success")) {
            alertType = Alert.AlertType.INFORMATION;
        } else {
            alertType = Alert.AlertType.INFORMATION;
        }

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
