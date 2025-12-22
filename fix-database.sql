-- ============================================
-- FIX DATABASE - DROP AND RECREATE
-- ============================================
-- This script will drop the entire database and recreate it
-- Run this in phpMyAdmin to fix the missing tables issue

DROP DATABASE IF EXISTS web3;
CREATE DATABASE web3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE web3;

-- ============================================
-- Now restart your Spring Boot backend
-- It will automatically create all tables
-- ============================================
