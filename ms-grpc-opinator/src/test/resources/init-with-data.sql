DROP DATABASE IF EXISTS opinator_read_db;
CREATE DATABASE opinator_read_db;

DROP TABLE IF EXISTS categories cascade;
DROP TABLE IF EXISTS products cascade;
DROP TABLE IF EXISTS reviews cascade;
DROP TABLE IF EXISTS votes cascade;

CREATE TABLE categories (
	category_id uuid PRIMARY KEY,
    category_name varchar(100) not null unique 
	);
CREATE TABLE products ( 
	product_id uuid PRIMARY KEY,
	category_id uuid not null,
	product_name varchar(100) not null unique, 
	product_description varchar(500) 
	);
CREATE TABLE reviews (
	review_id uuid PRIMARY KEY,
	rating integer not null, 
	product_id uuid not null, 
	email varchar(100) not null, 
	review_text varchar(1000) not null 
	);
CREATE TABLE votes (
	vote_id uuid PRIMARY KEY,
	useful boolean not null, 
	review_id uuid not null, 
	email varchar(100) not null
	);

ALTER TABLE products 
ADD CONSTRAINT FK_products_categories 
FOREIGN KEY (category_id) 
REFERENCES categories (category_id);

ALTER TABLE reviews 
ADD CONSTRAINT FK_reviews_products 
FOREIGN KEY (product_id) 
REFERENCES products (product_id);

ALTER TABLE votes 
ADD CONSTRAINT FK_votes_reviews 
FOREIGN KEY (review_id) 
REFERENCES reviews (review_id);

INSERT INTO categories VALUES('3800f778-271d-4070-9b06-b4aa0ca68878', 'Cereal');
INSERT INTO categories VALUES('3800f778-271d-4070-9b06-b4aa0ca68879', 'Pulse');
INSERT INTO products VALUES('54f16ed2-4ccb-42fb-81ec-aa53afa80f4a', '3800f778-271d-4070-9b06-b4aa0ca68878', 'Corn', 'Corn Cereal');
INSERT INTO products VALUES('54f16ed2-4ccb-42fb-81ec-aa53afa80f4b', '3800f778-271d-4070-9b06-b4aa0ca68879', 'Lentil', 'Iron supplier');
INSERT INTO reviews VALUES('f6d705be-01db-44f4-bd31-51ead4e671c8', 4, '54f16ed2-4ccb-42fb-81ec-aa53afa80f4a', 'email01@email.tk', 'meh');
INSERT INTO reviews VALUES('f6d705be-01db-44f4-bd31-51ead4e671c9', 2, '54f16ed2-4ccb-42fb-81ec-aa53afa80f4a', 'email02@email.tk', 'puf');
INSERT INTO votes VALUES('4434c285-4ef5-4a25-a5fa-08866f7c89b5', true, 'f6d705be-01db-44f4-bd31-51ead4e671c8', 'email03@email.tk');
INSERT INTO votes VALUES('4434c285-4ef5-4a25-a5fa-08866f7c89b6', false, 'f6d705be-01db-44f4-bd31-51ead4e671c8', 'email04@email.tk');
INSERT INTO votes VALUES('4434c285-4ef5-4a25-a5fa-08866f7c89b7', true, 'f6d705be-01db-44f4-bd31-51ead4e671c8', 'email05@email.tk');

