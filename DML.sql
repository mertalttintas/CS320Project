-- DML.sql for Car Rental System (CRS)
USE db_crs;

INSERT INTO Roles (RoleID, RoleName) VALUES (1, 'Admin'), (2, 'Fleet Manager'), (3, 'Customer');

-- Passwords are set to SHA-256 hash for "123456"
INSERT INTO User (UserID, RoleID, Name, Email, Password) VALUES
(1, 1, 'System Admin', 'admin@crs.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
(2, 2, 'Hertz Manager', 'manager1@crs.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
(3, 2, 'Avis Manager', 'manager2@crs.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
(4, 3, 'John Doe', 'john@test.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
(5, 3, 'Jane Smith', 'jane@test.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
(6, 3, 'Alice Brown', 'alice@test.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
(7, 3, 'Bob White', 'bob@test.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
(8, 3, 'Charlie Black', 'charlie@test.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
(9, 3, 'David Green', 'david@test.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
(10, 3, 'Emma Blue', 'emma@test.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');

INSERT INTO Admin (UserID) VALUES (1);
INSERT INTO FleetManager (UserID, CompanyName) VALUES (2, 'Hertz Rentals'), (3, 'Avis Rentals');
INSERT INTO Customer (UserID) VALUES (4), (5), (6), (7), (8), (9), (10);

-- Diverse vehicles with categories
INSERT INTO Vehicle (ManagerID, Brand, Model, Location, FuelType, Transmission, Seats, DailyPrice, StockQuantity, Category, Status, Features) VALUES
(1, 'Toyota', 'Corolla', 'Istanbul', 'Gasoline', 'Automatic', 5, 1200.00, 2, 'Sedan', 'Available', 'Bluetooth, Backup Camera'),
(1, 'Toyota', 'RAV4', 'Istanbul', 'Hybrid', 'Automatic', 5, 1800.00, 2, 'SUV', 'Available', 'AWD, Sunroof'),
(1, 'Honda', 'Civic', 'Istanbul', 'Gasoline', 'Automatic', 5, 1300.00, 3, 'Sedan', 'Available', 'Apple CarPlay, Heated Seats'),
(1, 'BMW', '5 Series', 'Izmir', 'Gasoline', 'Automatic', 5, 3200.00, 1, 'Luxury', 'Available', 'Executive Package, Harman Kardon'),
(1, 'Mercedes', 'C-Class', 'Istanbul', 'Gasoline', 'Automatic', 5, 2600.00, 2, 'Luxury', 'Available', 'AMG Line, Burmester Audio'),
(1, 'Ford', 'Focus', 'Izmir', 'Diesel', 'Manual', 5, 1000.00, 4, 'Economy', 'Available', 'Bluetooth, Cruise Control'),
(1, 'Volkswagen', 'Polo', 'Ankara', 'Gasoline', 'Manual', 5, 900.00, 5, 'Economy', 'Available', 'Basic Features'),
(1, 'Audi', 'A6', 'Istanbul', 'Diesel', 'Automatic', 5, 3300.00, 1, 'Luxury', 'Available', 'Matrix LED, B&O Audio'),
(1, 'Fiat', 'Egea', 'Antalya', 'Diesel', 'Manual', 5, 850.00, 10, 'Economy', 'Available', 'Air Conditioning, USB'),
(1, 'Renault', 'Clio', 'Bursa', 'Gasoline', 'Automatic', 5, 950.00, 8, 'Hatchback', 'Available', 'Parking Sensors'),
(1, 'Peugeot', '3008', 'Istanbul', 'Diesel', 'Automatic', 5, 1700.00, 3, 'SUV', 'Available', 'Panoramic Roof, i-Cockpit'),
(1, 'Nissan', 'Qashqai', 'Ankara', 'Hybrid', 'Automatic', 5, 1600.00, 2, 'SUV', 'Available', '360 Camera, Navigation'),
(1, 'Hyundai', 'i20', 'Izmir', 'Gasoline', 'Automatic', 5, 1000.00, 5, 'Hatchback', 'Available', 'Compact, Easy Parking'),
(1, 'Kia', 'Sportage', 'Istanbul', 'Gasoline', 'Automatic', 5, 1650.00, 2, 'SUV', 'Available', 'Leather Seats, Lane Assist'),
(1, 'Volvo', 'XC90', 'Ankara', 'Hybrid', 'Automatic', 7, 4500.00, 1, 'Luxury', 'Available', 'Ultimate Safety, Bowers & Wilkins'),
(2, 'Toyota', 'Yaris', 'Istanbul', 'Hybrid', 'Automatic', 5, 1100.00, 5, 'Hatchback', 'Available', 'Fuel Efficient'),
(2, 'Honda', 'CR-V', 'Izmir', 'Gasoline', 'Automatic', 5, 1750.00, 3, 'SUV', 'Available', 'AWD, Sensing'),
(2, 'BMW', 'X5', 'Istanbul', 'Diesel', 'Automatic', 5, 4200.00, 1, 'Luxury', 'Available', 'xDrive, Laser Lights'),
(2, 'Mercedes', 'GLC', 'Ankara', 'Gasoline', 'Automatic', 5, 2900.00, 2, 'SUV', 'Available', '4MATIC, AMG Line'),
(2, 'Ford', 'Fiesta', 'Bursa', 'Gasoline', 'Manual', 5, 800.00, 10, 'Economy', 'Available', 'Compact, Manual');


INSERT INTO Reservation (CustomerID, VehicleID, PickupDate, ReturnDate, TotalPrice, Status) VALUES
(1, 1, '2026-03-01', '2026-03-05', 4800.00, 'Completed'),
(2, 16, '2026-03-10', '2026-03-12', 2200.00, 'Completed');
