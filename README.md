
# BookingApp

## What is it

TODO: Descrizione breve di cos'è, termina con badge 

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

## Is your machine compatible

This project runs a Docker image with MongoDB, the version of which is greater than 5.0, then requires use of AVX instructions. To determine whether AVX supports your CPU model, check the manufacturer’s website and enter your CPU model number. Below are other methods for this verification.

> Note: even if your machine supports AVX instructions, a running virtual machine hosted by it might disabilitate them due to virtualization problems. See **LINK** for fixing this possible issue.  

### Linux and MacOS

To check if CPU has AVX capabilities on Unix systems, run the following commands on terminal:  
`grep avx /proc/cpuinfo`  
If the output is not empty, then your cores has AVX support.

### Windows 

Enable AVX capabilities directly by running the following command on a Command Prompt as Administrator:  
`bcdedit /set xsavedisable 0`  
You will see a confirmation message.

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

## Running on Virtual Machine

It is possible to clone and run BookingApp on a Virtual Machine (VM) program (e.g. VirtualBox, VMware Workstations, etc.), but it may not work due to lack of support for AVX/AVX2 instructions, even if your host machine's CPU supports them. 

### Hosted by Windows 11

On Windows 11 the supporting of AVX/AVX2 instructions in a VM may fail due to virtualization problems. In this case it's necessary to:

- *disable the hypervisor launch which is enabled by default*.
  
  First of all, check if the hypervisor is executing: **WIN + R** > enter **msinfo32** > in the **System Summary** window, you should find the following entry (otherwise you can skip to the second part **LINK HERE**):

  > A hypervisor was detected. The features required for Hyper-V will not be displayed.
  
  In this case, open a Command Prompt in Windows 11 Host as Administrator and run:  
  `bcdedit /set hypervisorlaunchtype off`
  Then restart your machine for applying changes.
  For revert changes about the hypervisor run on a Command Prompt in Windows 11 Host as Administrator:
  `bcdedit /set hypervisorlaunchtype auto`
  Then restart your machine for applying changes.
  
- *disable Windows security Memory Integrity*.

  This is a very bad thing to do because the Memory integrity feature is stated to *prevent attacks from inserting malicious code into high-security processes*. It is a *security feature that use virtualization-based security*. Unfortunately this is require in order to enable AVX/AVX2 instructions in VMs. You can find it on **Settings** > **Windows Security** > **Device security** > **Core isolation details** > **Memory integrity**. As soon as turned this feature off, restart your machine to allow Windows to apply the change.  

If everything went right, your VM should now support AVX/AVX2 instructions as well as the host machine. You can make this check running instructions of the previous chapter (**LINK HERE**).  
If you're using VirtualBox you can make this check earlier by looking at the below right side of your running VM: there must be an icon, chip-like, with a V letter inside. Instead, if the V is inside a turtle, it means that hypervisor is still running (and the virtualization is slower, just like a turtle), then AVX/AVX2 core instructions will be not supported.  

I recommand to revert all the changes here described as soon as you no longer have to use the VM.

## Import the repository

1. Open eclipse
2. Import from Git
3. Find the repository (marcopaglio/BookingApp)
4. If some errors (about dependencies) appears, just Refresh modules, and they go away.
5. Download database docker images before building, otherwise build fails due to timeout.
     docker pull mongo:6.0.7
     docker pull postgres:15.3
6. On Linux run xhost+ before running docker-compose or Maven docker profile (and xhost- as soon as it stop).

## Build BookingApp

### Linux

1. locate yourself into the project base directory (e.g. BookingApp) where the mvnw (Maven Wrapper) file is stored, through the command:
   `cd git/BookingApp`
   **TODO: è meglio piazzarsi in booking-aggregator in modo che vi siano tutti i file lì?**
2. Building is done by running the following command:  
   `./mvnw -f booking-aggregator/pom.xml clean install`
   Alternative buildings can be ran adding one or more profiles at the end of the previous command:
   - `-Pjacoco` > add coverage
     Once finished, the coverage report is located in booking-report/target/site/jacoco-aggregate/index.html.
   - `-Ppitest` > add mutation testing
   - `-Pdocker` > add dockerization of the application  

     > Note: docker profile will open BookingApp inside a Docker container, therefore it needs the access to the Linux X display server in order to work propertly. About that, run the following command just before:  
     > `xhost +`  
     > Then, when you finish, it is recommanded to remove free access to the X display server by running:  
     > `xhost -`  
