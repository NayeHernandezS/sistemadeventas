-- BD legacy: username/email suelen ser VARCHAR(20-50); la app usa hasta 100/150.
ALTER TABLE usuarios MODIFY username VARCHAR(100) NOT NULL;
ALTER TABLE usuarios MODIFY email VARCHAR(150) NOT NULL;
