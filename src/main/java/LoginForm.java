import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;


/**
 * Classe de la fenetre de connection à l'application
 */
public class LoginForm {
    private JFrame frame; // Fenêtre principale de l'application
    private JPanel mainPanel; // Panneau principal contenant les composants
    private JTextField usernameField; // Champ pour l'identifiant
    private JPasswordField passwordField; // Champ pour le mot de passe
    private JLabel feedbackLabel; // Label pour afficher des messages d'erreur ou de succès
    private Database db; // Objet représentant la connexion à la base de données

    /**
     * Constructeur de la classe LoginForm. Il initialise la connexion à la base de
     * données. Si la connexion réussit, il appelle la méthode pour générer
     * l'interface utilisateur.
     */
    public LoginForm() {
        try {
            db = new Database(); // Initialise la base de données
            initializeUI(); // Initialise l'interface utilisateur
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur de connexion à la base de données.");
            System.exit(1); // Ferme l'application si la connexion échoue
        }
    }

    /**
     * Méthode pour initialiser l'interface graphique. Elle crée la fenêtre
     * principale, définit sa taille, son comportement, et appelle la méthode pour
     * ajouter les composants à la fenêtre.
     */
    private void initializeUI() {
        frame = new JFrame("Login Form"); // Crée une nouvelle fenêtre avec un titre
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Définir le comportement de la fermeture de la fenêtre
        frame.setSize(600, 400); // Taille de la fenêtre
        frame.setLocationRelativeTo(null); // Centre la fenêtre à l'écran

        // Initialisation du panneau principal avec un GridBagLayout
        mainPanel = new JPanel(new GridBagLayout());

        // Ajout des composants à la fenêtre
        addComponentsToMainPanel();

        frame.setContentPane(mainPanel); // Définit le panneau principal comme contenu de la fenêtre
        frame.setVisible(true); // Rendre la fenêtre visible
    }

    /**
     * Méthode pour ajouter les éléments (labels, champs de texte, boutons) au
     * panneau principal. Elle positionne les composants avec le GridBagLayout.
     */
    private void addComponentsToMainPanel() {
        GridBagConstraints gbc = new GridBagConstraints(); // Contraintes pour le layout GridBag
        gbc.insets = new Insets(5, 5, 5, 5); // Ajoute de l'espace entre les composants
    
        // Composants de l'interface
        JLabel labelUsername = new JLabel("Identifiant:"); // Label pour l'identifiant
        JLabel labelPassword = new JLabel("Mot de passe:"); // Label pour le mot de passe
        usernameField = new JTextField(20); // Champ de texte pour l'identifiant
        passwordField = new JPasswordField(20); // Champ de texte pour le mot de passe
        feedbackLabel = new JLabel(); // Label pour afficher les retours (erreur ou succès)
        feedbackLabel.setForeground(Color.RED); // Définit la couleur du texte par défaut à rouge
    
        // Bouton de connexion
        JButton loginButton = new JButton("Se connecter");
        loginButton.addActionListener(e -> handleLogin()); // Ajoute un action listener pour gérer l'événement du clic
    
        JButton addUserButton = new JButton("Ajouter un utilisateur");
        addUserButton.addActionListener(e -> handleAddUser());
    
        // Ajout des composants au panneau avec des contraintes
        gbc.anchor = GridBagConstraints.WEST;
    
        // Position des composants dans le GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(labelUsername, gbc);
    
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(usernameField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(labelPassword, gbc);
    
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(passwordField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(feedbackLabel, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);
    
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(addUserButton, gbc);
    }
    
    /**
     * Méthode qui est appelée lorsqu'on clique sur le bouton "Se connecter". Elle
     * vérifie si les champs sont remplis et si les informations de connexion sont
     * valides. Si la connexion est réussie, elle ouvre la fenêtre principale et
     * ferme la fenêtre de connexion.
     */
    private void handleLogin() {
        String username = usernameField.getText(); // Récupère le texte du champ identifiant
        String password = new String(passwordField.getPassword()); // Récupère le mot de passe du champ de mot de passe

        // Vérifie si les champs sont vides
        if (username.isEmpty() || password.isEmpty()) {
            feedbackLabel.setText("Veuillez entrer un nom d'utilisateur et un mot de passe");
            return;
        }

        try {
            // Vérification des informations de connexion dans la base de données
            if (db.loginValides(username, password)) {
                int userId = db.returnIdFromUserName(username); // Récupère l'ID de l'utilisateur à partir du nom
                feedbackLabel.setForeground(Color.GREEN); // Change la couleur du texte à vert en cas de succès
                feedbackLabel.setText("Connexion réussie"); // Affiche un message de succès
                new MainWindow(userId); // Ouvre la fenêtre principale
                frame.dispose(); // Ferme la fenêtre de connexion
            } else {
                feedbackLabel.setText("Identifiant ou mot de passe incorrect"); // Affiche une erreur si les identifiants sont incorrects
            }
        } catch (SQLException e) {
            feedbackLabel.setText("Erreur de connexion à la base de données"); // Affiche une erreur en cas de problème avec la base de données
            e.printStackTrace(); // Affiche la trace de l'erreur
        }
    }

    /**
 * Méthode pour gérer l'ajout d'un nouvel utilisateur via une boîte de dialogue.
 */
    private void handleAddUser() {
        JDialog dialog = new JDialog(frame, "Ajouter un utilisateur", true);
        dialog.setLayout(new GridBagLayout());
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espacement entre les composants
    
        JLabel labelUsername = new JLabel("Nom d'utilisateur :");
        JLabel labelPassword = new JLabel("Mot de passe :");
        JLabel labelConfirmPassword = new JLabel("Confirmer le mot de passe :");
    
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);
    
        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
    
            // Validation des champs
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tous les champs doivent être remplis.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // Validation des mots de passe
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Les mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            if (db.addUser(username, password)) {
                JOptionPane.showMessageDialog(dialog, "Utilisateur ajouté avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose(); // Ferme la fenêtre d'ajout après la confirmation
            } else {
                JOptionPane.showMessageDialog(dialog, "Erreur lors de l'ajout de l'utilisateur.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dialog.dispose());
    
        JPanel contentPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(labelUsername, gbc);
    
        gbc.gridx = 1;
        contentPanel.add(usernameField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(labelPassword, gbc);
    
        gbc.gridx = 1;
        contentPanel.add(passwordField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(labelConfirmPassword, gbc);
    
        gbc.gridx = 1;
        contentPanel.add(confirmPasswordField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(saveButton, gbc);
        gbc.gridy = 4;
        contentPanel.add(cancelButton, gbc);
    
        dialog.add(contentPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame); // Centre le dialogue par rapport à la fenêtre principale
        dialog.setVisible(true);
    }
    
}
