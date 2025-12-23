# ✅ Checklist Liên Kết Module Kế Toán

## 🔗 1. Giao Dịch Tài Chính (Financial Transactions)

### Liên kết hiện tại:
- ✅ Tạo giao dịch thủ công qua modal
- ✅ Liên kết với `orderId` (mã đơn hàng)
- ✅ Liên kết với `supplierId` (mã nhà cung cấp)

### ⚠️ Cần bổ sung:
- ❌ **Tự động tạo giao dịch khi đơn hàng được thanh toán**
  - Khi order status = PAID → Tạo transaction type=REVENUE, category=SALES
  - Số tiền = order.total
  - Mô tả = "Doanh thu từ đơn hàng #[orderId]"

- ❌ **Tự động tạo giao dịch khi thanh toán NCC**
  - Khi nhập kho và thanh toán → Tạo transaction type=EXPENSE, category=SUPPLIER_PAYMENT
  - Số tiền = tổng tiền nhập kho
  - Liên kết supplierId

- ❌ **Tự động tạo giao dịch phí vận chuyển**
  - Khi đơn hàng hoàn thành → Tạo transaction type=EXPENSE, category=SHIPPING
  - Số tiền = order.shippingFee * 0.8 (80% là chi phí thực tế)

- ❌ **Tự động tạo giao dịch phí cổng thanh toán**
  - Khi thanh toán online → Tạo transaction type=EXPENSE, category=PAYMENT_FEE
  - Số tiền = order.total * 0.02 (2% phí cổng thanh toán)

### 📝 Code cần thêm:
```java
// Trong OrderService - khi cập nhật trạng thái đơn hàng
@Autowired
private FinancialTransactionService financialTransactionService;

public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.setStatus(newStatus);
    
    // Tự động tạo giao dịch khi đơn hàng được thanh toán
    if (newStatus == OrderStatus.CONFIRMED && order.getPaymentStatus() == PaymentStatus.PAID) {
        createRevenueTransaction(order);
        createShippingExpenseTransaction(order);
        if (order.getPaymentMethod().equals("ONLINE")) {
            createPaymentFeeTransaction(order);
        }
    }
    
    orderRepository.save(order);
}

private void createRevenueTransaction(Order order) {
    FinancialTransactionRequest request = new FinancialTransactionRequest();
    request.setType(TransactionType.REVENUE);
    request.setCategory(TransactionCategory.SALES);
    request.setAmount(order.getTotal());
    request.setOrderId(order.getId());
    request.setDescription("Doanh thu từ đơn hàng #" + order.getId());
    request.setTransactionDate(LocalDateTime.now());
    
    financialTransactionService.createTransaction(request, "SYSTEM");
}
```

---

## 🔗 2. Kỳ Kế Toán (Accounting Periods)

### Liên kết hiện tại:
- ✅ Tính tổng doanh thu từ orders trong kỳ
- ✅ Tính sai số giữa hệ thống và thực tế

### ⚠️ Cần bổ sung:
- ❌ **Tự động tạo kỳ kế toán hàng tháng**
  - Scheduled job chạy vào ngày 1 hàng tháng
  - Tạo kỳ cho tháng trước (từ ngày 1 đến ngày cuối tháng)

- ❌ **Tính toán chi tiết hơn**
  - Tổng doanh thu = SUM(transactions WHERE type=REVENUE AND category=SALES)
  - Tổng chi phí = SUM(transactions WHERE type=EXPENSE)
  - Lợi nhuận gộp = Doanh thu - Chi phí
  - So sánh với doanh thu từ orders để tính sai số

### 📝 Code cần thêm:
```java
@Scheduled(cron = "0 0 0 1 * ?") // Chạy vào 00:00 ngày 1 hàng tháng
public void createMonthlyPeriod() {
    LocalDate lastMonth = LocalDate.now().minusMonths(1);
    LocalDate startDate = lastMonth.withDayOfMonth(1);
    LocalDate endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
    
    AccountingPeriod period = new AccountingPeriod();
    period.setName("Kỳ " + lastMonth.getMonthValue() + "/" + lastMonth.getYear());
    period.setStartDate(startDate.atStartOfDay());
    period.setEndDate(endDate.atTime(23, 59, 59));
    period.setStatus(PeriodStatus.OPEN);
    
    // Tính toán tự động
    calculatePeriodData(period);
    
    accountingPeriodRepository.save(period);
}
```

---

## 🔗 3. Quản Lý Thuế (Tax Management)

### Liên kết hiện tại:
- ✅ Tạo báo cáo thuế thủ công
- ✅ Tính số thuế dựa trên doanh thu chịu thuế

