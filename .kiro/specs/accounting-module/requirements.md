# Tài Liệu Yêu Cầu - Module Kế Toán

## Giới Thiệu

Module Kế Toán là hệ thống quản lý tài chính toàn diện cho nền tảng thương mại điện tử. Module này quản lý tất cả các giao dịch tài chính, công nợ nhà cung cấp, đối soát thanh toán và báo cáo thuế. Module tự động theo dõi doanh thu từ các đơn hàng đã hoàn thành, chi phí từ hoạt động kho hàng, và cung cấp báo cáo tài chính chi tiết cho nhân viên kế toán.

## Bảng Thuật Ngữ

- **Hệ_Thống**: Module Kế Toán của nền tảng thương mại điện tử
- **Kế_Toán_Viên**: Người dùng có vai trò ACCOUNTANT, quản lý các hoạt động tài chính
- **Quản_Trị_Viên**: Người dùng có vai trò ADMIN, có quyền truy cập đầy đủ tất cả tính năng
- **Nhân_Viên**: Người dùng có vai trò EMPLOYEE, chỉ có quyền xem các báo cáo cơ bản
- **Giao_Dịch_Tài_Chính**: Bản ghi về dòng tiền vào (doanh thu) hoặc ra (chi phí) của doanh nghiệp
- **Kỳ_Kế_Toán**: Khoảng thời gian (tháng/quý/năm) để tạo báo cáo tài chính
- **Công_Nợ_NCC**: Số tiền phải trả cho nhà cung cấp cho hàng hóa đã mua
- **Báo_Cáo_Thuế**: Báo cáo tính toán số thuế phải nộp cho chính phủ
- **Đối_Soát_Thanh_Toán**: Quá trình đối chiếu bản ghi thanh toán trong hệ thống với bản ghi từ cổng thanh toán

## Các Yêu Cầu

### Yêu Cầu 1: Quản Lý Giao Dịch Tài Chính

**User Story:** Là một kế toán viên, tôi muốn xem và quản lý tất cả các giao dịch tài chính, để tôi có thể theo dõi dòng tiền vào và ra của doanh nghiệp.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-001 | KHI một giao dịch được tạo, THÌ Hệ_Thống PHẢI tự động gán mã giao dịch duy nhất theo định dạng: TXN + năm tháng ngày + số thứ tự (VD: TXN202512280001). | High |
| REQ-ACC-002 | KHI một đơn hàng chuyển sang trạng thái DELIVERED, THÌ Hệ_Thống PHẢI tự động tạo giao dịch doanh thu (REVENUE). | High |
| REQ-ACC-003 | KHI thanh toán cho nhà cung cấp được thực hiện, THÌ Hệ_Thống PHẢI tự động tạo giao dịch chi phí (EXPENSE). | High |
| REQ-ACC-004 | KHI xem giao dịch, THÌ Hệ_Thống PHẢI hiển thị: mã giao dịch, loại (REVENUE/EXPENSE/REFUND), danh mục, số tiền, ngày giao dịch, mô tả. | High |
| REQ-ACC-005 | Hệ_Thống PHẢI cho phép lọc giao dịch theo: loại giao dịch, danh mục, khoảng thời gian, mã đơn hàng, mã nhà cung cấp. | Medium |
| REQ-ACC-006 | Hệ_Thống PHẢI tự động tính tổng doanh thu bằng cách cộng tất cả giao dịch loại REVENUE trong khoảng thời gian được chọn. | High |
| REQ-ACC-007 | Hệ_Thống PHẢI tự động tính tổng chi phí bằng cách cộng tất cả giao dịch loại EXPENSE trong khoảng thời gian được chọn. | High |

### Yêu Cầu 2: Quản Lý Kỳ Kế Toán

