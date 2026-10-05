package com.fudn.customerservice.service;

import com.fudn.customerservice.dto.*;
import com.fudn.customerservice.exception.ApiException;
import com.fudn.customerservice.model.Customer;
import com.fudn.customerservice.model.CustomerStatus;
import com.fudn.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.admin.email}") private String adminEmail;

    @Transactional public CustomerResponse register(RegisterRequest r) {
        ensureEmailAvailable(r.email(), null); Customer c = new Customer();
        c.setCustomerName(r.customerName()); c.setTelephone(r.telephone()); c.setEmail(r.email()); c.setCustomerBirthday(r.customerBirthday());
        c.setCustomerStatus(CustomerStatus.ACTIVE); c.setPassword(passwordEncoder.encode(r.password()));
        return CustomerResponse.from(customerRepository.save(c));
    }
    public CustomerResponse getProfile(Long id) { return CustomerResponse.from(find(id)); }
    @Transactional public CustomerResponse updateProfile(Long id, ProfileUpdateRequest r) { Customer c=find(id); c.setCustomerName(r.customerName()); c.setTelephone(r.telephone()); c.setCustomerBirthday(r.customerBirthday()); return CustomerResponse.from(customerRepository.save(c)); }
    @Transactional public void changePassword(Long id, ChangePasswordRequest r) { Customer c=find(id); if(!passwordEncoder.matches(r.oldPassword(),c.getPassword())) throw ApiException.badRequest("Old password is incorrect"); if(r.oldPassword().equals(r.newPassword())) throw ApiException.badRequest("New password must be different from the old password"); c.setPassword(passwordEncoder.encode(r.newPassword())); customerRepository.save(c); }
    public List<CustomerResponse> search(String keyword) { List<Customer> found=(keyword==null||keyword.isBlank()) ? customerRepository.findAll(Sort.by("customerId")) : customerRepository.findByCustomerNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCustomerIdAsc(keyword.trim(),keyword.trim()); return found.stream().map(CustomerResponse::from).toList(); }
    public CustomerResponse getById(Long id) { return CustomerResponse.from(find(id)); }
    @Transactional public CustomerResponse create(AdminCustomerRequest r) { if(r.password()==null||r.password().isBlank()) throw ApiException.badRequest("password: Password is required when creating a customer"); ensureEmailAvailable(r.email(),null); Customer c=new Customer(); apply(c,r); c.setPassword(passwordEncoder.encode(r.password())); return CustomerResponse.from(customerRepository.save(c)); }
    @Transactional public CustomerResponse update(Long id, AdminCustomerRequest r) { Customer c=find(id); ensureEmailAvailable(r.email(),id); apply(c,r); if(r.password()!=null&&!r.password().isBlank()) c.setPassword(passwordEncoder.encode(r.password())); return CustomerResponse.from(customerRepository.save(c)); }
    @Transactional public void delete(Long id) { Customer c=find(id); c.setCustomerStatus(CustomerStatus.INACTIVE); customerRepository.save(c); }
    private Customer find(Long id) { return customerRepository.findById(id).orElseThrow(()->ApiException.notFound("Customer not found with id: "+id)); }
    private void ensureEmailAvailable(String email, Long excluded) { boolean exists=excluded==null?customerRepository.existsByEmailIgnoreCase(email):customerRepository.existsByEmailIgnoreCaseAndCustomerIdNot(email,excluded); if(exists||adminEmail.equalsIgnoreCase(email)) throw ApiException.conflict("Email is already in use: "+email); }
    private void apply(Customer c, AdminCustomerRequest r) { c.setCustomerName(r.customerName()); c.setTelephone(r.telephone()); c.setEmail(r.email()); c.setCustomerBirthday(r.customerBirthday()); c.setCustomerStatus(r.customerStatus()); }
}
