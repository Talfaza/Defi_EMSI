package com.example.defi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String licenseNumber;
    private String email;
    private String walletAddress;

    @OneToMany(mappedBy = "clinic")
    @JsonIgnore
    private List<PaymentRequest> paymentRequests;

    @OneToMany(mappedBy = "clinic")
    @JsonIgnore
    private List<Alert> alerts;

}
