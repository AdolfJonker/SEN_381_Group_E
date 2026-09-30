package com.example.demo.repo;

import com.example.demo.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
	List<UserAccount> findAllByOrderByNameAsc();
}
