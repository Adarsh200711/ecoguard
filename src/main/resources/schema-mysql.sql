-- ===================================================================
-- EcoGuard: Pallikaranai Marshland Waste Management Database Script
-- Safe Execution: Does NOT drop or erase existing databases
-- Compatible with MySQL Workbench 8.0+ and MySQL Server 8.0+
-- ===================================================================

-- Step 1: Create Database if not already present
CREATE DATABASE IF NOT EXISTS ecoguard
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Step 2: Switch to EcoGuard Database
USE ecoguard;

-- Step 3: Create Users Table (for Admin, Volunteer, Citizen authentication)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CITIZEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 4: Create Waste Categories Table
CREATE TABLE IF NOT EXISTS waste_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 5: Create Waste Reports Table (Core Incident Logs)
CREATE TABLE IF NOT EXISTS waste_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    location VARCHAR(255) NOT NULL,
    zone VARCHAR(100),
    severity VARCHAR(30) DEFAULT 'MEDIUM',
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    reporter_name VARCHAR(150),
    reporter_email VARCHAR(150),
    reporter_phone VARCHAR(50),
    image_url VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reports_status (status),
    INDEX idx_reports_category (category),
    INDEX idx_reports_zone (zone),
    INDEX idx_reports_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 6: Create Status History Table (Audit Trail of Remediation Actions)
CREATE TABLE IF NOT EXISTS status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    updated_by VARCHAR(100),
    comment VARCHAR(1000),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_history_report (report_id),
    CONSTRAINT fk_history_report
        FOREIGN KEY (report_id) REFERENCES waste_reports(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- Verification Queries (Run in MySQL Workbench to inspect saved records)
-- ===================================================================

-- 1. Verify all logged waste incident reports (latest first)
SELECT id, category, location, zone, severity, status, reporter_name, created_at 
FROM ecoguard.waste_reports 
ORDER BY created_at DESC;

-- 2. View audit trail of status changes
SELECT h.id, h.report_id, r.category, h.old_status, h.new_status, h.updated_by, h.comment, h.updated_at
FROM ecoguard.status_history h
LEFT JOIN ecoguard.waste_reports r ON h.report_id = r.id
ORDER BY h.updated_at DESC;

-- 3. Incident count by status
SELECT status, COUNT(*) AS incident_count 
FROM ecoguard.waste_reports 
GROUP BY status;

-- 4. Incident count by waste category
SELECT category, COUNT(*) AS incident_count 
FROM ecoguard.waste_reports 
GROUP BY category;

-- 5. Incident count by marshland zone / sector
SELECT zone, COUNT(*) AS incident_count 
FROM ecoguard.waste_reports 
GROUP BY zone;

-- 6. Verify registered users
SELECT id, name, email, role, created_at 
FROM ecoguard.users;

-- 7. Verify waste categories
SELECT id, category_name, code, icon 
FROM ecoguard.waste_categories;
