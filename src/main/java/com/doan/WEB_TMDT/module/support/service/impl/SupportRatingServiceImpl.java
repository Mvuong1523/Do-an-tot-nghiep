package com.doan.WEB_TMDT.module.support.service.impl;

import com.doan.WEB_TMDT.module.support.core.SupportTicketConstants;
import com.doan.WEB_TMDT.module.support.dto.request.CreateRatingRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportRatingResponse;
import com.doan.WEB_TMDT.module.support.entities.SupportRating;
import com.doan.WEB_TMDT.module.support.entities.SupportTicket;
import com.doan.WEB_TMDT.module.support.exceptions.ForbiddenException;
import com.doan.WEB_TMDT.module.support.exceptions.NotFoundException;
import com.doan.WEB_TMDT.module.support.exceptions.ValidationException;
import com.doan.WEB_TMDT.module.support.repository.SupportRatingRepository;
import com.doan.WEB_TMDT.module.support.repository.SupportTicketRepository;
import com.doan.WEB_TMDT.module.support.service.SupportRatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportRatingServiceImpl implements SupportRatingService {

    private final SupportRatingRepository supportRatingRepository;
    private final SupportTicketRepository supportTicketRepository;

    @Override
    @Transactional
    public SupportRatingResponse createRating(Long ticketId, CreateRatingRequest request, String customerEmail) {
        log.info("Customer {} rating ticket {}", customerEmail, ticketId);

        // 1. Tìm ticket
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu hỗ trợ"));

        // 2. Kiểm tra quyền - chỉ khách hàng tạo phiếu mới được đánh giá
        if (!ticket.getCustomer().getUser().getEmail().equals(customerEmail)) {
            throw new ForbiddenException("Bạn không có quyền đánh giá phiếu này");
        }

        // 3. Kiểm tra trạng thái - chỉ đánh giá phiếu đã đóng
        if (!SupportTicketConstants.STATUS_RESOLVED.equals(ticket.getStatus())) {
            throw new ValidationException("Chỉ có thể đánh giá phiếu đã được giải quyết");
        }

        // 4. Kiểm tra đã đánh giá chưa
        if (supportRatingRepository.existsBySupportTicketId(ticketId)) {
            throw new ValidationException("Phiếu này đã được đánh giá");
        }

        // 5. Tạo rating
        SupportRating rating = SupportRating.builder()
                .supportTicket(ticket)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        rating = supportRatingRepository.save(rating);
        log.info("Rating {} created for ticket {}", rating.getId(), ticketId);

        return mapToResponse(rating);
    }

    @Override
    public SupportRatingResponse getRatingByTicketId(Long ticketId) {
        return supportRatingRepository.findBySupportTicketId(ticketId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private SupportRatingResponse mapToResponse(SupportRating rating) {
        return SupportRatingResponse.builder()
                .id(rating.getId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
