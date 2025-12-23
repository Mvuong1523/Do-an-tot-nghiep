# Testing Strategy - Hệ Thống Thương Mại Điện Tử

## 1. Tổng Quan Testing Strategy

### 1.1. Mục Tiêu Testing

Chiến lược testing của hệ thống TMDT nhằm đảm bảo:
- **Chất lượng code**: Phát hiện bugs sớm trong quá trình phát triển
- **Tính đúng đắn**: Xác minh hệ thống hoạt động đúng theo requirements
- **Độ tin cậy**: Đảm bảo hệ thống ổn định trong môi trường production
- **Bảo trì dễ dàng**: Code có thể refactor an toàn với test coverage
- **Tích hợp liền mạch**: Các module hoạt động đúng khi kết hợp

### 1.2. Testing Pyramid

```
                    /\
                   /  \
                  / E2E \
                 /--------\
                /          \
               / Integration \
              /--------------\
             /                \
            /   Unit Tests     \
           /____________________\
```

**Phân bổ test cases**:
- **Unit Tests**: 70% - Test các component độc lập
- **Integration Tests**: 20% - Test tương tác giữa các module
- **End-to-End Tests**: 10% - Test toàn bộ user flows

### 1.3. Testing Tools & Frameworks

**Backend Testing**:
- **JUnit 5**: Framework chính cho unit testing
- **Mockito**: Mocking dependencies
- **Spring Boot Test**: Integration testing với Spring context
- **Spring Security Test**: Testing authentication & authorization
- **H2 Database**: In-memory database cho testing
- **REST Assured**: API testing (optional)
- **TestContainers**: Container-based integration testing (optional)

**API Testing**:
- **HTTP Client Files**: `.http` files với VS Code REST Client extension
- **Postman**: Manual API testing và documentation

**Frontend Testing** (Future):
- **Jest**: Unit testing cho React components
- **React Testing Library**: Component testing
- **Cypress/Playwright**: E2E testing

**Performance Testing** (Future):
- **JMeter**: Load testing
- **Gatling**: Performance testing

## 2. Unit Testing Strategy

### 2.1. Phạm Vi Unit Testing

Unit tests tập trung vào testing các component độc lập:
- **Service Layer**: Business logic
- **Repository Layer**: Data access (với H2 in-memory)
- **Controller Layer**: Request/response handling
- **Utility Classes**: Helper functions
- **Validators**: Input validation logic

### 2.2. Unit Testing Best Practices

**Naming Convention**:
```java
// Pattern: methodName_scenario_expectedBehavior
@Test
void createOrder_withValidData_shouldReturnOrderResponse()

@Test
void createOrder_withInsufficientStock_shouldThrowException()
```

**Test Structure (AAA Pattern)**:
```java
@Test
void testMethod() {
    // Arrange - Setup test data and mocks
    OrderRequest request = createValidOrderRequest();
    when(inventoryService.checkStock()).thenReturn(true);
    
    // Act - Execute the method under test
    OrderResponse response = orderService.createOrder(request);
    
    // Assert - Verify the results
    assertNotNull(response);
    assertEquals(OrderStatus.PENDING, response.getStatus());
    verify(inventoryService).reserveStock(any());
}
```

**Mocking Strategy**:
- Mock external dependencies (APIs, databases)
- Mock collaborating services
- Use real objects for simple DTOs and entities
- Avoid over-mocking (test real behavior when possible)

### 2.3. Unit Test Examples

#### 2.3.1. Service Layer Testing

