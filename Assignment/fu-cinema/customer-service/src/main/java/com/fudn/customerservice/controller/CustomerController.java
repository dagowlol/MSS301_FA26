package com.fudn.customerservice.controller;

import com.fudn.customerservice.dto.*;
import com.fudn.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/customers") @RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public CustomerResponse register(@Valid @RequestBody RegisterRequest r){return customerService.register(r);}
    @GetMapping("/me") public CustomerResponse me(@RequestHeader("X-User-Id") Long id){return customerService.getProfile(id);}
    @PutMapping("/me") public CustomerResponse updateMe(@RequestHeader("X-User-Id") Long id,@Valid @RequestBody ProfileUpdateRequest r){return customerService.updateProfile(id,r);}
    @PutMapping("/me/password") @ResponseStatus(HttpStatus.NO_CONTENT) public void password(@RequestHeader("X-User-Id") Long id,@Valid @RequestBody ChangePasswordRequest r){customerService.changePassword(id,r);}
    @GetMapping public List<CustomerResponse> search(@RequestParam(required=false) String keyword){return customerService.search(keyword);}
    @GetMapping("/{id}") public CustomerResponse get(@PathVariable Long id){return customerService.getById(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public CustomerResponse create(@Valid @RequestBody AdminCustomerRequest r){return customerService.create(r);}
    @PutMapping("/{id}") public CustomerResponse update(@PathVariable Long id,@Valid @RequestBody AdminCustomerRequest r){return customerService.update(id,r);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){customerService.delete(id);}
}
