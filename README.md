
# grpc-auth-and-opinator

The Goodfella gRPC does business with Spring, but also with Hexagonal Arch, Authorization Server, JWT, Reactive Programming, CQRS, Kafka and Hibernate Reactive. The system allows users to register, create categories, products, and leave opinions, reviews, and votes.

**Components** :rotating_light::no_entry: **IN CONSTRUCCTION**
- [Kafka](https://kafka.apache.org/) [9092] + [9093] 
- ms-discovery-server as registry and discovery service [8761]
- Authorization Server (ms-grpc-auth) [9000] + [9001]
- gRPC reactive Server ms-grpc-user [0]
- gRPC reactive Server ms-grpc-opinator with Hibernate Reactive [0]


## Table of contents

- [Installation](#installation)
- [Usage](#usage)
- [It's not a bug, it's a feature](#features)
- [Maintainers](#maintainers)
- [License](#license)


## Installation

#### First steps

1. As usual, please clone or download the project.

1. Inside the main folder, you could find a docker-compose yaml file.

1. From there use the command line to start the project

```    
    **Developer mode**  
    docker-compose -f docker-compose-dev.yml up -d

```
      
The dev environment is ready for using with your IDE. The microservice attempts to communicate with Kafka using the local host. 
   
4. You could stop the project and free resources with any of this orders

```
    **Developer mode**
    docker-compose -f docker-compose-dev.yml down --rmi local -v
    
```
5. It is important to have [grpcurl](https://github.com/fullstorydev/grpcurl) to call gRPC services in your system. 
  
#### About certificates

The communication between the gRPC server and clients is secure thanks to SSL certificates. The document  located at the root project, certificates.md, and the explanation on [grpc-ecosystem](https://grpc-ecosystem.github.io/grpc-spring/en/server/security.html) could clarify you how it works. 
     
     
## Usage



## Features



## Maintainers

Just me, [Iván](https://github.com/Ivan-Montes) :sweat_smile:


## License

[GPLv3 license](https://choosealicense.com/licenses/gpl-3.0/)


---


[![Java](https://badgen.net/static/JavaSE/21/orange)](https://www.java.com/es/)
[![Maven](https://badgen.net/badge/icon/maven?icon=maven&label&color=red)](https://https://maven.apache.org/)
[![Spring](https://img.shields.io/badge/spring-blue?logo=Spring&logoColor=white)](https://spring.io)
[![GitHub](https://badgen.net/badge/icon/github?icon=github&label)](https://github.com)
[![Eclipse](https://badgen.net/badge/icon/eclipse?icon=eclipse&label)](https://https://eclipse.org/)
[![SonarQube](https://badgen.net/badge/icon/sonarqube?icon=sonarqube&label&color=purple)](https://www.sonarsource.com/products/sonarqube/downloads/)
[![Docker](https://badgen.net/badge/icon/docker?icon=docker&label)](https://www.docker.com/)
[![Kafka](https://badgen.net/static/Apache/Kafka/cyan)](https://kafka.apache.org/)
[![GPLv3 license](https://badgen.net/static/License/GPLv3/blue)](https://choosealicense.com/licenses/gpl-3.0/)