**OrderServiceTest.java**:
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private InventoryService inventoryService;
    
    @Mock
    private PaymentService paymentService;
    
    @InjectMocks
    private OrderServiceImpl orderService;
    
    @Test
    void createOrder_withCODPayment_shouldCreateConfirmedOrder() {
        // Arrange
        OrderRequest request = OrderRequest.builder()
            .paymentMethod("COD")
            .items(List.of(createOrderItem()))
            .build();
            
        when(inventoryService.checkStockAvailability(any()))
            .thenReturn(true);
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(i -> i.getArgument(0));
        
        // Act
        OrderResponse response = orderService.createOrder(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals("COD", response.getPaymentMethod());
        verify(inventoryService).reserveStock(any());
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    void createOrder_withInsufficientStock_shouldThrowException() {
        // Arrange
        OrderRequest request = createOrderRequest();
        when(inventoryService.checkStockAvailability(any()))
            .thenReturn(false);
        
        // Act & Assert
        assertThrows(InsufficientStockException.class, 
            () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(any());
    }
    
    @Test
    void updateOrderStatus_withInvalidTransition_shouldThrowException() {
        // Arrange
        Order order = createOrder(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED));
    }
}
```


#### 2.3.2. Repository Layer Testing

**OrderRepositoryTest.java**:
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void findByOrderCode_withExistingCode_shouldReturnOrder() {
        // Arrange
        Order order = createOrder();
        order.setOrderCode("ORD123");
        entityManager.persist(order);
        entityManager.flush();
        
        // Act
        Optional<Order> found = orderRepository.findByOrderCode("ORD123");
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("ORD123", found.get().getOrderCode());
    }
    
    @Test
    void findByStatus_shouldReturnOrdersWithStatus() {
        // Arrange
        Order order1 = createOrder(OrderStatus.PENDING);
        Order order2 = createOrder(OrderStatus.CONFIRMED);
        Order order3 = createOrder(OrderStatus.PENDING);
        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.persist(order3);
        entityManager.flush();
        
        // Act
        List<Order> pendingOrders = orderRepository
            .findByStatus(OrderStatus.PENDING);
        
        // Assert
        assertEquals(2, pendingOrders.size());
    }
}
```


#### 2.3.3. Controller Layer Testing

**OrderControllerTest.java**:
```java
@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private OrderService orderService;
    
    @MockBean
    private JwtService jwtService;
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createOrder_withValidRequest_shouldReturn200() throws Exception {
        // Arrange
        OrderRequest request = createValidOrderRequest();
        OrderResponse response = createOrderResponse();
        when(orderService.createOrder(any())).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderCode").exists())
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
    
    @Test
    void createOrder_withoutAuthentication_shouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }
}
```

### 2.4. Test Coverage Goals

**Minimum Coverage Targets**:
- **Service Layer**: 80% line coverage
- **Repository Layer**: 70% line coverage
- **Controller Layer**: 75% line coverage
- **Utility Classes**: 90% line coverage


**Coverage Measurement**:
```bash
# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## 3. Integration Testing Strategy

### 3.1. Phạm Vi Integration Testing

Integration tests verify interactions between components:
- **Service-to-Service**: Business logic coordination
- **Service-to-Repository**: Data persistence
- **Controller-to-Service**: Request handling flow
- **External API Integration**: GHN, SePay, Cloudinary
- **Event Listeners**: Accounting automation

### 3.2. Integration Test Approach

**Spring Boot Test Context**:
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void completeOrderFlow_shouldUpdateAllRelatedEntities() {
        // Test full order creation flow with real services
    }
}
```

### 3.3. Integration Test Examples

#### 3.3.1. Order Creation Flow

**OrderCreationIntegrationTest.java**:
```java
@SpringBootTest
@Transactional
class OrderCreationIntegrationTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private InventoryStockRepository stockRepository;
    
    @BeforeEach
    void setup() {
        // Setup test data
        createTestProducts();
        createTestInventory();
    }
    
    @Test
    void createOrder_shouldReserveInventoryAndCreateOrder() {
        // Arrange
        Long productId = 1L;
        Long initialStock = stockRepository.findByProductId(productId)
            .get().getAvailable();
        
        OrderRequest request = OrderRequest.builder()
            .items(List.of(createOrderItem(productId, 2)))
            .paymentMethod("COD")
            .build();
        
        // Act
        OrderResponse response = orderService.createOrder(request);
        
        // Assert
        assertNotNull(response.getOrderCode());
        
        // Verify inventory was reserved
        Long newStock = stockRepository.findByProductId(productId)
            .get().getAvailable();
        assertEquals(initialStock - 2, newStock);
        
        // Verify order was saved
        Order savedOrder = orderRepository.findByOrderCode(response.getOrderCode())
            .orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, savedOrder.getStatus());
    }
}
```


#### 3.3.2. Accounting Event Integration

**AccountingEventIntegrationTest.java**:
```java
@SpringBootTest
@Transactional
class AccountingEventIntegrationTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AccountingService accountingService;
    
    @Autowired
    private FinancialTransactionRepository transactionRepository;
    
    @Test
    void orderDelivered_shouldCreateRevenueTransaction() {
        // Arrange
        Order order = createAndSaveOrder();
        
        // Act
        orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);
        
        // Assert
        List<FinancialTransaction> transactions = 
            transactionRepository.findByOrderId(order.getId());
        
        assertFalse(transactions.isEmpty());
        FinancialTransaction revenue = transactions.stream()
            .filter(t -> t.getType() == TransactionType.REVENUE)
            .findFirst()
            .orElseThrow();
        
        assertEquals(order.getTotal(), revenue.getAmount());
        assertEquals(TransactionCategory.SALES_REVENUE, revenue.getCategory());
    }
}
```

#### 3.3.3. External API Integration (Mocked)

**GHNIntegrationTest.java**:
```java
@SpringBootTest
@AutoConfigureMockMvc
class GHNIntegrationTest {
    
    @Autowired
    private ShippingService shippingService;
    
    @MockBean
    private RestTemplate restTemplate;
    
    @Test
    void createGHNOrder_withValidData_shouldReturnGHNCode() {
        // Arrange
        Order order = createReadyToShipOrder();
        
        GHNResponse mockResponse = GHNResponse.builder()
            .orderCode("GHN123456")
            .success(true)
            .build();
        
        when(restTemplate.postForObject(anyString(), any(), eq(GHNResponse.class)))
            .thenReturn(mockResponse);
        
        // Act
        GHNOrderResponse response = shippingService.createGHNOrder(order.getId());
        
        // Assert
        assertNotNull(response.getGhnOrderCode());
        assertEquals("GHN123456", response.getGhnOrderCode());
        verify(restTemplate).postForObject(anyString(), any(), eq(GHNResponse.class));
    }
}
```

### 3.4. Database Integration Testing

**Test Database Configuration**:
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

**Using Test Containers** (Optional):
```java
@SpringBootTest
@Testcontainers
class OrderRepositoryIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
```


## 4. API Testing với HTTP Files

### 4.1. HTTP Client Files Structure

Hệ thống sử dụng `.http` files với VS Code REST Client extension để test APIs:

```
project-root/
├── test-auth.http              # Authentication tests
├── test-ghn-integration.http   # GHN shipping tests
├── test-warehouse-export.http  # Warehouse operations
├── test-accounting-reconciliation.http
├── test-shipping-api.http
└── test-warehouse-orders-tabs.http
```

### 4.2. HTTP File Format

**test-auth.http**:
```http
### Variables
@baseUrl = http://localhost:8080
@token = {{login.response.body.data.token}}

### 1. Login as Customer
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "password123"
}

### 2. Get My Orders (requires authentication)
GET {{baseUrl}}/api/orders/my-orders
Authorization: Bearer {{token}}

### 3. Create Order
POST {{baseUrl}}/api/orders
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "province": "Hà Nội",
  "district": "Ba Đình",
  "ward": "Ngọc Hà",
  "address": "123 Test Street",
  "paymentMethod": "COD",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```


### 4.3. API Test Scenarios

#### 4.3.1. Authentication Flow
```http
### Test 1: Login Success
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}

# Expected: 200 OK with JWT token

### Test 2: Login Failed - Invalid Credentials
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "wrongpassword"
}

# Expected: 401 Unauthorized

### Test 3: Access Protected Endpoint Without Token
GET {{baseUrl}}/api/orders/my-orders

# Expected: 401 Unauthorized

### Test 4: Access Protected Endpoint With Invalid Token
GET {{baseUrl}}/api/orders/my-orders
Authorization: Bearer invalid_token_here

# Expected: 401 Unauthorized
```

#### 4.3.2. Order Management Flow
```http
### Test 1: Create Order - COD Payment
POST {{baseUrl}}/api/orders
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "province": "Hà Nội",
  "district": "Ba Đình",
  "ward": "Ngọc Hà",
  "address": "123 Test Street",
  "paymentMethod": "COD",
  "shippingFee": 0,
  "items": [{"productId": 1, "quantity": 2}]
}

# Expected: 200 OK, status = CONFIRMED
```


### Test 2: Create Order - Insufficient Stock
POST {{baseUrl}}/api/orders
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "items": [{"productId": 1, "quantity": 999999}]
}

