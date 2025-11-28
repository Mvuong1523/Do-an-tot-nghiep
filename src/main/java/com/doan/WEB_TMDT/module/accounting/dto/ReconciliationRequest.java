package com.doan.WEB_TMDT.module.accounting.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReconciliationRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String gateway; // ALL, VNPAY, MOMO, ZALOPAY
}
