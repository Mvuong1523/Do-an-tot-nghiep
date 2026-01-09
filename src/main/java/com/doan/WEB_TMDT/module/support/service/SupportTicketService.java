package com.doan.WEB_TMDT.module.support.service;

import com.doan.WEB_TMDT.module.support.dto.request.CreateSupportTicketRequest;
import com.doan.WEB_TMDT.module.support.dto.request.UpdateSupportTicketRequest;
import com.doan.WEB_TMDT.module.support.dto.request.UpdateTicketStatusRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportTicketDetailResponse;
import com.doan.WEB_TMDT.module.support.dto.response.SupportTicketListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface cho Service xử lý Support Ticket
 */
public interface SupportTicketService {

    // ==========================================
    // CUSTOMER OPERATIONS
    // ==========================================

    /**
     * Tạo phiếu hỗ trợ mới - REQ84-88
     * @param request Thông tin phiếu hỗ trợ
     * @param customerEmail Email của khách hàng
     * @return Chi tiết phiếu vừa tạo
     */
    SupportTicketDetailResponse createTicketByCustomer(
            CreateSupportTicketRequest request,
            String customerEmail
    );

    /**
     * Lấy danh sách phiếu hỗ trợ của customer - REQ89
     * @param customerEmail Email của khách hàng
     * @param status Lọc theo trạng thái (optional)
     * @param categoryId Lọc theo loại yêu cầu (optional)
     * @param pageable Phân trang
     * @return Danh sách phiếu hỗ trợ
     */
    Page<SupportTicketListResponse> getTicketsByCustomer(
            String customerEmail,
            String status,
            Long categoryId,
            Pageable pageable
    );

    /**
     * Xem chi tiết phiếu hỗ trợ - REQ90
     * @param ticketId ID của phiếu
     * @param customerEmail Email của khách hàng (để verify quyền)
     * @return Chi tiết phiếu hỗ trợ
     */
    SupportTicketDetailResponse getTicketDetailByCustomer(
            Long ticketId,
            String customerEmail
    );

    // ==========================================
    // EMPLOYEE OPERATIONS
    // ==========================================

    /**
     * Lấy tất cả phiếu hỗ trợ với filter - REQ94-95
     * @param status Lọc theo trạng thái (optional)
     * @param categoryId Lọc theo loại yêu cầu (optional)
     * @param priority Lọc theo mức độ ưu tiên (optional)
     * @param employeeId Lọc theo nhân viên xử lý (optional)
     * @param searchKeyword Tìm kiếm theo từ khóa (optional)
     * @param pageable Phân trang và sắp xếp
     * @return Danh sách phiếu hỗ trợ
     */
    Page<SupportTicketListResponse> getAllTicketsForEmployee(
            String status,
            Long categoryId,
            String priority,
            Long employeeId,
            String searchKeyword,
            Pageable pageable
    );

    /**
     * Xem chi tiết phiếu hỗ trợ (Employee)
     * @param ticketId ID của phiếu
     * @return Chi tiết phiếu hỗ trợ
     */
    SupportTicketDetailResponse getTicketDetailForEmployee(Long ticketId);

    /**
     * Nhận phiếu hỗ trợ để xử lý (assign to self)
     * @param ticketId ID của phiếu
     * @param employeeEmail Email của nhân viên
     * @return Chi tiết phiếu sau khi assign
     */
    SupportTicketDetailResponse assignTicketToEmployee(
            Long ticketId,
            String employeeEmail
    );

    /**
     * Cập nhật thông tin phiếu hỗ trợ - REQ97
     * @param ticketId ID của phiếu
     * @param request Thông tin cần cập nhật
     * @return Chi tiết phiếu sau khi cập nhật
     */
    SupportTicketDetailResponse updateTicketInfo(
            Long ticketId,
            UpdateSupportTicketRequest request
    );

    /**
     * Cập nhật trạng thái phiếu hỗ trợ - REQ96
     * @param ticketId ID của phiếu
     * @param request Trạng thái mới
     * @param employeeEmail Email của nhân viên thực hiện
     * @return Chi tiết phiếu sau khi cập nhật
     */
    SupportTicketDetailResponse updateTicketStatus(
            Long ticketId,
            UpdateTicketStatusRequest request,
            String employeeEmail
    );
}