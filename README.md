```mermaid
graph TD
subgraph Client
Browser[Browser / Mobile]
end
APIGW[API Gateway]
Auth[Auth Service (OAuth2)]
subgraph Services
Catalog[Catalog Service]\n(Products DB)
Cart[Cart Service]\n(Cart DB)
Orders[Orders Service]\n(Orders DB)
Inventory[Inventory Service]\n(Inventory DB)
Payment[Payment Service]
Notification[Notification Service]
end
Broker[(Message Broker) \n Kafka/RabbitMQ]
Browser -->|HTTP REST / gRPC| APIGW
APIGW -->|JWT Validation| Auth
APIGW --> Catalog
APIGW --> Cart
APIGW --> Orders
APIGW --> Payment
Catalog -->|synchronous read| Inventory
Orders -->|reserve item (sync/async)| Inventory
Orders -->|publish event: OrderCreated| Broker
Inventory -->|publish event: InventoryUpdated| Broker
Cart -->|publish event: CartCheckedOut| Broker
Broker --> Orders
Broker --> Notification
Orders -->|call| Payment
Payment -->|publish event: PaymentSucceeded/Failed| Broker
Notification -->|send email/sms| External[External Email/SMS Provider]
%% Databases (DB per service)
Catalog ---|postgres| CatalogDB[(Catalog DB)]
Cart ---|redis| CartDB[(Cart DB)]
Orders ---|postgres| OrdersDB[(Orders DB)]
4
Inventory ---|postgres| InventoryDB[(Inventory DB)]
style Broker stroke:#333,stroke-width:2px
```
