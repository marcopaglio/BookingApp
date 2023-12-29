
# BookingApp

BookingApp is a simple desktop application for managing reservations developed with TDD, build automation and continuous integration practices. Once the application is launched, you can add your clients and their reservations via a GUI. Informations are stored in a database server running as a Docker container. BookingApp is compatible with both MongoDB and PostgreSQL DBMSs.<br>
<p align="center">
  <img src="/../screenshots/screenshot-bookingapp-gui.png" alt="Screenshot of the simple BookingApp GUI." title="BookingApp GUI" width="80%"/>
</p>

> InfoPoint :information_source:: BookingApp followed by "***project***" indicates the entire job, which includes builds, tests and hence the source code, while "***application***" indicates the executable which you can launch and use.

On GitHub Actions are stored results about Maven builds and tests for Linux OS, MacOS, and Windows, and also the website status for the BookingApp project and the release status for the BookingApp application:<br>
[![Java CI with Maven in Linux](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-linux.yml/badge.svg?branch=main)](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-linux.yml)<br>
[![Java CI with Maven in MacOS](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-macos.yml/badge.svg)](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-macos.yml)<br>
[![Build with Maven in Windows](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-windows.yml/badge.svg)](https://github.com/marcopaglio/BookingApp/actions/workflows/maven-windows.yml)<br>
[![Deploy content to GitHub Pages](https://github.com/marcopaglio/BookingApp/actions/workflows/gh-pages.yml/badge.svg?branch=main)](https://github.com/marcopaglio/BookingApp/actions/workflows/gh-pages.yml)<br>
**HERE GITHUB RELEASES**<br>

> N.B. :alarm_clock:: On Windows systems some unit and integration tests cannot be executed due to lack of compatibility of required Docker images, like MongoDB and PostgresSQL. If you are brave enough, you can fill the void by creating custom Docker images for MongoDB and PostgreSQL starting from a [Windows OS base layer](https://hub.docker.com/_/microsoft-windows-base-os-images).
  
On Coveralls are published the history and statistics of BookingApp test code coverage, while on SonarCloud are published analysis of BookingApp code quality, particularly on *reliability*, *security* and *maintainability*:<br>
[![Coverage Status](https://coveralls.io/repos/github/marcopaglio/BookingApp/badge.svg?branch=main)](https://coveralls.io/github/marcopaglio/BookingApp?branch=main)<br>
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=coverage)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=bugs)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=marcopaglio_BookingApp&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=marcopaglio_BookingApp) 

## Before you start

### Is your machine compatible?

If you just launch the BookingApp application with PostgreSQL as DBMS there are no *known* machine requirements.<br>

Otherwise, the BookingApp project runs a Docker container with MongoDB, the version of which is greater than 5.0 and requires the use of AVX instructions. To determine whether your CPU model supports AVX, check the manufacturer’s website and enter your CPU model number. Alternatively, below there are other methods specific to the Operating System (OS).

> N.B. :alarm_clock:: Even if your machine supports AVX instructions, a hosted Virtual Machine may disable them due to virtualization issues. See the section on [Running on Virtual Machine](#running-on-virtual-machine) for fixing this possible problem.

#### Linux and MacOS

To check if CPU has AVX capabilities on Unix systems, run the following command on the terminal:
```
grep avx /proc/cpuinfo
``` 
If the output is not empty then your cores have AVX support.

#### Windows 

On Windows systems, enable AVX capabilities directly by running the following command on a Command Prompt as Administrator:
```
bcdedit /set xsavedisable 0
``` 
If you see a confirmation message then your cores have AVX support.

### Running on Virtual Machine

It is possible to clone and run BookingApp on a OS installed on a Virtual Machine (VM) program (e.g. VirtualBox, VMware Workstations, etc.), but it may not work due to lack of support for AVX/AVX2 instructions, even if your host machine's CPU supports them. 

#### Hosted by Windows 11

On Windows 11 the supporting of AVX/AVX2 instructions in a VM may fail due to virtualization problems. In this case it's necessary to:
- *disable the hypervisor launch which is enabled by default*.
  
  First of all, check if the hypervisor is executing: press together **WIN + R** > enter **msinfo32** > in the **System Summary** window you should find the following entry (otherwise skip over to the next point): `A hypervisor was detected. The features required for Hyper-V will not be displayed`. In this case, open a Command Prompt as Administrator and run `bcdedit /set hypervisorlaunchtype off`. Then restart your machine for applying changes.<br>
  
  When you need to undo the changes, open a Command Prompt as Administrator and run `bcdedit /set hypervisorlaunchtype auto`. Then restart your machine for applying changes.
  
- *disable Windows security Memory Integrity*.

  > Attention :bangbang:: This is a very bad thing to do because the Memory integrity feature is stated to *prevent injection attacks into virtualization-based security processes*. Unfortunately, this is require in order to enable AVX/AVX2 instructions in VMs.
  
  You can find it on **Settings** > **Windows Security** > **Device security** > **Core isolation details** > **Memory integrity**. As soon as turned this feature off, restart your machine to allow Windows to apply the change.  

If everything went right, your VM should now support AVX/AVX2 instructions as well as the host machine. You can make this check running the [previous section](#is-your-machine-compatible) instructions on the OS installed on your VM. If you're using VirtualBox you can make this check earlier by looking at the below right side of your running VM: there must be an icon like ![a chip with a V letter inside](/../screenshots/screenshot-chip-icon.png?raw=true "V chip icon"). Instead, if you see an icon like ![a turtle with a V letter inside](/../screenshots/screenshot-turtle-icon.png?raw=true "V turtle icon"), it means that hypervisor is still running (and the virtualization is slower, just like a turtle) then AVX/AVX2 core instructions will be not supported.  

> Attention :bangbang:: It is really recommended to revert all the changes here described as soon as you no longer have to use the VM.

### What else?

To use the BookingApp application, the following programs must be installed on your machine:
1. Java SDK (and JRE?) 11 (or more?)
2. Docker engine and Docker compose

To replicate builds, tests and so on, the BookingApp project also requires the following programs installed on your machine:
3. Git

If you wanna use an IDE:
4. Eclipse IDE
5. Maven v.x.y.z

**START: CHECK IF TO MANTAIN** :arrow_down:
#### 1. Install Java SDK (and JRE) 11

The installation guide depending on your operating system.

##### Linux (Ubuntu)

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

#### 2. Install Git

The installation guide depending on your operating system.

##### Linux (Ubuntu 22.04)

sudo apt-get update
sudo apt-get install git
The verify the installation was successful by typing:
git --version
TODO: configure username and email?

#### 3. Install Docker Engine and Docker Compose

The installation guide depending on your operating system.

##### Linux (Ubuntu 22.04)

A complete step-by-step guide is provided by the official page:
LINK: https://docs.docker.com/engine/install/ubuntu/

#### 4. Install Eclipse IDE

The installation guide depending on your operating system.

##### Linux (Ubuntu)

On Ubuntu Software application is stored Eclipse. So just find it on the store and install clicking the right botton. Then you can open the application.<br>

**END: CHECK IF TO MANTAIN** :arrow_up:

## Clone the BookingApp project

There are essentially two main ways for cloning, and then using, the BookingApp project: the Git command line or the import mode of an IDE.

> InfoPoint :information_source:: The first way is the simplest because it doesn't require any IDE or Maven installation. Anyway, the BookingApp project was developed using Eclipse, recommended if your goal is to use an IDE.

### Git command line

If you decide to use the Git command line, choose any folder and run on the terminal:
```
git clone https://github.com/marcopaglio/BookingApp.git
```
A copy of the BookingApp project will be downloaded in the chosen directory.

#### What if you now want to use Eclipse?

If you have already cloned the BookingApp project via Git command line and now want to use Eclipse, follow the [Import from Eclipse](#import-from-eclipse) guide and change the second step with the following one so as to avoid cloning again:

2. From the top left bar: **File** > **Open Projects from File System...** > use **Directory..** to choose for the *project root directory* > **Open** > make sure of selecting the `Search for nested projects` option > from the Folder list import all subfolders but not the root folder `BookingApp` > **Finish**.

### Import from Eclipse

> InfoPoint :information_source:: Following steps have been defined using EclipseIDE 2022-12 and 2023-09. Different versions may involve slightly different steps.

If you decide to use the Eclipse Smart Imports for cloning the BookingApp project:

1. Once installed, open Eclipse and choose any workspace location.
2. From the top left bar: **File** > **Import** > **Git** > **Projects from Git** > **Github** > search for the repository `marcopaglio/BookingApp` > select only the `main` branch > **Next** > choose any directory as *project root directory* > **Next** > select all modules from the list > **Finish**. **TODO:** l'opzione Github non è sempre presente (vedi su Windows)
   
> N.B. :alarm_clock:: Just imported on Eclipse, there may be appeared some errors (about dependencies) on the `Problems` tab. Don't worry, just **File** > **Refresh** once and they will go away.

#### Eclipse settings

Opening files which define DTD or XSD schemas, like in `pom.xml` and `persistence.xml` of the BookingApp project, requires those schemas to be downloaded and stored locally; for example, on Linux they are found in the `.lemminx` folder. If you find the error message `Downloading external resources is disabled` coming from these files, you can fix that by modifying Eclipse settings: **Window** > **Preferences** > **XML (Wild Web Developer)** > tick `Download external resources like referenced DTD, XSD` > **Apply** > **Apply and Close**.

## Build the BookingApp project

When you build the BookingApp project is necessary to have DBMSs' Docker image locally, otherwise they will be pulled during the build execution causing a possible timeout failure. In order to avoid this, before the very first build run the following commands on the terminal:
```
docker pull mongo:6.0.7
docker pull postgres:15.3
```
**TODO:** N.B. On Linux and macOS you could have to precede the Docker commands with `sudo`, depending on your system/docker configuration.<br>

After that, place yourself into the project root directory, where the Maven Wrapper files (`mvnw`, `mvnw.cmd`, etc.) are stored, open a Command Prompt, and choose whether to build the BookingApp project with [Maven](#build-with-maven) or [Maven Wrapper](#build-with-maven-wrapper).

> InfoPoint :information_source:: Maven Wrapper is very useful for users that don’t want to install Maven at all. For this reason building with Maven Wrapper is recommended.
> If you prefer using Maven directly, make sure to install Maven yourself, and preferably with the same version used for the BookingApp project, that is 3.8.6, otherwise build might fails.

#### Build with Maven Wrapper

If you decide to use Maven Wrapper, in the next [build commands](#build-commands) replace the placeholder `<MVN>` with the right script command, depending on your OS:

- **Linux and MacOS**: if you are using Unix systems, the script command is `./mvnw`.
- **Windows**: if you are using Windows, the script command is `mvnw.cmd`.

#### Build with Maven

If you take the time to install Maven, then replace the placeholder `<MVN>` with `mvn` in the next [build commands](#build-commands).<br>

In this case, you can also build the BookingApp project using launch files from Eclipse. They are located inside `booking-aggregate`, `booking-domain-module`, `booking-business-module` and `booking-ui-module` into `launches` folders. Just right click on the `.launch` file > select **Run As** > click on the same name Maven configuration to start the build. Remember that launch files in `booking-aggregate` execute on the whole project, while the others execute on the single module, so they need to have dependencies installed before starting.

> **TODO**: se non si vede niente dopo Run As che vuol dire? Si collega con gli schemas DTD o con assenza di Maven o altro?

### Build commands

The very basic command to build the BookingApp project is as follows (replace `<MVN>` with a command depending on whether [Maven](#build-with-maven) or [Maven Wrapper](#build-with-maven-wrapper) is used):
```
<MVN> -f booking-aggregator/pom.xml clean install
```
Its execution will remove unnecessary files generated in previous builds and install dependencies locally for each module. In this way, you can then build each sub-module indipendently, just change `booking-aggregator` with one between `booking-domain-module`, `booking-business-module` and `booking-ui-module` in the previous command.<br>
Additionally, all unit, integration and end-to-end tests will be performed with Maven. If the command is executed for a sub-module, tests will be executed only for the specific module.<br>

Alternative builds can be run by adding one or more profiles at the end of the previous command:

- `-Pjacoco` add test coverage. BookingApp project already provides test coverage results on [Coveralls](https://coveralls.io/github/marcopaglio/BookingApp?branch=main), but you can see them yourself once the execution finishes at `/booking-report/target/site/jacoco-aggregate/index.html`.
  
- `-Ppitest` add mutation testing. BookingApp project already provides mutation testing results on the [website](https://marcopaglio.github.io/BookingApp/pit-reports/index.html), but you can see them yourself once the execution finishes at `/booking-report/target/pit-reports/index.html`.
  
- `-Pdocker` dockerize the application. The Docker image created is named `booking-app` and it is checked with both MongoDB and PostgreSQL.

  > N.B. :alarm_clock:: This Maven profile opens the BookingApp application inside a Docker container, therefore it needs the access to the X display server in order to work propertly. Please, make sure you have [Setup X server environment for Docker](#setup-x-server-environment-for-docker) before using the `-Pdocker` profile.

### Run tests from Eclipse

In the BookingApp project there are three modules that contain tests: `booking-domain-module`, `booking-business-module` and `booking-ui-module`. You can run them directly from Eclipse: right click on the module > **Run As** > **JUnit Test**. This will execute all the tests of that module.<br>

Before running tests from Eclipse, make sure Docker is turned on and working properly. Then, unit tests doesn't require any additional setting, since they are run with TestContainers; instead, integration and end-to-end tests need a running instance (suitably configured) of MongoDB and once of PostgreSQL. You can start such instances in Docker containers through Docker Compose commands as follows:
```
docker compose -f docker-compose/MongoDB/docker-compose.yml up
docker compose -f docker-compose/PostgreSQL/docker-compose.yml up
```
**TODO:** docker compose comando cambia da sistema operativo?

> InfoPoint :information_source:: As you can see, there is another folder inside the `docker-compose` directory. It also contains a compose file which starts a SonarQube instance in a Docker container. This can be used in conjuction with the `-Psonar` profile (not previously mentioned) to measure the code quality locally.<br>
> Finally, in each compose file there is a short description of what it does and and what else can be done.

## Run the BookingApp application

You can run the BookingApp application through its jar file or using its Docker image.<br>
Remember that the BookingApp application is compatible with both MongoDB and PostgreSQL, so you also need to decide which of them to launch the application with.

### Run through jar

> InfoPoint :information_source:: You can obtain a FatJar of the BookingApp application in two ways: from the build of the BookingApp project or directly by downloading it from the release on GitHub (**LINK HERE**).

If you decide to run the BookingApp application through its jar file, you need a running instance of MongoDB or PostgreSQL, depending on which one you prefer.

#### MongoDB

The MongoDB instance has to be *part of a replica set* (or cluster, let's call it `rs0`). You can start it by running a Docker container with the following command:
```
docker run -d --name booking-mongo-set -p 27017:27017 mongo:6.0.7 mongod --replSet rs0
```
After few seconds, the MongoDB instance asks for the replica set initialization (if you remove the detached mode `-d` from the command above, you can read on terminal an error message just like `Cannot use a non-local read concern until replica set is finished initializing`). It's the right time to run this other command:
```
docker exec -it booking-mongo-set mongosh --eval "rs.initiate()"
```
If the confirmation message `ok: 1` appears, then the replica set is also initialized.<br>

> N.B: This procedure only needs to be applied once, then stop the MongoDB instance through `docker stop booking-mongo-set`, and start it again (ready for use) with `docker start booking-mongo-set`.

Once the MongoDB instance is ready, place yourself into the project root directory, open a Command Prompt and launch the BookingApp application via the following command:
```
java -jar ./booking-app/target/booking-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar --dbms=MONGO --host=localhost --port=27017 --name=<YOUR_DB_NAME>
```
The placeholder `<YOUR_DB_NAME>` must be replaced with a custom name for your database.

#### PostgreSQL

You can start the PostgreSQL instance by running a Docker container with the following command:
```
docker run -d --name booking-postgres -p 5432:5432 -e POSTGRES_DB=<YOUR_DB_NAME> -e POSTGRES_USER=<YOUR_USER> -e POSTGRES_PASSWORD=<YOUR_PSDW> -N 221 postgres:15.3
```
The placeholders `<YOUR_DB_NAME>`, `<YOUR_USER>` and `<YOUR_PSWD>` must be replaced with a custom name and login credentials for your database, respectively. Remember them for later.

> N.B: Once created, stop the PostgreSQL instance through `docker stop booking-postgres`, and start it again with `docker start booking-postgres`.

Once the PostgreSQL instance is ready, place yourself into the project root directory, open a Command Prompt and launch the BookingApp application with the following command:
```
java -jar ./booking-app/target/booking-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar --dbms=POSTGRES --host=localhost --port=5432 --name=<YOUR_DB_NAME> --user=<YOUR_USER> --pswd=<YOUR_PSWD>
```

### Run through Docker

> InfoPoint :information_source:: You can obtain a Docker image of the BookingApp application by building the BookingApp project with the `-Pdocker` profile. Remember to [Setup X server environment for Docker](#setup-x-server-environment-for-docker) before using this mode.

If you decide to run the BookingApp application through its Docker image, place yourself into the project root directory and open a Command Prompt. You can launch the BookingApp application and also a well-configured instance of the chosen DBMS (MongoDB or PostgreSQL) simply with the following Docker Compose command:
```
docker compose -f <COMPOSE_FILE> up
```
If your choice is **MongoDB**, replace `<COMPOSE_FILE>` with `docker-compose-mongo.yml`; otherwise, if your choice is **PostgreSQL**, replace `<COMPOSE_FILE>` with `docker-compose-postgres.yml`.

> N.B: Are you using Windows with WSLg? The all-in-one command becomes: `docker compose -f <COMPOSE_FILE> -f docker-compose-wslg.yml up`.

## Setup X server environment for Docker

Desktop GUI applications need a graphical environment for working propertly. Since BookingApp uses the standard *Java GUI Swing*, it also requires an X display server. Depending on the OS, the X server environment may or may not be native. Once it works, the X server must be shared with Docker in order to pass access controls.<br>

After that, you will be able to open the BookingApp application inside a Docker container, as required in the `-Pdocker` profile or with Docker Compose commands.

### Linux

Linux already has an X server environment, thus the only thing to do is to share it with Docker. The simplest way is to disable the access control to the X server for the Docker network:

- Run `ifconfig` for showing all the net interfaces. It may be necessary install the following package for running it: `sudo apt install net-tools`.
- From the printed list, find out the Docker virtual bridge (let's call it `<DOCKER_NET>`) which all the containers are connected to. As default it is `docker0`.
- Run `xhost +local:<DOCKER_NET>` e.g: `xhost +local:docker0`.

Now Docker can use the X server.

> Attention :exclamation:: Disabling access control is not a secure choice and you should rely on an authentication method, like *xauth*. For this reason, when you finish with Docker, it is highly recommanded to remove its access to the X display server by running `xhost -local:<DOCKER_NET>` e.g: `xhost -local:docker0`.

### Windows

Windows has not a default X server environment, but it can use the one provided by the *Windows Subsystem for Linux GUI*. **WSLg** is necessary to run Docker in Linux containers and is compatible with Windows 10 (Build 19041 or later) and 11.<br>

To use WSLg, just install the last version of WSL from the [Microsoft Store](https://aka.ms/wslstorepage) or update it if a previous version is already installed through `wsl --update`.
  
> N.B: If Docker was open you may need to restart it after WSL update.

Once done, Docker can already use the X display server without any changes to access control. However, if you run the BookingApp application through Docker, you need to override some configurations by adding `-f docker-compose-wslg.yml` in the Docker Compose command, just before `up`.
