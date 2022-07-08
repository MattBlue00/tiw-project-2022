CREATE DATABASE IF NOT EXISTS `database`;
USE `database`;

CREATE TABLE IF NOT EXISTS `utente` (
    `username` VARCHAR(20) NOT NULL,
    `email` VARCHAR(100) UNIQUE NOT NULL,
    `password` PASSWORD NOT NULL,
    PRIMARY KEY (`username`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `album` (
    `proprietario` VARCHAR(20) NOT NULL,
    `titolo` VARCHAR(30) NOT NULL,
    `data_creazione` DATE NOT NULL,
    PRIMARY KEY (`proprietario`, `titolo`),
    FOREIGN KEY (`proprietario`) REFERENCES `utente` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `immagine` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `proprietario_album` VARCHAR(20) NOT NULL,
    `titolo_album` VARCHAR(30) NOT NULL,
    `titolo_immagine` VARCHAR(30) NOT NULL,
    `data` DATE NOT NULL,
    `descrizione` TEXT NOT NULL,
    `path` VARCHAR(260) NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`proprietario_album`) REFERENCES `album` (`proprietario`),
    FOREIGN KEY (`titolo_album`) REFERENCES `album` (`titolo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `commento` (
    `autore` VARCHAR(20) NOT NULL,
    `id_immagine` INT NOT NULL,
    `numero_commento` INT NOT NULL,
    `testo` TEXT NOT NULL,
    PRIMARY KEY (`autore`, `id_immagine`, `numero_commento`),
    FOREIGN KEY (`autore`) REFERENCES `utente` (`username`),
    FOREIGN KEY (`id_immagine`) REFERENCES `immagine` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;