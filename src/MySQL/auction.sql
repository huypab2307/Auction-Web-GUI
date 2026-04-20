-- Active: 1774790809341@@localhost@3306@auction
CREATE TABLE auctions (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    itemId INT,
    startingPrice DECIMAL(10,2) NOT NULL,
    priceStep DECIMAL(10,2) NOT NULL,
    curPrice DECIMAL(10,2),
    lastBidderId INT,
    startTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    endTime DATETIME NOT NULL ,
    status ENUM('PENDING', 'OPEN', 'CLOSED', 'CANCELED') DEFAULT 'PENDING',
    FOREIGN KEY (itemId) REFERENCES items(id),
    FOREIGN KEY (lastBidderId) REFERENCES user(id),
    CONSTRAINT chk_price_step CHECK (priceStep <= startingPrice AND priceStep > 0),
    CONSTRAINT chk_time CHECK (endTime > startTime)
);
CREATE TABLE bidTransactions (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    auctionId INT NOT NULL,
    bidAmount DOUBLE NOT NULL,         
    createdAt DATETIME DEFAULT NOW(),  
    FOREIGN KEY (userId) REFERENCES user(id),
    FOREIGN KEY (auctionId) REFERENCES auctions(id)
);

CREATE TABLE notification (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    auctionId INT NOT NULL,
    message VARCHAR(255) NOT NULL, 
    isChecked BOOLEAN NOT NULL DEFAULT false, 
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES user(id),
    FOREIGN KEY (auctionId) REFERENCES auctions(id)
);
SELECT DISTINCT userId FROM notification