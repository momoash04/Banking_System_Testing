-- 1 Client Table
CREATE TABLE `bank_schema`.`client` (
                                        `client_id` int NOT NULL AUTO_INCREMENT,
                                        `f_name` varchar(45) NOT NULL,
                                        `l_name` varchar(45) NOT NULL,
                                        `father_name` varchar(45) NOT NULL,
                                        `mother_name` varchar(45) NOT NULL,
                                        `CNIC` varchar(45) NOT NULL,
                                        `DOB` date NOT NULL,
                                        `phone` varchar(45) NOT NULL,
                                        `email` varchar(45) DEFAULT NULL,
                                        `address` varchar(100) NOT NULL,
                                        PRIMARY KEY (`client_id`),
                                        UNIQUE KEY `client_id_UNIQUE` (`client_id`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE bank_schema.client AUTO_INCREMENT=10000;

-- 2 Login_Account Table
CREATE TABLE `bank_schema`.`login_account` (
                                               `login_id` int NOT NULL AUTO_INCREMENT,
                                               `username` varchar(45) NOT NULL,
                                               `password` char(8) NOT NULL,
                                               `type` char(1) NOT NULL,
                                               PRIMARY KEY (`login_id`),
                                               UNIQUE KEY `login_id_UNIQUE` (`login_id`)
) ENGINE=InnoDB AUTO_INCREMENT=206 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE bank_schema.login_account AUTO_INCREMENT=60000;

-- 3 Employee Table
CREATE TABLE `bank_schema`.`employee` (
                                          `employee_id` int NOT NULL AUTO_INCREMENT,
                                          `f_name` varchar(45) NOT NULL,
                                          `l_name` varchar(45) NOT NULL,
                                          `father_name` varchar(45) NOT NULL,
                                          `mother_name` varchar(45) NOT NULL,
                                          `job` varchar(45) NOT NULL,
                                          `phone_no` varchar(45) NOT NULL,
                                          `email` varchar(45) NOT NULL,
                                          `login_id` int DEFAULT NULL,
                                          PRIMARY KEY (`employee_id`),
                                          UNIQUE KEY `employee_id_UNIQUE` (`employee_id`),
                                          KEY `login_id_idx` (`login_id`),
                                          CONSTRAINT `login_id` FOREIGN KEY (`login_id`) REFERENCES `login_account` (`login_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE bank_schema.employee AUTO_INCREMENT=20000;

-- 4 Bank_Account table
CREATE TABLE `bank_schema`.`bank_account` (
                                              `acc_num` int NOT NULL AUTO_INCREMENT,
                                              `client_id` int NOT NULL,
                                              `login_id` int DEFAULT NULL,
                                              `type` char(10) NOT NULL,
                                              `balance` int NOT NULL,
                                              `status` int NOT NULL,
                                              `opening_date` date NOT NULL,
                                              PRIMARY KEY (`acc_num`),
                                              UNIQUE KEY `acc_num_UNIQUE` (`acc_num`),
                                              KEY `client_id` (`client_id`),
                                              KEY `login_id` (`login_id`),
                                              CONSTRAINT `bank_account_ibfk_1` FOREIGN KEY (`client_id`) REFERENCES `client` (`client_id`),
                                              CONSTRAINT `bank_account_ibfk_2` FOREIGN KEY (`login_id`) REFERENCES `login_account` (`login_id`)
) ENGINE=InnoDB AUTO_INCREMENT=402 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE bank_schema.bank_account AUTO_INCREMENT=500000;

-- data set

-- employees
Insert into bank_schema.employee values(NULL, "Abdelrahman", "Sallam", "Mostafa", "Mum", "Manager", "0129908336", "abdo@gmail.com", NULL );

-- clients
Insert Into bank_schema.client Values(NULL, "Omar", "Daradir", "Ahmed", "Mum", "7853257", STR_TO_DATE("30,1,2005", "%d,%m,%Y"), "01002345631", "GarGar@gmail.com", "24 Donught st. City of stars, La La Land");
Insert Into bank_schema.client Values(NULL, "Mohammed", "Ahmed", "Bashir", "Mum", "0978912", STR_TO_DATE("26,1,2005", "%d,%m,%Y"), "01027827193", "Bashi8@gmail.com", "In our hearts");
Insert Into bank_schema.client Values(NULL, "Mohammed", "Ashraf", "Ashraf", "Mamy", "7809821", STR_TO_DATE("19,8,2004", "%d,%m,%Y"), "03665132497", "Ashareefo@gmail.com", "Fe 3eyonna");
Insert Into bank_schema.client Values(NULL, "Roger", "Salama", "Sherif", "Mum", "0987456231", STR_TO_DATE("7,2,2004", "%d,%m,%Y"), "01002346789", "rogrog@gmail.com", "Fe 2olobna");

-- bank accounts
Insert Into bank_schema.bank_account Values(NULL, 10000, NULL,"Current", 1000, 1, CURDATE());
Insert Into bank_schema.bank_account Values(NULL, 10001, NULL,"Current", 20000, 1, CURDATE());
Insert Into bank_schema.bank_account Values(NULL, 10002, NULL,"Saving", 7000, 1, CURDATE());
Insert Into bank_schema.bank_account Values(NULL, 10003, NULL,"Saving", 7000, 1, CURDATE());

-- login account
Insert into bank_schema.login_account values (NULL, "Abdelrahman20", "Sallam", 'M');
Insert into bank_schema.login_account values (NULL, "OmarDardir", "Dardir", 'C');
Insert into bank_schema.login_account values (NULL, "Mbesheer", "Besheer", 'C');
Insert into bank_schema.login_account values (NULL, "MohAshraf", "Ashraf", 'C');
Insert into bank_schema.login_account values (NULL, "RogerSherif", "Salama", 'C');


update bank_schema.employee set login_id = 60000 where employee_id = 20000;
update bank_schema.bank_account set login_id = 60001 where acc_num = 500000;
update bank_schema.bank_account set login_id = 60002 where acc_num = 500001;
update bank_schema.bank_account set login_id = 60003 where acc_num = 500002;
update bank_schema.bank_account set login_id = 60004 where acc_num = 500003;