# Expected: 400 Bad Request with stock error

### Test 3: Update Order Status - Valid Transition
PUT {{baseUrl}}/api/admin/orders/1/status
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "status": "SHIPPING"
}

# Expected: 200 OK

### Test 4: Update Order Status - Invalid Transition
PUT {{baseUrl}}/api/admin/orders/1/status
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "status": "DELIVERED"
}

# Expected: 400 Bad Request - invalid transition
```

#### 4.3.3. GHN Integration Flow
```http
### Test 1: Calculate Shipping Fee - Hanoi (Free)
POST {{baseUrl}}/api/shipping/calculate-fee
Content-Type: application/json

{
  "province": "Hà Nội",
  "district": "Ba Đình",
  "weight": 1000,
  "value": 500000
}

# Expected: 200 OK, fee = 0

### Test 2: Calculate Shipping Fee - Outside Hanoi
POST {{baseUrl}}/api/shipping/calculate-fee
Content-Type: application/json

{
  "province": "Hải Phòng",
  "district": "Hồng Bàng",
  "weight": 1000,
  "value": 500000
}

# Expected: 200 OK, fee > 0
```


### Test 3: Create GHN Order
POST {{baseUrl}}/api/shipping/create-ghn-order
Authorization: Bearer {{warehouseToken}}
Content-Type: application/json

{
  "orderId": 1
}

# Expected: 200 OK with GHN order code

### Test 4: GHN Webhook - Delivered
POST {{baseUrl}}/api/webhooks/ghn
Content-Type: application/json

{
  "orderCode": "GHN123456",
  "status": "delivered",
  "statusText": "Đã giao hàng",
  "updatedDate": 1702454400,
  "partnerCode": "ORD20231212001"
}

# Expected: 200 OK, order status updated to DELIVERED
```

