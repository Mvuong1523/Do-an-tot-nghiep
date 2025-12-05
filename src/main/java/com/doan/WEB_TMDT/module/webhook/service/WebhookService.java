package com.doan.WEB_TMDT.module.webhook.service;

import com.doan.WEB_TMDT.module.webhook.dto.GHNWebhookRequest;

public interface WebhookService {
    void handleGHNWebhook(GHNWebhookRequest request);
}
