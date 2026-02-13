package com.inventory.order.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "customers",
       indexes = @Index(name = "idx_customers_email", columnList = "email_address"))
@ToString(exclude = {"orders", "shipments"})
public class Customers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer id;

    @Email @NotBlank
    @Column(name = "email_address", length = 255, nullable = false)
    private String emailAddress;

    @NotBlank
    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @OneToMany(mappedBy = "customer")
    @Builder.Default
    private Set<Orders> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    @Builder.Default
    private Set<Shipments> shipments = new LinkedHashSet<>();
}
//1