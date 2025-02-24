import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe gérant les interactions avec la base de données SQLite. Elle
 * implémente {@link AutoCloseable} pour garantir la fermeture des ressources.
 */
public class Database implements AutoCloseable {

    private static final String DB_URL = "jdbc:sqlite:src/main/resources/data/baseDeDonnées.db";
    private Connection conn;

    /**
     * Constructeur de la classe {@code Database}. Initialise une connexion avec la
     * base de données spécifiée.
     *
     * @throws SQLException si une erreur survient lors de l'établissement de la
     *                      connexion.
     */
    public Database() throws SQLException {
        this.conn = DriverManager.getConnection(DB_URL);
    }

    /**
     * Retourne l'instance de connexion à la base de données.
     *
     * @return l'instance {@link Connection} actuellement utilisée.
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Ferme la connexion avec la base de données si elle est ouverte.
     *
     * @throws SQLException si une erreur survient lors de la fermeture.
     */
    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    /**
     * Exécute une requête SQL avec des paramètres dynamiques.
     *
     * @param query  la requête SQL à exécuter.
     * @param params les paramètres de la requête.
     * @return un objet {@link ResultSet} contenant les résultats de la requête.
     * @throws SQLException si une erreur survient lors de l'exécution de la
     *                      requête.
     */
    private ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement stmt = this.getConnection().prepareStatement(query);

        // Traitement des paramètres
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            try {
                // Vérifiez si le paramètre est une chaîne de caractères
                if (param instanceof String) {
                    // Chiffrement uniquement pour les chaînes
                    param = Cryptage.encrypt((String) param);
                }
            } catch (Exception e) {
                throw new SQLException("Chiffrement impossible pour le paramètre : " + param, e);
            }

