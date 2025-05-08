-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: localhost    Database: t_dev
-- ------------------------------------------------------
-- Server version	8.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ax_salud_woo_user`
--

DROP TABLE IF EXISTS `ax_salud_woo_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ax_salud_woo_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doctor_welcome_message` varchar(255) DEFAULT NULL,
  `hid` varchar(255) DEFAULT NULL,
  `location_offices` enum('MX') DEFAULT NULL,
  `service_provider` bigint NOT NULL,
  `state` enum('OFFLINE','ONLINE','UNAVAILABLE') DEFAULT NULL,
  `user_type` enum('DOCTOR','HEALTH_SERVICE_PROVIDER','PATIENT','PSYCHOLOGIST') DEFAULT NULL,
  `core_user_id` bigint DEFAULT NULL,
  `doctor_data_user_id` bigint DEFAULT NULL,
  `patient_data_user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3wng06knfci2o6q4ts1yn8j6o` (`core_user_id`),
  UNIQUE KEY `UKhx22t0b7t4qmt3ndaitjuka4n` (`doctor_data_user_id`),
  UNIQUE KEY `UKh5mltaqg441fc9wedhsse0nr7` (`patient_data_user_id`),
  CONSTRAINT `FKan2jycuakr30794rdt8es1em9` FOREIGN KEY (`patient_data_user_id`) REFERENCES `patient_data` (`id`),
  CONSTRAINT `FKcc9tvvyw4idvis4i3k0eq0chc` FOREIGN KEY (`core_user_id`) REFERENCES `woow_user` (`user_id`),
  CONSTRAINT `FKphq2yllonh4ayfrmy8hhk8mm0` FOREIGN KEY (`doctor_data_user_id`) REFERENCES `doctor_data` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ax_salud_woo_user`
--

