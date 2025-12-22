# 🏦 Hướng dẫn Setup SePay - Cổng thanh toán tự động

## Tổng quan
SePay là dịch vụ webhook nhận thông báo giao dịch ngân hàng tự động qua API.

---

## 📋 Bước 1: Đăng ký tài khoản SePay

### 1.1. Truy cập website
- Vào: **https://my.sepay.vn**
- Click **Đăng ký** (nếu chưa có tài khoản)

### 1.2. Điền thông tin đăng ký
```
Email: your-email@example.com
Mật khẩu: ********
Xác nhận mật khẩu: ********
Số điện thoại: 09xxxxxxxx
```

### 1.3. Xác thực email
- Check email và click link xác thực
- Login vào hệ thống

---

## 📋 Bước 2: Liên kết tài khoản ngân hàng

### 2.1. Vào menu "Tài khoản ngân hàng"
- Click **Thêm tài khoản**

### 2.2. Chọn ngân hàng
Các ngân hàng được hỗ trợ:
- ✅ VCB (Vietcombank)
- ✅ TCB (Techcombank)
- ✅ MB (MBBank)
- ✅ ACB (ACB)
- ✅ VPBank
- ✅ TPBank
- ✅ Agribank
- ✅ BIDV
- ✅ VietinBank
- ✅ Sacombank
- ✅ HDBank
- ✅ OCB
- ✅ MSB
- ✅ SHB

### 2.3. Nhập thông tin tài khoản
```
Số tài khoản: 1234567890
Tên chủ tài khoản: NGUYEN VAN A
Ngân hàng: Vietcombank
Chi nhánh: TP. Hồ Chí Minh
```

### 2.4. Xác thực tài khoản
- SePay sẽ yêu cầu bạn chuyển 1 khoản tiền nhỏ (vd: 10,000đ)
- Với nội dung cụ thể để xác thực
- Sau khi chuyển, click "Đã chuyển khoản"

---

## 📋 Bước 3: Lấy API Key và Account Number

### 3.1. Vào menu "API"
- Click **Tạo API Key mới**
- Đặt tên: "E-commerce Website"
- Click **Tạo**

### 3.2. Copy thông tin
```
API Key: SEPAY_API_KEY_xxxxxxxxxxxxxx
Account Number: 1234567890
```

⚠️ **LƯU Ý:** Lưu API Key ngay, không thể xem lại sau này!

---

## 📋 Bước 4: Cấu hình Webhook

### 4.1. Vào menu "Webhook"
- Click **Thêm Webhook**

### 4.2. Nhập thông tin webhook
```
Tên webhook: Payment Notification
URL: https://your-domain.com/api/payment/sepay-webhook
Method: POST
```

### 4.3. Chọn sự kiện
- ✅ Nhận tiền vào tài khoản
- ✅ Chuyển tiền ra

### 4.4. Test webhook
- Click **Test webhook**
- Kiểm tra log backend có nhận được không

---

## 📋 Bước 5: Cấu hình Backend

### 5.1. Thêm vào `application.properties`

```properties
# SePay Configuration
sepay.api.key=SEPAY_API_KEY_xxxxxxxxxxxxxx
sepay.account.number=1234567890
sepay.webhook.secret=your-webhook-secret-key
sepay.api.url=https://my.sepay.vn/userapi
```

### 5.2. Tạo SePay Config Class

Tạo file: `src/main/java/com/doan/WEB_TMDT/config/SepayConfig.java`

```java
package com.doan.WEB_TMDT.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sepay")
public class SepayConfig {
    private Api api = new Api();
    private Account account = new Account();
    private Webhook webhook = new Webhook();
    
    @Data
    public static class Api {
        private String key;
        private String url;
    }
    
    @Data
    public static class Account {
        private String number;
    }
    
    @Data
    public static class Webhook {
        private String secret;
    }
}
```

### 5.3. Tạo SePay Service

Tạo file: `src/main/java/com/doan/WEB_TMDT/module/payment/service/SepayService.java`