#### 4.3.4. Warehouse Operations Flow
```http
### Test 1: Get Pending Export Orders
GET {{baseUrl}}/api/inventory/orders/pending-export?page=0&size=20
Authorization: Bearer {{warehouseToken}}

# Expected: 200 OK with list of orders

### Test 2: Create Export Order
POST {{baseUrl}}/api/inventory/export-for-sale
Authorization: Bearer {{warehouseToken}}
Content-Type: application/json

{
  "orderId": 1,
  "reason": "Xuất kho bán hàng",
  "items": [
    {
      "productSku": "IP15PM-256-BLK",
      "serialNumbers": ["IP15PM256BLK001"]
    }
  ]
}

# Expected: 200 OK, order status updated to READY_TO_SHIP
```


### Test 3: Import Products via Excel
POST {{baseUrl}}/api/inventory/import-excel
Authorization: Bearer {{warehouseToken}}
Content-Type: multipart/form-data

file: @sample-import-products.csv

# Expected: 200 OK with import summary
```

### 4.4. API Testing Checklist

**For Each Endpoint**:
- [ ] Test with valid data (happy path)
- [ ] Test with invalid data (validation errors)
- [ ] Test without authentication (if protected)
- [ ] Test with wrong role (authorization)
- [ ] Test with missing required fields
- [ ] Test with boundary values
- [ ] Test error responses (4xx, 5xx)
- [ ] Verify response format and structure
- [ ] Check response time (performance)

## 5. End-to-End Testing Flows

### 5.1. E2E Test Scenarios

#### 5.1.1. Complete Order Flow (COD)

**Scenario**: Customer đặt hàng COD và nhận hàng thành công

**Steps**:
1. Customer login
2. Browse products
3. Add products to cart
4. Checkout with COD payment
5. Sales staff confirms order
6. Warehouse creates export order
7. Warehouse completes export (generates serial numbers)
8. Shipping coordinator creates GHN order
9. GHN delivers order (webhook)
10. System records revenue automatically

**Expected Results**:
- Order status: PENDING → CONFIRMED → READY_TO_PICK → READY_TO_SHIP → SHIPPING → DELIVERED
- Inventory: Available quantity decreased
- Accounting: Revenue transaction created
- Customer: Can view order history and tracking


#### 5.1.2. Complete Order Flow (Online Payment)

**Scenario**: Customer đặt hàng online payment và thanh toán thành công

**Steps**:
1. Customer login
2. Add products to cart
3. Checkout with online payment
4. System generates QR code
5. Customer scans and transfers money
6. SePay sends webhook
7. System matches payment and confirms order
8. Continue with warehouse and shipping flow
9. Order delivered
10. System records both payment and revenue

**Expected Results**:
- Order status: PENDING_PAYMENT → CONFIRMED → ... → DELIVERED
- Payment status: PENDING → COMPLETED
- Accounting: Payment transaction + Revenue transaction
- Bank account balance updated

#### 5.1.3. Warehouse Import Flow

**Scenario**: Warehouse staff nhập hàng từ nhà cung cấp

**Steps**:
1. Warehouse staff login
2. Upload Excel file with product data
3. System validates and parses data
4. Create import transaction
5. Complete import transaction
6. System updates inventory stock
7. System creates supplier payable (if applicable)
8. Accountant can view payable report

**Expected Results**:
- Import transaction created with COMPLETED status
- Inventory stock increased (onHand and available)
- Supplier payable recorded
- Products available for sale

#### 5.1.4. Order Cancellation Flow

**Scenario**: Customer hủy đơn hàng sau khi đã xuất kho

**Steps**:
1. Order in READY_TO_SHIP status
2. Customer requests cancellation
3. Admin/Sales approves cancellation
4. System cancels export order
5. System restores inventory
6. System updates order status to CANCELLED
7. If payment was made, create refund transaction

**Expected Results**:
- Order status: CANCELLED
- Export order: CANCELLED
- Inventory: Available quantity restored
- Accounting: Refund transaction (if applicable)


### 5.2. E2E Testing Tools

**Manual Testing**:
- Use HTTP files for step-by-step testing
- Verify database state after each step
- Check frontend UI for correct display

**Automated E2E Testing** (Future):
```javascript
// Cypress example
describe('Complete Order Flow', () => {
  it('should complete COD order from checkout to delivery', () => {
    // Login as customer
    cy.login('customer@example.com', 'password');
    
    // Add product to cart
    cy.visit('/products/1');
    cy.get('[data-testid="add-to-cart"]').click();
    
    // Checkout
    cy.visit('/checkout');
    cy.fillShippingAddress({
      province: 'Hà Nội',
      district: 'Ba Đình',
      ward: 'Ngọc Hà',
      address: '123 Test Street'
    });
    cy.selectPaymentMethod('COD');
    cy.get('[data-testid="place-order"]').click();
    
    // Verify order created
    cy.url().should('include', '/orders/success');
    cy.contains('Đặt hàng thành công');
    
    // Login as warehouse staff
    cy.login('warehouse@example.com', 'password');
    
    // Create export order
    cy.visit('/warehouse/orders');
    cy.get('[data-testid="export-order"]').first().click();
    cy.fillSerialNumbers(['SN001', 'SN002']);
    cy.get('[data-testid="complete-export"]').click();
    
    // Verify order status updated
    cy.contains('READY_TO_SHIP');
  });
});
```

## 6. Performance Testing Considerations

### 6.1. Performance Test Scenarios

**Load Testing**:
- Concurrent user logins (100-500 users)
- Simultaneous order creation (50-100 orders/minute)
- Product search and browsing (1000 requests/minute)
- Inventory updates during high traffic


**Stress Testing**:
- Database connection pool exhaustion
- Memory usage under heavy load
- API response time degradation
- External API timeout handling

**Endurance Testing**:
- System stability over 24-48 hours
- Memory leaks detection
- Database connection leaks
- Resource cleanup verification

### 6.2. Performance Metrics

**Response Time Targets**:
- API endpoints: < 200ms (p95)
- Database queries: < 100ms (p95)
- Page load time: < 2 seconds
- Search operations: < 500ms

**Throughput Targets**:
- Order creation: 100 orders/minute
- Product search: 1000 requests/minute
- Concurrent users: 500 active users

**Resource Usage Targets**:
- CPU usage: < 70% under normal load
- Memory usage: < 2GB heap size
- Database connections: < 50 active connections

### 6.3. Performance Testing Tools

**JMeter Test Plan Example**:
```xml
<!-- Order Creation Load Test -->
<ThreadGroup>
  <numThreads>100</numThreads>
  <rampUp>60</rampUp>
  <loops>10</loops>
  
  <HTTPSampler>
    <path>/api/orders</path>
    <method>POST</method>
    <body>
      {
        "items": [{"productId": 1, "quantity": 2}],
        "paymentMethod": "COD"
      }
    </body>
  </HTTPSampler>
  
  <ResponseAssertion>
    <responseCode>200</responseCode>
    <responseTime>200</responseTime>
  </ResponseAssertion>
