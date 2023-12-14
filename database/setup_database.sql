-- setup_database.sql

-- Create the SongsDatabase
CREATE DATABASE SongsDatabase;

-- Connect to the SongsDatabase
\c songsdatabase

-- Create the media table
CREATE TABLE media (
    user_id SERIAL PRIMARY KEY,
    owner VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    file TEXT NOT NULL
);

-- Insert initial data into media
INSERT INTO media (owner, title, file) VALUES
('eduardo_albino', 'Numb', 'Ed_SheeranShape_of_youpop');

-- Select all rows from media to verify insertion
SELECT * FROM media;
