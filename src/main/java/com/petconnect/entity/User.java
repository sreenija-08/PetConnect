package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    private String city;

    private boolean enabled = true;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Collection<Role> roles = new HashSet<>();

    public User() {}

    public User(String fullName, String email, String password, String phone, Collection<Role> roles) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.roles = roles;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Collection<Role> getRoles() { return roles; }
    public void setRoles(Collection<Role> roles) { this.roles = roles; }

    public String getStatus() {
        if (status != null && !status.isBlank()) return status;
        return enabled ? "ACTIVE" : "INACTIVE";
    }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPrimaryRoleName() {
        if (roles != null) {
            boolean hasAdmin = roles.stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()) || "ADMIN".equalsIgnoreCase(r.getName()));
            if (hasAdmin) return "ADMIN";
            boolean hasOwner = roles.stream().anyMatch(r -> "ROLE_SHELTER_OWNER".equalsIgnoreCase(r.getName()) || "SHELTER_OWNER".equalsIgnoreCase(r.getName()));
            if (hasOwner) return "SHELTER_OWNER";
        }
        return "USER";
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    public boolean isActive() {
        return enabled && !"INACTIVE".equalsIgnoreCase(this.status);
    }

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }
}
