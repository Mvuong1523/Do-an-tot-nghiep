package com.doan.WEB_TMDT.module.support.core;

public class SupportTicketConstants {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Priority
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_HIGH = "HIGH";

    // Sender Type
    public static final String SENDER_CUSTOMER = "customer";
    public static final String SENDER_EMPLOYEE = "employee";

    private SupportTicketConstants() {
        // Private constructor to prevent instantiation
    }
}
