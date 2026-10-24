package com.petconnect.config;

import com.petconnect.entity.*;
import com.petconnect.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShelterRepository shelterRepository;
    private final PetRepository petRepository;

    @Autowired
    public DatabaseInitializer(UserRepository userRepository, 
                               RoleRepository roleRepository, 
                               PasswordEncoder passwordEncoder,
                               ShelterRepository shelterRepository,
                               PetRepository petRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.shelterRepository = shelterRepository;
        this.petRepository = petRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Seed Roles
        Role userRole = getOrCreateRole("ROLE_USER");
        Role ownerRole = getOrCreateRole("ROLE_SHELTER_OWNER");
        Role adminRole = getOrCreateRole("ROLE_ADMIN");

        // 2. Seed Default User (user@petconnect.com / user123)
        User user = userRepository.findByEmail("user@petconnect.com").orElse(null);
        if (user == null) {
            user = new User();
            user.setFullName("Sreenija Adopter");
            user.setEmail("user@petconnect.com");
            user.setPhone("9876543210");
            user.setRoles(Collections.singletonList(userRole));
        }
        user.setPassword(passwordEncoder.encode("user123"));
        user = userRepository.save(user);

        // 2b. Seed Second User (adopter2@petconnect.com / user123) for testing preference isolation
        User user2 = userRepository.findByEmail("adopter2@petconnect.com").orElse(null);
        if (user2 == null) {
            user2 = new User();
            user2.setFullName("Karthik Adopter");
            user2.setEmail("adopter2@petconnect.com");
            user2.setPhone("9876543219");
            user2.setRoles(Collections.singletonList(userRole));
            user2.setPassword(passwordEncoder.encode("user123"));
            userRepository.save(user2);
        }

        // 3. Seed Default Shelter Owner (owner@petconnect.com / owner123)
        User owner = userRepository.findByEmail("owner@petconnect.com").orElse(null);
        if (owner == null) {
            owner = new User();
            owner.setFullName("Rahul Shelter Owner");
            owner.setEmail("owner@petconnect.com");
            owner.setPhone("9876543211");
            owner.setRoles(Collections.singletonList(ownerRole));
        }
        owner.setPassword(passwordEncoder.encode("owner123"));
        owner = userRepository.save(owner);

        // 4. Seed Default Admin (admin@petconnect.com / admin123)
        User admin = userRepository.findByEmail("admin@petconnect.com").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setFullName("Platform Admin");
            admin.setEmail("admin@petconnect.com");
            admin.setPhone("9876543212");
            admin.setRoles(Collections.singletonList(adminRole));
        }
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin = userRepository.save(admin);

        // 5. Seed Shelters
        User saranya = userRepository.findByEmail("saranya@gmail.com").orElse(null);
        if (saranya != null) {
            saranya.setPassword(passwordEncoder.encode("saranya123"));
            userRepository.save(saranya);
        }
        User dhanushka = userRepository.findByEmail("dhanushka@gmail.com").orElse(null);

        Shelter happyPaws = shelterRepository.findByName("Happy Paws Shelter").orElse(null);
        User happyPawsOwner = (saranya != null) ? saranya : owner;
        if (happyPaws == null) {
            happyPaws = new Shelter(
                    "Happy Paws Shelter",
                    "Chennai, Tamil Nadu",
                    "We rescue and care for abandoned and rescued animals, helping them find loving forever homes.",
                    20, 5, "APPROVED", true,
                    "9876543210", "happy@paws.com", happyPawsOwner
            );
            happyPaws = shelterRepository.save(happyPaws);
        } else if (saranya != null && (happyPaws.getOwner() == null || !happyPaws.getOwner().getId().equals(saranya.getId()))) {
            happyPaws.setOwner(saranya);
            happyPaws = shelterRepository.save(happyPaws);
        }

        Shelter safeTails = shelterRepository.findByName("Safe Tails Shelter").orElse(null);
        if (safeTails == null) {
            safeTails = new Shelter(
                    "Safe Tails Shelter",
                    "Chennai, Tamil Nadu",
                    "Providing temporary care, medical aid, and adoption support for stray and abandoned pets.",
                    15, 3, "APPROVED", true,
                    "9876543211", "safe@tails.com", owner
            );
            safeTails = shelterRepository.save(safeTails);
        } else if (safeTails.getOwner() == null) {
            safeTails.setOwner(owner);
            safeTails = shelterRepository.save(safeTails);
        }

        Shelter pawsClaws = shelterRepository.findByName("Paws & Claws").orElse(null);
        User pawsOwner = (dhanushka != null) ? dhanushka : owner;
        if (pawsClaws == null) {
            pawsClaws = new Shelter(
                    "Paws & Claws",
                    "Chennai, Tamil Nadu",
                    "A loving community sanctuary providing high-quality boarding and adoption services for pets.",
                    10, 8, "APPROVED", true,
                    "9876543212", "paws@claws.com", pawsOwner
            );
            pawsClaws = shelterRepository.save(pawsClaws);
        } else if (dhanushka != null && (pawsClaws.getOwner() == null || !pawsClaws.getOwner().getId().equals(dhanushka.getId()))) {
            pawsClaws.setOwner(dhanushka);
            pawsClaws = shelterRepository.save(pawsClaws);
        }

        // 6. Seed Pets in MySQL
        if (petRepository.count() == 0) {
            petRepository.save(new Pet(
                    "Bruno", "DOG", "Labrador", 2, "Male",
                    "Bruno is a friendly, energetic Labrador who loves playing fetch and getting belly rubs. He gets along well with people and other pets.",
                    "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=800",
                    "Fully vaccinated, neutered, and dewormed.",
                    "APPROVED", "AVAILABLE", happyPaws, null
            ));

            petRepository.save(new Pet(
                    "Luna", "CAT", "Persian", 1, "Female",
                    "Luna is a quiet and affectionate Persian cat with a beautiful fluffy coat. She loves lounging in warm sun spots.",
                    "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&q=80&w=800",
                    "Vaccinated and spayed.",
                    "APPROVED", "AVAILABLE", safeTails, null
            ));

            petRepository.save(new Pet(
                    "Max", "DOG", "Beagle", 3, "Male",
                    "Max is an active Beagle who loves sniffing trails and running around. He is very playful and kid-friendly.",
                    "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=800",
                    "Fully vaccinated and neutered.",
                    "APPROVED", "AVAILABLE", pawsClaws, null
            ));

            petRepository.save(new Pet(
                    "Milo", "CAT", "Ragdoll", 1, "Male",
                    "Milo is a gentle Ragdoll cat who loves to be held. He has striking blue eyes and a soft color-point coat.",
                    "https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&q=80&w=800",
                    "Vaccinated and neutered.",
                    "APPROVED", "AVAILABLE", happyPaws, null
            ));

            petRepository.save(new Pet(
                    "Rocky", "DOG", "German Shepherd", 3, "Male",
                    "Rocky is a loyal and intelligent German Shepherd. He is well-trained, alert, and protective.",
                    "https://images.unsplash.com/photo-1589941013453-ec89f33b5e95?auto=format&fit=crop&q=80&w=800",
                    "Vaccinated, neutered, and microchipped.",
                    "APPROVED", "AVAILABLE", happyPaws, null
            ));

            petRepository.save(new Pet(
                    "Simba", "CAT", "Maine Coon", 2, "Male",
                    "Simba is a large, majestic Maine Coon. He is friendly, curious, and enjoys being around his human companions.",
                    "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?auto=format&fit=crop&q=80&w=800",
                    "Vaccinated and neutered.",
                    "APPROVED", "AVAILABLE", safeTails, null
            ));

            petRepository.save(new Pet(
                    "Buddy", "DOG", "Golden Retriever", 4, "Male",
                    "Buddy is a classic Golden Retriever who loves swimming and retrieving toys. He is exceptionally sweet-tempered.",
                    "https://images.unsplash.com/photo-1561037404-61cd46aa615b?auto=format&fit=crop&q=80&w=800",
                    "Fully vaccinated and neutered.",
                    "APPROVED", "AVAILABLE", happyPaws, null
            ));

            petRepository.save(new Pet(
                    "Coco", "CAT", "Siamese", 1, "Female",
                    "Coco is an elegant Siamese cat with striking blue eyes. She is vocal, playful, and highly interactive.",
                    "https://images.unsplash.com/photo-1592194996308-7b43878e84a6?auto=format&fit=crop&q=80&w=800",
                    "Vaccinated and spayed.",
                    "APPROVED", "AVAILABLE", safeTails, null
            ));
        }

        // Enrich PetMatch AI recommendation attributes for seeded pets
        for (Pet p : petRepository.findAll()) {
            if ("Bruno".equalsIgnoreCase(p.getName())) {
                p.setSize("Medium");
                p.setActivityLevel("High");
                p.setGoodWithChildren(true);
                p.setExperienceLevel("Beginner-friendly");
                p.setAgeCategory("Young");
                petRepository.save(p);
            } else if ("Luna".equalsIgnoreCase(p.getName())) {
                p.setSize("Small");
                p.setActivityLevel("Low");
                p.setGoodWithChildren(true);
                p.setExperienceLevel("Beginner-friendly");
                p.setAgeCategory("Young");
                petRepository.save(p);
            } else if ("Max".equalsIgnoreCase(p.getName())) {
                p.setSize("Medium");
                p.setActivityLevel("High");
                p.setGoodWithChildren(true);
                p.setExperienceLevel("Some Experience");
                p.setAgeCategory("Adult");
                petRepository.save(p);
            } else if ("Milo".equalsIgnoreCase(p.getName())) {
                p.setSize("Small");
                p.setActivityLevel("Low");
                p.setGoodWithChildren(true);
                p.setExperienceLevel("Beginner-friendly");
                p.setAgeCategory("Young");
                petRepository.save(p);
            } else if ("Rocky".equalsIgnoreCase(p.getName())) {
                p.setSize("Large");
                p.setActivityLevel("High");
                p.setGoodWithChildren(false); // Alert and protective, not suitable for young children
                p.setExperienceLevel("Experienced");
                p.setAgeCategory("Adult");
                petRepository.save(p);
            } else if ("Simba".equalsIgnoreCase(p.getName())) {
                p.setSize("Large");
                p.setActivityLevel("Medium");
                p.setGoodWithChildren(true);
                p.setExperienceLevel("Some Experience");
                p.setAgeCategory("Young");
                petRepository.save(p);
            } else if ("Buddy".equalsIgnoreCase(p.getName())) {
                p.setSize("Large");
                p.setActivityLevel("Medium");
                p.setGoodWithChildren(true);
                p.setExperienceLevel("Beginner-friendly");
                p.setAgeCategory("Adult");
                petRepository.save(p);
            } else if ("Coco".equalsIgnoreCase(p.getName())) {
                p.setSize("Small");
                p.setActivityLevel("Medium");
                p.setGoodWithChildren(true);
                p.setExperienceLevel("Beginner-friendly");
                p.setAgeCategory("Young");
                petRepository.save(p);
            }
        }

        // Ensure Charlie exists with activityLevel = null for missing data edge case testing (Test 11)
        if (petRepository.findAll().stream().noneMatch(p -> "Charlie".equalsIgnoreCase(p.getName()))) {
            Pet charlie = new Pet(
                    "Charlie", "DOG", "Pug", 2, "Male",
                    "Charlie is a sweet little pug with big curious eyes. Activity level is pending shelter assessment.",
                    "https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&q=80&w=800",
                    "Vaccinated and dewormed.",
                    "APPROVED", "AVAILABLE", happyPaws, null
            );
            charlie.setSize("Small");
            charlie.setActivityLevel(null); // Explicitly null
            charlie.setGoodWithChildren(true);
            charlie.setExperienceLevel("Beginner-friendly");
            charlie.setAgeCategory("Young");
            petRepository.save(charlie);
        }
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}