### ⚠️ Cần bổ sung:
- ❌ **Tự động tính doanh thu chịu thuế**
  - Lấy từ transactions: SUM(amount WHERE type=REVENUE AND category=SALES)
  - Hoặc lấy từ orders: SUM(total WHERE status=COMPLETED AND paymentStatus=PAID)

- ❌ **Tự động tạo báo cáo thuế hàng tháng/quý**
  - VAT: Báo cáo hàng tháng
  - Thuế TNDN: Báo cáo hàng quý

### 📝 Code cần thêm:
```java
public TaxReport createTaxReportFromPeriod(Long periodId) {
    AccountingPeriod period = periodRepository.findById(periodId).orElseThrow();
    
    // Tính doanh thu chịu thuế từ transactions
    Double taxableRevenue = transactionRepository
        .sumAmountByTypeAndCategoryAndDateBetween(
            TransactionType.REVENUE,
            TransactionCategory.SALES,
            period.getStartDate(),
            period.getEndDate()
        );
    
    TaxReport report = new TaxReport();
    report.setTaxType(TaxType.VAT);
    report.setPeriodStart(period.getStartDate());
    report.setPeriodEnd(period.getEndDate());
    report.setTaxableRevenue(taxableRevenue);
    report.setTaxRate(10.0); // VAT 10%
    report.setTaxAmount(taxableRevenue * 0.1);
    report.setRemainingTax(taxableRevenue * 0.1);
    
    return taxReportRepository.save(report);
}
```

---

## 🔗 4. Báo Cáo Nâng Cao (Advanced Reports)

### Liên kết hiện tại:
- ✅ Báo cáo lãi lỗ
- ✅ Báo cáo dòng tiền
- ✅ Phân tích chi phí

### ⚠️ Cần bổ sung:
- ❌ **Lấy dữ liệu từ transactions thay vì tính toán giả**
  - Doanh thu = SUM(transactions WHERE type=REVENUE)
  - Chi phí vận chuyển = SUM(transactions WHERE category=SHIPPING)
  - Phí cổng thanh toán = SUM(transactions WHERE category=PAYMENT_FEE)
  - Chi phí NCC = SUM(transactions WHERE category=SUPPLIER_PAYMENT)

### 📝 Code cần cập nhật:
```java
public ProfitLossReport generateProfitLossReport(String startDate, String endDate) {
    LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
    LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
    
    // Lấy từ transactions thực tế
    Double salesRevenue = transactionRepository
        .sumByTypeAndCategoryAndDateBetween(
            TransactionType.REVENUE, 
            TransactionCategory.SALES, 
            start, end
        );
    
    Double shippingCosts = transactionRepository
        .sumByTypeAndCategoryAndDateBetween(
            TransactionType.EXPENSE, 
            TransactionCategory.SHIPPING, 
            start, end
        );
    
    Double paymentFees = transactionRepository
        .sumByTypeAndCategoryAndDateBetween(
            TransactionType.EXPENSE, 
            TransactionCategory.PAYMENT_FEE, 
            start, end
        );
    
    // Tính toán
    Double totalRevenue = salesRevenue;
    Double totalCosts = shippingCosts + paymentFees;
    Double grossProfit = totalRevenue - totalCosts;
    Double vatAmount = totalRevenue * 0.1;
    Double netProfit = grossProfit - vatAmount;
    
    return ProfitLossReport.builder()
        .salesRevenue(salesRevenue)
        .shippingCosts(shippingCosts)
        .paymentFees(paymentFees)
        .totalRevenue(totalRevenue)
        .grossProfit(grossProfit)
        .vatAmount(vatAmount)
        .netProfit(netProfit)
        .build();
}
```

---

## 🔗 5. Đối Soát Vận Chuyển (Shipping Reconciliation)

### Liên kết hiện tại:
- ✅ Lấy dữ liệu từ orders
- ✅ Tính phí vận chuyển thu từ khách
- ✅ Tính chi phí thực tế (80% phí thu)

### ⚠️ Cần bổ sung:
- ❌ **Liên kết với GHN API để lấy chi phí thực tế**
  - Thay vì tính 80%, lấy từ GHN actual cost
  - Cập nhật khi có webhook từ GHN

- ❌ **Tạo transaction tự động cho chi phí vận chuyển**
  - Khi đơn hàng hoàn thành → Tạo transaction EXPENSE/SHIPPING

---

## 🔗 6. Công Nợ NCC (Supplier Payables)

### Liên kết hiện tại:
- ✅ Quản lý công nợ nhà cung cấp
- ✅ Theo dõi đã trả/chưa trả

