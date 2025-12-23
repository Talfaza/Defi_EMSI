package com.example.defi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String CIN;
    private String address;
    private String wallet;
    private String email;

    // Private key for demo blockchain transactions (NOT for production!)
    @JsonIgnore
    private String privateKey;

    @OneToMany(mappedBy = "patient")
    @JsonIgnore
    private List<PaymentRequest> paymentRequests;

}
