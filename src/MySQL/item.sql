CREATE TABLE items (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL, 
    sellerId INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description VARCHAR(500) NOT NULL, 
    type ENUM('ELECTRONICS', 'ARTS', 'VEHICLE', 'OTHER') NOT NULL,
    FOREIGN KEY (sellerId) REFERENCES user(id)
);
CREATE TABLE arts (
    itemId INT PRIMARY KEY,
    artist VARCHAR(100),
    yearOfcreation INT,
    dimensions VARCHAR(50),
    medium VARCHAR(100),
    FOREIGN KEY (itemId) REFERENCES items(id) ON DELETE CASCADE
);
CREATE TABLE electronics (
    itemId INT PRIMARY KEY,
    brand VARCHAR(100),
    power INT,
    voltage DECIMAL(10, 2), 
    current DECIMAL(10, 2),
    status VARCHAR(50),     
    color VARCHAR(30),
    weight DECIMAL(10, 2),
    FOREIGN KEY (itemId) REFERENCES items(id) ON DELETE CASCADE
);
CREATE TABLE vehicles (
    itemId INT PRIMARY KEY,
    titleStatus VARCHAR(100),
    trim VARCHAR(100),
    model VARCHAR(100),
    brand VARCHAR(100),
    mileage DECIMAL(10,2),
    mFG DATETIME,
    Foreign Key (itemId) REFERENCES items(id) ON DELETE CASCADE

);
