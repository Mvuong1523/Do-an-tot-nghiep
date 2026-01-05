package com.doan.WEB_TMDT.module.support.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "support_reply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "support_ticket_id", nullable = false)
    private SupportTicket supportTicket;

    @Column(name = "sender_type")
    private String senderType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "supportReply", cascade = CascadeType.ALL)
    private Set<SupportAttachment> supportAttachments;
}
