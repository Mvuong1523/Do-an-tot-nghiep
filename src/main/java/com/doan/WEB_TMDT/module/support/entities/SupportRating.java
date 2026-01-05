package com.doan.WEB_TMDT.module.support.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_rating")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "support_ticket_id", nullable = false)
    private SupportTicket supportTicket;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
