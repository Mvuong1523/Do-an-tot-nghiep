# 🚀 CÔNG NGHỆ SỬ DỤNG TRONG DỰ ÁN

## 📊 TỔNG QUAN KIẾN TRÚC

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT (Browser)                      │
│              Next.js 14 + React 18 + TypeScript         │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/REST API
                     │ JWT Authentication
┌────────────────────▼────────────────────────────────────┐
│                  BACKEND (Server)                        │
│           Spring Boot 3.5.6 + Java 21                   │
│              RESTful API + Security                      │
└────────────────────┬────────────────────────────────────┘
                     │ JPA/Hibernate
┌────────────────────▼────────────────────────────────────┐
│                   DATABASE                               │
│                   MySQL 8.x                              │
└──────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│              EXTERNAL SERVICES                           │
│  • GHN API (Shipping)                                   │
│  • SePay (Payment Monitoring)                           │
│  • Cloudinary (Image Storage)                           │
│  • SMTP (Email)                                         │
└──────────────────────────────────────────────────────────┘
```

---

## 🎨 FRONTEND STACK

### **Core Framework**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **Next.js** | 14.2.33 | React framework với SSR, routing, API routes |
| **React** | 18.2.0 | UI library - component-based |
| **TypeScript** | 5.2.2 | Type safety, better IDE support |

### **State Management**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **Zustand** | 4.4.7 | Global state management (auth, cart) |
| - | - | Lightweight alternative to Redux |

### **Styling**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **Tailwind CSS** | 3.3.5 | Utility-first CSS framework |
| **PostCSS** | 8.4.31 | CSS processing |
| **Autoprefixer** | 10.4.16 | Auto add vendor prefixes |

### **UI Components & Icons**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **React Icons** | 4.12.0 | Icon library (FiUser, FiMail, etc.) |
| **Swiper** | 11.0.0 | Touch slider/carousel |
| **Sonner** | 2.0.7 | Toast notifications |
| **React Hot Toast** | 2.4.1 | Toast notifications (alternative) |

### **HTTP & Data**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **Axios** | 1.6.0 | HTTP client for API calls |
| **XLSX** | 0.18.5 | Excel file parsing/generation |

### **Utilities**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **html5-qrcode** | 2.3.8 | QR code scanning |
| **react-to-print** | 3.2.0 | Print functionality |

### **Development Tools**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **ESLint** | 8.52.0 | Code linting |
| **eslint-config-next** | 14.0.0 | Next.js ESLint config |

---

## ⚙️ BACKEND STACK

### **Core Framework**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **Spring Boot** | 3.5.6 | Java application framework |
| **Java** | 21 (LTS) | Programming language |
| **Maven** | - | Build tool & dependency management |

### **Spring Modules**
| Module | Mục đích |
|--------|----------|
| **spring-boot-starter-web** | RESTful API, MVC |
| **spring-boot-starter-data-jpa** | Database ORM (Hibernate) |
| **spring-boot-starter-security** | Authentication & Authorization |
| **spring-boot-starter-validation** | Bean validation (JSR-380) |
| **spring-boot-starter-mail** | Email sending |
| **spring-boot-devtools** | Hot reload, development tools |

### **Database**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **MySQL** | 8.x | Primary database |
| **mysql-connector-j** | Runtime | MySQL JDBC driver |
| **Hibernate** | (via JPA) | ORM - Object Relational Mapping |
| **mssql-jdbc** | Runtime | SQL Server driver (backup option) |

### **Security & Authentication**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **Spring Security** | 6.5.5 | Security framework |
| **JWT (JJWT)** | 0.11.5 | JSON Web Token authentication |
| - jjwt-api | 0.11.5 | JWT API |
| - jjwt-impl | 0.11.5 | JWT implementation |
| - jjwt-jackson | 0.11.5 | JWT JSON processing |
| **BCrypt** | (via Spring) | Password hashing |

### **Code Quality & Productivity**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **Lombok** | 1.18.32 | Reduce boilerplate code (@Getter, @Setter, @Builder) |
| **SpringDoc OpenAPI** | 2.5.0 | API documentation (Swagger UI) |

### **Data Processing**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **Apache POI** | 5.2.5 | Excel file generation/parsing |
| **org.json** | 20240303 | JSON parsing & manipulation |

### **External Integrations**
| Công nghệ | Version | Mục đích |
|-----------|---------|----------|
| **Cloudinary** | 1.36.0 | Image upload & storage |
| **RestTemplate** | (via Spring) | HTTP client for API calls (GHN, SePay) |

### **Testing**
| Công nghệ | Mục đích |
|-----------|----------|
| **spring-boot-starter-test** | Unit & integration testing |
| **spring-security-test** | Security testing |

---

## 🗄️ DATABASE

### **MySQL 8.x**
- **Engine:** InnoDB
- **Character Set:** utf8mb4
- **Collation:** utf8mb4_unicode_ci
- **Features used:**
  - Foreign Keys
  - Transactions (ACID)
  - Indexes
  - JSON columns (tech_specs_json)
  - ENUM types (status, role, position)
  - Triggers (optional)

---

## 🌐 EXTERNAL SERVICES

### **1. GHN (Giao Hàng Nhanh)**
- **Purpose:** Shipping & logistics
- **Integration:** REST API
- **Features:**
  - Calculate shipping fee
  - Create shipping orders
  - Track delivery status
  - Webhook for status updates

### **2. SePay**
- **Purpose:** Bank transaction monitoring
- **Integration:** Webhook
- **Features:**
  - Monitor bank account
  - Receive payment notifications
  - Transaction verification

### **3. Cloudinary**
- **Purpose:** Image storage & CDN
- **Integration:** SDK (cloudinary-http44)
- **Features:**
  - Upload product images
  - Image transformation
  - CDN delivery

### **4. SMTP Email**
- **Purpose:** Email notifications
- **Integration:** JavaMailSender
- **Features:**
  - Order confirmation
  - Employee registration approval
  - Password reset

---

## 🏗️ DESIGN PATTERNS & ARCHITECTURES

### **Backend Patterns**
1. **Layered Architecture**
   - Controller → Service → Repository → Entity
   
2. **Dependency Injection**
   - Constructor injection with Lombok @RequiredArgsConstructor
   
3. **DTO Pattern**
   - Separate DTOs for request/response
   
4. **Repository Pattern**
   - Spring Data JPA repositories
   
5. **Builder Pattern**
   - Lombok @Builder for entity creation
   
6. **Event-Driven Architecture**
   - Spring Events (@TransactionalEventListener)
   
7. **Strategy Pattern**
   - Different shipping strategies (GHN vs Internal)

### **Frontend Patterns**
1. **Component-Based Architecture**
   - Reusable React components
   
2. **Custom Hooks**
   - useAuthStore, useCartStore
   
3. **Server-Side Rendering (SSR)**
   - Next.js App Router
   
4. **Client-Side State Management**
   - Zustand with persistence
   
5. **Atomic Design**
   - Components, layouts, pages structure

---

## 🔐 SECURITY FEATURES

### **Backend Security**
- ✅ JWT-based authentication
- ✅ BCrypt password hashing
- ✅ CORS configuration
- ✅ CSRF protection (disabled for API)
- ✅ Method-level security (@PreAuthorize)
- ✅ Role-based access control (RBAC)
- ✅ Position-based authorization
- ✅ SQL injection prevention (JPA)
- ✅ XSS protection (validation)

### **Frontend Security**
- ✅ JWT token storage (localStorage)
- ✅ Protected routes
- ✅ Role-based rendering
- ✅ Input validation
- ✅ HTTPS (production)

---

## 📦 BUILD & DEPLOYMENT

### **Backend Build**
```bash
# Maven build
mvn clean package

