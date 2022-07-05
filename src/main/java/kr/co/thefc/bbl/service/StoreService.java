package kr.co.thefc.bbl.service;

import kr.co.thefc.bbl.model.storeForm.StoreForm;
import kr.co.thefc.bbl.model.trainerForm.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;

@Service
public interface StoreService {

	HashMap storeRegister(StoreForm storeForm);
	HashMap emailCheck(String userName);
	HashMap login(String userName,String password);

	HashMap approveList(Integer Idx);

	HashMap approveUpdate(Integer idx, String notice);

}