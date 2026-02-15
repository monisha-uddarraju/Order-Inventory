package com.order.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers",
       uniqueConstraints = @UniqueConstraint(name = "customers_email_u", columnNames = "email_address"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer id;

    @Column(name = "email_address", nullable = false, length = 255)
    private String emailAddress;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
}
