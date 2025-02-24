-- Suppression des tables si elles existent
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS data;

-- Création de la table des utilisateurs
CREATE TABLE users (
  user_id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL,
  password TEXT NOT NULL
);

-- Création de la table des données
CREATE TABLE data (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER,
  title TEXT NOT NULL,
  username TEXT NOT NULL,
  password TEXT NOT NULL,
  description TEXT NOT NULL,
  expiration_date DATE,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);