**User Story:** Là một kế toán viên, tôi muốn quản lý các kỳ kế toán, để tôi có thể tổ chức dữ liệu tài chính theo khoảng thời gian và ngăn chặn sửa đổi các kỳ đã đóng.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-008 | KHI tạo một kỳ, THÌ Hệ_Thống PHẢI yêu cầu các thông tin: tên kỳ, ngày bắt đầu, ngày kết thúc. | High |
| REQ-ACC-009 | KHI một kỳ được tạo, THÌ Hệ_Thống PHẢI tự động đặt trạng thái của kỳ là MỞ. | High |
| REQ-ACC-010 | KHI một kỳ đang ở trạng thái MỞ, THÌ Hệ_Thống PHẢI cho phép sửa đổi các giao dịch trong kỳ đó. | High |
| REQ-ACC-011 | KHI đóng một kỳ, THÌ Hệ_Thống PHẢI tự động tính tổng doanh thu, tổng chi phí và lợi nhuận ròng. | High |
| REQ-ACC-012 | KHI một kỳ đã ĐÓNG, THÌ Hệ_Thống PHẢI ngăn chặn sửa đổi các giao dịch trong kỳ đó. | High |
| REQ-ACC-013 | Hệ_Thống PHẢI tính lợi nhuận ròng theo công thức: Lợi nhuận ròng = Tổng doanh thu - Tổng chi phí. | High |
| REQ-ACC-014 | Hệ_Thống PHẢI chỉ cho phép một kỳ kế toán ở trạng thái MỞ tại một thời điểm. | High |

### Yêu Cầu 3: Báo Cáo Tài Chính

**User Story:** Là một kế toán viên, tôi muốn xem các báo cáo tài chính, để tôi có thể hiểu được hiệu suất tài chính của doanh nghiệp.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-015 | KHI xem báo cáo tài chính, THÌ Hệ_Thống PHẢI hiển thị: tổng doanh thu, tổng chi phí, lợi nhuận ròng, lợi nhuận gộp. | High |
| REQ-ACC-016 | KHI tạo báo cáo cho một kỳ, THÌ Hệ_Thống PHẢI bao gồm tất cả giao dịch trong khoảng thời gian của kỳ đó. | High |
| REQ-ACC-017 | Hệ_Thống PHẢI tính lợi nhuận gộp theo công thức: Lợi nhuận gộp = Doanh thu - Giá vốn hàng bán. | High |
| REQ-ACC-018 | Hệ_Thống PHẢI hiển thị phân tích doanh thu theo danh mục: bán hàng, vận chuyển, khác. | Medium |
| REQ-ACC-019 | Hệ_Thống PHẢI hiển thị phân tích chi phí theo danh mục: thanh toán NCC, chi phí vận chuyển, thuế, khác. | Medium |
| REQ-ACC-020 | Hệ_Thống PHẢI cho phép xuất báo cáo tài chính ra định dạng Excel hoặc PDF. | Medium |
| REQ-ACC-021 | Hệ_Thống PHẢI hiển thị dashboard tổng quan với: doanh thu tháng hiện tại, lợi nhuận, công nợ phải trả, thuế phải nộp. | High |

### Yêu Cầu 4: Quản Lý Công Nợ Nhà Cung Cấp

**User Story:** Là một kế toán viên, tôi muốn theo dõi số tiền nợ nhà cung cấp, để tôi có thể đảm bảo thanh toán đúng hạn và duy trì mối quan hệ tốt với nhà cung cấp.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-022 | KHI một đơn mua hàng (Purchase Order) chuyển sang trạng thái RECEIVED, THÌ Hệ_Thống PHẢI tự động tạo công nợ nhà cung cấp. | High |
| REQ-ACC-023 | KHI tạo công nợ, THÌ Hệ_Thống PHẢI ghi lại: tổng số tiền, nhà cung cấp, ngày hóa đơn, ngày đến hạn thanh toán. | High |
| REQ-ACC-024 | KHI thanh toán được thực hiện, THÌ Hệ_Thống PHẢI tự động cập nhật số tiền đã trả và số tiền còn lại. | High |
| REQ-ACC-025 | KHI số tiền còn lại bằng 0, THÌ Hệ_Thống PHẢI tự động chuyển trạng thái công nợ sang ĐÃ_TRẢ. | High |
| REQ-ACC-026 | KHI ngày đến hạn đã qua và trạng thái không phải ĐÃ_TRẢ, THÌ Hệ_Thống PHẢI tự động đánh dấu công nợ là QUÁ_HẠN. | High |
| REQ-ACC-027 | Hệ_Thống PHẢI hiển thị danh sách công nợ với: tên nhà cung cấp, tổng số tiền, số tiền đã trả, số tiền còn lại, trạng thái, ngày đến hạn. | High |
| REQ-ACC-028 | Hệ_Thống PHẢI cho phép lọc công nợ theo: nhà cung cấp, trạng thái (CHƯA_TRẢ/MỘT_PHẦN/ĐÃ_TRẢ/QUÁ_HẠN), khoảng thời gian. | Medium |
| REQ-ACC-029 | Hệ_Thống PHẢI tính tổng công nợ phải trả cho từng nhà cung cấp. | Medium |
| REQ-ACC-030 | Hệ_Thống PHẢI tạo báo cáo phân tích công nợ theo số ngày quá hạn (aging report). | Medium |

