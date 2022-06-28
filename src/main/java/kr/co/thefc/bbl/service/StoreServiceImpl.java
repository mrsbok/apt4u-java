package kr.co.thefc.bbl.service;


import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import kr.co.thefc.bbl.converter.JwtProvider;
import kr.co.thefc.bbl.converter.PasswordCryptConverter;
import kr.co.thefc.bbl.model.trainerForm.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class StoreServiceImpl implements StoreService {

  private Gson gson = new Gson();
  HashMap rtnVal = new HashMap();

  @Autowired
  private DBConnService dbConnService;


  @Autowired
  private S3Service s3Service;



}
