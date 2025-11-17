-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: qlksdb
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `bang_gia`
--

DROP TABLE IF EXISTS `bang_gia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bang_gia` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `gia_ap_dung` decimal(38,2) DEFAULT NULL,
  `ngay_bat_dau` datetime(6) DEFAULT NULL,
  `ngay_ket_thuc` datetime(6) DEFAULT NULL,
  `phong_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnlt6td8wv1kryb85dqe8i7k10` (`phong_id`),
  CONSTRAINT `FKnlt6td8wv1kryb85dqe8i7k10` FOREIGN KEY (`phong_id`) REFERENCES `phong` (`id`),
  CONSTRAINT `bang_gia_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bang_gia`
--

LOCK TABLES `bang_gia` WRITE;
/*!40000 ALTER TABLE `bang_gia` DISABLE KEYS */;
/*!40000 ALTER TABLE `bang_gia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_dat_phong`
--

DROP TABLE IF EXISTS `chi_tiet_dat_phong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_dat_phong` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `check_in_thuc_te` bigint DEFAULT NULL,
  `checkout_thuc_te` bigint DEFAULT NULL,
  `gia` decimal(38,2) DEFAULT NULL,
  `status_chi_tiet` enum('BOOKED','CANCELLED','CHECKIN','CHECKOUT') DEFAULT NULL,
  `phieu_dat_phong_id` varchar(36) DEFAULT NULL,
  `phong_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7xhxwga2i94fiur3r27x4uj7x` (`phieu_dat_phong_id`),
  KEY `FK2avs50pdi1vq6kkrtvioi7yas` (`phong_id`),
  CONSTRAINT `FK2avs50pdi1vq6kkrtvioi7yas` FOREIGN KEY (`phong_id`) REFERENCES `phong` (`id`),
  CONSTRAINT `FK7xhxwga2i94fiur3r27x4uj7x` FOREIGN KEY (`phieu_dat_phong_id`) REFERENCES `phieu_dat_phong` (`id`),
  CONSTRAINT `chi_tiet_dat_phong_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_dat_phong`
--

LOCK TABLES `chi_tiet_dat_phong` WRITE;
/*!40000 ALTER TABLE `chi_tiet_dat_phong` DISABLE KEYS */;
/*!40000 ALTER TABLE `chi_tiet_dat_phong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dat_phong`
--

DROP TABLE IF EXISTS `dat_phong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dat_phong` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `ghi_chu` varchar(255) DEFAULT NULL,
  `gia_hien_tai` decimal(38,2) DEFAULT NULL,
  `loai_phong` varchar(255) DEFAULT NULL,
  `ma_phong` varchar(255) DEFAULT NULL,
  `suc_chua` varchar(255) DEFAULT NULL,
  `ten_phong` varchar(255) DEFAULT NULL,
  `thoi_gian_check_in` datetime(6) DEFAULT NULL,
  `thoi_gian_check_out` datetime(6) DEFAULT NULL,
  `thoi_gian_dat` datetime(6) DEFAULT NULL,
  `trang_thai_phong` tinyint DEFAULT NULL,
  `id_khach_hang` varchar(36) DEFAULT NULL,
  `id_lich_su_dat_phong` varchar(36) DEFAULT NULL,
  `id_phong` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKncvf8dp2k6gql2cvbu0u465m6` (`id_khach_hang`),
  KEY `FKk2a31c7n6jclwhb3gbavs7fh4` (`id_lich_su_dat_phong`),
  KEY `FKbo6ts9iwq1rerjl4p4rvba9g8` (`id_phong`),
  CONSTRAINT `FKbo6ts9iwq1rerjl4p4rvba9g8` FOREIGN KEY (`id_phong`) REFERENCES `phong` (`id`),
  CONSTRAINT `FKk2a31c7n6jclwhb3gbavs7fh4` FOREIGN KEY (`id_lich_su_dat_phong`) REFERENCES `lich_su_dat_phong` (`id`),
  CONSTRAINT `FKncvf8dp2k6gql2cvbu0u465m6` FOREIGN KEY (`id_khach_hang`) REFERENCES `khach_hang` (`id`),
  CONSTRAINT `dat_phong_chk_1` CHECK ((`status` between 0 and 1)),
  CONSTRAINT `dat_phong_chk_2` CHECK ((`trang_thai_phong` between 0 and 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dat_phong`
--

LOCK TABLES `dat_phong` WRITE;
/*!40000 ALTER TABLE `dat_phong` DISABLE KEYS */;
/*!40000 ALTER TABLE `dat_phong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dich_vu_phat_sinh`
--

DROP TABLE IF EXISTS `dich_vu_phat_sinh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dich_vu_phat_sinh` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `don_gia` varchar(255) DEFAULT NULL,
  `ma_dich_vu` varchar(255) DEFAULT NULL,
  `so_luong` int DEFAULT NULL,
  `ten_dich_vu` varchar(255) DEFAULT NULL,
  `thanh_tien` decimal(38,2) DEFAULT NULL,
  `id_dat_phong` varchar(36) DEFAULT NULL,
  `id_le_tan` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKe9b99ftii5dip9np5fd67tvwo` (`id_dat_phong`),
  KEY `FK1wxmvacnwwdng869335f5vog9` (`id_le_tan`),
  CONSTRAINT `FK1wxmvacnwwdng869335f5vog9` FOREIGN KEY (`id_le_tan`) REFERENCES `le_tan` (`id`),
  CONSTRAINT `FKe9b99ftii5dip9np5fd67tvwo` FOREIGN KEY (`id_dat_phong`) REFERENCES `dat_phong` (`id`),
  CONSTRAINT `dich_vu_phat_sinh_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dich_vu_phat_sinh`
--

LOCK TABLES `dich_vu_phat_sinh` WRITE;
/*!40000 ALTER TABLE `dich_vu_phat_sinh` DISABLE KEYS */;
/*!40000 ALTER TABLE `dich_vu_phat_sinh` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `khach_hang`
--

DROP TABLE IF EXISTS `khach_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khach_hang` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `dia_chi` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `ho_ten` varchar(255) DEFAULT NULL,
  `ma_nguoi_dung` varchar(255) DEFAULT NULL,
  `ngay_het_han_ho_chieu` varchar(255) DEFAULT NULL,
  `quoc_tich` varchar(255) DEFAULT NULL,
  `so-cccd` varchar(255) DEFAULT NULL,
  `so_dien_thoai` varchar(255) DEFAULT NULL,
  `so_ho_chieu` varchar(255) DEFAULT NULL,
  `id_loai_khach_hang` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKob7anijbxdwxgx299sddarrbr` (`id_loai_khach_hang`),
  CONSTRAINT `FKob7anijbxdwxgx299sddarrbr` FOREIGN KEY (`id_loai_khach_hang`) REFERENCES `loai_khach_hang` (`id`),
  CONSTRAINT `khach_hang_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khach_hang`
--

LOCK TABLES `khach_hang` WRITE;
/*!40000 ALTER TABLE `khach_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `khach_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `le_tan`
--

DROP TABLE IF EXISTS `le_tan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `le_tan` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `gioi_tinh` decimal(38,2) DEFAULT NULL,
  `ma_le_tan` varchar(255) DEFAULT NULL,
  `mat_khau` varchar(255) DEFAULT NULL,
  `so_dien_thoai` varchar(255) DEFAULT NULL,
  `ten_dang_nhap` varchar(255) DEFAULT NULL,
  `ten_le_tan` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `le_tan_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `le_tan`
--

LOCK TABLES `le_tan` WRITE;
/*!40000 ALTER TABLE `le_tan` DISABLE KEYS */;
/*!40000 ALTER TABLE `le_tan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lich_su_dat_phong`
--

DROP TABLE IF EXISTS `lich_su_dat_phong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lich_su_dat_phong` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `id_dat_phong` varchar(36) DEFAULT NULL,
  `id_le_tan` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKor79n97imtxl81kcwx3tkcxb6` (`id_dat_phong`),
  KEY `FKjx0avy8mdsulkllrawflpb777` (`id_le_tan`),
  CONSTRAINT `FKjx0avy8mdsulkllrawflpb777` FOREIGN KEY (`id_le_tan`) REFERENCES `le_tan` (`id`),
  CONSTRAINT `FKor79n97imtxl81kcwx3tkcxb6` FOREIGN KEY (`id_dat_phong`) REFERENCES `dat_phong` (`id`),
  CONSTRAINT `lich_su_dat_phong_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_dat_phong`
--

LOCK TABLES `lich_su_dat_phong` WRITE;
/*!40000 ALTER TABLE `lich_su_dat_phong` DISABLE KEYS */;
/*!40000 ALTER TABLE `lich_su_dat_phong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loai_khach_hang`
--

DROP TABLE IF EXISTS `loai_khach_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loai_khach_hang` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `ma_loai` varchar(255) DEFAULT NULL,
  `mo_ta` varchar(255) DEFAULT NULL,
  `ten_loai` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `loai_khach_hang_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loai_khach_hang`
--

LOCK TABLES `loai_khach_hang` WRITE;
/*!40000 ALTER TABLE `loai_khach_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `loai_khach_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loai_phong`
--

DROP TABLE IF EXISTS `loai_phong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loai_phong` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `mo_ta` varchar(255) DEFAULT NULL,
  `so_giuong_doi` int DEFAULT NULL,
  `so_giuong_don` int DEFAULT NULL,
  `so_nguoi_quy_dinh` int DEFAULT NULL,
  `so_nguoi_toi_da` int DEFAULT NULL,
  `gia_ca_ngay` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `loai_phong_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loai_phong`
--

LOCK TABLES `loai_phong` WRITE;
/*!40000 ALTER TABLE `loai_phong` DISABLE KEYS */;
INSERT INTO `loai_phong` VALUES ('210c45f7-74c7-426f-b166-e09e6359ae75',1763239071605,1763239515244,'LP-7674',0,'Phòng Suite',NULL,0,3,3,6,2000000.00),('68400b44-661d-4a2b-9969-b24d7effff67',1763238965340,1763239538062,'LP-5719',0,'Phòng Đơn',NULL,1,1,2,3,1000000.00),('c44bd342-63bf-4f2e-8f0b-278e6c520a0b',1763239010587,1763239544916,'LP-3436',0,'Phòng Đôi',NULL,0,2,2,4,1500000.00);
/*!40000 ALTER TABLE `loai_phong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhan_vien`
--

DROP TABLE IF EXISTS `nhan_vien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhan_vien` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `cccd` varchar(255) DEFAULT NULL,
  `chuc_vu` tinyint DEFAULT NULL,
  `dia_chi` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `gioi_timh` bit(1) DEFAULT NULL,
  `huyen` varchar(255) DEFAULT NULL,
  `mat_khau` varchar(255) DEFAULT NULL,
  `ngay_sinh` datetime(6) DEFAULT NULL,
  `so_dien_thoai` varchar(255) DEFAULT NULL,
  `tinh` varchar(255) DEFAULT NULL,
  `vai_tro` tinyint DEFAULT NULL,
  `xa` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `nhan_vien_chk_1` CHECK ((`status` between 0 and 1)),
  CONSTRAINT `nhan_vien_chk_2` CHECK ((`chuc_vu` between 0 and 1)),
  CONSTRAINT `nhan_vien_chk_3` CHECK ((`vai_tro` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhan_vien`
--

LOCK TABLES `nhan_vien` WRITE;
/*!40000 ALTER TABLE `nhan_vien` DISABLE KEYS */;
/*!40000 ALTER TABLE `nhan_vien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieu_dat_phong`
--

DROP TABLE IF EXISTS `phieu_dat_phong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieu_dat_phong` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `ngay_check_in` bigint DEFAULT NULL,
  `ngay_check_out` bigint DEFAULT NULL,
  `status_phieu_dat_phong` tinyint DEFAULT NULL,
  `khach_hang_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6e9wjmw3odfrkolhhos8xw123` (`khach_hang_id`),
  CONSTRAINT `FK6e9wjmw3odfrkolhhos8xw123` FOREIGN KEY (`khach_hang_id`) REFERENCES `khach_hang` (`id`),
  CONSTRAINT `phieu_dat_phong_chk_1` CHECK ((`status` between 0 and 1)),
  CONSTRAINT `phieu_dat_phong_chk_2` CHECK ((`status_phieu_dat_phong` between 0 and 4))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieu_dat_phong`
--

LOCK TABLES `phieu_dat_phong` WRITE;
/*!40000 ALTER TABLE `phieu_dat_phong` DISABLE KEYS */;
/*!40000 ALTER TABLE `phieu_dat_phong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phong`
--

DROP TABLE IF EXISTS `phong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phong` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `tang` int DEFAULT NULL,
  `trang_thai_phong` tinyint DEFAULT NULL,
  `loai_phong_id` varchar(36) DEFAULT NULL,
  `trang_thai_hoat_dong` tinyint DEFAULT NULL,
  `trang_thai_ve_sinh` tinyint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK193u887c0us39289ggjyh71by` (`loai_phong_id`),
  CONSTRAINT `FK193u887c0us39289ggjyh71by` FOREIGN KEY (`loai_phong_id`) REFERENCES `loai_phong` (`id`),
  CONSTRAINT `phong_chk_1` CHECK ((`status` between 0 and 1)),
  CONSTRAINT `phong_chk_2` CHECK ((`trang_thai_phong` between 0 and 5)),
  CONSTRAINT `phong_chk_4` CHECK ((`trang_thai_hoat_dong` between 0 and 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phong`
--

LOCK TABLES `phong` WRITE;
/*!40000 ALTER TABLE `phong` DISABLE KEYS */;
INSERT INTO `phong` VALUES ('03d5c296-18f2-42f1-a339-3351e0154b22',1763247553291,1763247553291,'201',NULL,'201',2,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,0),('071be8c3-faac-4f0d-8b5c-5b93db91068c',1763247344941,1763247344941,'103',NULL,'103',1,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,0),('0ab783b4-99a4-41ff-99ed-c5deac228805',1763247394911,1763247394911,'104',NULL,'104',1,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('11601213-4f34-45eb-8557-e16ad0646ab5',1763247603336,1763247603336,'206',NULL,'206',2,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('170e18e5-2027-4289-9874-13215f3d4eac',1763247779374,1763280240594,'311',NULL,'311',3,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,2),('1a58525a-f374-4362-8822-b5f46bc60a33',1763247712772,1763247712772,'304',NULL,'304',3,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,1),('1aae7c81-7581-42ff-a115-dd4e17ea9dea',1763247624428,1763247624428,'208',NULL,'208',2,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('20ed561f-4643-424d-a1bf-eca801a09f3f',1763247577606,1763247577606,'203',NULL,'203',2,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,0),('302f31f1-1676-4f18-a07f-f0f44bafd72e',1763247300469,1763247300475,'102',NULL,'102',1,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,0),('332516e3-362c-455b-bd4b-641682864c59',1763247693211,1763247693211,'302',NULL,'302',3,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,0),('38873dd6-a772-46a0-91d8-68fe78a224bb',1763247673519,1763247673519,'212',NULL,'212',2,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,0),('3d538645-2211-48be-8704-d14a40bccd6b',1763247638619,1763247638619,'209',NULL,'209',2,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,0),('401b9ad1-a5d7-4dc2-8458-bf43aa2ca5ff',1763247752237,1763247752237,'308',NULL,'308',3,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('4632a33c-184d-49f1-9922-90a6fda54322',1763247788261,1763247788261,'312',NULL,'312',3,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,0),('486c7ea2-c390-4942-af6a-fefc7f5e6566',1763247722179,1763280218425,'305',NULL,'305',3,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,2),('4d976952-0ef9-4e11-9ab6-16f1ba23b0bb',1763247594358,1763247594358,'205',NULL,'205',2,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,0),('53bef20e-bb8f-4695-8a6c-14e6713a4176',1763247478662,1763280390394,'109',NULL,'109',1,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,0),('57c3d415-dc2b-49cf-a167-fec4712b6e9e',1763246219023,1763246219023,'101',NULL,'101',1,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,0),('5af12a4f-4b8a-4317-8dea-20de68d6a17b',1763247567275,1763280384691,'202',NULL,'202',2,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,1),('6573f1e9-b696-4748-8727-91b74614bf2c',1763247649021,1763247649021,'210',NULL,'210',2,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('73ff0911-78db-4f8f-8772-7e597792fb89',1763247685951,1763247685951,'301',NULL,'301',3,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,0),('82e84c97-45a1-4ce6-91aa-39134a79d7fd',1763247613755,1763281516076,'207',NULL,'207',2,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,1),('8552fff6-f6e1-427f-95b3-ec9d6ae6f44d',1763247443383,1763247443383,'106',NULL,'106',1,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('8ad6dc65-91a1-461e-b092-56afeadd3ea4',1763247740583,1763280227265,'307',NULL,'307',3,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,2),('8d7e762f-172e-4801-b70a-9096d2c4c42a',1763247490232,1763247490232,'110',NULL,'110',1,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('a8ca780c-e3e5-4595-b4db-e26bb7bc7ece',1763247515525,1763277083277,'112',NULL,'112',1,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,1),('b07da492-a960-4116-bb79-c755d90c14f6',1763247704737,1763277018157,'303',NULL,'303',3,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,2),('b99e225d-0deb-4bdd-b84f-c4810ef440f8',1763247762473,1763280229730,'309',NULL,'309',3,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,1),('bcbf4406-169f-4306-9b10-9610a52ccb23',1763247772206,1763247772206,'310',NULL,'310',3,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('c3be0b38-5938-452d-9040-4198bd9db6e9',1763247731595,1763247731595,'306',NULL,'306',3,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('c8da8639-943e-4820-8477-164aed8ca56a',1763247466426,1763247466426,'108',NULL,'108',1,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,0),('c8dc25da-471b-416a-96b4-216f61e96261',1763247506588,1763280579198,'111',NULL,'111',1,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,0),('d2271049-a081-492e-b103-64a9a81f1b16',1763247427084,1763280203703,'105',NULL,'105',1,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,0),('e18d6442-461f-4aac-ad26-0f22c8c166d2',1763247662951,1763247662951,'211',NULL,'211',2,NULL,'210c45f7-74c7-426f-b166-e09e6359ae75',0,0),('eccb906f-f51d-441f-be2f-a09337779122',1763247453643,1763280392980,'107',NULL,'107',1,NULL,'c44bd342-63bf-4f2e-8f0b-278e6c520a0b',0,2),('fd1a19b4-8fd5-41e9-ba0d-1c8a5b89dc7e',1763247585814,1763280380258,'204',NULL,'204',2,NULL,'68400b44-661d-4a2b-9969-b24d7effff67',0,2);
/*!40000 ALTER TABLE `phong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phong_tags`
--

DROP TABLE IF EXISTS `phong_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phong_tags` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `phong_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKewhrncg3o0ilv5c693cfc8sow` (`phong_id`),
  CONSTRAINT `FKewhrncg3o0ilv5c693cfc8sow` FOREIGN KEY (`phong_id`) REFERENCES `phong` (`id`),
  CONSTRAINT `phong_tags_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phong_tags`
--

LOCK TABLES `phong_tags` WRITE;
/*!40000 ALTER TABLE `phong_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `phong_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refresh_token`
--

DROP TABLE IF EXISTS `refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_token` (
  `id` varchar(36) NOT NULL,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `ten` varchar(255) DEFAULT NULL,
  `expired_at` bigint DEFAULT NULL,
  `refresh_token` varchar(8000) DEFAULT NULL,
  `revoked_at` bigint DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `refresh_token_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_token`
--

LOCK TABLES `refresh_token` WRITE;
/*!40000 ALTER TABLE `refresh_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `refresh_token` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-16 15:42:18
