package com.fudn.customerservice.service;

import com.fudn.customerservice.dto.LoginRequest;
import com.fudn.customerservice.dto.LoginResponse;
import com.fudn.customerservice.exception.ApiException;
import com.fudn.customerservice.model.Customer;
import com.fudn.customerservice.model.CustomerStatus;
import com.fudn.customerservice.repository.CustomerRepository;
import com.fudn.customerservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.admin.email}") private String adminEmail;
    @Value("${app.admin.password}") private String adminPassword;

    public LoginResponse login(LoginRequest request) {
        if (adminEmail.equalsIgnoreCase(request.email())) {
            if (!adminPassword.equals(request.password())) throw ApiException.unauthorized("Invalid email or password");
            return response(0L, adminEmail, "Administrator", ROLE_ADMIN);
        }
        Customer customer = customerRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), customer.getPassword()))
            throw ApiException.unauthorized("Invalid email or password");
        if (customer.getCustomerStatus() == CustomerStatus.INACTIVE)
            throw ApiException.forbidden("Your account is inactive. Please contact the administrator.");
        return response(customer.getCustomerId(), customer.getEmail(), customer.getCustomerName(), ROLE_CUSTOMER);
    }

    private LoginResponse response(Long id, String email, String name, String role) {
        return new LoginResponse(jwtService.generateToken(id, email, role), "Bearer", jwtService.getExpirationSeconds(), role, id, email, name);
    }
}