### Yêu Cầu 5: Xử Lý Thanh Toán Nhà Cung Cấp

**User Story:** Là một kế toán viên, tôi muốn ghi lại các khoản thanh toán cho nhà cung cấp, để tôi có thể theo dõi lịch sử thanh toán và cập nhật số dư công nợ.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-031 | KHI tạo thanh toán, THÌ Hệ_Thống PHẢI yêu cầu: mã công nợ, số tiền thanh toán, ngày thanh toán, phương thức thanh toán. | High |
| REQ-ACC-032 | KHI một thanh toán được tạo, THÌ Hệ_Thống PHẢI tự động gán mã thanh toán duy nhất theo định dạng: PAY + năm tháng ngày + số thứ tự (VD: PAY202512280001). | High |
| REQ-ACC-033 | KHI một thanh toán được tạo, THÌ Hệ_Thống PHẢI tự động cập nhật số tiền đã trả của công nợ. | High |
| REQ-ACC-034 | KHI một thanh toán được tạo, THÌ Hệ_Thống PHẢI tự động tính lại số tiền còn lại của công nợ. | High |
| REQ-ACC-035 | KHI một thanh toán được tạo, THÌ Hệ_Thống PHẢI tự động tạo giao dịch tài chính loại EXPENSE. | High |
| REQ-ACC-036 | Hệ_Thống PHẢI xác thực rằng số tiền thanh toán không vượt quá số tiền công nợ còn lại. | High |
| REQ-ACC-037 | Hệ_Thống PHẢI cho phép thanh toán một phần (số tiền nhỏ hơn số tiền còn lại). | High |
| REQ-ACC-038 | Hệ_Thống PHẢI hiển thị lịch sử thanh toán cho mỗi công nợ với: mã thanh toán, số tiền, ngày thanh toán, phương thức. | Medium |

### Yêu Cầu 6: Quản Lý Báo Cáo Thuế

**User Story:** Là một kế toán viên, tôi muốn tạo và quản lý báo cáo thuế, để tôi có thể tính toán số thuế phải nộp và theo dõi việc nộp thuế.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-039 | KHI tạo báo cáo thuế, THÌ Hệ_Thống PHẢI yêu cầu: loại thuế (VAT/THUẾ_TNDN), ngày bắt đầu kỳ, ngày kết thúc kỳ, doanh thu chịu thuế. | High |
| REQ-ACC-040 | KHI loại thuế là VAT, THÌ Hệ_Thống PHẢI tự động tính số thuế = Doanh thu chịu thuế × 10%. | High |
| REQ-ACC-041 | KHI loại thuế là THUẾ_TNDN, THÌ Hệ_Thống PHẢI tự động tính số thuế = Doanh thu chịu thuế × 20%. | High |
| REQ-ACC-042 | Hệ_Thống PHẢI tự động tính doanh thu chịu thuế từ các giao dịch tài chính trong khoảng thời gian được chọn. | High |
| REQ-ACC-043 | ĐỐI VỚI báo cáo VAT, THÌ Hệ_Thống PHẢI sử dụng tổng doanh thu (REVENUE) làm doanh thu chịu thuế. | High |
| REQ-ACC-044 | ĐỐI VỚI báo cáo THUẾ_TNDN, THÌ Hệ_Thống PHẢI sử dụng lợi nhuận (Doanh thu - Chi phí) làm doanh thu chịu thuế. | High |
| REQ-ACC-045 | KHI một báo cáo được tạo, THÌ Hệ_Thống PHẢI tự động đặt trạng thái là NHÁP. | High |
| REQ-ACC-046 | KHI kế toán viên nộp báo cáo, THÌ Hệ_Thống PHẢI chuyển trạng thái sang ĐÃ_NỘP. | High |
| REQ-ACC-047 | KHI kế toán viên đánh dấu đã thanh toán, THÌ Hệ_Thống PHẢI chuyển trạng thái sang ĐÃ_TRẢ và ghi lại số tiền đã trả. | High |
| REQ-ACC-048 | Hệ_Thống PHẢI ngăn chặn chỉnh sửa báo cáo thuế khi trạng thái là ĐÃ_NỘP hoặc ĐÃ_TRẢ. | High |
| REQ-ACC-049 | Hệ_Thống PHẢI tự động tạo mã báo cáo thuế duy nhất theo định dạng: [Loại thuế]-[Tháng][Năm] (VD: VAT-122025). | High |

