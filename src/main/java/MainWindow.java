import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jdatepicker.impl.*;


/**
 * Classe de la fenetre principale de l'application, montrant les données d'un utlisateur donné
 */
public class MainWindow {

    private JFrame frame;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private int currentUserId;
    private List<Object[]> userData;
    private Database db;

    /**
     * Constructeur principal de la fenêtre principale.
     *
     * @param userId L'ID de l'utilisateur courant.
     */

    public MainWindow(int userId) {
        try {
            db = new Database();
            this.currentUserId = userId;
            initializeFrame();
            tableModel = initializeTableModel();
            dataTable = initializeTable(tableModel);
            JPanel mainPanel = createMainPanel();
            frame.add(mainPanel);
            loadTableData();
            frame.setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.");
            System.exit(1); // Ferme l'application si la connexion échoue
        }
    }

    /**
     * Initialise la fenêtre principale avec les paramètres de base.
     */

    private void initializeFrame() {
        frame = new JFrame("Gestionnaire de Mots de Passe");
        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(800, 600));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Initialise le modèle de tableau avec les colonnes nécessaires.
     *
     * @return Le modèle de tableau avec colonnes et données initiales.
     */

    private DefaultTableModel initializeTableModel() {
        String[] columns = { "Titre", "Nom d'utilisateur", "Description", "Date d'expiration" };
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /**
     * Initialise la table avec le modèle donné.
     *
     * @param model Le modèle de table à associer.
     * @return La table initialisée.
     */

    private JTable initializeTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(25);
        return table;
    }

    /**
     * Crée le panneau principal contenant la table et les panneaux supérieurs et
     * inférieurs.
     *
     * @return Le panneau principal.
     */

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // Utilisation du panneau avec le titre et le bouton de déconnexion
        JPanel topPanel = createTopPanel();
        JPanel botPanel = createBotPanel();
        JScrollPane tableScrollPane = new JScrollPane(dataTable);