            // Ajout du paramètre au PreparedStatement
            stmt.setObject(i + 1, param);
        }

        return stmt.executeQuery();
    }

    /**
     * Récupère toutes les données associées à un utilisateur dans la base de
     * données.
     *
     * @param userId l'identifiant de l'utilisateur.
     * @return une liste de tableaux d'objets contenant les données de
     *         l'utilisateur.
     * @throws SQLException si une erreur survient lors de la récupération.
     */
    public List<Object[]> fetchUserDataFromDatabase(int userId) throws SQLException {
        List<Object[]> data = new ArrayList<>();
        String query = "SELECT title, username, password, description, expiration_date FROM data WHERE user_id = ?";

        try (ResultSet rs = executeQuery(query, userId)) {
            while (rs.next()) {
                try {

                    // Récupération et déchiffrement des données sensibles
                    String title = Cryptage.decrypt(rs.getString("title"));
                    String username = Cryptage.decrypt(rs.getString("username"));
                    String password = Cryptage.decrypt(rs.getString("password")); // Déchiffrement
                    String description = Cryptage.decrypt(rs.getString("description")); // Déchiffrement
                    String expirationDate = Cryptage.decrypt(rs.getString("expiration_date"));

                    // Ajout des données dans la liste
                    data.add(new Object[] { title, username, password, description, expirationDate });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

    // Méthode pour récupérer l'ID basé sur le mot de passe et le titre
    public int getRowIdByTitleAndPassword(String title, String password) {
    String query = "SELECT id FROM data WHERE title = ? AND password = ?";
    int rowId = -1; // Default value for not found

    try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
        // Encrypt the parameters if needed
        String encryptedTitle = Cryptage.encrypt(title); // Assurez-vous que le chiffrement est effectué ici
        String encryptedPassword = Cryptage.encrypt(password); // Assurez-vous que le chiffrement est effectué ici

        stmt.setString(1, encryptedTitle);
        stmt.setString(2, encryptedPassword);

        try (ResultSet resultSet = stmt.executeQuery()) {
            if (resultSet.next()) {
                rowId = resultSet.getInt("id");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }

    return rowId;
}



    /**
     * Récupère l'identifiant d'un utilisateur à partir de son nom.
     *
     * @param userName le nom d'utilisateur.
     * @return l'identifiant de l'utilisateur.
     * @throws SQLException si l'utilisateur n'est pas trouvé ou en cas d'erreur
     *                      SQL.
     */
    public int returnIdFromUserName(String userName) throws SQLException {
        String query = "SELECT user_id FROM users WHERE username = ?";
        try (ResultSet rs = executeQuery(query, userName)) {
            if (rs.next()) {
                return rs.getInt("user_id");
            } else {
                throw new SQLException("Utilisateur non trouvé : " + userName);
            }
        }
    }

    /**
     * Récupère tous les titres associés à un utilisateur.
     *
     * @param userId l'identifiant de l'utilisateur.
     * @return une liste de titres.
     */
    public List<String> getAllTitlesForUser(int userId) {
        List<String> titles = new ArrayList<>();
        String query = "SELECT title FROM data WHERE user_id = ?";

        try (ResultSet rs = executeQuery(query, userId)) {
            while (rs.next()) {
                try {
                    String decryptedTitle = Cryptage.decrypt(rs.getString("title"));
                    titles.add(decryptedTitle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return titles;
    }

    /**
     * Vérifie si un couple nom d'utilisateur/mot de passe existe dans la base de
     * données.
     *
     * @param username le nom d'utilisateur.
     * @param password le mot de passe.
     * @return {@code true} si le couple est valide, {@code false} sinon.
     * @throws SQLException en cas d'erreur SQL.
     */
    public boolean loginValides(String username, String password) throws SQLException {
        String query = "SELECT username,password FROM users WHERE username = ? AND password = ?";

        try (ResultSet rs = executeQuery(query, username, password)) {
            return rs.next();
        }
    }

    /**
     * Supprime un utilisateur de la base de données en fonction de son identifiant
     * et d'un titre.
     *
     * @param idUser l'identifiant de l'utilisateur.
     * @param title  le titre associé à l'utilisateur.
     * @return {@code true} si la suppression a réussi, {@code false} sinon.
     * @throws SQLException en cas d'erreur SQL.
     */
    public boolean deleteUserFromDatabase(int idUser, String title) throws SQLException {
        String query = "DELETE FROM data WHERE user_id = ? AND title = ?";

        try {

            title = Cryptage.encrypt(title);
        } catch (Exception e) {
            return false; // Retournez false si le chiffrement échoue
        }

        try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
            stmt.setInt(1, idUser);
            stmt.setString(2, title);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Ajoute un utilisateur et ses informations à la base de données.
     *
     * @param currentUserId  l'identifiant de l'utilisateur actuel.
     * @param title          le titre associé.
     * @param username       le nom d'utilisateur.
     * @param password       le mot de passe.
     * @param description    une description.
     * @param expirationDate une date d'expiration.
     * @return {@code true} si l'ajout a réussi, {@code false} sinon.
     * @throws SQLException en cas d'erreur SQL.
     */
    public boolean addUserToDatabase(String currentUserId, String title, String username, String password,
            String description, String expirationDate) throws SQLException {
        String query = "INSERT INTO data (user_id, title, username, password, description, expiration_date) VALUES (?, ?, ?, ?, ?, ?)";

        try {

            // Chiffrement des données
            title = Cryptage.encrypt(title);
            username = Cryptage.encrypt(username);
            password = Cryptage.encrypt(password);
            description = Cryptage.encrypt(description);
            expirationDate = Cryptage.encrypt(expirationDate);
        } catch (Exception e) {
            return false; // Retournez false si le chiffrement échoue
        }

        // Insérer les données chiffrées dans la base de données
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
            stmt.setString(1, currentUserId);
            stmt.setString(2, title);
            stmt.setString(3, username);
            stmt.setString(4, password);
            stmt.setString(5, description);
            stmt.setString(6, expirationDate);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Méthode pour récupérer une ligne de la base de données à partir de l'ID
     * spécifié.
     *
     * @param id L'ID de la ligne à récupérer.
     * @return Une liste contenant les colonnes de la ligne correspondant à l'ID, ou
     *         une liste vide si aucune correspondance n'est trouvée.
     */
    public List<String> fetchRowFromId(int id) {
        List<String> row = new ArrayList<>();
        String query = "SELECT title, username, password, description, expiration_date FROM data WHERE id = ?";

        try (ResultSet rs = executeQuery(query, id)) {
            if (rs.next()) {
                row.add(rs.getString("title"));
                row.add(rs.getString("username"));
                row.add(rs.getString("password"));
                row.add(rs.getString("description"));
                row.add(rs.getString("expiration_date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }

    /**
     * Met à jour une ligne spécifique dans la base de données.
     *
     * @param rowId       L'ID de la ligne à mettre à jour.
     * @param title       Le nouveau titre à enregistrer.
     * @param username    Le nouveau nom d'utilisateur à enregistrer.
     * @param password    Le nouveau mot de passe à enregistrer.
     * @param description La nouvelle description à enregistrer.
     * @param date        La nouvelle date d'expiration à enregistrer.
     */
    void updateRowInDatabase(int rowId, String title, String username, String password, String description,
            String date) {
        String query = "UPDATE data SET title = ?, username = ?, password = ?, description = ?, expiration_date = ? WHERE id = ?";

        // Étape 1 : Chiffrement des données
        try {
            title = Cryptage.encrypt(title);
            username = Cryptage.encrypt(username);
            password = Cryptage.encrypt(password);
            description = Cryptage.encrypt(description);
            date = Cryptage.encrypt(date);

        } catch (Exception e) {
            e.printStackTrace();
            return; // Arrêter l'exécution si le chiffrement échoue
        }

        // Étape 2 : Mise à jour dans la base de données
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
            // Vérification de la connexion
            if (this.getConnection() == null || this.getConnection().isClosed()) {

                return;
            }

            // Configuration des paramètres de la requête
            stmt.setString(1, title);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, description);
            stmt.setString(5, date);
            stmt.setInt(6, rowId);

            // Exécution de la requête
            int rowsAffected = stmt.executeUpdate();

            // Vérification du résultat
            if (rowsAffected > 0) {
                System.out.println("Mise à jour réussie pour la ligne ID : " + rowId);
            } else {
                System.err.println("Aucune ligne affectée. ID invalide ou données identiques.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la mise à jour de la ligne : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ajoute un utilisateur à la base de données.
     *
     * @param username Le nom d'utilisateur à ajouter.
     * @param password Le mot de passe associé à l'utilisateur.
     * @return true si l'utilisateur a été ajouté avec succès, false sinon.
     */
    public boolean addUser(String username, String password) {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (PreparedStatement checkStmt = this.getConnection().prepareStatement(checkQuery)) {
            // Chiffrement du paramètre pour la vérification
            String encryptedUsername = Cryptage.encrypt(username);
            checkStmt.setString(1, encryptedUsername);

            // Exécution de la requête de vérification
            try (ResultSet resultSet = checkStmt.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return false; // L'utilisateur existe déjà
                }
            }

            // Si l'utilisateur n'existe pas, on l'insère
            try (PreparedStatement insertStmt = this.getConnection().prepareStatement(insertQuery)) {
                // Chiffrement des paramètres
                String encryptedPassword = Cryptage.encrypt(password);

                // Assignation des paramètres chiffrés
                insertStmt.setString(1, encryptedUsername);
                insertStmt.setString(2, encryptedPassword);

                // Exécution de la requête
                int rowsAffected = insertStmt.executeUpdate();
                return rowsAffected > 0; // Retourne true si au moins une ligne a été insérée
            }

        } catch (SQLException e) {
            return false; // Retourne false en cas d'erreur SQL
        } catch (Exception e) {
            return false; // Retourne false pour d'autres exceptions
        }
    }

}