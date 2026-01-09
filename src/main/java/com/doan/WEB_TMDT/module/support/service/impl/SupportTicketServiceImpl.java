package com.doan.WEB_TMDT.module.support.service.impl;

import com.doan.WEB_TMDT.module.auth.entity.Customer;
import com.doan.WEB_TMDT.module.auth.entity.Employee;
import com.doan.WEB_TMDT.module.auth.repository.CustomerRepository;
import com.doan.WEB_TMDT.module.auth.repository.EmployeeRepository;
import com.doan.WEB_TMDT.module.order.entity.Order;
import com.doan.WEB_TMDT.module.order.repository.OrderRepository;
import com.doan.WEB_TMDT.module.support.core.SupportTicketConstants;
import com.doan.WEB_TMDT.module.support.dto.request.CreateSupportTicketRequest;
import com.doan.WEB_TMDT.module.support.dto.request.UpdateSupportTicketRequest;
import com.doan.WEB_TMDT.module.support.dto.request.UpdateTicketStatusRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportReplyResponse;
import com.doan.WEB_TMDT.module.support.dto.response.SupportTicketDetailResponse;
import com.doan.WEB_TMDT.module.support.dto.response.SupportTicketListResponse;
import com.doan.WEB_TMDT.module.support.entities.*;
import com.doan.WEB_TMDT.module.support.exceptions.ForbiddenException;
import com.doan.WEB_TMDT.module.support.exceptions.NotFoundException;
import com.doan.WEB_TMDT.module.support.exceptions.ValidationException;
import com.doan.WEB_TMDT.module.support.repository.SupportCategoryRepository;
import com.doan.WEB_TMDT.module.support.repository.SupportStatusHistoryRepository;
import com.doan.WEB_TMDT.module.support.repository.SupportTicketOrderRepository;
import com.doan.WEB_TMDT.module.support.repository.SupportTicketRepository;
import com.doan.WEB_TMDT.module.support.service.SupportChatSocketIOHandler;
import com.doan.WEB_TMDT.module.support.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final SupportTicketOrderRepository ticketOrderRepository;
    private final SupportStatusHistoryRepository historyRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SupportCategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final SupportChatSocketIOHandler supportChatSocketIOHandler;

    @Override
    public SupportTicketDetailResponse createTicketByCustomer(
            CreateSupportTicketRequest request,
            String customerEmail
    ) {
        log.info("Creating ticket for customer: {}", customerEmail);

        // 1. Tìm customer từ email
        Customer customer = customerRepository.findByUserEmail(customerEmail)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        // 2. Validate category
        SupportCategory category = categoryRepository.findById(request.getSupportCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!category.getIsActive()) {
            throw new ValidationException("Category is not active");
        }

        // 3. Validate order nếu có
        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new NotFoundException("Order not found"));

            if (!order.getCustomer().getId().equals(customer.getId())) {
                throw new ForbiddenException("Order does not belong to customer");
            }
        }

        // 4. Tạo ticket
        SupportTicket ticket = SupportTicket.builder()
                .customer(customer)
                .supportCategory(category)
                .title(request.getTitle())
                .content(request.getContent())
                .priority(request.getPriority())
                .status(SupportTicketConstants.STATUS_PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ticket = ticketRepository.save(ticket);
        log.info("Ticket created with ID: {}", ticket.getId());

        // 5. Liên kết với order nếu có
        if (order != null) {
            SupportTicketOrder ticketOrder = SupportTicketOrder.builder()
                    .supportTicket(ticket)
                    .order(order)
                    .createdAt(LocalDateTime.now())
                    .build();
            ticketOrderRepository.save(ticketOrder);
            log.info("Linked ticket {} with order {}", ticket.getId(), order.getId());
        }

        // 6. Tạo history
        createStatusHistory(ticket, null, SupportTicketConstants.STATUS_PENDING);

        // 🔥 Trigger tạo room chat
        supportChatSocketIOHandler.ticketRooms(ticket.getId());

        // 7. Map to response
        return mapToDetailResponse(ticket);
    }

    @Override
    @Transactional
    public Page<SupportTicketListResponse> getTicketsByCustomer(
            String customerEmail,
            String status,
            Long categoryId,
            Pageable pageable
    ) {
        log.info("Getting tickets for customer: {}", customerEmail);

        // 1. Tìm customer
        Customer customer = customerRepository.findByUserEmail(customerEmail)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        // 2. Build specification
        Specification<SupportTicket> spec = getSupportTicketSpecification(status, categoryId, customer);

        // 3. Query
        Page<SupportTicket> tickets = ticketRepository.findAll(spec, pageable);

        // 4. Map to response
        return tickets.map(this::mapToListResponse);
    }

    private static Specification<SupportTicket> getSupportTicketSpecification(String status, Long categoryId, Customer customer) {
        Specification<SupportTicket> spec = (root, query, cb) -> cb.conjunction();

        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("customer").get("id"), customer.getId())
        );

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status)
            );
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("supportCategory").get("id"), categoryId)
            );
        }
        return spec;
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketDetailResponse getTicketDetailByCustomer(
            Long ticketId,
            String customerEmail
    ) {
        log.info("Getting ticket detail {} for customer: {}", ticketId, customerEmail);

        // 1. Tìm ticket
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        // 2. Kiểm tra quyền sở hữu
        if (!ticket.getCustomer().getUser().getEmail().equals(customerEmail)) {
            throw new ForbiddenException("Access denied");
        }

        // 3. Map to response
        return mapToDetailResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketListResponse> getAllTicketsForEmployee(
            String status,
            Long categoryId,
            String priority,
            Long employeeId,
            String searchKeyword,
            Pageable pageable
    ) {
        log.info("Getting all tickets with filters");

        // Build specification
        Specification<SupportTicket> spec = (root, query, cb) -> cb.conjunction();


        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status)
            );
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("supportCategory").get("id"), categoryId)
            );
        }

        if (priority != null && !priority.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("priority"), priority)
            );
        }

        if (employeeId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("employee").get("id"), employeeId)
            );
        }

        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            String keyword = "%" + searchKeyword + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("title"), keyword),
                            cb.like(root.get("content"), keyword)
                    )
            );
        }

        Page<SupportTicket> tickets = ticketRepository.findAll(spec, pageable);
        return tickets.map(this::mapToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketDetailResponse getTicketDetailForEmployee(Long ticketId) {
        log.info("Getting ticket detail {} for employee", ticketId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        return mapToDetailResponse(ticket);
    }

    @Override
    public SupportTicketDetailResponse assignTicketToEmployee(
            Long ticketId,
            String employeeEmail
    ) {
        log.info("Assigning ticket {} to employee: {}", ticketId, employeeEmail);

        // 1. Tìm ticket
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        // 2. Kiểm tra trạng thái
        if (!SupportTicketConstants.STATUS_PENDING.equals(ticket.getStatus())) {
            throw new ValidationException("Chỉ có thể nhận phiếu đang chờ xử lý");
        }

        // 3. Tìm employee
        Employee employee = employeeRepository.findByUserEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found"));

        // 4. Assign và đổi status
        ticket.setEmployee(employee);
        ticket.setStatus(SupportTicketConstants.STATUS_PROCESSING);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);

        // 5. Tạo history
        createStatusHistory(ticket, SupportTicketConstants.STATUS_PENDING, SupportTicketConstants.STATUS_PROCESSING);

        log.info("Ticket {} assigned to employee {}", ticketId, employee.getId());
        return mapToDetailResponse(ticket);
    }

    @Override
    public SupportTicketDetailResponse updateTicketInfo(
            Long ticketId,
            UpdateSupportTicketRequest request
    ) {
        log.info("Updating ticket info: {}", ticketId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        // Cập nhật category nếu có
        if (request.getSupportCategoryId() != null) {
            SupportCategory category = categoryRepository
                    .findById(request.getSupportCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            ticket.setSupportCategory(category);
        }

        // Cập nhật priority nếu có
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);

        return mapToDetailResponse(ticket);
    }

    @Override
    public SupportTicketDetailResponse updateTicketStatus(
            Long ticketId,
            UpdateTicketStatusRequest request,
            String employeeEmail
    ) {
        log.info("Updating ticket {} status to: {}", ticketId, request.getNewStatus());

        // 1. Tìm ticket
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        // 2. Validate transition
        validateStatusTransition(ticket.getStatus(), request.getNewStatus());

        // 3. Update status
        String oldStatus = ticket.getStatus();
        ticket.setStatus(request.getNewStatus());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);

        // 4. Create history
        createStatusHistory(ticket, oldStatus, request.getNewStatus());

        log.info("Ticket {} status updated from {} to {}",
                ticketId, oldStatus, request.getNewStatus());

        return mapToDetailResponse(ticket);
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private void validateStatusTransition(String oldStatus, String newStatus) {
        if (SupportTicketConstants.STATUS_RESOLVED.equals(oldStatus) || SupportTicketConstants.STATUS_CANCELLED.equals(oldStatus)) {
            throw new ValidationException(
                    "Không thể thay đổi trạng thái phiếu đã xử lý hoặc đã huỷ"
            );
        }

        if (SupportTicketConstants.STATUS_PROCESSING.equals(newStatus) && !SupportTicketConstants.STATUS_PENDING.equals(oldStatus)) {
            throw new ValidationException(
                    "Chỉ có thể chuyển sang 'Đang xử lý' từ 'Chờ xử lý'"
            );
        }

        if (SupportTicketConstants.STATUS_RESOLVED.equals(newStatus) && !SupportTicketConstants.STATUS_PROCESSING.equals(oldStatus)) {
            throw new ValidationException(
                    "Chỉ có thể đóng phiếu từ trạng thái 'Đang xử lý'"
            );
        }
    }

    private void createStatusHistory(
            SupportTicket ticket,
            String oldStatus,
            String newStatus
    ) {
        SupportStatusHistory history = SupportStatusHistory.builder()
                .supportTicket(ticket)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .notedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }

    private SupportTicketListResponse mapToListResponse(SupportTicket ticket) {
        return SupportTicketListResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .categoryName(ticket.getSupportCategory().getName())
                .customerName(ticket.getCustomer().getFullName())
                .employeeName(ticket.getEmployee() != null
                        ? ticket.getEmployee().getFullName()
                        : null)
                .hasUnreadReply(false) // TODO: implement later
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private SupportTicketDetailResponse mapToDetailResponse(SupportTicket ticket) {
        // Map related orders (tránh null)
        List<Order> relatedOrders = (ticket.getSupportTicketOrders() == null ? Set.<SupportTicketOrder>of() : ticket.getSupportTicketOrders())
                .stream()
                .filter(Objects::nonNull)
                .map(sto -> {
                    Order order = sto.getOrder();
                    return Order.builder()
                            .id(order != null ? order.getId() : null)
                            .orderCode(order != null ? order.getOrderCode() : null)
                            .status(order != null ? order.getStatus() : null)
                            .build();
                })
                .collect(Collectors.toList());


        // Map replies (tránh null)
        List<SupportReplyResponse> replies = (ticket.getSupportReplies() == null ? Set.<SupportReply>of() : ticket.getSupportReplies())
                .stream()
                .sorted(Comparator.comparing(SupportReply::getCreatedAt))
                .map(this::mapReplyToResponse)
                .collect(Collectors.toList());

        return SupportTicketDetailResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .content(ticket.getContent())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .categoryId(ticket.getSupportCategory() != null ? ticket.getSupportCategory().getId() : null)
                .categoryName(ticket.getSupportCategory() != null ? ticket.getSupportCategory().getName() : null)
                .customerId(ticket.getCustomer() != null ? ticket.getCustomer().getId() : null)
                .customerName(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : null)
                .customerEmail(ticket.getCustomer() != null && ticket.getCustomer().getUser() != null
                        ? ticket.getCustomer().getUser().getEmail()
                        : null)
                .employeeId(ticket.getEmployee() != null ? ticket.getEmployee().getId() : null)
                .employeeName(ticket.getEmployee() != null ? ticket.getEmployee().getFullName() : null)
                .relatedOrders(relatedOrders)
                .replies(replies)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }


    private SupportReplyResponse mapReplyToResponse(SupportReply reply) {
        String senderName = "customer".equals(reply.getSenderType())
                ? reply.getSupportTicket().getCustomer().getFullName()
                : reply.getSupportTicket().getEmployee().getFullName();

        return SupportReplyResponse.builder()
                .id(reply.getId())
                .senderType(reply.getSenderType())
                .senderName(senderName)
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}
