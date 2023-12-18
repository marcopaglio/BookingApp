
# BookingApp

## About

BookingApp is a simple desktop application for managing reservations developed with TDD, build automation and continuous integration practices. Once the application is launched, you can add your clients and their reservations via a GUI. Informations are stored in a database server running as a Docker container. BookingApp is compatible both with MongoDB and PostgreSQL DBMSs.  <br>
**IMAGES HERE**  <br>

On GitHub Actions are stored results about Maven builds e tests for Linux OS, MacOS, and Windows:  <br>
[![Java CI with Maven in Linux](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-linux.yml/badge.svg?branch=main)](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-linux.yml)  <br>
[![Java CI with Maven in MacOS](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-macos.yml/badge.svg)](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-macos.yml)  <br>
[![Build with Maven in Windows](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-windows.yml/badge.svg)](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-windows.yml)  <br>

> N.B: On Windows systems some unit and integration tests cannot be executed due to lack of compatibility of required Docker images, like MongoDB and PostgresSQL. If you are brave enough, you can fill the void creating custom Docker images for MongoDB and PostgreSQL starting from a [Windows OS base layer](https://hub.docker.com/_/microsoft-windows-base-os-images).

Also GitHub Actions provides website and release status of the BookingApp project:  <br>
[![Deploy content to GitHub Pages](https://github.com/marcopaglio/BookingApp/actions/workflows/gh-pages.yml/badge.svg?branch=main)](https://github.com/marcopaglio/BookingApp/actions/workflows/gh-pages.yml)  <br>
**HERE GITHUB RELEASES**  <br>
  
As a consequence of TDD, BookingApp project is a fully test-covered application; on Coveralls are published code coverage history and statistics:  <br>
[![Coverage Status](https://coveralls.io/repos/github/marcopaglio/BookingApp/badge.svg?branch=main)](https://coveralls.io/github/marcopaglio/BookingApp?branch=main)  <br>

Another aim of BookingApp project is reaching the best possible code quality; on SonarCloud are published analysis about *reliability*, *security* and *maintainability*:  <br>
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=coverage)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=bugs)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp)  <br>

## Is your machine compatible

This project runs a Docker image with MongoDB, the version of which is greater than 5.0 and requires the use of AVX instructions. To determine whether your CPU model supports AVX, check the manufacturer’s website and enter your CPU model number. Alternatively, below there are other methods specific to the Operating System (OS).

> Note: even if your machine supports AVX instructions, a hosted Virtual Machine may disable them due to virtualization issues. See **LINK** for fixing this possible problem.  

### Linux and MacOS

To check if CPU has AVX capabilities on Unix systems, run the following command on the terminal:  <br>
```
grep avx /proc/cpuinfo
```
If the output is not empty then your cores have AVX support.

### Windows 

On Windows systems, enable AVX capabilities directly by running the following command on a Command Prompt as Administrator:  
```
bcdedit /set xsavedisable 0
```
You should see a confirmation message.

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

## Clone the BookingApp repository

There are essentially two main ways for cloning, and then using, the BookingApp repository.

### Clone by command line

If you decide to use command line, get a copy of BookingApp by running:  <br>
`git clone https://github.com/marcopaglio/BookingApp.git`  <br>


### Import from Eclipse

> Following steps have been defined using EclipseIDE 2022-12 and 2023-09. Different versions may involve slightly different steps.

1. Once installed, open Eclipse and choose any workspace location.
2. From the top left bar: **File** > **Import** > **Git** > **Projects from Git** > **Github**.
3. Search for the repository *marcopaglio/BookingApp* > select only the branch named *Main* > **Next** > choose any directory (we will refer to it as ***BASE_DIR***) > **Next** > every module must be selected from the list > **Finish**
   
   > Once imported, there may be appeared some errors (about dependencies) on the *Problems* tab of Eclipse. Don't worry, just **Refresh** once and they will go away.
   
6. Download database docker images before building, otherwise build fails due to timeout.
     docker pull mongo:6.0.7
     docker pull postgres:15.3
7. On Linux run xhost+ before running docker-compose or Maven docker profile (and xhost- as soon as it stop).

### Eclipse settings

Opening files which define DTD or XSD schemas, like in pom.xml and persistence.xml files of BookingApp project, requires that such schemas have to be downloaded and stored locally (inside .lemminx folder, in Linux).
About that, if you find the following error message coming from these files:

> Downloading external resources is disabled.

Then you have to allow Eclipse downloading schemas. This can be done modifying Eclipse settings: **Window** > **Preferences** > **XML (Wild Web Developer)** > tick **Download external resources like referenced DTD, XSD** > Apply > Aplly and Close.
Now you can find the .lemminx file containing all the resources necessary for building. 

## Build BookingApp

For building a copy of BookingApp, place yourself into the project root directory (or *BASE_DIR* e.g: `BookingApp`) where the Maven Wrapper files (`mvnw`, `mvnw.cmd`, etc.) are stored, and open a Command Prompt.

### Linux

On Linux systems, building is done by running the following command:
```
./mvnw -f booking-aggregator/pom.xml clean install
```
Its execution will remove unnecessary files generated in previous builds and install dependencies locally for each module. In this way, you can then build each sub-module indipendently, just change `booking-aggregator` with one between `booking-domain-module`, `booking-business-module` and `booking-ui-module` in the previous command.  <br>
This execution executes unit, integration and end-to-end tests at all. If the command is executed for a sub-module, these tests will be executed only for the specific module.  <br>
Alternative builds can be run adding one or more profiles at the end of the previous command:
- `-Pjacoco` will add test coverage.  <br>
  BookingApp project already provides test coverage results **LINK HERE**, but you can see them for yourself once execution finishes at `BASE_DIR/booking-report/target/site/jacoco-aggregate/index.html`.
- `-Ppitest` will add mutation testing.  <br>
  BookingApp project already provides mutation testing results **LINK HERE**, but you can see them for yourself once execution finishes at `BASE_DIR/booking-report/target/pit-reports/index.html`.
- `-Pdocker` > add dockerization of the application  

  > Note: docker profile will open BookingApp inside a Docker container, therefore it needs the access to the Linux X display server in order to work propertly:
  > - run the following command for showing all the net interfaces:  <br>
  >   `ifconfig`
  >   
  >   > It may be necessary install the package for running it: `sudo apt install net-tools`
  >   
  > - from the printed list, find out the Docker virtual bridge (alias *<DOCKER_NET>*) which all the containers are connected to (as default it is `docker0`).
  > - run the following command before using docker profile or docker-compose:  <br>
  >   `xhost +local:<DOCKER_NET>` e.g: `xhost +local:docker0`  
  > - When running finishes, it is recommanded to remove the access to the X display server by running:  <br>
  >   `xhost -local:<DOCKER_NET>` e.g: `xhost -local:docker0`

You can also build the BookingApp project using the command `mvn` instead of `./mvnw`, but without Maven Wrapper build might fails due to different versions of Maven. For this reason building with maven Wrapper is recommended. If you prefer taking your risks, you can also run launch files from Eclipse. They are located inside `booking-aggregate`, `booking-domain-module`, `booking-business-module` and `booking-ui-module` into `launches` folders. Just right click on the .launch file, select **Run As**, and click on the same name maven configuration for starting the building. Remember that launch files in Reactor execute on the whole project, while the others execute on the single module, so they need to have dependencies installed before starting.