```java
package com.doan.WEB_TMDT.module.payment.service;

import com.doan.WEB_TMDT.config.SepayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SepayService {
    
    private final SepayConfig sepayConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Lấy lịch sử giao dịch
     */
    public String getTransactionHistory(int limit) {
        String url = sepayConfig.getApi().getUrl() + "/transactions/list";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + sepayConfig.getApi().getKey());
        
        Map<String, Object> body = new HashMap<>();
        body.put("account_number", sepayConfig.getAccount().getNumber());
        body.put("limit", limit);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting transaction history: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Kiểm tra số dư tài khoản
     */
    public String getBalance() {
        String url = sepayConfig.getApi().getUrl() + "/account/balance";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + sepayConfig.getApi().getKey());
        
        Map<String, Object> body = new HashMap<>();
        body.put("account_number", sepayConfig.getAccount().getNumber());
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting balance: {}", e.getMessage());
            return null;
        }
    }
}
```

### 5.4. Tạo Webhook Controller

Tạo file: `src/main/java/com/doan/WEB_TMDT/module/payment/controller/SepayWebhookController.java`

```java
package com.doan.WEB_TMDT.module.payment.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.config.SepayConfig;
import com.doan.WEB_TMDT.module.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class SepayWebhookController {
    
    private final PaymentService paymentService;
    private final SepayConfig sepayConfig;
    
    /**
     * Webhook nhận thông báo từ SePay
     */
    @PostMapping("/sepay-webhook")
    public ApiResponse handleSepayWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Sepay-Signature", required = false) String signature) {
        
        log.info("Received SePay webhook: {}", payload);
        
        try {
            // 1. Verify signature (nếu có)
            if (signature != null && !verifySignature(payload, signature)) {
                log.warn("Invalid webhook signature");
                return ApiResponse.error("Invalid signature");
            }
            
            // 2. Lấy thông tin giao dịch
            String transactionId = (String) payload.get("id");
            String transferType = (String) payload.get("transfer_type"); // in hoặc out
            Double amount = ((Number) payload.get("amount_in")).doubleValue();
            String description = (String) payload.get("transaction_content");
            
            // 3. Chỉ xử lý giao dịch nhận tiền
            if (!"in".equals(transferType)) {
                log.info("Ignore outgoing transaction: {}", transactionId);
                return ApiResponse.success("Ignored");
            }
            
            // 4. Parse order code từ description
            // Ví dụ: "ORD-20241218-001 thanh toan don hang"
            String orderCode = extractOrderCode(description);
            if (orderCode == null) {
                log.warn("Cannot extract order code from: {}", description);
                return ApiResponse.error("Invalid description");
            }
            
            // 5. Xác nhận thanh toán
            paymentService.confirmPayment(orderCode, amount, transactionId, "SEPAY");
            
            log.info("Payment confirmed for order: {}", orderCode);
            return ApiResponse.success("Payment confirmed");
            
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ApiResponse.error("Processing error");
        }
    }
    
    /**
     * Verify webhook signature
     */
    private boolean verifySignature(Map<String, Object> payload, String signature) {
        // TODO: Implement signature verification
        // Sử dụng webhook secret để verify
        return true;
    }
    
    /**
     * Extract order code từ transaction content
     */
    private String extractOrderCode(String content) {
        if (content == null) return null;
        
        // Pattern: ORD-YYYYMMDD-XXX
        String pattern = "ORD-\\d{8}-\\d{3}";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(content);
        
        if (m.find()) {
            return m.group();
        }
        
        return null;
    }
}
```

---

## 📋 Bước 6: Test với Ngrok

### 6.1. Chạy ngrok
```cmd
ngrok http 8080
```

Copy URL: `https://xxxx-xxxx-xxxx.ngrok-free.app`

### 6.2. Cập nhật webhook URL trên SePay
```
URL: https://xxxx-xxxx-xxxx.ngrok-free.app/api/payment/sepay-webhook
```

### 6.3. Test chuyển khoản
```
Số tài khoản: [Số TK đã liên kết]
Số tiền: 100,000đ
Nội dung: ORD-20241218-001 thanh toan don hang
```

### 6.4. Kiểm tra log
```
Backend log sẽ hiển thị:
- Received SePay webhook: {...}
- Payment confirmed for order: ORD-20241218-001
```

---

## 📋 Bước 7: Cấu hình Frontend

### 7.1. Thêm SePay vào payment options

