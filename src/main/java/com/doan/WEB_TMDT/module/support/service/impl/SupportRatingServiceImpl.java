package com.doan.WEB_TMDT.module.support.service.impl;

import com.doan.WEB_TMDT.module.support.repository.SupportRatingRepository;
import com.doan.WEB_TMDT.module.support.service.SupportRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupportRatingServiceImpl implements SupportRatingService {

    private final SupportRatingRepository supportRatingRepository;

}