</ThreadGroup>
```


**Database Performance Testing**:
```sql
-- Test query performance
EXPLAIN ANALYZE 
SELECT o.*, oi.* 
FROM orders o 
JOIN order_items oi ON o.id = oi.order_id 
WHERE o.customer_id = 1 
AND o.created_at > NOW() - INTERVAL 30 DAY;

-- Check index usage
SHOW INDEX FROM orders;

-- Monitor slow queries
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;
```

## 7. Test Data Management

### 7.1. Test Data Strategy

**Test Data Categories**:
1. **Seed Data**: Base data for all tests (categories, provinces, districts)
2. **Fixture Data**: Reusable test objects (sample orders, products)
3. **Generated Data**: Random data for specific tests
4. **Production-like Data**: Anonymized production data for realistic testing

### 7.2. Test Data Setup

**Database Seeding**:
```java
@Component
public class TestDataSeeder {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public void seedTestData() {
        // Create categories
        Category electronics = Category.builder()
            .name("Electronics")
            .build();
        categoryRepository.save(electronics);
        
        // Create products
        Product iphone = Product.builder()
            .name("iPhone 15 Pro Max")
            .sku("IP15PM-256-BLK")
            .price(29990000.0)
            .category(electronics)
            .build();
        productRepository.save(iphone);
    }
}
```


**Test Fixtures**:
```java
public class TestFixtures {
    
    public static OrderRequest createValidOrderRequest() {
        return OrderRequest.builder()
            .province("Hà Nội")
            .district("Ba Đình")
            .ward("Ngọc Hà")
            .address("123 Test Street")
            .paymentMethod("COD")
            .items(List.of(createOrderItem()))
            .build();
    }
    
    public static OrderItem createOrderItem() {
        return OrderItem.builder()
            .productId(1L)
            .quantity(2)
            .price(1000000.0)
            .build();
    }
    
    public static Order createOrder(OrderStatus status) {
        Order order = new Order();
        order.setOrderCode("ORD" + System.currentTimeMillis());
        order.setStatus(status);
        order.setTotal(2000000.0);
        order.setPaymentMethod("COD");
        return order;
    }
}
```

**Data Builders**:
```java
public class OrderBuilder {
    private Order order = new Order();
    
    public OrderBuilder withOrderCode(String code) {
        order.setOrderCode(code);
        return this;
    }
    
    public OrderBuilder withStatus(OrderStatus status) {
        order.setStatus(status);
        return this;
    }
    
    public OrderBuilder withCustomer(Customer customer) {
        order.setCustomer(customer);
        return this;
    }
    
    public Order build() {
        return order;
    }
}

