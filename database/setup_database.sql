-- setup_database.sql

-- Create the SongsDatabase
CREATE DATABASE SongsDatabase;

-- Connect to the SongsDatabase
\c SongsDatabase

-- Create the media table
CREATE TABLE media (
    user_id SERIAL PRIMARY KEY,
    owner VARCHAR(50) NOT NULL,
    format VARCHAR(10) NOT NULL,
    artist VARCHAR(100) NOT NULL,
    title VARCHAR(100) NOT NULL,
    genre1 VARCHAR(100) NOT NULL,
    genre2 VARCHAR(100), -- Add the new columns but allow nulls for now
    lyrics VARCHAR(1000),
    audiobase64 TEXT
);

-- Insert initial data into media
INSERT INTO media (owner, format, artist, title, genre1) VALUES
('eduardo_albino', 'WAV', 'Ed_Sheeran', 'Shape_of_you', 'pop'),
('antonio_mendes', 'WAV', 'Linkin_Park', 'Numb', 'rock'),
('joao_bento', 'WAV', 'BMTH', 'Avalanche', 'arena rock');

-- Select all rows from media to verify insertion
SELECT * FROM media;