### Yêu Cầu 7: Tính Thuế Tự Động

**User Story:** Là một kế toán viên, tôi muốn hệ thống tự động tính doanh thu chịu thuế, để tôi có thể tiết kiệm thời gian và giảm lỗi thủ công.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-050 | KHI tính doanh thu chịu thuế, THÌ Hệ_Thống PHẢI truy vấn các giao dịch tài chính trong khoảng thời gian được chỉ định. | High |
| REQ-ACC-051 | KHI tính doanh thu chịu thuế VAT, THÌ Hệ_Thống PHẢI cộng tất cả giao dịch loại DOANH_THU (REVENUE). | High |
| REQ-ACC-052 | KHI tính doanh thu chịu thuế THUẾ_TNDN, THÌ Hệ_Thống PHẢI tính lợi nhuận = Doanh thu - Chi phí. | High |
| REQ-ACC-053 | Hệ_Thống PHẢI cung cấp API endpoint để tính doanh thu chịu thuế cho khoảng thời gian được chỉ định. | High |
| REQ-ACC-054 | Hệ_Thống PHẢI cung cấp API endpoint để tự động tạo báo cáo thuế với các giá trị đã tính toán. | High |
| REQ-ACC-055 | KHI tự động tạo báo cáo, THÌ Hệ_Thống PHẢI xác thực không có báo cáo thuế trùng lặp (cùng kỳ và loại thuế). | High |
| REQ-ACC-056 | Hệ_Thống PHẢI hiển thị giá trị doanh thu chịu thuế đã tính tự động trong form tạo báo cáo thuế. | Medium |
| REQ-ACC-057 | Hệ_Thống PHẢI cung cấp nút "Tính toán tự động" để tự động điền doanh thu chịu thuế trong form tạo báo cáo. | Medium |

### Yêu Cầu 8: Đối Soát Thanh Toán

**User Story:** Là một kế toán viên, tôi muốn đối soát bản ghi cổng thanh toán với bản ghi hệ thống, để tôi có thể xác định và giải quyết các sai lệch thanh toán.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-058 | KHI đối soát một thanh toán, THÌ Hệ_Thống PHẢI so sánh số tiền hệ thống với số tiền cổng thanh toán. | Medium |
| REQ-ACC-059 | KHI số tiền khớp, THÌ Hệ_Thống PHẢI đánh dấu trạng thái đối soát là KHỚP. | Medium |
| REQ-ACC-060 | KHI số tiền khác nhau, THÌ Hệ_Thống PHẢI đánh dấu trạng thái đối soát là KHÔNG_KHỚP. | Medium |
| REQ-ACC-061 | KHI số tiền khác nhau, THÌ Hệ_Thống PHẢI tính sai lệch = Số tiền cổng thanh toán - Số tiền hệ thống. | Medium |
| REQ-ACC-062 | Hệ_Thống PHẢI cho phép xem danh sách đối soát với bộ lọc theo trạng thái và cổng thanh toán. | Medium |
| REQ-ACC-063 | Hệ_Thống PHẢI cho phép thêm ghi chú để giải quyết sai lệch đối soát. | Medium |
| REQ-ACC-064 | Hệ_Thống PHẢI hiển thị thông tin đối soát: mã đơn hàng, mã giao dịch, cổng thanh toán, số tiền hệ thống, số tiền cổng, sai lệch, trạng thái. | Medium |

### Yêu Cầu 9: Phân Quyền và Kiểm Soát Truy Cập

