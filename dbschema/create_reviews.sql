 CREATE TABLE reviews ( id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
                stars INTEGER NOT NULL DEFAULT 0,
                body text NOT NULL, 
                book_id INTEGER NOT NULL,
                created_at datetime, updated_at datetime,
                FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
                )