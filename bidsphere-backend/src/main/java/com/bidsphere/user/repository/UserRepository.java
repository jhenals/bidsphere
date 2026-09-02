package com.bidsphere.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bidsphere.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> { }
