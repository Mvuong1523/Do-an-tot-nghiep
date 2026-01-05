package com.doan.WEB_TMDT.module.support.entities;

import com.doan.WEB_TMDT.module.auth.entity.Customer;
import com.doan.WEB_TMDT.module.auth.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "support_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "support_category_id")
    private SupportCategory supportCategory;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String priority;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL)
    private Set<SupportTicketOrder> supportTicketOrders;

    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL)
    private Set<SupportReply> supportReplies;

    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL)
    private Set<SupportStatusHistory> supportStatusHistories;

    @OneToOne(mappedBy = "supportTicket", cascade = CascadeType.ALL)
    private SupportRating supportRating;
}