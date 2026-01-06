package com.doan.WEB_TMDT.module.support.service;

import com.doan.WEB_TMDT.module.support.dto.request.CreateReplyRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportReplyResponse;

/**
 * Interface cho Service xử lý Reply
 */
public interface SupportReplyService {

    /**
     * Customer gửi phản hồi - REQ91
     * @param ticketId ID của phiếu hỗ trợ
     * @param request Nội dung phản hồi
     * @param customerEmail Email của khách hàng
     * @return Phản hồi vừa tạo
     */
    SupportReplyResponse addReplyByCustomer(
            Long ticketId,
            CreateReplyRequest request,
            String customerEmail
    );

    /**
     * Employee gửi phản hồi - REQ92-93
     * @param ticketId ID của phiếu hỗ trợ
     * @param request Nội dung phản hồi
     * @param employeeEmail Email của nhân viên
     * @return Phản hồi vừa tạo
     */
    SupportReplyResponse addReplyByEmployee(
            Long ticketId,
            CreateReplyRequest request,
            String employeeEmail
    );


    /**
     * Lưu tin nhắn từ Socket.IO / WebSocket
     * @param ticketId ID của phiếu hỗ trợ
     * @param content  Nội dung tin nhắn
     * @param senderEmail Email của người gửi
     * @return Tin nhắn/Reply đã lưu
     */
    SupportReplyResponse saveMessageFromWebSocket(
            Long ticketId,
            String content,
            String senderEmail
    );
}