### ⚠️ Cần bổ sung:
- ❌ **Tự động tạo công nợ khi nhập kho**
  - Khi tạo phiếu nhập kho → Tạo SupplierPayable
  - Số tiền = tổng tiền nhập kho
  - Trạng thái = UNPAID

- ❌ **Tự động cập nhật khi thanh toán**
  - Khi tạo transaction SUPPLIER_PAYMENT → Cập nhật payable status = PAID
  - Cập nhật paidAmount và paidDate

---

## 📊 Tổng Kết Các Liên Kết Cần Thiết

### 1. Order → Financial Transaction
```
Order PAID → Create Transaction (REVENUE/SALES)
Order COMPLETED → Create Transaction (EXPENSE/SHIPPING)
Order ONLINE_PAYMENT → Create Transaction (EXPENSE/PAYMENT_FEE)
```

### 2. Warehouse Import → Supplier Payable & Transaction
```
Warehouse Import → Create SupplierPayable (UNPAID)
Payment to Supplier → Create Transaction (EXPENSE/SUPPLIER_PAYMENT)
                   → Update SupplierPayable (PAID)
```

### 3. Accounting Period → Transactions
```
Period Calculation → SUM(Transactions in period)
Period Close → Lock all transactions in period
```

### 4. Tax Report → Transactions
```
Tax Report Creation → Calculate from Transactions (REVENUE/SALES)
Tax Payment → Create Transaction (EXPENSE/TAX)
```

### 5. Advanced Reports → Transactions
```
All Reports → Query from Transactions table
Real-time data → No hardcoded values
```

---

## 🚀 Kế Hoạch Triển Khai

### Phase 1: Tự động hóa giao dịch cơ bản (Ưu tiên cao)
1. ✅ Tạo transaction khi order được thanh toán
2. ✅ Tạo transaction chi phí vận chuyển
3. ✅ Tạo transaction phí cổng thanh toán

### Phase 2: Liên kết với kho và NCC (Ưu tiên trung bình)
4. ✅ Tự động tạo công nợ NCC khi nhập kho
5. ✅ Liên kết thanh toán NCC với transactions

### Phase 3: Tự động hóa báo cáo (Ưu tiên thấp)
6. ✅ Scheduled job tạo kỳ kế toán hàng tháng
7. ✅ Scheduled job tạo báo cáo thuế hàng tháng/quý
8. ✅ Cập nhật advanced reports lấy dữ liệu thực

---

## 📝 Repository Methods Cần Thêm

```java
// FinancialTransactionRepository
Double sumByTypeAndCategoryAndDateBetween(
    TransactionType type, 
    TransactionCategory category,
    LocalDateTime start, 
    LocalDateTime end
);

List<FinancialTransaction> findByOrderId(Long orderId);
List<FinancialTransaction> findBySupplierId(Long supplierId);

// OrderRepository  
List<Order> findByStatusAndPaymentStatusAndCreatedAtBetween(
    OrderStatus status,
    PaymentStatus paymentStatus,
    LocalDateTime start,
    LocalDateTime end
);

// SupplierPayableRepository
List<SupplierPayable> findBySupplierIdAndStatus(Long supplierId, PayableStatus status);
```

---

## ✅ Checklist Kiểm Tra

- [ ] Khi tạo đơn hàng và thanh toán → Có tạo transaction REVENUE không?
- [ ] Khi đơn hàng hoàn thành → Có tạo transaction SHIPPING không?
- [ ] Khi nhập kho → Có tạo SupplierPayable không?
- [ ] Khi thanh toán NCC → Có tạo transaction và cập nhật payable không?
- [ ] Báo cáo lãi lỗ → Có lấy dữ liệu từ transactions không?
- [ ] Báo cáo thuế → Có tính từ doanh thu thực tế không?
- [ ] Kỳ kế toán → Có tính từ transactions trong kỳ không?
- [ ] Đối soát vận chuyển → Có lấy từ orders thực tế không?

---

## 🎯 Kết Luận

Hiện tại module kế toán đã có:
- ✅ UI/UX hoàn chỉnh
- ✅ CRUD operations đầy đủ
- ✅ Security và phân quyền

Cần bổ sung:
- ⚠️ Tự động hóa tạo giao dịch từ orders
- ⚠️ Liên kết với warehouse và suppliers
- ⚠️ Scheduled jobs cho báo cáo định kỳ
- ⚠️ Lấy dữ liệu thực từ transactions thay vì hardcode

**Ưu tiên**: Triển khai Phase 1 trước để đảm bảo dữ liệu kế toán chính xác!