# Run
java -jar target/WEB_TMDT-0.0.1-SNAPSHOT.jar

# Or with Maven
mvn spring-boot:run
```

### **Frontend Build**
```bash
# Development
npm run dev

# Production build
npm run build
npm start
```

### **Deployment Options**
1. **VPS/Cloud Server**
   - Backend: PM2 + Java
   - Frontend: PM2 + Node.js
   - Nginx: Reverse proxy + SSL

2. **Docker** (optional)
   - Containerize both apps
   - Docker Compose for orchestration

---

## 📊 PERFORMANCE OPTIMIZATIONS

### **Backend**
- ✅ Connection pooling (HikariCP)
- ✅ JPA lazy loading
- ✅ Database indexing
- ✅ Query optimization
- ✅ Caching (optional: Redis)
- ✅ Async event processing

### **Frontend**
- ✅ Next.js SSR/SSG
- ✅ Image optimization (next/image)
- ✅ Code splitting
- ✅ Lazy loading components
- ✅ CDN for images (Cloudinary)
- ✅ Debouncing/throttling

---

## 🧪 TESTING TOOLS

### **Backend**
- JUnit 5
- Mockito
- Spring Boot Test
- REST Assured (optional)

### **Frontend**
- Jest (optional)
- React Testing Library (optional)
- Cypress (optional)

---

## 📝 DEVELOPMENT TOOLS

### **IDE**
- IntelliJ IDEA (recommended for Java)
- VS Code (recommended for React)

### **API Testing**
- Swagger UI (http://localhost:8080/swagger-ui.html)
- Postman
- .http files (test-auth.http, test-ghn-integration.http)

### **Database Tools**
- MySQL Workbench
- DBeaver
- phpMyAdmin

### **Version Control**
- Git
- GitHub/GitLab

---

## 🎯 WHY THESE TECHNOLOGIES?

### **Spring Boot 3.5.6 + Java 21**
- ✅ Enterprise-grade framework
- ✅ Large ecosystem
- ✅ Strong typing
- ✅ Excellent for complex business logic
- ✅ Great performance
- ✅ Easy to scale

### **Next.js 14 + React 18**
- ✅ SEO-friendly (SSR)
- ✅ Fast page loads
- ✅ Great developer experience
- ✅ Built-in routing
- ✅ API routes
- ✅ Large community

### **MySQL**
- ✅ Reliable & mature
- ✅ ACID compliance
- ✅ Good performance
- ✅ Free & open source
- ✅ Wide hosting support

### **JWT Authentication**
- ✅ Stateless
- ✅ Scalable
- ✅ Mobile-friendly
- ✅ Cross-domain support

### **Tailwind CSS**
- ✅ Fast development
- ✅ Consistent design
- ✅ Small bundle size
- ✅ Responsive by default

---

## 📈 SCALABILITY CONSIDERATIONS

### **Current Architecture**
- Monolithic backend (Spring Boot)
- Monolithic frontend (Next.js)
- Single database (MySQL)

### **Future Scaling Options**
1. **Horizontal Scaling**
   - Load balancer (Nginx)
   - Multiple backend instances
   - Session sharing (Redis)

2. **Microservices** (if needed)
   - Order Service
   - Payment Service
   - Shipping Service
   - Inventory Service

3. **Database Scaling**
   - Read replicas
   - Sharding
   - Caching layer (Redis)

4. **CDN**
   - Static assets
   - Images (Cloudinary)
   - Frontend deployment (Vercel/Netlify)

---

## 🔗 USEFUL LINKS

### **Documentation**
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Next.js Docs](https://nextjs.org/docs)
- [React Docs](https://react.dev)
- [Tailwind CSS Docs](https://tailwindcss.com/docs)
- [MySQL Docs](https://dev.mysql.com/doc/)

### **Tutorials**
- [Spring Boot Tutorial](https://www.baeldung.com/spring-boot)
- [Next.js Tutorial](https://nextjs.org/learn)
- [JWT Authentication](https://jwt.io/introduction)

---

## 📊 TECH STACK SUMMARY

```
Frontend:  Next.js 14 + React 18 + TypeScript + Tailwind CSS
Backend:   Spring Boot 3.5.6 + Java 21 + Spring Security + JWT
Database:  MySQL 8.x + JPA/Hibernate
External:  GHN API + SePay + Cloudinary + SMTP
Tools:     Maven + npm + Git + Swagger
```

**Total Dependencies:**
- Backend: ~20 dependencies
- Frontend: ~15 dependencies
- External Services: 4 integrations

**Lines of Code (estimated):**
- Backend: ~15,000+ lines
- Frontend: ~10,000+ lines
- Total: ~25,000+ lines

---

## 🎓 SKILL REQUIREMENTS

### **Backend Developer**
- ✅ Java 17+ (preferably 21)
- ✅ Spring Boot & Spring Framework
- ✅ Spring Security & JWT
- ✅ JPA/Hibernate
- ✅ MySQL & SQL
- ✅ RESTful API design
- ✅ Maven
- ✅ Git

### **Frontend Developer**
- ✅ JavaScript/TypeScript
- ✅ React 18+
- ✅ Next.js 14+
- ✅ Tailwind CSS
- ✅ State management (Zustand)
- ✅ REST API integration
- ✅ Git

### **Full-Stack Developer**
- ✅ All of the above
- ✅ DevOps basics (deployment)
- ✅ Database design
- ✅ API integration
- ✅ Security best practices

---

**Dự án này sử dụng stack công nghệ hiện đại, phổ biến và production-ready!** 🚀
