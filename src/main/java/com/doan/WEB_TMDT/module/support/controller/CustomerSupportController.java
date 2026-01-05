package com.doan.WEB_TMDT.module.support.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.support.dto.request.CreateReplyRequest;
import com.doan.WEB_TMDT.module.support.dto.request.CreateSupportTicketRequest;
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


/**
 * Controller cho khách hàng quản lý phiếu hỗ trợ
 */
@RestController
@RequestMapping("/api/customer/support-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerSupportController {

    private final SupportTicketService ticketService;
    private final SupportReplyService replyService;

    /**
     * REQ84-88: Tạo phiếu hỗ trợ
     * POST /api/customer/support-tickets
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createTicket(
            @Valid @RequestBody CreateSupportTicketRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        SupportTicketDetailResponse ticket = ticketService.createTicketByCustomer(request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Phiếu hỗ trợ đã được tạo thành công", ticket));
    }

    /**
     * REQ89: Xem danh sách phiếu hỗ trợ của mình
     * GET /api/customer/support-tickets?status=CHO_XU_LY&categoryId=1&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getMyTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();

        // Parse sort
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<SupportTicketListResponse> tickets = ticketService.getTicketsByCustomer(
                email, status, categoryId, pageable
        );

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu hỗ trợ thành công", tickets));
    }

    /**
     * REQ90: Xem chi tiết phiếu hỗ trợ
     * GET /api/customer/support-tickets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTicketDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        SupportTicketDetailResponse ticket = ticketService.getTicketDetailByCustomer(id, email);

        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phiếu hỗ trợ thành công", ticket));
    }

    /**
     * REQ91: Gửi phản hồi cho phiếu hỗ trợ
     * POST /api/customer/support-tickets/{id}/replies
     */
    @PostMapping("/{id}/replies")
    public ResponseEntity<ApiResponse> addReply(
            @PathVariable Long id,
            @Valid @RequestBody CreateReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        SupportReplyResponse reply = replyService.addReplyByCustomer(id, request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã gửi phản hồi thành công", reply));
    }
}