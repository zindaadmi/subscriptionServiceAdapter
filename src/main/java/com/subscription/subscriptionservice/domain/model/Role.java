package com.subscription.subscriptionservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for Role - Pure POJO with Lombok
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Long id;
    private RoleName name;

    public enum RoleName {
        ROLE_USER,
        ROLE_ADMIN,
        ROLE_AGENT
    }
}