**User Story:** Là một quản trị viên hệ thống, tôi muốn kiểm soát quyền truy cập vào các tính năng kế toán, để chỉ những người dùng được ủy quyền mới có thể xem hoặc sửa đổi dữ liệu tài chính.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-065 | Hệ_Thống PHẢI cho phép người dùng ADMIN truy cập đầy đủ tất cả tính năng kế toán. | High |
| REQ-ACC-066 | Hệ_Thống PHẢI cho phép người dùng ACCOUNTANT truy cập đầy đủ tất cả tính năng kế toán. | High |
| REQ-ACC-067 | Hệ_Thống PHẢI cho phép người dùng EMPLOYEE chỉ xem các báo cáo tài chính cơ bản (chế độ chỉ đọc). | High |
| REQ-ACC-068 | Hệ_Thống PHẢI ngăn người dùng EMPLOYEE tạo, cập nhật hoặc xóa bản ghi tài chính. | High |
| REQ-ACC-069 | Hệ_Thống PHẢI yêu cầu xác thực (authentication) cho tất cả API endpoint kế toán. | High |
| REQ-ACC-070 | Hệ_Thống PHẢI xác thực vai trò người dùng (authorization) trước khi cho phép truy cập các endpoint được bảo vệ. | High |

### Yêu Cầu 10: Ghi Log Kiểm Toán

**User Story:** Là một kế toán viên, tôi muốn theo dõi ai đã thực hiện thay đổi đối với bản ghi tài chính, để tôi có thể duy trì trách nhiệm giải trình và dấu vết kiểm toán.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-071 | KHI một giao dịch được tạo, THÌ Hệ_Thống PHẢI ghi lại tên người tạo (createdBy). | Medium |
| REQ-ACC-072 | KHI một kỳ được đóng, THÌ Hệ_Thống PHẢI ghi lại người đóng kỳ (closedBy) và thời gian đóng (closedAt). | Medium |
| REQ-ACC-073 | KHI một báo cáo thuế được nộp, THÌ Hệ_Thống PHẢI ghi lại thời gian nộp (submittedAt). | Medium |
| REQ-ACC-074 | KHI một thanh toán được thực hiện, THÌ Hệ_Thống PHẢI ghi lại tên người tạo (createdBy). | Medium |
| REQ-ACC-075 | Hệ_Thống PHẢI lưu trữ thời gian tạo (createdAt) cho tất cả bản ghi tài chính. | Medium |
| REQ-ACC-076 | Hệ_Thống PHẢI lưu trữ thời gian cập nhật (updatedAt) cho tất cả bản ghi tài chính có thể sửa đổi. | Medium |

### Yêu Cầu 11: Xác Thực Dữ Liệu

**User Story:** Là một kế toán viên, tôi muốn hệ thống xác thực dữ liệu tài chính, để tôi có thể ngăn chặn lỗi và duy trì tính toàn vẹn dữ liệu.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-077 | KHI tạo một giao dịch, THÌ Hệ_Thống PHẢI xác thực số tiền phải lớn hơn 0. | High |
| REQ-ACC-078 | KHI tạo một thanh toán, THÌ Hệ_Thống PHẢI xác thực số tiền không vượt quá số tiền công nợ còn lại. | High |
| REQ-ACC-079 | KHI tạo một báo cáo thuế, THÌ Hệ_Thống PHẢI xác thực ngày kết thúc kỳ phải sau ngày bắt đầu kỳ. | High |
| REQ-ACC-080 | KHI tạo một kỳ kế toán, THÌ Hệ_Thống PHẢI xác thực ngày không trùng lặp với các kỳ hiện có. | High |
| REQ-ACC-081 | Hệ_Thống PHẢI xác thực các trường bắt buộc không được null hoặc rỗng. | High |
| REQ-ACC-082 | Hệ_Thống PHẢI trả về thông báo lỗi rõ ràng và dễ hiểu khi xác thực thất bại. | High |

### Yêu Cầu 12: Dashboard và Tổng Quan

**User Story:** Là một kế toán viên, tôi muốn xem dashboard với các chỉ số tài chính chính, để tôi có thể nhanh chóng hiểu tình trạng tài chính hiện tại.

#### Tiêu Chí Chấp Nhận

