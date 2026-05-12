CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('BIDDER', 'SELLER', 'ADMIN') NOT NULL DEFAULT 'BIDDER',
    avatar VARCHAR(500) DEFAULT 'https://res.cloudinary.com/devnd8ndw/image/upload/v1778324818/earth_d8ylgw.png'
);
CREATE TABLE items (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL, 
    sellerId INT NOT NULL,
    description TEXT NOT NULL, 
    type ENUM('ELECTRONICS', 'ARTS', 'VEHICLE', 'OTHER') NOT NULL,
    imagePath VARCHAR(500),
    CONSTRAINT fk_items_seller FOREIGN KEY (sellerId) REFERENCES user(id) ON DELETE CASCADE
);
CREATE TABLE arts (
    itemId INT PRIMARY KEY,
    artist VARCHAR(100),
    yearOfcreation INT,
    dimensions VARCHAR(50),
    medium VARCHAR(100),
    CONSTRAINT fk_arts_item FOREIGN KEY (itemId) REFERENCES items(id) ON DELETE CASCADE
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
    CONSTRAINT fk_elec_item FOREIGN KEY (itemId) REFERENCES items(id) ON DELETE CASCADE
);

CREATE TABLE vehicles (
    itemId INT PRIMARY KEY,
    titleStatus VARCHAR(100),
    trim VARCHAR(100),
    model VARCHAR(100),
    brand VARCHAR(100),
    mileage DECIMAL(10,2),
    mFG INT,
    CONSTRAINT fk_veh_item FOREIGN KEY (itemId) REFERENCES items(id) ON DELETE CASCADE
);
CREATE TABLE auctions (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    itemId INT NOT NULL,
    sellerId INT NOT NULL,
    startingPrice DECIMAL(15,2) NOT NULL,
    priceStep DECIMAL(15,2) NOT NULL,
    curPrice DECIMAL(15,2),
    lastBidderId INT,
    startTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    endTime DATETIME NOT NULL,
    status ENUM('PENDING', 'OPEN', 'CLOSED', 'CANCELED') DEFAULT 'PENDING',
    CONSTRAINT fk_auc_item FOREIGN KEY (itemId) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_auc_seller FOREIGN KEY (sellerId) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_auc_bidder FOREIGN KEY (lastBidderId) REFERENCES user(id) ON DELETE SET NULL, -- Nếu Bidder bị xóa, giữ lại đấu giá nhưng để trống người thắng
    CONSTRAINT chk_time CHECK (endTime > startTime)
);
CREATE TABLE bidTransactions (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    auctionId INT NOT NULL,
    bidAmount DECIMAL(15,2) NOT NULL,         
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,  
    CONSTRAINT fk_bid_user FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_bid_auc FOREIGN KEY (auctionId) REFERENCES auctions(id) ON DELETE CASCADE
);

CREATE TABLE notification (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    auctionId INT NOT NULL,
    message VARCHAR(500) NOT NULL, 
    isChecked BOOLEAN NOT NULL DEFAULT false, 
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_noti_user FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_noti_auc FOREIGN KEY (auctionId) REFERENCES auctions(id) ON DELETE CASCADE
);


CREATE TABLE autoBidding (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    auctionId INT NOT NULL,
    userId INT NOT NULL,
    maxPrice DECIMAL(15, 2) NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auto_auc FOREIGN KEY (auctionId) REFERENCES auctions(id) ON DELETE CASCADE,
    CONSTRAINT fk_auto_user FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE
);
CREATE TABLE notificationList (
    userId INT NOT NULL,
    auctionId INT NOT NULL,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userId, auctionId), 
    CONSTRAINT fk_notiList_user FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_notiList_auc FOREIGN KEY (auctionId) REFERENCES auctions(id) ON DELETE CASCADE
);