        mainPanel.add(topPanel, BorderLayout.NORTH); // Ajout de la barre du haut
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(botPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * Crée le panneau inférieur contenant les boutons de gestion ("Modifier", "Voir
     * les détails", "Ajouter", "Supprimer").
     *
     * @return Le panneau inférieur.
     */

    private JPanel createBotPanel() {
        JPanel botPanel = new JPanel(new BorderLayout()); // Utilisation de BorderLayout pour mieux organiser les boutons

        // Button "Modifier"
        JButton editButton = new JButton("Modifier");
        editButton.addActionListener(e -> handleEdit());
        JPanel leftJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftJPanel.add(editButton);

        // Bouton "Voir les détails" (centré)
        JButton viewButton = new JButton("Voir les détails");
        viewButton.addActionListener(e -> handleViewAction());
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Sous-panneau pour centrer
        centerPanel.add(viewButton);

        // Bouton "Ajouter" (à droite)
        JButton ajouterData = new JButton("Ajouter");
        ajouterData.addActionListener(e -> handleAjouter(dataTable));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Sous-panneau pour aligner à droite
        rightPanel.add(ajouterData);

        //Bouton "Supprimer (à gauche du bouton ajouter)"
        JButton supprimerData = new JButton("Supprimer");
        supprimerData.addActionListener(e -> handleDeleteRow());
        rightPanel.add(supprimerData);

        // Ajout des sous-panneaux au panneau principal
        botPanel.add(leftJPanel, BorderLayout.WEST);
        botPanel.add(centerPanel, BorderLayout.CENTER); // Bouton "Voir les détails" centré
        botPanel.add(rightPanel, BorderLayout.EAST); // Bouton "Ajouter" aligné à droite

        return botPanel;
    }

    /**
     * Crée le panneau supérieur contenant le label de titre et les boutons de
     * filtrage et de déconnexion.
     *
     * @return Le panneau supérieur.
     */

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        // Création du label de titre (centré)
        JLabel titleLabel = createTitleLabel();
        topPanel.add(titleLabel, BorderLayout.CENTER);

        // Création du bouton de déconnexion (aligné à droite)
        JButton filtreButton = new JButton("Filtrer mbp avant date");
        filtreButton.addActionListener(e -> showExpiredPasswordsDialogWithDatePicker());
        topPanel.add(filtreButton, BorderLayout.WEST);

        JButton deconnexionButton = new JButton("Déconnexion");
        deconnexionButton.addActionListener(e -> handleDisconnect());
        topPanel.add(deconnexionButton, BorderLayout.EAST);

        return topPanel;
    }

    /**
     * Crée le label du titre central pour l'application.
     *
     * @return Le label de titre.
     */

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("Gestionnaire de Mots de Passe", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        return titleLabel;
    }

    /**
     * Gère la suppression de la ligne sélectionnée dans la JTable. Si une ligne est
     * sélectionnée, elle est supprimée de la base de données et de la JTable.
     * Sinon, une boîte de dialogue avertit l'utilisateur de sélectionner une ligne.
     * 
     * @throws SQLException Si une erreur SQL se produit lors de la suppression.
     */

    private void handleDeleteRow() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow != -1) {
            // Récupérer les données de la ligne sélectionnée
            String title = (String) tableModel.getValueAt(selectedRow, 0); // Titre (colonne 1)

            try {
                // Appeler la méthode de suppression
                boolean deleted = db.deleteUserFromDatabase(currentUserId, title);
                if (deleted) {
                    tableModel.removeRow(selectedRow); // Supprime la ligne de la JTable
                    JOptionPane.showMessageDialog(frame, "Ligne supprimée avec succès !");
                } else {
                    JOptionPane.showMessageDialog(frame, "Erreur lors de la suppression.", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Erreur SQL : " + e.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Veuillez sélectionner une ligne.", "Aucune sélection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Gère l'affichage des détails de l'entrée sélectionnée dans une boîte de
     * dialogue. Si aucune ligne n'est sélectionnée, affiche une boîte de dialogue
     * avertissant l'utilisateur.
     */

    private void handleViewAction() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow != -1) {
            openDetailsDialog();
        } else {
            displayWarningMessage("Veuillez sélectionner une entrée à afficher");
        }
    }

    /**
     * Gère l'enregistrement d'un nouveau mot de passe dans la base de données.
     * Vérifie si l'opération a réussi et met à jour la table. En cas d'erreur,
     * affiche un message d'avertissement.
     * 
     * @param ajouterFrame     La fenêtre contenant le formulaire de saisie.
     * @param dataTable        La table affichant les mots de passe existants.
     * @param titleField       Champ de saisie pour le titre du mot de passe.
     * @param usernameField    Champ de saisie pour le nom d'utilisateur.
     * @param passwordField    Champ de saisie pour le mot de passe.
     * @param descriptionField Champ de saisie pour la description.
     * @param expirationField  Champ de saisie pour la date d'expiration.
     * @throws SQLException Si une erreur SQL se produit lors de l'ajout.
     */

    private void handleSaveAction(JFrame ajouterFrame, JTable dataTable, JTextField titleField,
            JTextField usernameField, JPasswordField passwordField, JTextField descriptionField,
            JTextField expirationField) {
        String title = titleField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String description = descriptionField.getText();
        String expirationDate = expirationField.getText();

        try {
            if (db.addUserToDatabase(Integer.toString(currentUserId), title, username, password, description,
                    expirationDate)) {
                JOptionPane.showMessageDialog(ajouterFrame, "Mot de passe ajouté avec succès !");

                // Actualiser la table et userData
                loadTableData(); // Met à jour `userData` et rafraîchit la table
                ajouterFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(ajouterFrame, "Échec de l'ajout du mot de passe !", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ajouterFrame, "Erreur lors de l'ajout : " + ex.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Rafraîchit la table en chargeant les données à partir de la base de données.
     * Met à jour les données affichées dans la table en excluant la colonne "mot de
     * passe".
     * 
     * @param dataTable La table à mettre à jour avec de nouvelles données.
     * @throws SQLException Si une erreur SQL se produit lors de la récupération des
     *                      données.
     */

    private void refreshTable(JTable dataTable) {
        try {
            // Récupère les nouvelles données sous forme de List<Object[]>
            List<Object[]> data = db.fetchUserDataFromDatabase(currentUserId);

            DefaultTableModel tableModel = (DefaultTableModel) dataTable.getModel();
            tableModel.setRowCount(0); // Vide la table actuelle

            // Reformatage des données pour exclure "password"
            for (Object[] row : data) {
                // Filtrer les colonnes dans l'ordre des colonnes définies dans initializeTableModel
                Object[] formattedRow = { row[0], row[1], row[3], row[4] }; // Exclure "password" (index 2)
                String[] stringRow = Arrays.stream(formattedRow).map(obj -> obj != null ? obj.toString() : "") // Convertir en String
                        .toArray(String[]::new); // Transformer en tableau String[]
                tableModel.addRow(stringRow); // Ajouter la ligne convertie
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur lors de l'actualisation de la table : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Crée le panneau contenant les boutons "Enregistrer" et "Annuler" pour la
     * fenêtre d'ajout.
     * 
     * @param ajouterFrame     La fenêtre dans laquelle les boutons seront ajoutés.
     * @param dataTable        La table à mettre à jour après l'enregistrement.
     * @param titleField       Champ de texte pour le titre à saisir.
     * @param usernameField    Champ de texte pour le nom d'utilisateur à saisir.
     * @param passwordField    Champ de mot de passe pour la saisie.
     * @param descriptionField Champ de texte pour la description à saisir.
     * @param expirationField  Champ de texte pour la date d'expiration à saisir.
     * @return Le panneau contenant les boutons "Enregistrer" et "Annuler".
     */
    private JPanel createButtonPanel(JFrame ajouterFrame, JTable dataTable, JTextField titleField,
            JTextField usernameField, JPasswordField passwordField, JTextField descriptionField,
            JTextField expirationField) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Enregistrer");
        JButton cancelButton = new JButton("Annuler");

        // Ajout des actions aux boutons
        saveButton.addActionListener(e -> handleSaveAction(ajouterFrame, dataTable, titleField, usernameField,
                passwordField, descriptionField, expirationField));
        cancelButton.addActionListener(e -> ajouterFrame.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    /**
     * Crée le panneau contenant les champs de formulaire et un sélecteur de date
     * pour la date d'expiration.
     * 
     * @param titleField       Champ de texte pour le titre.
     * @param usernameField    Champ de texte pour le nom d'utilisateur.
     * @param passwordField    Champ de texte pour le mot de passe.
     * @param descriptionField Champ de texte pour la description.
     * @param expirationField  Champ de texte pour la date d'expiration.
     * @return Le panneau contenant les champs de formulaire et le sélecteur de
     *         date.
     */

    private JPanel createFormPanel(JTextField titleField, JTextField usernameField, JPasswordField passwordField,
            JTextField descriptionField, JTextField expirationField) {
        // Création d'un sélecteur de date
        UtilDateModel model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Aujourd'hui");
        properties.put("text.month", "Mois");
        properties.put("text.year", "Année");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());

        // Récupération de la date sélectionnée et stockage dans expirationField
        datePicker.addActionListener(e -> {
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            if (selectedDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                expirationField.setText(sdf.format(selectedDate));
            }
        });

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10)); // 5 lignes, 2 colonnes
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Marges autour

        formPanel.add(new JLabel("Titre :"));
        formPanel.add(titleField);

        formPanel.add(new JLabel("Nom d'utilisateur :"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Mot de passe :"));
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Description :"));
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Date d'expiration :"));
        formPanel.add(datePicker);

        return formPanel;
    }

    /**
     * Ouvre une fenêtre pour ajouter un nouveau mot de passe avec des champs de
     * formulaire et des boutons d'action.
     * 
     * @param dataTable La table principale contenant les données de mots de passe.
     */

    private void handleAjouter(JTable dataTable) {
        // Création de la fenêtre
        JFrame ajouterFrame = new JFrame("Ajouter un mot de passe");
        ajouterFrame.setSize(400, 300);
        ajouterFrame.setLayout(new BorderLayout());
        ajouterFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ajouterFrame.setLocationRelativeTo(frame);

        // Champs de formulaire
        JTextField titleField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField descriptionField = new JTextField();
        JTextField expirationField = new JTextField();

        // Ajout des panneaux
        ajouterFrame.add(createFormPanel(titleField, usernameField, passwordField, descriptionField, expirationField),
                BorderLayout.CENTER);
        ajouterFrame.add(createButtonPanel(ajouterFrame, dataTable, titleField, usernameField, passwordField,
                descriptionField, expirationField), BorderLayout.SOUTH);

        ajouterFrame.setVisible(true);
    }

    /**
     * Ouvre une boîte de dialogue affichant les détails d'un mot de passe
     * sélectionné.
     */

    private void openDetailsDialog() {
        int selectedRow = dataTable.getSelectedRow();
        String title = (String) userData.get(selectedRow)[0];
        String username = (String) userData.get(selectedRow)[1];
        String password = (String) userData.get(selectedRow)[2];
        String description = (String) userData.get(selectedRow)[3];
        String expirationDate = (String) userData.get(selectedRow)[4];

        JDialog dialog = new JDialog(frame, "Détails - " + title, true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Panel principal avec bordure
        JPanel contentPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Ajout des informations
        contentPanel.add(new JLabel("Titre:"));
        contentPanel.add(createStyledLabel(title));
        contentPanel.add(new JLabel("Nom d'utilisateur:"));
        contentPanel.add(createStyledLabel(username));
        contentPanel.add(new JLabel("Mot de passe:"));
        contentPanel.add(createStyledLabel(password));
        contentPanel.add(new JLabel("Description:"));
        contentPanel.add(createStyledLabel(description));
        contentPanel.add(new JLabel("Date d'expiration:"));
        contentPanel.add(createStyledLabel(expirationDate));

        // Bouton de fermeture
        JPanel buttonPanel = new JPanel();
        JButton closeButton = createCloseButton(dialog);
        buttonPanel.add(closeButton);

        // Ajout des panels au dialog
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    /**
     * Crée un JLabel avec un style personnalisé.
     *
     * @param text le texte à afficher sur le JLabel.
     * @return un JLabel stylisé avec une font et une couleur spécifiques.
     */

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(new Color(50, 50, 150));
        return label;
    }

    /**
     * Crée un bouton de fermeture pour une JDialog.
     *
     * @param dialog la JDialog associée.
     * @return un bouton de fermeture configuré pour fermer la JDialog.
     */

    private JButton createCloseButton(JDialog dialog) {
        JButton closeButton = new JButton("Fermer");
        closeButton.setBackground(new Color(200, 200, 255));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeButton.addActionListener(e -> dialog.dispose());
        return closeButton;
    }

    /**
     * Affiche un message d'erreur via une boîte de dialogue.
     *
     * @param message le message d'erreur à afficher.
     */
    private void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Erreur de Base de Données", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Affiche un message d'avertissement via une boîte de dialogue.
     *
     * @param message le message d'avertissement à afficher.
     */

    private void displayWarningMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Aucune sélection", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Gère la déconnexion de l'utilisateur. Redirige vers la fenêtre de connexion
     * et ferme la fenêtre actuelle.
     */

    void handleDisconnect() {
        new LoginForm();
        frame.dispose();
    }

    /**
     * Charge les données de la table depuis la base de données et les affiche.
     * Exclut les mots de passe des colonnes affichées dans la JTable.
     * @throws SQLException If there is a connection error to the database.
 
     */

    private void loadTableData() {
        try {
            // Vider la table avant d'ajouter de nouvelles lignes
            tableModel.setRowCount(0);

            // Récupérer toutes les données depuis la base de données
            userData = db.fetchUserDataFromDatabase(currentUserId);

            // Parcourir les données et exclure les mots de passe (index 2)
            for (Object[] row : userData) {
                // Filtrer les colonnes pour la fenêtre principale
                Object[] filteredRow = new Object[] { row[0], // title
                        row[1], // username
                        row[3], // description
                        row[4] // expiration_date
                };
                // Ajouter la ligne filtrée au modèle de la table
                tableModel.addRow(filteredRow);
            }
        } catch (SQLException e) {
            // Gestion des erreurs
            displayErrorMessage("Erreur lors de la récupération des données : " + e.getMessage());
        }
    }

    /**
     * Permet de modifier une ligne sélectionnée dans la JTable en affichant une
     * fenêtre modale. L'utilisateur peut entrer de nouvelles valeurs et enregistrer
     * les modifications.
     */

    void handleEdit() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Veuillez sélectionner une ligne pour modifier.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Retrieve data from the selected row
        String title = (String) userData.get(selectedRow)[0];
        String username = (String) userData.get(selectedRow)[1];
        String password = (String) userData.get(selectedRow)[2];
        String description = (String) userData.get(selectedRow)[3];
        String date = (String) userData.get(selectedRow)[4];

        JDialog dialog = new JDialog(frame, "Modifier - " + title, true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Panel principal avec bordure
        JPanel contentPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Editable fields
        contentPanel.add(new JLabel("Titre:"));
        JTextField titleField = new JTextField(title);
        contentPanel.add(titleField);

        contentPanel.add(new JLabel("Nom d'utilisateur:"));
        JTextField usernameField = new JTextField(username);
        contentPanel.add(usernameField);

        contentPanel.add(new JLabel("Mot de passe:"));
        JTextField passwordField = new JTextField(password);
        contentPanel.add(passwordField);

        contentPanel.add(new JLabel("Description:"));
        JTextField descriptionField = new JTextField(description);
        contentPanel.add(descriptionField);

        // Date expiration with JDatePicker
        contentPanel.add(new JLabel("Date expiration:"));
        UtilDateModel model = new UtilDateModel();
        model.setValue(java.sql.Date.valueOf(date)); // Set the current date as default
        JDatePanelImpl datePanel = new JDatePanelImpl(model, new Properties());
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());

        contentPanel.add(datePicker); // Add the date picker to the form

        // Save button
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            
            String updatedTitle = titleField.getText();
            String updatedUsername = usernameField.getText();
            String updatedPassword = passwordField.getText();
            String updatedDescription = descriptionField.getText();
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            String updatedDate = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);

            // Update the table data
            userData.get(selectedRow)[0] = updatedTitle;
            userData.get(selectedRow)[1] = updatedUsername;
            userData.get(selectedRow)[2] = updatedPassword;
            userData.get(selectedRow)[3] = updatedDescription;
            userData.get(selectedRow)[4] = updatedDate;

            // Call the update function
            int id = db.getRowIdByTitleAndPassword(title, password);
            db.updateRowInDatabase(id, updatedTitle, updatedUsername, updatedPassword, updatedDescription,
                    updatedDate);

            loadTableData();;
            dialog.dispose();
        });

        JButton closeButton = createCloseButton(dialog);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        // Add panels to the dialog
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    /**
     * Affiche une boîte de dialogue modale avec un sélecteur de date pour filtrer
     * les mots de passe expirant avant une date spécifiée.
     */

    public void showExpiredPasswordsDialogWithDatePicker() {
        JDialog dialog = new JDialog(frame, "Mot de passe expirant avant une date", true);

        JPanel datePanel = createDatePanel();
        dialog.add(datePanel, BorderLayout.NORTH);

        JPanel buttonPanel = createButtonPanel(dialog);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    /**
     * Créer un panneau contenant un label et un sélecteur de date pour filtrer les
     * mots de passe expirant avant une date spécifique.
     */

    private JPanel createDatePanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dateLabel = new JLabel("Date d'expiration avant :");
        JDatePickerImpl datePicker = createDatePicker();
        datePanel.add(dateLabel);
        datePanel.add(datePicker);
        return datePanel;
    }

    /**
     * Crée un sélecteur de date avec un modèle et un format de date.
     *
     * @return un {@link JDatePickerImpl} pour choisir une date.
     */

    private JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Aujourd'hui");
        properties.put("text.month", "Mois");
        properties.put("text.year", "Année");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        return new JDatePickerImpl(datePanel, new DateComponentFormatter());
    }

    /**
     * Crée un panneau de boutons pour fermer le dialog et appliquer le filtre.
     *
     * @param dialog la fenêtre parent contenant ce panneau de boutons.
     * @return un {@link JPanel} contenant les boutons "Filtrer" et "Fermer".
     */

    private JPanel createButtonPanel(JDialog dialog) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton filterButton = new JButton("Filtrer");
        filterButton.addActionListener(e -> filterExpiredPasswords(dialog));
        JButton closeButton = createCloseButton(dialog);
        buttonPanel.add(filterButton);
        buttonPanel.add(closeButton);
        return buttonPanel;
    }

    /**
     * Filtre les mots de passe expirés avant une date spécifiée sélectionnée via le
     * date picker du dialog.
     * 
     * @param dialog la fenêtre dialog contenant le date picker.
     */

    private void filterExpiredPasswords(JDialog dialog) {
        JDatePickerImpl datePicker = (JDatePickerImpl) ((JPanel) dialog.getContentPane().getComponent(0))
                .getComponent(1);
        java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
        if (selectedDate != null) {
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
            List<Object[]> expiredPasswords = getExpiredPasswordsBeforeDate(formattedDate);

            if (expiredPasswords.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Aucun mot de passe expirant avant la date sélectionnée.",
                        "Avertissement", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showExpiredPasswordsTable(dialog, expiredPasswords);
            }
        } else {
            JOptionPane.showMessageDialog(dialog, "Veuillez choisir une date valide.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Récupère la liste des mots de passe expirés avant une date spécifiée.
     * 
     * @param formattedDate la date formatée en "yyyy-MM-dd" à partir de laquelle
     *                      les mots de passe expirés sont filtrés.
     *  * @throws SQLException If there is a connection error to the database.
 
     * @return une liste de tableaux d'objets contenant les données des mots de
     *         passe expirés.
     */

    private List<Object[]> getExpiredPasswordsBeforeDate(String formattedDate) {
        return userData.stream().filter(row -> {
            try {
                String expirationDate = (String) row[4];
                java.util.Date expDate = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate);
                return !expDate.after(new SimpleDateFormat("yyyy-MM-dd").parse(formattedDate));
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }).collect(Collectors.toList());
    }

    /**
     * Affiche un tableau contenant les mots de passe expirés avant la date
     * spécifiée dans une boîte de dialogue.
     * 
     * @param dialog           la boîte de dialogue où le tableau sera affiché.
     * @param expiredPasswords la liste des mots de passe expirés à afficher.
     */

    private void showExpiredPasswordsTable(JDialog dialog, List<Object[]> expiredPasswords) {
        String[] columnNames = { "Titre", "Nom d'utilisateur", "Description", "Date expiration" };

        JTable expiredPasswordsTable = new JTable(new AbstractTableModel() {
            public int getRowCount() {
                return expiredPasswords.size();
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public String getColumnName(int column) {
                return columnNames[column];
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                return expiredPasswords.get(rowIndex)[columnIndex];
            }
        });

        JScrollPane scrollPane = new JScrollPane(expiredPasswordsTable);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
}