| Identifier | Requirement | Priority |
|------------|-------------|----------|
| REQ-ACC-083 | Hệ_Thống PHẢI hiển thị doanh thu tháng hiện tại trên dashboard. | High |
| REQ-ACC-084 | Hệ_Thống PHẢI hiển thị chi phí tháng hiện tại trên dashboard. | High |
| REQ-ACC-085 | Hệ_Thống PHẢI hiển thị lợi nhuận tháng hiện tại trên dashboard. | High |
| REQ-ACC-086 | Hệ_Thống PHẢI hiển thị tổng công nợ chưa thanh toán trên dashboard. | High |
| REQ-ACC-087 | Hệ_Thống PHẢI hiển thị tổng thuế chưa nộp trên dashboard. | High |
| REQ-ACC-088 | Hệ_Thống PHẢI hiển thị số lượng công nợ quá hạn trên dashboard. | High |
| REQ-ACC-089 | Hệ_Thống PHẢI tự động cập nhật các chỉ số dashboard theo thời gian thực khi có giao dịch mới được tạo. | Medium |

## Phạm Vi Yêu Cầu

### Ưu Tiên Cao (Bắt Buộc Phải Có)
- YC-1: Quản Lý Giao Dịch Tài Chính (REQ-ACC-001 đến REQ-ACC-007)
- YC-2: Quản Lý Kỳ Kế Toán (REQ-ACC-008 đến REQ-ACC-014)
- YC-3: Báo Cáo Tài Chính (REQ-ACC-015 đến REQ-ACC-021)
- YC-4: Quản Lý Công Nợ Nhà Cung Cấp (REQ-ACC-022 đến REQ-ACC-030)
- YC-5: Xử Lý Thanh Toán Nhà Cung Cấp (REQ-ACC-031 đến REQ-ACC-038)
- YC-6: Quản Lý Báo Cáo Thuế (REQ-ACC-039 đến REQ-ACC-049)
- YC-7: Tính Thuế Tự Động (REQ-ACC-050 đến REQ-ACC-057)
- YC-9: Phân Quyền và Kiểm Soát Truy Cập (REQ-ACC-065 đến REQ-ACC-070)
- YC-11: Xác Thực Dữ Liệu (REQ-ACC-077 đến REQ-ACC-082)
- YC-12: Dashboard và Tổng Quan (REQ-ACC-083 đến REQ-ACC-089)

### Ưu Tiên Trung Bình (Nên Có)
- YC-8: Đối Soát Thanh Toán (REQ-ACC-058 đến REQ-ACC-064)
- YC-10: Ghi Log Kiểm Toán (REQ-ACC-071 đến REQ-ACC-076)

### Ưu Tiên Thấp (Tốt Nếu Có)
- Xuất báo cáo ra nhiều định dạng
- Lọc và tìm kiếm nâng cao
- Thông báo email cho công nợ quá hạn

## Ghi Chú Kỹ Thuật

### Điểm Tích Hợp

Module Kế Toán tích hợp với:
- **Module Đơn Hàng (Orders)**: Tự động tạo giao dịch doanh thu khi đơn hàng DELIVERED
- **Module Kho (Warehouse)**: Tự động tạo công nợ NCC khi đơn mua hàng RECEIVED
- **Module Vận Chuyển (Shipping)**: Theo dõi chi phí vận chuyển
- **Module Người Dùng (User)**: Xác thực vai trò và phân quyền

### Công Thức Tính Toán

```
Lợi Nhuận Ròng = Tổng Doanh Thu - Tổng Chi Phí
Lợi Nhuận Gộp = Doanh Thu - Giá Vốn Hàng Bán
Thuế VAT = Doanh Thu Chịu Thuế × 10%
Thuế TNDN = Lợi Nhuận × 20%
Công Nợ Còn Lại = Tổng Số Tiền - Số Tiền Đã Trả
Sai Lệch Thanh Toán = Số Tiền Cổng Thanh Toán - Số Tiền Hệ Thống
```

### Quy Trình Trạng Thái

**Kỳ Kế Toán:**
```
MỞ → ĐÓNG (một chiều, không thể mở lại)
```

**Báo Cáo Thuế:**
```
NHÁP → ĐÃ_NỘP → ĐÃ_TRẢ (tiến triển một chiều)
```

**Công Nợ Nhà Cung Cấp:**
```
CHƯA_TRẢ → MỘT_PHẦN → ĐÃ_TRẢ
CHƯA_TRẢ → QUÁ_HẠN (nếu quá ngày đến hạn)
```

