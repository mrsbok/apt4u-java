package io.apt4u.main.service;

import io.apt4u.main.model.storeForm.StoreForm;
import io.apt4u.main.model.trainerForm.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public interface StoreService {

	HashMap storeRegister(StoreForm storeForm);
	HashMap emailCheck(String userName);
	HashMap login(String userName,String password);

	HashMap approveList(Integer Idx);

	HashMap approveUpdate(Integer idx);
	HashMap storeCoachCount(Integer idx);

	HashMap storeName(Integer idx);

}