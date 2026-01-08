package com.doan.WEB_TMDT.module.support.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.support.dto.request.*;
import com.doan.WEB_TMDT.module.support.dto.response.SupportReplyResponse;
import com.doan.WEB_TMDT.module.support.dto.response.SupportTicketDetailResponse;
import com.doan.WEB_TMDT.module.support.dto.response.SupportTicketListResponse;
import com.doan.WEB_TMDT.module.support.service.SupportReplyService;
import com.doan.WEB_TMDT.module.support.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/employee/support-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
public class EmployeeSupportController {

    private final SupportTicketService ticketService;
    private final SupportReplyService replyService;

    /**
     * REQ94-95: Xem và lọc danh sách phiếu hỗ trợ
     * GET /api/employee/support-tickets?status=CHO_XU_LY&priority=HIGH&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // Parse sort
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<SupportTicketListResponse> tickets = ticketService.getAllTicketsForEmployee(
                status, categoryId, priority, employeeId, searchKeyword, pageable
        );

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu hỗ trợ thành công", tickets));
    }

    /**
     * Xem chi tiết phiếu hỗ trợ
     * GET /api/employee/support-tickets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTicketDetail(
            @PathVariable Long id
    ) {
        SupportTicketDetailResponse ticket = ticketService.getTicketDetailForEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phiếu hỗ trợ thành công", ticket));
    }

    /**
     * Nhận phiếu để xử lý (assign to self)
     * PUT /api/employee/support-tickets/{id}/assign
     */
    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse> assignTicketToSelf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        SupportTicketDetailResponse ticket = ticketService.assignTicketToEmployee(id, email);

        return ResponseEntity.ok(ApiResponse.success("Đã nhận phiếu hỗ trợ thành công", ticket));
    }

    /**
     * REQ97: Cập nhật thông tin phiếu
     * PUT /api/employee/support-tickets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupportTicketRequest request
    ) {
        SupportTicketDetailResponse ticket = ticketService.updateTicketInfo(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phiếu hỗ trợ thành công", ticket));
    }

    /**
     * REQ96: Cập nhật trạng thái phiếu
     * PUT /api/employee/support-tickets/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        SupportTicketDetailResponse ticket = ticketService.updateTicketStatus(id, request, email);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", ticket));
    }

    /**
     * REQ96: Cập nhật trạng thái phiếu
     * PUT /api/employee/support-tickets/{id}/status
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse> closeTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UpdateTicketStatusRequest request = UpdateTicketStatusRequest.builder()
                .note("CLOSED")
                .newStatus("RESOLVED")
                .build();
        String email = userDetails.getUsername();
        SupportTicketDetailResponse ticket = ticketService.updateTicketStatus(id, request, email);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", ticket));
    }

    /**
     * REQ92: Gửi phản hồi
     * POST /api/employee/support-tickets/{id}/replies
     */
    @PostMapping("/{id}/replies")
    public ResponseEntity<ApiResponse> addReply(
            @PathVariable Long id,
            @Valid @RequestBody CreateReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        SupportReplyResponse reply = replyService.addReplyByEmployee(id, request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã gửi phản hồi thành công", reply));
    }
}