File: `src/frontend/app/checkout/page.tsx`

```typescript
const paymentMethods = [
  {
    id: 'BANK_TRANSFER',
    name: 'Chuyển khoản ngân hàng',
    description: 'Chuyển khoản qua SePay - Tự động xác nhận',
    icon: '🏦'
  },
  // ... other methods
];
```

### 7.2. Hiển thị thông tin chuyển khoản

```typescript
{selectedPayment === 'BANK_TRANSFER' && (
  <div className="mt-4 p-4 bg-blue-50 rounded-lg">
    <h4 className="font-semibold mb-2">Thông tin chuyển khoản:</h4>
    <div className="space-y-2">
      <p><strong>Ngân hàng:</strong> Vietcombank</p>
      <p><strong>Số tài khoản:</strong> 1234567890</p>
      <p><strong>Chủ tài khoản:</strong> NGUYEN VAN A</p>
      <p><strong>Số tiền:</strong> {total.toLocaleString('vi-VN')}đ</p>
      <p><strong>Nội dung:</strong> {orderCode} thanh toan don hang</p>
    </div>
    <p className="mt-3 text-sm text-blue-600">
      ⚡ Thanh toán sẽ được xác nhận tự động sau khi chuyển khoản
    </p>
  </div>
)}
```

---

## 🔒 Bảo mật

### 1. Verify Webhook Signature
```java
private boolean verifySignature(Map<String, Object> payload, String signature) {
    String secret = sepayConfig.getWebhook().getSecret();
    String data = new ObjectMapper().writeValueAsString(payload);
    
    String expectedSignature = HmacUtils.hmacSha256Hex(secret, data);
    
    return signature.equals(expectedSignature);
}
```

### 2. Whitelist IP
Chỉ cho phép IP của SePay:
```java
@Component
public class SepayIpFilter implements Filter {
    private static final List<String> ALLOWED_IPS = Arrays.asList(
        "103.xx.xx.xx", // SePay IP
        "127.0.0.1"     // Localhost for testing
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String remoteIp = request.getRemoteAddr();
        if (!ALLOWED_IPS.contains(remoteIp)) {
            throw new SecurityException("Unauthorized IP: " + remoteIp);
        }
        chain.doFilter(request, response);
    }
}
```

### 3. Idempotency
Tránh xử lý trùng webhook:
```java
@Service
public class PaymentService {
    private final Set<String> processedTransactions = new ConcurrentHashMap().newKeySet();
    
    public void confirmPayment(String orderCode, Double amount, String transactionId, String gateway) {
        if (processedTransactions.contains(transactionId)) {
            log.warn("Transaction already processed: {}", transactionId);
            return;
        }
        
        // Process payment...
        
        processedTransactions.add(transactionId);
    }
}
```

---

## 📊 Monitoring

### 1. Log tất cả webhook
```java
@PostMapping("/sepay-webhook")
public ApiResponse handleSepayWebhook(@RequestBody Map<String, Object> payload) {
    // Save to database for audit
    webhookLogRepository.save(WebhookLog.builder()
        .payload(new ObjectMapper().writeValueAsString(payload))
        .receivedAt(LocalDateTime.now())
        .build());
    
    // Process...
}
```

### 2. Alert khi có lỗi
```java
if (error) {
    // Send email/SMS alert
    alertService.sendAlert("SePay webhook error: " + error.getMessage());
}
```

---

## 🎯 Checklist

- [ ] Đăng ký tài khoản SePay
- [ ] Liên kết tài khoản ngân hàng
- [ ] Lấy API Key
- [ ] Cấu hình Webhook
- [ ] Thêm config vào application.properties
- [ ] Tạo SepayService
- [ ] Tạo WebhookController
- [ ] Setup ngrok
- [ ] Test chuyển khoản
- [ ] Verify signature
- [ ] Whitelist IP
- [ ] Setup monitoring

---

## 📞 Hỗ trợ

- Website: https://my.sepay.vn
- Email: support@sepay.vn
- Hotline: 1900 xxxx
- Telegram: @sepay_support

---

**✅ Hoàn thành setup SePay!**

Bây giờ hệ thống có thể nhận thanh toán tự động qua chuyển khoản ngân hàng! 🎉
