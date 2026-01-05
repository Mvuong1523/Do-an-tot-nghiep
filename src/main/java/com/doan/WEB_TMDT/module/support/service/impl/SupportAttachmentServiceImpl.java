package com.doan.WEB_TMDT.module.support.service.impl;

import com.doan.WEB_TMDT.module.support.repository.SupportAttachmentRepository;
import com.doan.WEB_TMDT.module.support.service.SupportAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupportAttachmentServiceImpl implements SupportAttachmentService {

    private final SupportAttachmentRepository supportAttachmentRepository;

}
