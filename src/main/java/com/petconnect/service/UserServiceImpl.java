package com.petconnect.service;

import com.petconnect.dto.UserRegistrationDto;
import com.petconnect.entity.Role;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;
import com.petconnect.repository.RoleRepository;
import com.petconnect.repository.ShelterRepository;
import com.petconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShelterRepository shelterRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, 
                           RoleRepository roleRepository, 
                           ShelterRepository shelterRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.shelterRepository = shelterRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User save(UserRegistrationDto registrationDto) {
        // Determine role name based on selection
        String tempRoleName = "ROLE_USER"; // default
        if ("owner".equalsIgnoreCase(registrationDto.getRole()) || 
            "ROLE_SHELTER_OWNER".equalsIgnoreCase(registrationDto.getRole()) || 
            "shelter_owner".equalsIgnoreCase(registrationDto.getRole())) {
            tempRoleName = "ROLE_SHELTER_OWNER";
        }
        
        final String roleName = tempRoleName;

        // Fetch role from DB
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        // Create User entity
        User user = new User();
        user.setFullName(registrationDto.getFullName() != null ? registrationDto.getFullName().trim() : "");
        user.setEmail(registrationDto.getEmail() != null ? registrationDto.getEmail().trim().toLowerCase() : "");
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setPhone(registrationDto.getPhone() != null ? registrationDto.getPhone().trim() : null);
        user.setRoles(Collections.singletonList(role));
        user.setEnabled(true);
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // If Shelter Owner, atomically create and link their Shelter
        if ("ROLE_SHELTER_OWNER".equals(roleName)) {
            String sName = (registrationDto.getShelterName() != null && !registrationDto.getShelterName().isBlank())
                    ? registrationDto.getShelterName().trim()
                    : (savedUser.getFullName() != null ? savedUser.getFullName() + "'s Shelter" : "Rescue Sanctuary");

            String sArea = (registrationDto.getArea() != null && !registrationDto.getArea().isBlank())
                    ? registrationDto.getArea().trim() : "";

            String sCity = (registrationDto.getCity() != null && !registrationDto.getCity().isBlank())
                    ? registrationDto.getCity().trim() : "Chennai";

            String sState = (registrationDto.getState() != null && !registrationDto.getState().isBlank())
                    ? registrationDto.getState().trim() : "Tamil Nadu";

            String sAddress = (registrationDto.getAddress() != null && !registrationDto.getAddress().isBlank())
                    ? registrationDto.getAddress().trim() : (sArea.isEmpty() ? sCity + ", " + sState : sArea + ", " + sCity + ", " + sState);

            String sLocation = (registrationDto.getLocation() != null && !registrationDto.getLocation().isBlank())
                    ? registrationDto.getLocation().trim() : (sCity + ", " + sState);

            Shelter shelter = new Shelter();
            shelter.setName(sName);
            shelter.setArea(sArea);
            shelter.setCity(sCity);
            shelter.setState(sState);
            shelter.setAddress(sAddress);
            shelter.setLocation(sLocation);
            shelter.setDescription("Rescue and animal welfare sanctuary providing boarding, medical aid, and adoption support.");
            shelter.setCapacity(20);
            shelter.setAvailableCapacity(10);
            shelter.setVerificationStatus("PENDING");
            shelter.setStatus("ACTIVE");
            shelter.setTemporaryCareEnabled(true);
            shelter.setPhone(savedUser.getPhone() != null ? savedUser.getPhone() : "9876543210");
            shelter.setEmail(savedUser.getEmail());
            shelter.setOwner(savedUser);
            shelter.setCreatedAt(LocalDateTime.now());

            shelterRepository.save(shelter);
        }

        return savedUser;
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
    }
}