**Đối Soát Thanh Toán:**
```
KHỚP (số tiền bằng nhau)
KHÔNG_KHỚP (số tiền khác nhau)
THIẾU_TRONG_HỆ_THỐNG (có trong cổng nhưng không có trong hệ thống)
THIẾU_TRONG_CỔNG (có trong hệ thống nhưng không có trong cổng)
```

## Trạng Thái Triển Khai

✅ **Đã hoàn thành**: REQ-ACC-001 đến REQ-ACC-089

- Tất cả các chức năng cốt lõi đã được triển khai
- API endpoints đã được tạo và kiểm tra
- Frontend đã được tích hợp
- Phân quyền đã được cấu hình
- Tính năng tự động tính thuế đã hoạt động

## Tài Liệu Tham Khảo

### Tài Liệu Kiến Trúc và Thiết Kế
- [MODULE-KE-TOAN-DATABASE-DIAGRAM.md](../../../MODULE-KE-TOAN-DATABASE-DIAGRAM.md) - Sơ đồ database và class diagram chi tiết
- [MODULE-KE-TOAN-ERD-DIAGRAM.md](../../../MODULE-KE-TOAN-ERD-DIAGRAM.md) - Sơ đồ ERD quan hệ giữa các bảng
- [ACCOUNTING-CLASS-DIAGRAM-ARCHITECTURE.md](../../../ACCOUNTING-CLASS-DIAGRAM-ARCHITECTURE.md) - Kiến trúc phân tầng (Layered Architecture)

### Tài Liệu Triển Khai
- [ACCOUNTING-COMPLETE-IMPLEMENTATION.md](../../../ACCOUNTING-COMPLETE-IMPLEMENTATION.md) - Tổng quan triển khai đầy đủ 6 module
- [ACCOUNTING-AUTOMATION-COMPLETE.md](../../../ACCOUNTING-AUTOMATION-COMPLETE.md) - Tự động hóa giao dịch tài chính
- [ACCOUNTING-INTEGRATION-CHECKLIST.md](../../../ACCOUNTING-INTEGRATION-CHECKLIST.md) - Checklist tích hợp module
- [TONG-KET-MODULE-KE-TOAN.md](../../../TONG-KET-MODULE-KE-TOAN.md) - Tổng kết module kế toán

### Tài Liệu Tính Năng Thuế
- [TAX-AUTO-CALCULATION-GUIDE.md](../../../TAX-AUTO-CALCULATION-GUIDE.md) - Hướng dẫn tính thuế tự động (English)
- [HUONG-DAN-TINH-THUE-TU-DONG.md](../../../HUONG-DAN-TINH-THUE-TU-DONG.md) - Hướng dẫn tính thuế tự động (Tiếng Việt)
- [TONG-KET-TINH-THUE-TU-DONG.md](../../../TONG-KET-TINH-THUE-TU-DONG.md) - Tổng kết tính năng tính thuế tự động
- [TAX-AUTO-UPDATE-FEATURE.md](../../../TAX-AUTO-UPDATE-FEATURE.md) - Tính năng cập nhật thuế tự động
- [ADMIN-TAX-AUTO-CALCULATION-ADDED.md](../../../ADMIN-TAX-AUTO-CALCULATION-ADDED.md) - Thêm tính năng tính thuế cho Admin

### Tài Liệu Công Nợ Nhà Cung Cấp
- [CONG-NO-NCC-GUIDE.md](../../../CONG-NO-NCC-GUIDE.md) - Hướng dẫn quản lý công nợ nhà cung cấp
- [TEST-CONG-NO-NCC.md](../../../TEST-CONG-NO-NCC.md) - Test cases công nợ NCC

### Tài Liệu Dashboard và Báo Cáo
- [ACCOUNTING-DASHBOARD-API.md](../../../ACCOUNTING-DASHBOARD-API.md) - API Dashboard kế toán
- [ADMIN-ACCOUNTING-PAGES-FIX.md](../../../ADMIN-ACCOUNTING-PAGES-FIX.md) - Sửa lỗi trang kế toán Admin
- [ADMIN-ACCOUNTING-FINAL-FIX.md](../../../ADMIN-ACCOUNTING-FINAL-FIX.md) - Sửa lỗi cuối cùng trang Admin

