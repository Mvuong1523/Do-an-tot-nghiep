package com.doan.WEB_TMDT.module.support.service.impl;

import com.doan.WEB_TMDT.module.support.repository.SupportTicketOrderRepository;
import com.doan.WEB_TMDT.module.support.service.SupportTicketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupportTicketOrderServiceImpl  implements SupportTicketOrderService {

    private final SupportTicketOrderRepository supportTicketOrderRepository;

}
