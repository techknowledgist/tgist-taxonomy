-- phpMyAdmin SQL Dump
-- version 4.3.8
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Mar 29, 2018 at 11:17 AM
-- Server version: 5.6.32-78.1
-- PHP Version: 5.6.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Table structure for table `hierarchy`
--

DROP TABLE IF EXISTS `hierarchy`;
CREATE TABLE IF NOT EXISTS `hierarchy` (
  `source` varchar(250) NOT NULL,
  `type` varchar(25) NOT NULL,
  `subtype` varchar(25) NOT NULL,
  `target` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `relations`
--

DROP TABLE IF EXISTS `relations`;
CREATE TABLE IF NOT EXISTS `relations` (
  `source` varchar(250) NOT NULL,
  `type` varchar(25) NOT NULL,
  `subtype` varchar(25) NOT NULL,
  `count` int(11) NOT NULL,
  `target` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `technologies`
--

DROP TABLE IF EXISTS `technologies`;
CREATE TABLE IF NOT EXISTS `technologies` (
  `name` varchar(250) NOT NULL,
  `tscore` float NOT NULL,
  `count` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `hierarchy`
--
ALTER TABLE `hierarchy`
  ADD PRIMARY KEY (`source`);

--
-- Indexes for table `relations`
--
ALTER TABLE `relations`
  ADD KEY `source` (`source`);

--
-- Indexes for table `technologies`
--
ALTER TABLE `technologies`
  ADD PRIMARY KEY (`name`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
