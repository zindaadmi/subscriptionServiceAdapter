package com.subscription.subscriptionservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain model for User - Pure POJO with Lombok
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String mobileNumber;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Boolean deleted = false;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private AuthProvider provider = AuthProvider.LOCAL;
    private String providerId;
    private Boolean enabled = true;
    private Set<Role> roles = new HashSet<>();

    public enum AuthProvider {
        LOCAL,
        GOOGLE
    }

    // Domain methods
    public boolean hasRole(String roleName) {
        if (roles == null) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role != null && role.getName() != null && role.getName().name().equals(roleName));
    }

    public Set<String> getRoleNames() {
        Set<String> roleNames = new HashSet<>();
        if (roles != null) {
            for (Role role : roles) {
                if (role != null && role.getName() != null) {
                    roleNames.add(role.getName().name());
                }
            }
        }
        return roleNames;
    }

    public boolean isActive() {
        return (enabled == null || enabled) && (deleted == null || !deleted);
    }
}
