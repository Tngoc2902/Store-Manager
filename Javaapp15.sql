CREATE DATABASE ShopDB;

CREATE TABLE Customers (
    CustomerId INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(100),
    Email NVARCHAR(100),
    Phone NVARCHAR(20)
);

-- Tạo bảng Products
CREATE TABLE Products (
    ProductID INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(100) NOT NULL,
    Price FLOAT NOT NULL,
    Quantity INT NOT NULL
);


-- Tạo bảng Orders
CREATE TABLE Orders (
    OrderID INT PRIMARY KEY IDENTITY(1,1),
    CustomerId INT NOT NULL,
    OrderDate DATETIME NOT NULL,
    TotalAmount FLOAT NOT NULL,
    FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId)
);


-- Tạo bảng OrderDetails
CREATE TABLE OrderDetails (
    OrderDetailID INT PRIMARY KEY IDENTITY(1,1),
    OrderID INT NOT NULL,
    ProductID INT NOT NULL,
    Quantity INT NOT NULL,
    Price FLOAT NOT NULL,
    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);


CREATE TABLE Invoices (
    InvoiceID INT PRIMARY KEY IDENTITY(1,1),
    CustomerName NVARCHAR(100),
    Email NVARCHAR(100),
    Phone NVARCHAR(20),
    Address NVARCHAR(200),
    CreatedAt DATETIME DEFAULT GETDATE()
);


CREATE TABLE InvoiceDetails (
    ID INT PRIMARY KEY IDENTITY(1,1),
    InvoiceID INT,           -- Mã hóa đơn
    ProductID INT,           -- Mã sản phẩm
    Quantity INT NOT NULL,   -- Số lượng mua
    Price FLOAT NOT NULL,    -- Giá tại thời điểm bán
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
    -- Có thể thêm FOREIGN KEY (InvoiceID) nếu có bảng Invoice
);

-- Thêm một số sản phẩm mẫu
INSERT INTO Products (Name, Price, Quantity) VALUES
(N'Chuột Logitech', 300000, 20),
(N'Bàn phím cơ AKKO', 1200000, 15),
(N'Màn hình Samsung 24 inch', 3500000, 10);


ALTER TABLE Customers
ADD Address NVARCHAR(255);

ALTER TABLE OrderDetails
ADD CONSTRAINT FK_ProductID
FOREIGN KEY (ProductID)
REFERENCES Products(ID)
ON DELETE CASCADE;



INSERT INTO Products (Name, Price, Quantity) VALUES
(N'Laptop Dell XPS 13', 28000000, 5),
(N'Bàn phím không dây Rapoo', 450000, 25),
(N'Chuột Gaming Razer', 1500000, 12),
(N'Loa Bluetooth JBL', 2200000, 8),
(N'Tai nghe Sony WH-1000XM4', 6900000, 6),
(N'USB SanDisk 64GB', 250000, 50),
(N'Ổ cứng SSD Samsung 1TB', 2900000, 7),
(N'RAM Kingston 16GB DDR4', 1200000, 14),
(N'Card màn hình RTX 3060', 9500000, 4),
(N'Máy in Canon LBP 2900', 2800000, 3),
(N'Màn hình LG 27 inch', 4100000, 9),
(N'Camera hành trình VietMap', 2300000, 6),
(N'Balo laptop Xiaomi', 590000, 18),
(N'Sạc dự phòng Anker 10000mAh', 750000, 20),
(N'Giá đỡ điện thoại đa năng', 120000, 40),
(N'Bàn laptop gấp gọn', 320000, 22),
(N'Chuột không dây Microsoft', 470000, 17),
(N'Tai nghe AirPods Pro', 5800000, 5),
(N'Webcam Logitech C920', 2100000, 7),
(N'Router Wi-Fi TP-Link AX50', 1750000, 10),
(N'Bàn phím DareU EK87', 890000, 11),
(N'Chuột SteelSeries Rival 3', 890000, 13),
(N'Màn hình ASUS ProArt 27"', 9800000, 4),
(N'Ổ cứng HDD WD 1TB', 1450000, 9),
(N'Cáp sạc Type-C Anker', 190000, 35),
(N'Laptop Lenovo Ideapad 3', 16500000, 6),
(N'Bộ loa vi tính Microlab', 630000, 15);


ALTER TABLE InvoiceDetails
ADD CONSTRAINT FK_InvoiceID
FOREIGN KEY (InvoiceID)
REFERENCES Invoices(InvoiceID)
ON DELETE CASCADE;


CREATE INDEX idx_OrderDetails_OrderId ON OrderDetails(OrderID);
CREATE INDEX idx_InvoiceDetails_InvoiceID ON InvoiceDetails(InvoiceID);

ALTER TABLE Products
ALTER COLUMN Price DECIMAL(18,2);

ALTER TABLE OrderDetails
ALTER COLUMN Price DECIMAL(18,2);

ALTER TABLE Orders
ALTER COLUMN TotalAmount DECIMAL(18,2);

ALTER TABLE OrderDetails
DROP CONSTRAINT FK_ProductID;

ALTER TABLE OrderDetails
ADD CONSTRAINT FK_ProductID
FOREIGN KEY (ProductID)
REFERENCES Products(ProductID)
ON DELETE CASCADE;
