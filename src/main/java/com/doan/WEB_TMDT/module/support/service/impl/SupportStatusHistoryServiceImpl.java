package com.doan.WEB_TMDT.module.support.service.impl;

import com.doan.WEB_TMDT.module.support.repository.SupportRatingRepository;
import com.doan.WEB_TMDT.module.support.repository.SupportStatusHistoryRepository;
import com.doan.WEB_TMDT.module.support.service.SupportStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupportStatusHistoryServiceImpl implements SupportStatusHistoryService {

    private final SupportStatusHistoryRepository supportStatusHistoryRepository;

}
