
# booking-app

[![Java CI with Maven in Linux](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-linux.yml/badge.svg?branch=main)](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-linux.yml) 
[![Build with Maven in Windows](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-windows.yml/badge.svg)](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-windows.yml) 
[![Java CI with Maven in MacOS](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-macos.yml/badge.svg)](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-macos.yml) 
[![Deploy content to GitHub Pages](https://github.com/marcopaglio/BookingApp/actions/workflows/gh-pages.yml/badge.svg?branch=main)](https://github.com/marcopaglio/BookingApp/actions/workflows/gh-pages.yml)  
Desktop application for managing reservations developed with TDD, build automation and continuous integration practices.  
  
[![Coverage Status](https://coveralls.io/repos/github/marcopaglio/BookingApp/badge.svg?branch=main)](https://coveralls.io/github/marcopaglio/BookingApp?branch=main)  
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=coverage)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=bugs)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)

## Before you start

In order to replicate buildings or using the application, following programs have to be installed on your machine:
1. Java SDK (and JRE?) 11
2. Git
3. Docker engine and Docker compose
4. Eclipse IDE

### 1. Install Java SDK (and JRE) 11

The installation guide depending on your operating system.

#### Linux (Ubuntu)

First of all, check if Java JRE is already installed, running the following commands on terminal:
java -version
and:
javac --version
If it appears something like:
openjdk version "11.0.21"
and:
javac 11.0.21
Then you have already installed java, and you have to pass to next part of tutorial (link on words).
Otherwise, if there is another version (major versions are ok or not?), or something like:
Command 'java' not found
or:
Command 'javac' not found
These responses indicate that the necessary Java version is not installed and you can proceed with the next steps.

Before installing any new package is reccomanded to update your package listing with the most recent information:
sudo apt-get update
Install the JDK with the following commands:
sudo apt install openjdk-11-jre
sudo apt install openjdk-11-jdk
The full Java suite includes the Java Runtime Environment (JRE), Java Virtual Machine (JVM) and utilities to develop Java source code.
The Java Development Kit (JDK) provides everything a user needs to run Java applications. 
To validate the successful install of Java on Ubuntu with apt, issue the following command:
java -version
openjdk version "11.0.21"
and
javac --version
javac 11.0.21
TODO: set JAVA_HOME?

### 2. Install Git

The installation guide depending on your operating system.

#### Linux (Ubuntu 22.04)

sudo apt-get update
sudo apt-get install git
The verify the installation was successful by typing:
git --version
TODO: configure username and email?

### 3. Install Docker Engine and Docker Compose

The installation guide depending on your operating system.

#### Linux (Ubuntu 22.04)

A complete step-by-step guide is provided by the official page:
LINK: https://docs.docker.com/engine/install/ubuntu/

### 4. Install Eclipse IDE

The installation guide depending on your operating system.

#### Linux (Ubuntu)

On Ubuntu Software application is stored Eclipse. So just find it on the store and install clicking the right botton. Then you can open the application.

## Running on Virtual Machine (VirtualBox) with Windows 11 as host

In this case it's necessary:
- Disable all kind of virtualizations.
  Run on terminal with privilegis: $ bcdedit /set hypervisorlaunchtype off
  For revert run: $ bcdedit /set hypervisorlaunchtype auto 
    Go to Impostazioni > App > funzionalità facoltative > Altre funzionalità Windows
  Disable:
    1. Sottosistema Windows per Linux
    2. Piattaforma Windows Hypervisor
    3. Piattaforma macchina virtuale
  Then OK > Restart.
  In order to verify if any kind of virtualization is disabled, click Win + R > msinfo32 > OK > Scroll at the end and there must be no writing "rilevato hypervisor. le funzionalità necessarie per hyper-v non verranno visualizzate"
- Disable Windows security Memory Integrity (very bad)
    Go to Impostazioni > Privacy e Sicurezza > Sicurezza di Windows > Sicurezza dispositivi > Isolament Core > Integrità della Memoria
  Disable it and restart.
For cheching everything goes, open VirtualBox and start the virtual machine. In the right bottom side there must be a symbol with V but not in a turtle. The presence of turtle means that AVX and AVX2 about cores aren't enabled/presented and Mongo 5+ needs them for running.

## Import the repository

1. Open eclipse
2. Import from Git
3. Find the repository (marcopaglio/BookingApp)
4. If some errors (about dependencies) appears, just Refresh modules, and they go away.
5. Download database docker images before building, otherwise build fails due to timeout.
     docker pull mongo:6.0.7
     docker pull postgres:15.3
