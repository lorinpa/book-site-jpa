CREATE TABLE book_categories ( id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
                book_id INTEGER NOT NULL,
                category_id INTEGER NOT NULL,
                created_at datetime, updated_at datetime,
                FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
                )