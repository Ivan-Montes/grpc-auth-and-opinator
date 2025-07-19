
# grpc-auth-and-opinator

The Goodfella gRPC does business with Spring, but also with Hexagonal Arch, Authorization Server, JWT, Reactive Programming, CQRS, Kafka and Hibernate Reactive. The system allows users to register, create categories, products, and leave opinions, reviews, and votes.

**Components**
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

1. Inside the main folder, you could find two docker-compose yaml files.

1. From there use the command line to start the project

```    
    **Developer mode**  
    docker-compose -f docker-compose-dev.yml up -d
    
    **Production mode**
    docker-compose -f docker-compose-prod.yml up -d

```
      
The dev environment is ready for using with your IDE. The microservice attempts to communicate with Kafka using the local host. 
   
4. You could stop the project and free resources with any of these orders

```
    **Developer mode**
    docker-compose -f docker-compose-dev.yml down --rmi local -v
      
    **Production mode**
    docker-compose -f docker-compose-prod.yml down --rmi local -v  
    
```
5. It is important to have [grpcurl](https://github.com/fullstorydev/grpcurl) to call gRPC services in your system. 
  
#### About certificates

The communication between the gRPC server and clients is secure thanks to SSL certificates. The document  located at the root project, certificates.md, and the explanation on [grpc-ecosystem](https://grpc-ecosystem.github.io/grpc-spring/en/server/security.html) could clarify you how it works. 
     
     
## Usage

#### Initial considerations

- In your IDE, you should always follow this order to run microservices. First Discovery service, then the-grpc-autho, the-grpc-user-pack, and finally the-grpc-opinator.
- In the project, when using [grpcurl](https://github.com/fullstorydev/grpcurl), **it is mandatory to send a certificate with the command.** For convenience the same certificate is used for all services and is located in the folder `certificates` within each module, except for the discovery server. 
- Additionally, some gRPC methods require including a JWT token with the request. If this constraint is configured, you could revise it in the `GrpcConfig` class, under the `dev.ime.common.config` package in that module

| Module | Service | Method | Token JWT Required|
|-----|-----|-----|-----|
| ms-grpc-opinator | ReviewGrpcCommandService | CreateReview | :white_check_mark: |
| ms-grpc-opinator | ReviewGrpcCommandService | UpdateReview | :white_check_mark: | 
| ms-grpc-opinator | ReviewGrpcCommandService | DeleteReview | :white_check_mark: | 
| ms-grpc-opinator | VoteGrpcCommandService | CreateVote | :white_check_mark: | 
| ms-grpc-opinator | VoteGrpcCommandService | UpdateVote | :white_check_mark: | 
| ms-grpc-opinator | VoteGrpcCommandService | DeleteVote | :white_check_mark: | 

- To learn more about each gRPC service methods simply revise its .proto file under `src.main.proto` folder. An alternative approach is to run against the microservice the following commands, adjusting the port as needed. <u>For instance, you could find the port in the console log</u> near the text "gRPC Server started":

```
grpcurl -cacert ./ca.crt localhost:9001 list
grpcurl -cacert ./ca.crt localhost:9001 describe
```

#### Register

- In the first place you should register an user using the suitable gRPC method. Here is an example of the command you need to send:

```
grpcurl -cacert ./ca.crt -d '{"name":"Ryuu","lastname":"Astrea","email":"ryuu@orario.dm","password":"12345"}' localhost:9001 net.proto.AuthGrpcService.CreateUser
```
- Next, since form-based login authorization is active, we need to use a browser. Navigate to `http://localhost:9000/login` and use the credentials you have just created.
- If the authentication is successful, you will be redirected to the success page where you can request an **authorization code**.

#### Request Token

A good way for knowing more about the JWT generation and validation flow, is reading [this article](https://adictosaltrabajo.com/2023/06/29/securizacion-aplicacion-oauth-2-spring-authorization-server-spring-resource-server/). Otherwise you can follow these steps:

- From the success page, push the button `Request Auth Code`. The authorization server will respond with a code, which the client can exchange for tokens over a secure channel.
- We use the POST method to request our JWT token sending the authorization code and the rest of parameters

<img src="./src/main/resources/static/images/request_token_conf.png" style="width: 800px; max-width: 1024px; flex-grow: 1;" />

&nbsp;

- Now we can use the retrieved JWT token with the tool [grpcurl](https://github.com/fullstorydev/grpcurl) when necessary

You can find a [SoapUI](https://www.soapui.org/) profile with the request token order configured at the root folder.
  
#### Main operations

You can use [grpcurl](https://github.com/fullstorydev/grpcurl) to call gRPC services by selecting the appropriate port. <u>Since the port is random, you will need to adjust the examples accordingly</u>. The main operations in the project are managed by `ms-grpc-opinator` microservice, so the following commands are for it. <u>It's essential to change the ID of each item in the examples</u>. Remember to request and update the token too.

```   
- Category endpoint examples

grpcurl -cacert ./ca.crt localhost:50051 describe net.proto.CategoryGrpcCommandService
grpcurl -cacert ./ca.crt localhost:50051 describe net.proto.CategoryGrpcQueryService
grpcurl -cacert ./ca.crt -d '{"categoryName":"Grocery"}' localhost:50051 net.proto.CategoryGrpcCommandService.CreateCategory
grpcurl -cacert ./ca.crt -d '{"categoryId":"4434c285-4ef5-4a25-a5fa-08866f7c89b5", "categoryName":"Vegetables"}' localhost:50051 net.proto.CategoryGrpcCommandService.UpdateCategory
grpcurl -cacert ./ca.crt -d '{"categoryId":"4434c285-4ef5-4a25-a5fa-08866f7c89b5"}' localhost:50051 net.proto.CategoryGrpcCommandService.DeleteCategory
grpcurl -cacert ./ca.crt localhost:50051 net.proto.CategoryGrpcQueryService.ListCategories
grpcurl -cacert ./ca.crt -d '{"page":0, "size":3, "sortBy":"categoryName", "sortDir":"DESC"}' localhost:50051 net.proto.CategoryGrpcQueryService.ListCategoriesPaginated
grpcurl -cacert ./ca.crt -d '{"categoryId":"4434c285-4ef5-4a25-a5fa-08866f7c89b5"}' localhost:50051 net.proto.CategoryGrpcQueryService.GetCategory

- Product endpoint examples

grpcurl -cacert ./ca.crt localhost:50051 describe net.proto.ProductGrpcCommandService
grpcurl -cacert ./ca.crt localhost:50051 describe net.proto.ProductGrpcQueryService
grpcurl -cacert ./ca.crt -d '{"productName":"Lettuce", "productDescription":"green", "categoryId":"4434c285-4ef5-4a25-a5fa-08866f7c89b5"}' localhost:50051 net.proto.ProductGrpcCommandService.CreateProduct
grpcurl -cacert ./ca.crt -d '{"productId":"f6d705be-01db-44f4-bd31-51ead4e671c8", "productName":"Tomatoes", "productDescription":"full of red", "categoryId":"4434c285-4ef5-4a25-a5fa-08866f7c89b5"}' localhost:50051 net.proto.ProductGrpcCommandService.UpdateProduct
grpcurl -cacert ./ca.crt -d '{"productId":"f6d705be-01db-44f4-bd31-51ead4e671c8"}' localhost:50051 net.proto.ProductGrpcCommandService.DeleteProduct
grpcurl -cacert ./ca.crt localhost:50051 net.proto.ProductGrpcQueryService.ListProducts
grpcurl -cacert ./ca.crt -d '{"page":0, "size":3, "sortBy":"productName", "sortDir":"DESC"}' localhost:50051 net.proto.ProductGrpcQueryService.ListProductsPaginated
grpcurl -cacert ./ca.crt -d '{"productId":"f6d705be-01db-44f4-bd31-51ead4e671c8"}' localhost:50051 net.proto.ProductGrpcQueryService.GetProduct

- Review endpoint examples

grpcurl -cacert ./ca.crt localhost:50051 describe net.proto.ReviewGrpcCommandService
grpcurl -cacert ./ca.crt localhost:50051 describe net.proto.ReviewGrpcQueryService
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"productId":"f6d705be-01db-44f4-bd31-51ead4e671c8", "reviewText":"Medium", "rating":"3"}' localhost:50051 net.proto.ReviewGrpcCommandService.CreateReview
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"reviewId":"54f16ed2-4ccb-42fb-81ec-aa53afa80f4a", "reviewText":"Awesome", "rating":"4"}' localhost:50051 net.proto.ReviewGrpcCommandService.UpdateReview
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"reviewId":"54f16ed2-4ccb-42fb-81ec-aa53afa80f4a"}' localhost:50051 net.proto.ReviewGrpcCommandService.DeleteReview
grpcurl -cacert ./ca.crt localhost:50051 net.proto.ReviewGrpcQueryService.ListReviews
grpcurl -cacert ./ca.crt -d '{"page":0, "size":3, "sortBy":"reviewId", "sortDir":"DESC"}' localhost:50051 net.proto.ReviewGrpcQueryService.ListReviewsPaginated
grpcurl -cacert ./ca.crt -d '{"reviewId":"54f16ed2-4ccb-42fb-81ec-aa53afa80f4a"}' localhost:50051 net.proto.ReviewGrpcQueryService.GetReview

- Vote endpoint examples

grpcurl -cacert ./ca.crt localhost:50051 describe net.proto.VoteGrpcCommandService
grpcurl -cacert ./ca.crt localhost:50051 describe net.proto.VoteGrpcQueryService
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"reviewId":"54f16ed2-4ccb-42fb-81ec-aa53afa80f4a", "useful":"true"}' localhost:50051 net.proto.VoteGrpcCommandService.CreateVote
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"voteId":"3800f778-271d-4070-9b06-b4aa0ca68878", "useful":"false"}' localhost:50051 net.proto.VoteGrpcCommandService.UpdateVote
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"voteId":"3800f778-271d-4070-9b06-b4aa0ca68878"}' localhost:50051 net.proto.VoteGrpcCommandService.DeleteVote
grpcurl -cacert ./ca.crt localhost:50051 net.proto.VoteGrpcQueryService.ListVotes
grpcurl -cacert ./ca.crt -d '{"page":0, "size":3, "sortBy":"voteId", "sortDir":"DESC"}' localhost:50051 net.proto.VoteGrpcQueryService.ListVotesPaginated
grpcurl -cacert ./ca.crt -d '{"voteId":"3800f778-271d-4070-9b06-b4aa0ca68878"}' localhost:50051 net.proto.VoteGrpcQueryService.GetVote
      
```
 

## Features

#### :arrow_forward: Secure gRPC communications with SSL encryption

#### :arrow_forward: Integration of Hibernate for efficient database management

#### :arrow_forward: JWT token-based authorization for secure access

#### :arrow_forward: Comprehensive unit testing for business logic classes



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
