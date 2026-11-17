package com.ata.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class CustomerDtos {

    @Data
    @NoArgsConstructor
    public static class CustomerRequest {

        @NotBlank
        private String name;

        private LocalDate dob;

        private String gender;

        private String address;

        @NotBlank
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
        private String phone;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateRequest {

        @NotBlank
        private String name;

        private LocalDate dob;

        private String gender;

        private String address;

        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
        private String phone;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomerResponse {
        private Long customerId;
        private String name;
        private LocalDate dob;
        private String gender;
        private String address;
        private String phone;
        private String email;
        private boolean active;
    }
}