// Usage
Order order = new OrderBuilder()
    .withOrderCode("ORD123")
    .withStatus(OrderStatus.CONFIRMED)
    .build();
```


### 7.3. Test Data Cleanup

**Transaction Rollback**:
```java
@SpringBootTest
@Transactional  // Automatically rollback after each test
class OrderServiceTest {
    
    @Test
    void testMethod() {
        // Test data is automatically cleaned up
    }
}
```

**Manual Cleanup**:
```java
@SpringBootTest
class IntegrationTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @AfterEach
    void cleanup() {
        orderRepository.deleteAll();
    }
}
```

**Database Reset Script**:
```sql
-- reset-test-db.sql
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE inventory_stock;
TRUNCATE TABLE financial_transaction;
TRUNCATE TABLE supplier_payables;

-- Reset auto-increment
ALTER TABLE orders AUTO_INCREMENT = 1;
ALTER TABLE order_items AUTO_INCREMENT = 1;
```

### 7.4. Test Data Isolation

**Separate Test Database**:
```properties
# application-test.properties
spring.datasource.url=jdbc:mysql://localhost:3306/testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

**Test Data Namespacing**:
```java
// Use unique identifiers per test
String testId = UUID.randomUUID().toString();
String orderCode = "TEST_" + testId + "_ORD001";
```

## 8. Testing Best Practices

### 8.1. General Testing Principles

**FIRST Principles**:
- **Fast**: Tests should run quickly
- **Independent**: Tests should not depend on each other
- **Repeatable**: Same results every time
- **Self-validating**: Clear pass/fail
- **Timely**: Written alongside code


**Test Naming**:
```java
// Good: Descriptive and clear
@Test
void createOrder_withInsufficientStock_shouldThrowInsufficientStockException()

// Bad: Vague
@Test
void testCreateOrder()
```

**Test Organization**:
```java
@Nested
@DisplayName("Order Creation Tests")
class OrderCreationTests {
    
    @Nested
    @DisplayName("When payment method is COD")
    class CODPaymentTests {
        
        @Test
        @DisplayName("Should create confirmed order")
        void shouldCreateConfirmedOrder() {
            // Test implementation
        }
    }
    
    @Nested
    @DisplayName("When payment method is Online")
    class OnlinePaymentTests {
        
        @Test
        @DisplayName("Should create pending payment order")
        void shouldCreatePendingPaymentOrder() {
            // Test implementation
        }
    }
}
```

### 8.2. Mocking Best Practices

**When to Mock**:
- External APIs (GHN, SePay, Cloudinary)
- Database in unit tests
- Time-dependent operations
- Random number generation

**When NOT to Mock**:
- Simple DTOs and value objects
- Domain logic (test real behavior)
- Integration tests (use real components)

**Mock Verification**:
```java
// Verify method was called
verify(inventoryService).reserveStock(any());

// Verify method was called with specific arguments
verify(inventoryService).reserveStock(argThat(items -> 
    items.size() == 2 && items.get(0).getQuantity() == 1
));

// Verify method was never called
verify(paymentService, never()).createPayment(any());

// Verify number of invocations
verify(emailService, times(2)).sendEmail(any());
```


### 8.3. Assertion Best Practices

**Use Specific Assertions**:
```java
// Good: Specific assertions
assertNotNull(response);
assertEquals(OrderStatus.CONFIRMED, response.getStatus());
assertTrue(response.getTotal() > 0);

// Bad: Generic assertions
assertTrue(response != null);
assertTrue(response.getStatus().equals(OrderStatus.CONFIRMED));
```

**AssertJ for Fluent Assertions**:
```java
// More readable and provides better error messages
assertThat(response)
    .isNotNull()
    .extracting(OrderResponse::getStatus)
    .isEqualTo(OrderStatus.CONFIRMED);

assertThat(response.getItems())
    .hasSize(2)
    .extracting(OrderItem::getQuantity)
    .containsExactly(1, 2);
```

**Exception Testing**:
```java
// JUnit 5
assertThrows(InsufficientStockException.class, 
    () -> orderService.createOrder(request));

// With message verification
Exception exception = assertThrows(InsufficientStockException.class,
    () -> orderService.createOrder(request));
assertEquals("Product X is out of stock", exception.getMessage());

// AssertJ
assertThatThrownBy(() -> orderService.createOrder(request))
    .isInstanceOf(InsufficientStockException.class)
    .hasMessageContaining("out of stock");
```

### 8.4. Test Maintenance

**Avoid Test Duplication**:
```java
// Use @BeforeEach for common setup
@BeforeEach
void setup() {
    testProduct = createTestProduct();
    testCustomer = createTestCustomer();
}

// Use helper methods
private OrderRequest createValidRequest() {
    return OrderRequest.builder()
        .items(List.of(createOrderItem()))
        .build();
}
```


