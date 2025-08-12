package com.example.mermaidmaker.data.template

import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template

object BuiltInTemplates {
    
    val allTemplates: List<Template> = listOf(
        // Flowchart Templates
        Template(
            id = "flowchart_basic",
            name = "Basic Flowchart",
            description = "Simple decision-making flowchart template",
            content = """graph TD
    A[Start] --> B{Decision?}
    B -->|Yes| C[Process A]
    B -->|No| D[Process B]
    C --> E[End]
    D --> E""",
            diagramType = DiagramType.FLOWCHART,
            isBuiltIn = true,
            previewImage = null
        ),
        
        Template(
            id = "flowchart_process",
            name = "Process Flow",
            description = "Business process flowchart with multiple steps",
            content = """graph TD
    A[Start Process] --> B[Input Data]
    B --> C{Validate Input}
    C -->|Valid| D[Process Data]
    C -->|Invalid| E[Show Error]
    E --> B
    D --> F[Save Results]
    F --> G{Success?}
    G -->|Yes| H[Notify User]
    G -->|No| I[Log Error]
    H --> J[End]
    I --> J""",
            diagramType = DiagramType.FLOWCHART,
            isBuiltIn = true,
            previewImage = null
        ),
        
        Template(
            id = "flowchart_algorithm",
            name = "Algorithm Flow",
            description = "Algorithm or code logic flowchart",
            content = """graph TD
    A[Initialize Variables] --> B[Read Input]
    B --> C{Input Available?}
    C -->|No| D[End Program]
    C -->|Yes| E[Process Input]
    E --> F{Condition Met?}
    F -->|Yes| G[Execute Action]
    F -->|No| H[Skip Action]
    G --> I[Update State]
    H --> I
    I --> C""",
            diagramType = DiagramType.FLOWCHART,
            isBuiltIn = true,
            previewImage = null
        ),
        
        // Sequence Diagram Templates
        Template(
            id = "sequence_api",
            name = "API Request Flow",
            description = "Client-server API interaction sequence",
            content = """sequenceDiagram
    participant Client
    participant API
    participant Database
    
    Client->>+API: POST /api/data
    API->>+Database: Query data
    Database-->>-API: Return results
    API-->>-Client: 200 OK + data
    
    Client->>+API: GET /api/status
    API-->>-Client: 200 OK + status""",
            diagramType = DiagramType.SEQUENCE,
            isBuiltIn = true,
            previewImage = null
        ),
        
        Template(
            id = "sequence_auth",
            name = "Authentication Flow",
            description = "User authentication sequence diagram",
            content = """sequenceDiagram
    participant User
    participant Frontend
    participant Auth
    participant Database
    
    User->>+Frontend: Login Request
    Frontend->>+Auth: Validate Credentials
    Auth->>+Database: Check User
    Database-->>-Auth: User Data
    Auth-->>-Frontend: JWT Token
    Frontend-->>-User: Login Success
    
    User->>+Frontend: Access Protected Resource
    Frontend->>+Auth: Verify Token
    Auth-->>-Frontend: Token Valid
    Frontend-->>-User: Resource Data""",
            diagramType = DiagramType.SEQUENCE,
            isBuiltIn = true,
            previewImage = null
        ),
        
        Template(
            id = "sequence_payment",
            name = "Payment Processing",
            description = "E-commerce payment flow sequence",
            content = """sequenceDiagram
    participant Customer
    participant Shop
    participant PaymentGateway
    participant Bank
    
    Customer->>+Shop: Place Order
    Shop->>+PaymentGateway: Process Payment
    PaymentGateway->>+Bank: Charge Card
    Bank-->>-PaymentGateway: Payment Result
    PaymentGateway-->>-Shop: Transaction Status
    Shop-->>-Customer: Order Confirmation""",
            diagramType = DiagramType.SEQUENCE,
            isBuiltIn = true,
            previewImage = null
        ),
        
        // Class Diagram Templates
        Template(
            id = "class_basic",
            name = "Basic Class Structure",
            description = "Simple class diagram with inheritance",
            content = """classDiagram
    class Animal {
        +String name
        +int age
        +eat()
        +sleep()
    }
    
    class Dog {
        +String breed
        +bark()
        +wagTail()
    }
    
    class Cat {
        +String color
        +meow()
        +purr()
    }
    
    Animal <|-- Dog
    Animal <|-- Cat""",
            diagramType = DiagramType.CLASS,
            isBuiltIn = true,
            previewImage = null
        ),
        
        Template(
            id = "class_ecommerce",
            name = "E-commerce System",
            description = "E-commerce domain model class diagram",
            content = """classDiagram
    class User {
        +Long id
        +String email
        +String password
        +String firstName
        +String lastName
        +register()
        +login()
    }
    
    class Product {
        +Long id
        +String name
        +String description
        +BigDecimal price
        +int stock
        +updateStock()
    }
    
    class Order {
        +Long id
        +Date orderDate
        +OrderStatus status
        +BigDecimal total
        +calculateTotal()
        +updateStatus()
    }
    
    class OrderItem {
        +Long id
        +int quantity
        +BigDecimal price
    }
    
    User ||--o{ Order
    Order ||--o{ OrderItem
    Product ||--o{ OrderItem""",
            diagramType = DiagramType.CLASS,
            isBuiltIn = true,
            previewImage = null
        ),
        
        // State Diagram Templates
        Template(
            id = "state_user_session",
            name = "User Session States",
            description = "User session lifecycle state diagram",
            content = """stateDiagram-v2
    [*] --> Unauthenticated
    
    Unauthenticated --> Authenticating : login()
    Authenticating --> Authenticated : success
    Authenticating --> Unauthenticated : failure
    
    Authenticated --> Active : user_activity
    Authenticated --> Idle : timeout
    Idle --> Active : user_activity
    Idle --> Unauthenticated : session_expired
    
    Active --> Unauthenticated : logout()
    Authenticated --> Unauthenticated : logout()
    
    Unauthenticated --> [*]""",
            diagramType = DiagramType.STATE,
            isBuiltIn = true,
            previewImage = null
        ),
        
        Template(
            id = "state_order",
            name = "Order Processing States",
            description = "E-commerce order lifecycle states",
            content = """stateDiagram-v2
    [*] --> Draft
    
    Draft --> Pending : submit_order()
    Pending --> Confirmed : payment_success()
    Pending --> Cancelled : payment_failed()
    Pending --> Cancelled : cancel_order()
    
    Confirmed --> Processing : start_fulfillment()
    Processing --> Shipped : ship_order()
    Shipped --> Delivered : delivery_confirmed()
    
    Confirmed --> Cancelled : cancel_before_ship()
    Delivered --> Returned : return_request()
    
    Cancelled --> [*]
    Delivered --> [*]
    Returned --> [*]""",
            diagramType = DiagramType.STATE,
            isBuiltIn = true,
            previewImage = null
        ),
        
        // Entity Relationship Templates
        Template(
            id = "er_blog",
            name = "Blog System ERD",
            description = "Blog database entity relationship diagram",
            content = """erDiagram
    USER {
        int user_id PK
        string username
        string email
        string password_hash
        datetime created_at
        datetime updated_at
    }
    
    POST {
        int post_id PK
        int author_id FK
        string title
        text content
        string status
        datetime published_at
        datetime created_at
        datetime updated_at
    }
    
    CATEGORY {
        int category_id PK
        string name
        string description
        string slug
    }
    
    TAG {
        int tag_id PK
        string name
        string slug
    }
    
    COMMENT {
        int comment_id PK
        int post_id FK
        int user_id FK
        text content
        datetime created_at
    }
    
    POST_CATEGORY {
        int post_id FK
        int category_id FK
    }
    
    POST_TAG {
        int post_id FK
        int tag_id FK
    }
    
    USER ||--o{ POST : "writes"
    POST ||--o{ COMMENT : "has"
    USER ||--o{ COMMENT : "writes"
    POST }o--o{ CATEGORY : "belongs to"
    POST }o--o{ TAG : "tagged with"
    POST ||--o{ POST_CATEGORY : ""
    CATEGORY ||--o{ POST_CATEGORY : ""
    POST ||--o{ POST_TAG : ""
    TAG ||--o{ POST_TAG : ""
    
    
""",
            diagramType = DiagramType.ER_DIAGRAM,
            isBuiltIn = true,
            previewImage = null
        ),
        
        Template(
            id = "er_ecommerce",
            name = "E-commerce ERD",
            description = "E-commerce database schema",
            content = """erDiagram
    CUSTOMER {
        int customer_id PK
        string email
        string first_name
        string last_name
        string phone
        datetime created_at
    }
    
    PRODUCT {
        int product_id PK
        string name
        text description
        decimal price
        int stock_quantity
        int category_id FK
        datetime created_at
    }
    
    CATEGORY {
        int category_id PK
        string name
        string description
        int parent_id FK
    }
    
    ORDER {
        int order_id PK
        int customer_id FK
        decimal total_amount
        string status
        datetime order_date
        datetime shipped_date
    }
    
    ORDER_ITEM {
        int order_item_id PK
        int order_id FK
        int product_id FK
        int quantity
        decimal unit_price
    }
    
    ADDRESS {
        int address_id PK
        int customer_id FK
        string address_line1
        string address_line2
        string city
        string state
        string postal_code
        string country
        string type
    }
    
    CUSTOMER ||--o{ ORDER : "places"
    CUSTOMER ||--o{ ADDRESS : "has"
    ORDER ||--o{ ORDER_ITEM : "contains"
    PRODUCT ||--o{ ORDER_ITEM : "included in"
    CATEGORY ||--o{ PRODUCT : "contains"
    CATEGORY ||--o{ CATEGORY : "parent of"
    
    
""",
            diagramType = DiagramType.ER_DIAGRAM,
            isBuiltIn = true,
            previewImage = null
        ),
        
        // Additional specialized templates
        Template(
            id = "gitgraph_feature",
            name = "Git Feature Branch",
            description = "Git workflow with feature branches",
            content = """gitgraph:
    commit id: "Initial commit"
    branch develop
    checkout develop
    commit id: "Setup project"
    branch feature-login
    checkout feature-login
    commit id: "Add login form"
    commit id: "Add validation"
    checkout develop
    merge feature-login
    checkout main
    merge develop
    commit id: "Release v1.0"
""",
            diagramType = DiagramType.GITGRAPH,
            isBuiltIn = true,
            previewImage = null
        ),
        
        Template(
            id = "journey_shopping",
            name = "Shopping User Journey",
            description = "E-commerce user journey map",
            content = """journey
    title Shopping Experience
    section Discovery
      Visit homepage: 5: Customer
      Browse products: 4: Customer
      Read reviews: 3: Customer
    section Purchase
      Add to cart: 5: Customer
      Checkout process: 2: Customer
      Payment: 3: Customer, Payment Gateway
    section Fulfillment
      Order confirmation: 5: Customer, System
      Shipping: 4: Customer, Logistics
      Delivery: 5: Customer
    section Support
      Product question: 3: Customer, Support
      Return request: 2: Customer, Support
""",
            diagramType = DiagramType.JOURNEY,
            isBuiltIn = true,
            previewImage = null
        )
    )
    
    fun getTemplatesByType(diagramType: DiagramType): List<Template> {
        return allTemplates.filter { it.diagramType == diagramType }
    }
    
    fun getTemplateById(id: String): Template? {
        return allTemplates.find { it.id == id }
    }
}