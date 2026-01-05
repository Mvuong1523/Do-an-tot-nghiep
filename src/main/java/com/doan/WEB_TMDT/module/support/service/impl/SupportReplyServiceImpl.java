package com.doan.WEB_TMDT.module.support.service.impl;

import com.doan.WEB_TMDT.module.support.core.SupportTicketConstants;
import com.doan.WEB_TMDT.module.support.dto.request.CreateReplyRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportReplyResponse;
import com.doan.WEB_TMDT.module.support.entities.SupportReply;
import com.doan.WEB_TMDT.module.support.entities.SupportTicket;
import com.doan.WEB_TMDT.module.support.exceptions.ForbiddenException;
import com.doan.WEB_TMDT.module.support.exceptions.NotFoundException;
import com.doan.WEB_TMDT.module.support.exceptions.ValidationException;
import com.doan.WEB_TMDT.module.support.repository.SupportReplyRepository;
import com.doan.WEB_TMDT.module.support.repository.SupportTicketRepository;
import com.doan.WEB_TMDT.module.support.service.SupportReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SupportReplyServiceImpl implements SupportReplyService {

    private final SupportReplyRepository replyRepository;
    private final SupportTicketRepository ticketRepository;

    @Override
    public SupportReplyResponse addReplyByCustomer(
            Long ticketId,
            CreateReplyRequest request,
            String customerEmail
    ) {
        log.info("Customer {} adding reply to ticket {}", customerEmail, ticketId);

        // 1. Tìm ticket
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        // 2. Kiểm tra quyền
        if (!ticket.getCustomer().getUser().getEmail().equals(customerEmail)) {
            throw new ForbiddenException("Access denied");
        }

        // 3. Kiểm tra trạng thái
        if (SupportTicketConstants.STATUS_RESOLVED.equals(ticket.getStatus()) || SupportTicketConstants.STATUS_CANCELLED.equals(ticket.getStatus())) {
            throw new ValidationException("Không thể phản hồi phiếu đã đóng hoặc đã huỷ");
        }

        // 4. Tạo reply
        SupportReply reply = SupportReply.builder()
                .supportTicket(ticket)
                .senderType(SupportTicketConstants.SENDER_CUSTOMER)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        reply = replyRepository.save(reply);

        // 5. Cập nhật ticket
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        log.info("Reply {} created by customer", reply.getId());

        // 6. Map to response
        return mapToResponse(reply);
    }

    @Override
    public SupportReplyResponse addReplyByEmployee(
            Long ticketId,
            CreateReplyRequest request,
            String employeeEmail
    ) {
        log.info("Employee {} adding reply to ticket {}", employeeEmail, ticketId);

        // 1. Tìm ticket
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        // 2. Kiểm tra trạng thái
        if (SupportTicketConstants.STATUS_RESOLVED.equals(ticket.getStatus()) || SupportTicketConstants.STATUS_CANCELLED.equals(ticket.getStatus())) {
            throw new ValidationException("Không thể phản hồi phiếu đã đóng hoặc đã huỷ");
        }

        // 3. Tạo reply
        SupportReply reply = SupportReply.builder()
                .supportTicket(ticket)
                .senderType(SupportTicketConstants.SENDER_EMPLOYEE)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        reply = replyRepository.save(reply);

        // 4. Cập nhật ticket
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        log.info("Reply {} created by employee", reply.getId());

        // 5. Map to response
        return mapToResponse(reply);
    }

    private SupportReplyResponse mapToResponse(SupportReply reply) {
        String senderName = SupportTicketConstants.SENDER_CUSTOMER.equals(reply.getSenderType())
                ? reply.getSupportTicket().getCustomer().getFullName()
                : reply.getSupportTicket().getEmployee().getFullName();

        return SupportReplyResponse.builder()
                .id(reply.getId())
                .senderType(reply.getSenderType())
                .senderName(senderName)
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}