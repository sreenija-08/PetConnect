package com.petconnect.service;

import com.petconnect.entity.User;
import com.petconnect.dto.UserRegistrationDto;

public interface UserService {
    User save(UserRegistrationDto registrationDto);
    User save(User user);
    User findByEmail(String email);
}