**Parameterized Tests**:
```java
@ParameterizedTest
@ValueSource(strings = {"COD", "ONLINE", "BANK_TRANSFER"})
void createOrder_withDifferentPaymentMethods_shouldSucceed(String paymentMethod) {
    OrderRequest request = createRequest(paymentMethod);
    OrderResponse response = orderService.createOrder(request);
    assertNotNull(response);
}

@ParameterizedTest
@CsvSource({
    "PENDING, CONFIRMED, true",
    "CONFIRMED, SHIPPING, true",
    "PENDING, DELIVERED, false",
    "SHIPPING, PENDING, false"
})
void validateStatusTransition(OrderStatus from, OrderStatus to, boolean expected) {
    boolean result = orderService.isValidTransition(from, to);
    assertEquals(expected, result);
}
```

## 9. Continuous Integration Testing

### 9.1. CI/CD Pipeline

**GitHub Actions Example**:
```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: testdb
        ports:
          - 3306:3306
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 21
      uses: actions/setup-java@v2
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v2
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests
      run: mvn clean test
    
    - name: Generate coverage report
      run: mvn jacoco:report
```
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v2
      with:
        file: ./target/site/jacoco/jacoco.xml
    
    - name: Publish test results
      uses: EnricoMi/publish-unit-test-result-action@v1
      if: always()
      with:
        files: target/surefire-reports/*.xml
```

### 9.2. Pre-commit Hooks

**Maven Verify**:
```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Running tests before commit..."
mvn clean test

if [ $? -ne 0 ]; then
    echo "Tests failed. Commit aborted."
    exit 1
fi

echo "All tests passed. Proceeding with commit."
```

### 9.3. Test Reporting

**JaCoCo Coverage Report**:
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```


## 10. Testing Checklist by Module

### 10.1. Authentication Module

**Unit Tests**:
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] JWT token generation
- [ ] JWT token validation
- [ ] Password hashing
- [ ] Role-based authorization

**Integration Tests**:
- [ ] Complete login flow
- [ ] Token refresh flow
- [ ] First-time password change
- [ ] Employee registration approval

**API Tests**:
- [ ] POST /api/auth/login
- [ ] POST /api/auth/register
- [ ] POST /api/auth/employee-register
- [ ] PUT /api/auth/change-password

### 10.2. Order Module

**Unit Tests**:
- [ ] Order creation with COD
- [ ] Order creation with online payment
- [ ] Order status validation
- [ ] Status transition validation
- [ ] Order cancellation logic
- [ ] Shipping fee calculation

**Integration Tests**:
- [ ] Order creation with inventory reservation
- [ ] Order status update with accounting
- [ ] Order cancellation with inventory restoration
- [ ] Payment timeout handling

**API Tests**:
- [ ] POST /api/orders (COD)
- [ ] POST /api/orders (Online)
- [ ] GET /api/orders/my-orders
- [ ] GET /api/orders/{id}
- [ ] PUT /api/admin/orders/{id}/status
- [ ] DELETE /api/orders/{id}

### 10.3. Inventory Module

**Unit Tests**:
- [ ] Stock availability check
- [ ] Stock reservation
- [ ] Stock release
- [ ] Import order creation
- [ ] Export order creation
- [ ] Serial number generation
- [ ] Excel parsing logic


**Integration Tests**:
- [ ] Import flow with supplier payable
- [ ] Export flow with order status update
- [ ] Stock calculation (onHand, reserved, available)
- [ ] Concurrent stock updates

**API Tests**:
- [ ] POST /api/inventory/import-excel
- [ ] POST /api/inventory/export-for-sale
- [ ] GET /api/inventory/orders/pending-export
- [ ] GET /api/inventory/stock/{productId}

### 10.4. Shipping Module

**Unit Tests**:
- [ ] GHN order creation request building
- [ ] Shipping fee calculation (Hanoi vs outside)
- [ ] Address validation
- [ ] Webhook signature verification
- [ ] Status mapping (GHN to internal)

**Integration Tests**:
- [ ] Complete GHN order creation flow
- [ ] Webhook processing with order update
- [ ] Delivery status update with accounting
- [ ] Return handling with inventory restoration

**API Tests**:
- [ ] POST /api/shipping/calculate-fee
- [ ] POST /api/shipping/create-ghn-order
- [ ] GET /api/orders/{id}/ghn-tracking
- [ ] POST /api/webhooks/ghn

### 10.5. Payment Module

**Unit Tests**:
- [ ] QR code generation
- [ ] Payment matching logic
- [ ] Amount validation
- [ ] Payment timeout detection
- [ ] SePay webhook verification

**Integration Tests**:
- [ ] Complete payment flow
- [ ] Payment webhook processing
- [ ] Payment timeout with order cancellation
- [ ] Multi-account bank reconciliation

**API Tests**:
- [ ] POST /api/payments/create
- [ ] GET /api/payments/{id}/status
- [ ] POST /api/webhooks/sepay
- [ ] GET /api/payments/reconciliation


### 10.6. Accounting Module

**Unit Tests**:
- [ ] Revenue recognition calculation
- [ ] Supplier payable calculation
- [ ] Financial transaction creation
- [ ] Report generation logic
- [ ] Profit/loss calculation

**Integration Tests**:
- [ ] Event-driven accounting (order delivered)
- [ ] Payment accounting integration
- [ ] Supplier payable with import
- [ ] Multi-period reporting

**API Tests**:
- [ ] GET /api/accounting/transactions
- [ ] GET /api/accounting/reports
- [ ] GET /api/accounting/payables
- [ ] POST /api/accounting/supplier-payment

### 10.7. Product Module

**Unit Tests**:
- [ ] Product creation validation
- [ ] Image upload handling
- [ ] Multi-image management
- [ ] Product search logic
- [ ] Category assignment

**Integration Tests**:
- [ ] Product creation with Cloudinary
- [ ] Product search with filters
- [ ] Excel import with products

**API Tests**:
- [ ] POST /api/products
- [ ] GET /api/products
- [ ] GET /api/products/{id}
- [ ] PUT /api/products/{id}
- [ ] POST /api/products/upload-image

## 11. Test Execution Commands

### 11.1. Maven Commands

```bash
# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=OrderServiceTest

# Run specific test method
mvn test -Dtest=OrderServiceTest#createOrder_withValidData_shouldReturnOrderResponse

# Run tests with coverage
mvn clean test jacoco:report

# Run tests and skip if failures
mvn test -DskipTests=false -Dmaven.test.failure.ignore=true

# Run integration tests only
mvn verify -DskipUnitTests=true

# Run with specific profile
mvn test -Ptest
```


### 11.2. Test Profiles

**application-test.properties**:
```properties
# Test database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop

# Disable external services in tests
ghn.api.enabled=false
sepay.api.enabled=false
cloudinary.enabled=false

# Logging
logging.level.com.doan.WEB_TMDT=DEBUG
logging.level.org.springframework.test=DEBUG
```

### 11.3. Running HTTP Tests

**VS Code REST Client**:
1. Install "REST Client" extension
2. Open `.http` file
3. Click "Send Request" above each request
4. View response in split pane

**IntelliJ HTTP Client**:
1. Open `.http` file
2. Click green arrow next to request
3. View response in tool window

## 12. Kết Luận

### 12.1. Testing Strategy Summary

Chiến lược testing của hệ thống TMDT được thiết kế để:
- **Đảm bảo chất lượng**: Phát hiện bugs sớm và ngăn chặn regression
- **Tăng tốc phát triển**: Refactor an toàn với test coverage
- **Cải thiện thiết kế**: Tests thúc đẩy code modularity
- **Tài liệu hóa**: Tests là documentation sống cho hệ thống

### 12.2. Current Testing Status

**Implemented**:
- ✅ Basic Spring Boot test setup
- ✅ HTTP files for manual API testing
- ✅ Test database configuration

**To Be Implemented**:
- ⏳ Comprehensive unit tests for all services
- ⏳ Integration tests for critical flows
- ⏳ Automated E2E tests
- ⏳ Performance testing suite
- ⏳ CI/CD pipeline with automated testing


### 12.3. Testing Priorities

**Phase 1 - Critical Path Testing** (High Priority):
1. Order creation and status management
2. Inventory stock management
3. Payment processing
4. Authentication and authorization
5. GHN integration

**Phase 2 - Extended Coverage** (Medium Priority):
1. Accounting automation
2. Supplier payable management
3. Product management
4. Report generation
5. Error handling scenarios

**Phase 3 - Advanced Testing** (Low Priority):
1. Performance testing
2. Load testing
3. Security testing
4. Accessibility testing
5. Cross-browser testing (frontend)

### 12.4. Testing Metrics to Track

**Code Coverage**:
- Line coverage: Target 70%+
- Branch coverage: Target 60%+
- Method coverage: Target 75%+

**Test Execution**:
- Total test count
- Pass/fail rate
- Test execution time
- Flaky test rate

**Quality Metrics**:
- Bugs found in testing vs production
- Test maintenance effort
- Time to fix failing tests
- Regression rate

### 12.5. Recommendations

1. **Start with critical paths**: Focus on order and inventory flows first
2. **Automate early**: Set up CI/CD pipeline from the beginning
3. **Maintain test quality**: Refactor tests as code evolves
4. **Monitor coverage**: Track and improve coverage over time
5. **Document test scenarios**: Keep test documentation up-to-date
6. **Review test failures**: Investigate and fix flaky tests immediately
7. **Performance baseline**: Establish performance benchmarks early

---

**Document Version**: 1.0  
**Last Updated**: December 2024  
**Author**: Development Team  
**Status**: Active
