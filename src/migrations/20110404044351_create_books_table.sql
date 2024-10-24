CREATE TABLE books (
  id SERIAL PRIMARY KEY,
  author VARCHAR(128),
  title VARCHAR(128),
  isbn VARCHAR(128),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);