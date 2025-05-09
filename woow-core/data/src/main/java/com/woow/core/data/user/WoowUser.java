package com.woow.core.data.user;

import com.woow.security.api.SecurityUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class WoowUser implements SecurityUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    private LocalDate birth;
    private String sex;
    private String name;
    private String userName;
    private String lastName;
    private String country;
    private String password;
    private String email;
    private String acceptTermsAndConditions;
    private String mobilePhone;
    private String city;
    private String state;
    private String cp;
    private String addressLine1;
    private String addressLine2;
    private String imgURL;
    private String nationality;
    private boolean userActive;
    private boolean phoneNumberConfirm;
    private boolean emailConfirm;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Getter
    @Setter
    private Set<String> roles = new HashSet<>();


    int is_user_blocked;
    int login_attempts;
    int mfa;

    public List<String> getSecurityRoles() {
        List<String> roleList = new ArrayList<>();
        roleList.addAll(roles);
        return roleList;
    }

    @Transient
    public int getAge() {
        if (birth == null) return 0;
        return java.time.Period.between(birth,
                java.time.LocalDate.now()
        ).getYears();
    }

    public boolean getUserActive() {
        return userActive;
    }

    public void setUserActive(boolean userActive) {
        this.userActive = userActive;
    }

    public boolean getPhoneNumberConfirm() {
        return phoneNumberConfirm;
    }

    public void setPhoneNumberConfirm(boolean phoneNumberConfirm) {
        this.phoneNumberConfirm = phoneNumberConfirm;
    }

    public boolean getEmailConfirm() {
        return emailConfirm;
    }

    public void setEmailConfirm(boolean emailConfirm) {
        this.emailConfirm = emailConfirm;
    }

    @Override
    public String getTenantId() {
        return null;
    }
}
