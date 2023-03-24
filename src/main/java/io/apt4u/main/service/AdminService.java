package io.apt4u.main.service;

import io.apt4u.main.model.adminForm.AdminForm;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public interface AdminService {

	HashMap adminRegister(AdminForm adminForm);
	HashMap emailCheck(String userName);
	HashMap login(String userName,String password);

	HashMap approveList();

	HashMap approveUpdate(Integer idx);
}