package com.doan.WEB_TMDT.module.support.service;

import com.doan.WEB_TMDT.module.support.dto.request.CreateRatingRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportRatingResponse;

public interface SupportRatingService {
    SupportRatingResponse createRating(Long ticketId, CreateRatingRequest request, String customerEmail);
    SupportRatingResponse getRatingByTicketId(Long ticketId);
}