### Tài Liệu Hướng Dẫn Sử Dụng
- [ACCOUNTING-QUICK-START.md](../../../ACCOUNTING-QUICK-START.md) - Hướng dẫn nhanh module kế toán
- [ACCOUNTING-CLEAN-GUIDE.md](../../../ACCOUNTING-CLEAN-GUIDE.md) - Hướng dẫn clean và đơn giản

### Tài Liệu Kiểm Tra và Test
- [TEST-TAX-AUTO-CALCULATION.md](../../../TEST-TAX-AUTO-CALCULATION.md) - Test cases tính thuế tự động
- [TEST-ACCOUNTING-API.md](../../../TEST-ACCOUNTING-API.md) - Test API kế toán
- [TEST-ACCOUNTING-AUTOMATION.http](../../../TEST-ACCOUNTING-AUTOMATION.http) - HTTP requests test automation
- [test-tax-api.http](../../../test-tax-api.http) - HTTP requests test thuế
- [test-tax-auto-calculation.http](../../../test-tax-auto-calculation.http) - HTTP requests test tính thuế tự động

### Tài Liệu Sửa Lỗi
- [ACCOUNTING-FIX-SUMMARY.md](../../../ACCOUNTING-FIX-SUMMARY.md) - Tổng hợp các lỗi đã sửa
- [FIX-ACCOUNTANT-TRANSACTIONS-PERMISSION.md](../../../FIX-ACCOUNTANT-TRANSACTIONS-PERMISSION.md) - Sửa lỗi phân quyền giao dịch
- [FIX-EMPLOYEE-DASHBOARD-PERMISSION.md](../../../FIX-EMPLOYEE-DASHBOARD-PERMISSION.md) - Sửa lỗi phân quyền dashboard nhân viên

### Tài Liệu Restart và Deploy
- [RESTART-FRONTEND-TAX-FEATURE.md](../../../RESTART-FRONTEND-TAX-FEATURE.md) - Restart frontend cho tính năng thuế
- [RESTART-FOR-TAX-UPDATE-FEATURE.md](../../../RESTART-FOR-TAX-UPDATE-FEATURE.md) - Restart cho cập nhật tính năng thuế
- [CHECKLIST-RESTART-BACKEND.md](../../../CHECKLIST-RESTART-BACKEND.md) - Checklist restart backend

### Tài Liệu Hình Ảnh và Demo
- [HINH-ANH-TINH-NANG-MOI.md](../../../HINH-ANH-TINH-NANG-MOI.md) - Hình ảnh các tính năng mới

### Database Scripts
- [insert-sample-tax-data.sql](../../../insert-sample-tax-data.sql) - Dữ liệu mẫu báo cáo thuế
- [check-tax-data.sql](../../../check-tax-data.sql) - Script kiểm tra dữ liệu thuế
- [check-accounting-data.sql](../../../check-accounting-data.sql) - Script kiểm tra dữ liệu kế toán
- [insert-sample-accounting-data.sql](../../../insert-sample-accounting-data.sql) - Dữ liệu mẫu kế toán
- [create-accounting-periods-table.sql](../../../create-accounting-periods-table.sql) - Tạo bảng kỳ kế toán
- [create-financial-transactions-table.sql](../../../create-financial-transactions-table.sql) - Tạo bảng giao dịch tài chính

### Source Code Tham Khảo

#### Backend (Java Spring Boot)
- `src/main/java/com/doan/WEB_TMDT/module/accounting/`
  - `controller/` - Controllers (API endpoints)
  - `service/` - Business logic services
  - `repository/` - Data access repositories
  - `entity/` - JPA entities
  - `dto/` - Data transfer objects
  - `enums/` - Enumerations

#### Frontend (Next.js + TypeScript)
- `src/frontend/app/admin/accounting/` - Giao diện Admin
  - `transactions/` - Quản lý giao dịch
  - `periods/` - Quản lý kỳ kế toán
  - `tax/` - Quản lý thuế
  - `payables/` - Quản lý công nợ NCC
- `src/frontend/app/employee/accounting/` - Giao diện Employee
  - `dashboard/` - Dashboard kế toán
  - `tax/` - Xem báo cáo thuế
  - `transactions/` - Xem giao dịch