LOCK TABLES `ax_salud_woo_user` WRITE;
/*!40000 ALTER TABLE `ax_salud_woo_user` DISABLE KEYS */;
INSERT INTO `ax_salud_woo_user` VALUES (1,NULL,'HID-123',NULL,21,'OFFLINE','PATIENT',1,NULL,NULL),(2,NULL,'HID-123',NULL,1,'OFFLINE','PATIENT',2,NULL,NULL);
/*!40000 ALTER TABLE `ax_salud_woo_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultation`
--

DROP TABLE IF EXISTS `consultation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `consultation_id` binary(16) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `current_session_id_if_exists` varchar(255) DEFAULT NULL,
  `finished_at` datetime(6) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` enum('FINISHED','ON_GOING','SUSPENDED','WAITING_FOR_DOCTOR') DEFAULT NULL,
  `symptoms` text NOT NULL,
  `patient_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKio232y6wlf8nty9cxyrv7tver` (`patient_id`),
  CONSTRAINT `FKio232y6wlf8nty9cxyrv7tver` FOREIGN KEY (`patient_id`) REFERENCES `ax_salud_woo_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation`
--

LOCK TABLES `consultation` WRITE;
/*!40000 ALTER TABLE `consultation` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultation_document`
--

DROP TABLE IF EXISTS `consultation_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation_document` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `element_public_id` varchar(255) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `file_type` varchar(255) DEFAULT NULL,
  `format` varchar(255) DEFAULT NULL,
  `last_accessed_at` datetime(6) DEFAULT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `secure_url` varchar(255) DEFAULT NULL,
  `uploader_role` enum('DOCTOR','HEALTH_SERVICE_PROVIDER','PATIENT','PSYCHOLOGIST') DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `consultation_session_id` bigint DEFAULT NULL,
  `uploaded_by_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgey5u8v6y61e1dosdiyaisp87` (`consultation_session_id`),
  KEY `FKkpgdwcltpcdm9rmwcpoj7nppp` (`uploaded_by_id`),
  CONSTRAINT `FKgey5u8v6y61e1dosdiyaisp87` FOREIGN KEY (`consultation_session_id`) REFERENCES `consultation_session` (`id`),
  CONSTRAINT `FKkpgdwcltpcdm9rmwcpoj7nppp` FOREIGN KEY (`uploaded_by_id`) REFERENCES `ax_salud_woo_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_document`
--

LOCK TABLES `consultation_document` WRITE;
/*!40000 ALTER TABLE `consultation_document` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultation_document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultation_message_entity`
--

DROP TABLE IF EXISTS `consultation_message_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation_message_entity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `message_type` varchar(255) DEFAULT NULL,
  `status` enum('DELIVERED','SENT_TO_RECEIVER','SERVER_RECEIVED') DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  `consultation_session_id` bigint DEFAULT NULL,
  `sent_by_user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4t0cgxlofpujqldaqkx14ecjp` (`consultation_session_id`),
  KEY `FKt5rq9pd9k4oc99y7nrc72s0jm` (`sent_by_user_id`),
  CONSTRAINT `FK4t0cgxlofpujqldaqkx14ecjp` FOREIGN KEY (`consultation_session_id`) REFERENCES `consultation_session` (`id`),
  CONSTRAINT `FKt5rq9pd9k4oc99y7nrc72s0jm` FOREIGN KEY (`sent_by_user_id`) REFERENCES `ax_salud_woo_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_message_entity`
--

LOCK TABLES `consultation_message_entity` WRITE;
/*!40000 ALTER TABLE `consultation_message_entity` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultation_message_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultation_session`
--

DROP TABLE IF EXISTS `consultation_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `consultation_session_id` binary(16) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `finished_at` datetime(6) DEFAULT NULL,
  `start_at` datetime(6) DEFAULT NULL,
  `status` enum('FINISHED','ON_GOING','SUSPENDED','WAITING_FOR_DOCTOR') DEFAULT NULL,
  `consultation_id` bigint DEFAULT NULL,
  `doctor_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk4fq5hpgjjmq1wjpjtty0dwxg` (`consultation_id`),
  KEY `FKdiwihht1jeq8i3sgugtybr782` (`doctor_id`),
  CONSTRAINT `FKdiwihht1jeq8i3sgugtybr782` FOREIGN KEY (`doctor_id`) REFERENCES `ax_salud_woo_user` (`id`),
  CONSTRAINT `FKk4fq5hpgjjmq1wjpjtty0dwxg` FOREIGN KEY (`consultation_id`) REFERENCES `consultation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_session`
--

LOCK TABLES `consultation_session` WRITE;
/*!40000 ALTER TABLE `consultation_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultation_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultation_session_closed_by`
--

DROP TABLE IF EXISTS `consultation_session_closed_by`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation_session_closed_by` (
  `consultation_session_id` bigint NOT NULL,
  `closed_by` varchar(255) DEFAULT NULL,
  KEY `FK8q5yxl34gemh48iaf9u436vla` (`consultation_session_id`),
  CONSTRAINT `FK8q5yxl34gemh48iaf9u436vla` FOREIGN KEY (`consultation_session_id`) REFERENCES `consultation_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_session_closed_by`
--

LOCK TABLES `consultation_session_closed_by` WRITE;
/*!40000 ALTER TABLE `consultation_session_closed_by` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultation_session_closed_by` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctor_data`
--

DROP TABLE IF EXISTS `doctor_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `license_number` varchar(255) DEFAULT NULL,
  `speciality` varchar(255) DEFAULT NULL,
  `university` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor_data`
--

LOCK TABLES `doctor_data` WRITE;
/*!40000 ALTER TABLE `doctor_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `doctor_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctor_prescription`
--

DROP TABLE IF EXISTS `doctor_prescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor_prescription` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `diagnostico` text NOT NULL,
  `notas_de_recomendaciones` text NOT NULL,
  `observaciones_medicas` text NOT NULL,
  `receta_medica` text NOT NULL,
  `consultation_session_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6chsqabkemqogwfg69xdi7fit` (`consultation_session_id`),
  CONSTRAINT `FK6chsqabkemqogwfg69xdi7fit` FOREIGN KEY (`consultation_session_id`) REFERENCES `consultation_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor_prescription`
--

LOCK TABLES `doctor_prescription` WRITE;
/*!40000 ALTER TABLE `doctor_prescription` DISABLE KEYS */;
/*!40000 ALTER TABLE `doctor_prescription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `health_service_provider_review`
--

DROP TABLE IF EXISTS `health_service_provider_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_service_provider_review` (
  `id` int NOT NULL AUTO_INCREMENT,
  `service_type` int NOT NULL,
  `text` text NOT NULL,
  `total_starts` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_service_provider_review`
--

LOCK TABLES `health_service_provider_review` WRITE;
/*!40000 ALTER TABLE `health_service_provider_review` DISABLE KEYS */;
/*!40000 ALTER TABLE `health_service_provider_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jwt_black_list`
--

DROP TABLE IF EXISTS `jwt_black_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jwt_black_list` (
  `id` int NOT NULL,
  `creation_date` datetime(6) NOT NULL,
  `token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jwt_black_list`
--

LOCK TABLES `jwt_black_list` WRITE;
/*!40000 ALTER TABLE `jwt_black_list` DISABLE KEYS */;
/*!40000 ALTER TABLE `jwt_black_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jwt_black_list_seq`
--

DROP TABLE IF EXISTS `jwt_black_list_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jwt_black_list_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jwt_black_list_seq`
--

LOCK TABLES `jwt_black_list_seq` WRITE;
/*!40000 ALTER TABLE `jwt_black_list_seq` DISABLE KEYS */;
/*!40000 ALTER TABLE `jwt_black_list_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `laboratory_prescription`
--

DROP TABLE IF EXISTS `laboratory_prescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `laboratory_prescription` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notas_de_recomendaciones` text NOT NULL,
  `observaciones_medicas` text NOT NULL,
  `orden_de_laboratorio` text NOT NULL,
  `posible_diagnostico` text NOT NULL,
  `consultation_session_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKiw5058cmn1qhpp5wajcjq7x7w` (`consultation_session_id`),
  CONSTRAINT `FKiw5058cmn1qhpp5wajcjq7x7w` FOREIGN KEY (`consultation_session_id`) REFERENCES `consultation_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `laboratory_prescription`
--

LOCK TABLES `laboratory_prescription` WRITE;
/*!40000 ALTER TABLE `laboratory_prescription` DISABLE KEYS */;
/*!40000 ALTER TABLE `laboratory_prescription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient_additional`
--

DROP TABLE IF EXISTS `patient_additional`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_additional` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `birth` date DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `patient_data_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKto7o1iau4rsbw08adqxct2uhb` (`patient_data_id`),
  CONSTRAINT `FKto7o1iau4rsbw08adqxct2uhb` FOREIGN KEY (`patient_data_id`) REFERENCES `patient_data` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_additional`
--

LOCK TABLES `patient_additional` WRITE;
/*!40000 ALTER TABLE `patient_additional` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_additional` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient_data`
--

DROP TABLE IF EXISTS `patient_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alcohol` text NOT NULL,
  `allergies` text NOT NULL,
  `build` float NOT NULL,
  `diseases` text NOT NULL,
  `emergency_contact_name` varchar(255) DEFAULT NULL,
  `emergency_contact_number` varchar(255) DEFAULT NULL,
  `feeding` text NOT NULL,
  `height` float NOT NULL,
  `hospitalized` text NOT NULL,
  `hours_you_sleep` text NOT NULL,
  `medical_treatment` text NOT NULL,
  `medications` text NOT NULL,
  `occupation` text NOT NULL,
  `physical_activity` text NOT NULL,
  `preexistences` text NOT NULL,
  `smoke` text NOT NULL,
  `supplements` text NOT NULL,
  `surgery` text NOT NULL,
  `weight` float NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_data`
--

LOCK TABLES `patient_data` WRITE;
/*!40000 ALTER TABLE `patient_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_provider`
--

DROP TABLE IF EXISTS `service_provider`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_provider` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `endpoint` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `service_expiration` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_provider`
--

LOCK TABLES `service_provider` WRITE;
/*!40000 ALTER TABLE `service_provider` DISABLE KEYS */;
INSERT INTO `service_provider` VALUES (1,'2025-05-08 08:31:31.000000','https://localhost:8080/external_provider','HealthConnect','2025-12-31');
/*!40000 ALTER TABLE `service_provider` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  KEY `FK28nhh0goa9n5ch31eob428g67` (`user_id`),
  CONSTRAINT `FK28nhh0goa9n5ch31eob428g67` FOREIGN KEY (`user_id`) REFERENCES `woow_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,'ADMIN'),(2,'USER');
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `woow_user`
--

DROP TABLE IF EXISTS `woow_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `woow_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `accept_terms_and_conditions` varchar(255) DEFAULT NULL,
  `address_line1` varchar(255) DEFAULT NULL,
  `address_line2` varchar(255) DEFAULT NULL,
  `birth` date DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `cp` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `email_confirm` bit(1) NOT NULL,
  `imgurl` varchar(255) DEFAULT NULL,
  `is_user_blocked` int NOT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `login_attempts` int NOT NULL,
  `mfa` int NOT NULL,
  `mobile_phone` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `nationality` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_number_confirm` bit(1) NOT NULL,
  `state` varchar(255) DEFAULT NULL,
  `user_active` bit(1) NOT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `woow_user`
--

LOCK TABLES `woow_user` WRITE;
/*!40000 ALTER TABLE `woow_user` DISABLE KEYS */;
INSERT INTO `woow_user` VALUES (1,'yes','Av Reforma 123','Int 5','1995-06-15','CDMX','MX','01234','2025-05-08 08:31:31.000000','master@example.com',_binary '',NULL,0,'masterLastName',0,1,'5551234567','master@example.com',NULL,'$2a$10$nOyz3qX1lLYv9GOZcwBieeO1KSYDT6funrQx322uHIuX8LWY9XQQW',_binary '\0','CDMX',_binary '','master@example.com'),(2,'yes','Street 1','Street 2',NULL,'CDMX','MX','12345','2025-05-08 07:31:32.977284','realuser@woow.com',_binary '\0',NULL,0,'User',0,0,'1234567890','Real',NULL,'$2a$10$mCbCeWuO7AMco8sd7uWQQe7fbChLA2PV/ObAnIyXzBMTbZ/CaaaZG',_binary '\0','CDMX',_binary '','realuser@woow.com');
/*!40000 ALTER TABLE `woow_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-08  8:41:29
