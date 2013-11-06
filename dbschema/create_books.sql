CREATE TABLE books ( id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
                title varchar(50) NOT NULL,
                created_at datetime, updated_at datetime, author_id integer NOT NULL,
                FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE);