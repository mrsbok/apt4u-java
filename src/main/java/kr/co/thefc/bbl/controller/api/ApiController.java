package kr.co.thefc.bbl.controller.api;

import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import io.jsonwebtoken.Jwts;
import io.swagger.annotations.ApiOperation;
import kr.co.thefc.bbl.converter.JwtProvider;
import kr.co.thefc.bbl.converter.PasswordCryptConverter;
import kr.co.thefc.bbl.model.writeForm.FreeTalksWriteForm;
import kr.co.thefc.bbl.model.writeForm.ReviewWriteForm;
import kr.co.thefc.bbl.service.DBConnService;
import kr.co.thefc.bbl.service.S3Service;
import kr.co.thefc.bbl.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private DBConnService dbConnService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private UserServiceImpl userService;

    private Gson gson = new Gson();

    @RequestMapping(value="/now", method = RequestMethod.POST)
    public HashMap now() {
        log.info("####now#####");

        HashMap rtnVal = new HashMap();

        List<HashMap> list = dbConnService.select("select_now_api", null);
        rtnVal.put("now", list.get(0).get("now"));

        return rtnVal;
    }

    @RequestMapping(value="/getPTLessonVouchers", method = RequestMethod.POST)
    @ApiOperation(value = "PT 상품 목록 조회",
            notes = "")
    public HashMap getPTLessonVouchers() {
        log.info("####getPTLessonVouchers#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try {
            HashMap map = new HashMap();

            // category 1:store, 2:product, 3:trainer
            map.put("noteCategory", "2");

            // 프로필, 근무경력, 수상경력, 자격증 등등
            map.put("imageType", "프로필");

            List<HashMap> list = dbConnService.select("getPTLessonVouchers", map);

            if(list.isEmpty()) {
                error = "PT 상품 목록을 찾을 수 없습니다.";
            } else {
                HashMap infos = new HashMap();

                list = userService.getPTLessonVoucherSellerInfo(list, map);

                infos.put("products", list);
                rtnVal.put("infos", infos);
            }

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error !=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }

        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/searchPTLessonVouchers", method = RequestMethod.POST)
    @ApiOperation(value = "PT 상품 검색",
            notes = "{\"keyword\":\"홍길동\"}")
    public HashMap searchPTLessonVouchers(@RequestBody String data) {
        log.info("####searchPTLessonVouchers#####");

        HashMap rtnVal = new HashMap();
        String error = null;
        JSONParser parser = new JSONParser();

        try {
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();

            if(jsonData.isEmpty()) {
                error = "Data is empty";
            } else {
                jsonData.forEach((key, value) -> map.put(key,value));

                // category 1:store, 2:product, 3:trainer
                map.put("noteCategory", "2");
                // 프로필, 근무경력, 수상경력, 자격증 등등
                map.put("imageType", "프로필");

                List<HashMap> list = dbConnService.select("searchPTLessonVouchers", map);

                if(list.isEmpty()) {
                    error = "PT 상품 목록을 찾을 수 없습니다.";
                } else {
                    HashMap infos = new HashMap();

                    list = userService.getPTLessonVoucherSellerInfo(list, map);

                    infos.put("products", list);
                    rtnVal.put("infos", infos);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error !=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }

        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getPTLessonVoucherDetail", method = RequestMethod.POST)
    @ApiOperation(value = "PT 상품 상세보기",
            notes = "{\"productIdx\":\"1\"}")
    public HashMap getPTLessonVoucherDetail(@RequestBody String data) {
        log.info("####getPTLessonVoucherDetail##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();

            if(jsonData.isEmpty()) {
                error = "Data is empty";
            } else {
                HashMap infos = new HashMap();
                jsonData.forEach((key, value) -> map.put(key,value));

                HashMap temp = userService.getPTLessonVoucherDetail(map);

                if(temp != null) {
                    infos.put("productInfo", temp);
                    rtnVal.put("infos", infos);
                } else {
                    error = "INDEX " + map.get("productIdx") + " NOT FOUND ! ";
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }


    @RequestMapping(value="/getPTTrainers", method = RequestMethod.POST)
    @ApiOperation(value = "PT 트레이너 목록 조회",
        notes = "")
    public HashMap getPTTrainers(HttpServletRequest auth) {
        log.info("####getPTTrainers#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            // 프로필, 근무경력, 수상경력, 자격증 등등
            map.put("imageType", "프로필");

            List<HashMap> list = dbConnService.select("getPTTrainers", map);

            if(list.isEmpty()) {
                error = "Not found PTTrainers List";
            } else {
                HashMap infos = new HashMap();
                infos.put("PTTrainers", list);

                rtnVal.put("infos", infos);
            }


        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getPTTrainerDetail", method = RequestMethod.POST)
    @ApiOperation(value = "PT 트레이너 상세 조회",
        notes = "{\"PTTrainerIdx\":\"1\"}")
    public HashMap getPTTrainerDetail(@RequestBody String data) {
        log.info("####getPTTrainerDetail##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try {
            JSONObject jsonData = (JSONObject) parser.parse(data);

            if (jsonData.isEmpty()) {
                error = "Data is empty";
            } else {
                HashMap map = new HashMap();
                Set set = jsonData.keySet();
                jsonData.forEach((key, value) -> map.put(key, value));

                HashMap infos = new HashMap();
                infos.put("PTTrainerInfo", userService.getPTTrainerDetail(map));

                rtnVal.put("infos", infos);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getShoppingItems", method = RequestMethod.POST)
    @ApiOperation(value = "장바구니 목록 조회",
        notes = "")
    public HashMap getShoppingItems(HttpServletRequest auth) {
        log.info("####getShoppingItems#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getShoppingItems", map);

            if(list.isEmpty()) {
                error = "장바구니가 비어있습니다.";
            } else {
                HashMap infos = new HashMap();
                infos.put("usersShoppingBasketProducts", list);

                int totalAmount = 0;

                for(int i=0; i<list.size(); i++) {
                    int price = Integer.parseInt(list.get(i).get("price").toString());
                    int discountPrice = Integer.parseInt(list.get(i).get("discountPrice").toString());

                    if(discountPrice > 0) {
                        totalAmount = totalAmount + discountPrice;
                    } else {
                        totalAmount = totalAmount + price;
                    }
                }
                infos.put("totalAmount", totalAmount);

                rtnVal.put("infos", infos);
            }

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/addShoppingItems", method = RequestMethod.POST)
    @ApiOperation(value = "장바구니 상품 추가",
        notes = "{\"productIdx\":\"1\", \"productCategory\":\"1\", \"quantity\":\"1\"}" +
            "\n\nproductCategory(=CD_ProductClassification) 1: PTVoucher")
    public HashMap addShoppingItems(@RequestBody String data, HttpServletRequest auth) {
        log.info("####addShoppingItems##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            int result = dbConnService.insert("addShoppingItems", map);

            if(result == 0) {
                error = "Failed to add to shopping basket";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/deleteShoppingItems", method = RequestMethod.POST)
    @ApiOperation(value = "장바구니 상품 삭제",
        notes = "{\"usersShoppingBasketIdx\":\"1\"}")
    public HashMap deleteShoppingItems(@RequestBody String data) {
        log.info("####deleteShoppingItems##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            int result = dbConnService.delete("deleteShoppingItems", map);

            if(result == 0) {
                error = "Failed to delete from shopping basket";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    // 찜
    @RequestMapping(value="/getUserPick", method = RequestMethod.POST)
    @ApiOperation(value = "사용자 찜 목록",
        notes = "{\"category\":\"1\"}" +
            "\n\ncategory 1:운동 상품, 2:뷰티")
    public HashMap getUserPick(@RequestBody String data, HttpServletRequest auth) {
        log.info("####getUserPick##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = null;

            if(map.get("category").equals("1")) {
                list = dbConnService.select("getUserPick_Products", map);
            } else if (map.get("category").equals("2")) {

            }

            if(list == null) {
                error = "User index number " + map.get("userIdx") + " and category number " + map.get("category") +  " is not found";
            } else {
                HashMap infos = new HashMap();

                if(map.get("category").equals("1")) {
                    infos.put("products", list);
                }
                else if (map.get("category").equals("2")) {

                }

                rtnVal.put("infos", infos);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getUserPickDetail", method = RequestMethod.POST)
    @ApiOperation(value = "사용자 찜 상품 상세 보기",
        notes = "{\"pickedItemIdx\":\"1\", \"category\":\"1\"}" +
            "\n\ncategory 1:운동 상품, 2:뷰티")
    public HashMap getUserPickDetail(@RequestBody String data) {
        log.info("####getUserPickDetail##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try {
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            jsonData.forEach((key, value) -> map.put(key, value));

            HashMap infos = new HashMap();
            HashMap temp = new HashMap();

            if (map.get("category").equals("1")) {
                // 1:운동 상품, 2:뷰티
                map.put("productIdx", map.get("pickedItemIdx"));

                temp = userService.getPTLessonVoucherDetail(map);

                if(temp != null) {
                    infos.put("productInfo", temp);
                } else {
                    error = "INDEX " + map.get("productIdx") + " NOT FOUND ! ";
                }
            } else if (map.get("category").equals("2")) {

            } else {
                error = "잘못된 카테고리 항목입니다.";
            }

            rtnVal.put("infos", infos);
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getUsersPicks", method = RequestMethod.POST)
    @ApiOperation(value = "사용자의 상품 찜 여부",
        notes = "{\"category\":\"2\", \"pickedItemIdx\":\"1\"}" +
            "\n\ncategory 1:업체, 2:상품, 3:트레이너")
    public HashMap getUsersPicks(@RequestBody String data, HttpServletRequest auth) {
        log.info("####getUsersPicks##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getUsersPicks", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                infos.put("usersPicksYN", false);
            } else {
                infos.put("usersPicksYN", true);
            }

            rtnVal.put("infos", infos);

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/addUserPick", method = RequestMethod.POST)
    @ApiOperation(value = "사용자 찜 상품 추가",
        notes = "{\"category\":\"2\", \"pickedItemIdx\":\"1\"}" +
            "\n\ncategory 1:업체, 2:상품, 3:트레이너")
    public HashMap addUserPick(@RequestBody String data, HttpServletRequest auth) {
        log.info("####addUserPick##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getUsersPicks", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                int result = dbConnService.insert("addUserPick", map);

                if(result == 0) {
                    error = "Failed to add to user pick list";
                } else {
                    rtnVal.put("result1", false);
                }
            } else {
                error = "Product is already picked by the user.";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/deleteUserPick", method = RequestMethod.POST)
    @ApiOperation(value = "사용자 찜 상품 삭제",
        notes = "{\"category\":\"2\", \"pickedItemIdx\":\"1\"}")
    public HashMap deleteUserPick(@RequestBody String data, HttpServletRequest auth) {
        log.info("####deleteUserPick##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getUsersPicks", map);
            HashMap infos = new HashMap();

            if(!list.isEmpty()) {
                int result = dbConnService.insert("deleteUserPick", map);

                if(result == 0) {
                    error = "Failed to delete from user pick list";
                } else {
                    rtnVal.put("result1", true);
                }
            } else {
                error = "Product is not picked by the user.";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/buySingleProduct_purchaseInfo", method = RequestMethod.POST)
    @ApiOperation(value = "상품 구매 - 구매 정보(단일 상품 구매)",
        notes = "{\"productIdx\":\"8\"}")
    public HashMap buySingleProduct_purchaseInfo(HttpServletRequest auth, @RequestBody String data) {
        log.info("####buySingleProduct_purchaseInfo#####");

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            HashMap product = dbConnService.selectOne("getPurchaseInfo_product", map);

            if(product.isEmpty()) {
                error = map.get("productIdx") + "번 인덱스 상품을 찾지 못했습니다";
            } else {
                infos.put("productInfo", product);
            }

            HashMap temp = dbConnService.selectOne("getUsersInfo", map);

            HashMap user = new HashMap();

            if(!temp.isEmpty()) {
                user.put("userName", temp.get("userName"));
                user.put("userTelephone", temp.get("telephone"));
                user.put("totalPoint", dbConnService.selectWithReturnInt("getTotalPoint", map));
            }

            infos.put("consumerInfo", user);

            rtnVal.put("infos", infos);

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/buyBundleProduct_purchaseInfo", method = RequestMethod.POST)
    @ApiOperation(value = "상품 구매 - 구매 정보(묶음 상품 구매)",
        notes = "{\"products\":[{\"productIdx\":\"8\"}, {\"productIdx\":\"4\"}]}")
    public HashMap buyBundleProduct_purchaseInfo(HttpServletRequest auth, @RequestBody String data) {
        log.info("####buySingleProduct_purchaseInfo#####");

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            jsonData.forEach((key, value) -> map.put(key,value));

            JSONArray arr = (JSONArray) map.get("products");

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = new ArrayList<>();

            for(int i=0; i<arr.size(); i++) {
                JSONObject obj = (JSONObject) arr.get(i);
                obj.forEach((key, value) -> map.put(key, value));

                HashMap product = dbConnService.selectOne("getPurchaseInfo_product", map);

                if(product.isEmpty()) {
                    error = map.get("productIdx") + "번 인덱스 상품을 찾지 못했습니다";
                } else {
                    list.add(product);
                }
            }
            infos.put("productInfo", list);

            HashMap temp = dbConnService.selectOne("getUsersInfo", map);

            HashMap user = new HashMap();

            if(!temp.isEmpty()) {
                user.put("userName", temp.get("userName"));
                user.put("userTelephone", temp.get("telephone"));
                user.put("totalPoint", dbConnService.selectWithReturnInt("getTotalPoint", map));
            }

            infos.put("consumerInfo", user);

            rtnVal.put("infos", infos);

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/buyProduct", method = RequestMethod.POST)
    @ApiOperation(value = "상품 구매",
        notes = "{\"userTelephone\":\"01012341234\", \"pointUse\":\"10000\", \"billingMethod\":\"1\", " +
            "\"totalAmount\":\"25000\", \"billingAmount\":\"15000\"," +
            "\n\n\"products\":[\n\n{\"productCategory\":\"1\", \"productIdx\":\"8\", \"price\":\"25000\", " +
            "\"quantity\":\"1\", \"amount\":\"25000\", \"sellerType\":\"1\", \"sellerIdx\":\"16\"}\n\n]}" +
            "\n\nbillingMethod 1: 무통장입금, 2: 신용/체크카드, 3: 카카오페이, 4: 삼성페이, 5: 페이코, 6: 토스" +
            "\n\nproductCategory 1: PTVoucher, ... " +
            "\n\nproducts: 여러 개의 데이터가 될 수 있음")
    public HashMap buyProduct(@RequestBody String data, HttpServletRequest auth) {
        log.info("####buyProduct##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            jsonData.forEach((key, value) -> map.put(key,value));

            JSONArray arr = (JSONArray) map.get("products");

            int kindOfItem = arr.size();
            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);
            map.put("kindOfItem", kindOfItem);

            int result = dbConnService.insert("insertTransaction", map);

            if(result > 0)  {
                for(int i=0; i<arr.size(); i++) {
                    JSONObject obj = (JSONObject) arr.get(i);
                    obj.forEach((key, value) -> map.put(key, value));

                    result = dbConnService.insert("insertTransactionDetail", map);
                    if(result == 0) {
                        error = "INSERT Transaction Detail FAILED";
                    }
                }

                // 포인트 사용
                if(Integer.parseInt(map.get("pointUse").toString()) > 0) {
                    pointUse(auth, map);
                }

                if(Integer.parseInt(map.get("totalAmount").toString()) > 0) {
                    int billingMethod = Integer.parseInt(map.get("billingMethod").toString());

                    if (billingMethod == 1) {
                        // 무통장 입금 안내 메세지
                    } else if (billingMethod == 2) {
                        // 카드 결제 시스템 호출

                        // 카드 결제 성공 시 결제 여부와 결제 일시(tbl_transactions : billingYN, billingDate UPDATE)
                        // + validTicket에 데이터 저장
                        result = dbConnService.update("updateTransaction", map);

                        for(int i=0; i<arr.size(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            obj.forEach((key, value) -> map.put(key, value));

                            result = dbConnService.insert("insertValidTicket", map);
                            if(result == 0) {
                                error = "INSERT valid_ticket FAILED";
                            }
                        }

                        // 카드 결제 실패 시 재시도
                    }
                }

                // 구매 완료 알림 메세지 전송(유저 휴대폰번호)
                for(int i=0; i<arr.size(); i++) {
                    JSONObject obj = (JSONObject) arr.get(i);
                    obj.forEach((key, value) -> map.put(key, value));

                    HashMap product = dbConnService.selectOne("getPTLessonVoucherDetail", map);
                    HashMap user = dbConnService.selectOne("getUsersInfo", map);

                    if("1".equals(product.get("productType").toString())) {//productType = 1 (상품 타입이 PTVoucher이면)
                        map.put("title", "PT 이용권 구매 알림");
                        map.put("content",
                                "[ "+user.get("userName")+" ] 회원님이 PT 이용권을 구매하셨습니다. \n"
                                +user.get("userName")+" 회원님의 휴대폰 번호는 " + userService.phoneFormat(map.get("userTelephone").toString()) + "입니다.");
                        map.put("receiverType", "3");
                        map.put("receiverIdx", map.get("sellerIdx"));
                        map.put("messageType", "5");
                        map.put("typeIdx", map.get("transactionIdx"));

                        result = dbConnService.insert("sendMessage", map);
                        if(result == 0) {
                            error = "INSERT message FAILED";
                        }
                    }



                }
            } else {
                error = "transaction insert failed";
            }



        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    public HashMap pointUse(HttpServletRequest auth, @RequestBody HashMap map) {
        log.info("####pointUse#####");

                HashMap rtnVal = new HashMap();
                String error = null;

                try{
                    HashMap infos = new HashMap();

                    String token = auth.getHeader("token");
                    int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

                    map.put("userIdx", idx);

                    List<HashMap> list = dbConnService.select("getUsersSavingPoints", map);

                    if(list.isEmpty()) {
                        error = "포인트 정보가 없습니다.";
                    } else {
                        int pointUse = Integer.parseInt(map.get("pointUse").toString());
                        int totalHoldPoint = 0;

                        for (int i = 0; i < list.size(); i++) {
                            int inPracticalPoint = Integer.parseInt(list.get(i).get("in_practical_point").toString());

                            totalHoldPoint += inPracticalPoint;
                        }

                        if (totalHoldPoint > pointUse) {
                            for (int i = 0; i < list.size(); i++) {
                                int inPracticalPoint = Integer.parseInt(list.get(i).get("in_practical_point").toString());

                                map.put("savingPointsIseq", list.get(i).get("iseq"));
                                map.put("subtractionType", 1);

                                if (inPracticalPoint > 0) {
                                    if (pointUse > 0) {
                                        if (pointUse > inPracticalPoint) {
                                            map.put("subtractionPoint", inPracticalPoint);

                                            pointUse = pointUse - inPracticalPoint;
                                            inPracticalPoint = 0;

                                            map.put("inPracticalPoint", inPracticalPoint);

                                            dbConnService.update("updateInPracticalPoints", map);
                                            dbConnService.insert("updateSubtractionPoints", map);
                                        } else if (pointUse <= inPracticalPoint) {
                                            map.put("subtractionPoint", pointUse);

                                            inPracticalPoint = inPracticalPoint - pointUse;
                                            pointUse = 0;

                                            map.put("inPracticalPoint", inPracticalPoint);

                                            dbConnService.update("updateInPracticalPoints", map);
                                            dbConnService.insert("updateSubtractionPoints", map);
                                        }
                                    }
                                }
                            }
                        } else {
                            error = "사용하고자 하는 포인트가 보유 포인트보다 적습니다.";
                        }
                    }

                    rtnVal.put("infos", infos);
                } catch (Exception e) {
                    e.printStackTrace();
                    error = "정보를 파싱하지 못했습니다.";
                }

                if (error!=null) {
                    rtnVal.put("result", false);
                }
                else {
                    rtnVal.put("result", true);
                }
                rtnVal.put("errorMsg", error);

                return rtnVal;
    }

    @RequestMapping(value="/getTransactions", method = RequestMethod.POST)
    @ApiOperation(value = "구매 목록 보기",
        notes = "")
    public HashMap getTransactions(HttpServletRequest auth) {
        log.info("####getTransactions#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getTransactions", map);

            if(list.isEmpty()) {
                error = "User index number " + map.get("userIdx") + " and category number " + map.get("category") +  " is not found";
            } else {
                HashMap infos = new HashMap();
                infos.put("Transactions", list);

                rtnVal.put("infos", infos);
            }

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getTransactionDetail", method = RequestMethod.POST)
    @ApiOperation(value = "구매 목록 상세 보기", notes = "{\"transactionIdx\":\"8\"}")
    public HashMap getTransactionDetail(@RequestBody String data) {
        log.info("####getTransactionDetail##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getTransactionDetail", map);

            if(list.isEmpty()) {
                error = "Transaction index " +jsonData.values() + " not found";
            } else {
                HashMap infos = new HashMap();
                infos.put("transactionsDetail", list);

                // product 데이터 가져오기
                list = dbConnService.select("getTransactionDetail_product", map);
                infos.put("productInfo", list);

                rtnVal.put("infos", infos);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/cancelTransaction", method = RequestMethod.POST)
    @ApiOperation(value = "구매 취소 신청", notes = "{\"transactionIdx\":\"10\", \"refundFee\":\"3000\", \"refundAmount\":\"\", \"cancelReason\":\"3\", \"reasonDetail\":\"지역이 멀어요.\"}")
    public HashMap cancelTransaction(@RequestBody String data) {
        log.info("####cancelTransaction##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            int selectResult = dbConnService.selectWithReturnInt("getCancelYN", map);
            int cancelYN = selectResult;

            if(cancelYN == 0) {
                int updateResult = dbConnService.update("updateCancel", map);

                dbConnService.insert("setCancelReason", map);

                if(updateResult > 0) {
                    selectResult = dbConnService.selectWithReturnInt("getBillingYN", map);
                    int billingYN = selectResult;

                    // 결제가 완료된 상태라면 환불 진행
                    if(billingYN == 1) {
                        // 환불 진행(:포인트 사용여부도 확인) 후 refundYN, refundDate update
                        System.out.println("환불 처리 ,,,");
                        updateResult = dbConnService.update("updateRefund", map);

                        if(updateResult > 0) {
                            System.out.println("환불 완료");
                        }
                    }
                }

            } else {
                error = "이미 취소된 구매 목록입니다.";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getCancelTransactions", method = RequestMethod.POST)
    @ApiOperation(value = "취소/환불 목록 보기",
        notes = "")
    public HashMap getCancelTransactions(HttpServletRequest auth) {
        log.info("####getCancelTransactions#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getCancelTransactions", map);

            if(list.isEmpty()) {
                error = "User index number " + map.get("userIdx") + " and category number " + map.get("category") +  " is not found";
            } else {
                HashMap infos = new HashMap();
                infos.put("CancelTransactions", list);

                rtnVal.put("infos", infos);
            }

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

//    @RequestMapping(value="/getCancelTransactionDetail", method = RequestMethod.POST)
//    @ApiOperation(value = "취소/환불 내역 상세 보기",
//        notes = "{\"transactionIdx\":\"1\"}")
//    public HashMap getCancelTransactionDetail(HttpServletRequest auth, @RequestBody String data) {
//        log.info("####getCancelTransactionDetail##### : " + data);
//
//        HashMap rtnVal = new HashMap();
//        JSONParser parser = new JSONParser();
//        String error = null;
//
//        try{
//            JSONObject jsonData = (JSONObject) parser.parse(data);
//
//            HashMap map = new HashMap();
//            jsonData.forEach((key, value) -> map.put(key,value));
//
//            String token = auth.getHeader("token");
//            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));
//
//            map.put("userIdx", idx);
//
//            List<HashMap> list = dbConnService.select("getCancelTransactionDetail", map);
//
//            if(list.isEmpty()) {
//                error = "User index number " + map.get("userIdx") + " and category number " + map.get("category") +  " is not found";
//            } else {
//                HashMap infos = new HashMap();
//                infos.put("CancelTransactions", list);
//
//                rtnVal.put("infos", infos);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            error = "정보를 파싱하지 못했습니다.";
//        }
//
//        if (error!=null) {
//            rtnVal.put("result", false);
//        }
//        else {
//            rtnVal.put("result", true);
//        }
//        rtnVal.put("errorMsg", error);
//
//        return rtnVal;
//    }

    @RequestMapping(value="/addUserPTRecords", method = RequestMethod.POST)
    @ApiOperation(value = "개인 운동 일정 등록",
        notes = "{\"date\":\"2022-03-24\", \"exerciseCategory\":\"1\", \"exerciseType\":\"2\", " +
            "\"exerciseName\":\"푸쉬업\", \"exerciseDetails\":\"70,0,10,5\"}" +
            "\n\nexerciseCategory : 운동 종목(1: PT&헬스, 2: 필라테스&요가)" +
            "\n\nexerciseType : PT 운동 구분(열거형 데이터 정의 필요)" +
            "\n\nexerciseName : 운동 명칭" +
            "\n\nexerciseDetails : 중량,지속시간,운동횟수,세트수 순서대로 기입 만약 데이터가 없으면 0이 들어가게")
    public HashMap addUserPTRecords(@RequestBody String data, HttpServletRequest auth) {
        log.info("####addUserPTRecords##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            int result = dbConnService.insert("addUserPTRecords", map);

            if (result == 0) {
                error = "users_pt_records 데이터 등록 실패";
            } else {
                result = dbConnService.insert("addUserPTContents", map);

                int exerciseCount = dbConnService.selectWithReturnInt("getExerciseCount", map);

                if(exerciseCount > 0) {
                    map.put("exerciseCount", exerciseCount);
                    dbConnService.update("setUserPTRecordsExerciseCount", map);
                }
                if (result == 0) {
                    error = "users_pt_contents 데이터 등록 실패";
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getUserPTRecords", method = RequestMethod.POST)
    @ApiOperation(value = "개인 운동 일정 보기", notes = "")
    public HashMap getUserPTRecords(HttpServletRequest auth) {
        log.info("####getUserPTRecords#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getUserPTRecords", map);

            if(list.isEmpty()) {
                error = "User index " + idx + " not found";
            } else {
                HashMap infos = new HashMap();

                for(int i = 0; i < list.size(); i++) {
                    String exerciseDetails = String.valueOf(list.get(i).get("exerciseDetails"));

                    String[] array = exerciseDetails.split(",");

                    if(array.length > 1) {
                        list.get(i).put("weight", array[0]);
                        list.get(i).put("time", array[1]);
                        list.get(i).put("numberOfExercise", array[2]);
                        list.get(i).put("numberOfSet", array[3]);
                    }
                }

                infos.put("PTRecords", list);

                rtnVal.put("infos", infos);
            }

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/deleteUserPTRecords", method = RequestMethod.POST)
    @ApiOperation(value = "개인 운동 일정 삭제", notes = "{\"recordIdx\":\"9\"}")
    public HashMap deleteUserPTRecords(@RequestBody String data) {
        log.info("####deleteUserPTRecords##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            int result = dbConnService.delete("deleteUserPTRecord", map);

            if(result == 0) {
                error = "레코드 삭제 실패";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getUserPTRecordsWithTrainer", method = RequestMethod.POST)
    @ApiOperation(value = "트레이너가 등록한 운동 일정 보기", notes = "")
    public HashMap getUserPTRecordsWithTrainer(HttpServletRequest auth) {
        log.info("####getUserPTRecordsWithTrainer#####");

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getUserPTRecordsWithTrainer", map);

            if(list.isEmpty()) {
                error = "User index " + idx + " not found";
            } else {
                HashMap infos = new HashMap();

                for(int i = 0; i < list.size(); i++) {
                    String exerciseDetails = String.valueOf(list.get(i).get("exerciseDetails"));

                    String[] array = exerciseDetails.split(",");

                    if(array.length > 1) {
                        list.get(i).put("weight", array[0]);
                        list.get(i).put("time", array[1]);
                        list.get(i).put("numberOfExercise", array[2]);
                        list.get(i).put("numberOfSet", array[3]);
                    }

                }

                infos.put("PTRecordsWithTrainer", list);

                rtnVal.put("infos", infos);
            }

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/checkedUnreadMessage", method = RequestMethod.POST)
    @ApiOperation(value = "읽지 않은 알림 확인", notes = "")
    public HashMap checkedUnreadMessage(HttpServletRequest auth) {
        log.info("####checkedUnreadMessage#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            // IDType 1:User, 2:Store, 3:PTTrainer, 4:BBL Manager
            map.put("IDType", "1");

            Integer result = dbConnService.selectWithReturnInt("checkedUnreadMessage", map);

            HashMap infos = new HashMap();

            if(result > 0) {
                infos.put("unreadMessages", true);
            } else {
                infos.put("unreadMessages", false);
            }

            rtnVal.put("infos", infos);

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="/getScheduleMessages", method = RequestMethod.POST)
    @ApiOperation(value = "일정 알림 메세지 보기", notes = "")
    public HashMap getScheduleMessages(HttpServletRequest auth) {
        log.info("####getScheduleMessages#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            // IDType 1:User, 2:Store, 3:PTTrainer, 4:BBL Manager
            map.put("IDType", "1");
            // messageType 1:PT 일정 관리
            map.put("messageType", "1");

            List<HashMap> list = dbConnService.select("getMessages", map);

            if(list.isEmpty()) {
                error = "User index " + idx + " messages not found";
            } else {
                HashMap infos = new HashMap();
                infos.put("messages", list);

                rtnVal.put("infos", infos);
            }

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="/getScheduleMessagesDetail", method = RequestMethod.POST)
    @ApiOperation(value = "일정 알림 메세지 상세 보기", notes = "{\"messageIdx\":\"3\"}")
    public HashMap getScheduleMessagesDetail(@RequestBody String data) {
        log.info("####getScheduleMessagesDetail##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getMessagesDetail", map);

            if(list.isEmpty()) {
                error = "Message index " +jsonData.values() + " messages not found";
            } else {
                HashMap infos = new HashMap();
                dbConnService.update("setReceivedDate", map);

                // relatedMessage가 null이 아니라면, 연관된 메세지의 인덱스를 가져와서 연관 메세지 상세 보기를 출력한다.
                if(list.get(0).get("relatedMessage") != null) {
                    map.put("messageIdx", list.get(0).get("relatedMessage"));

                    list = dbConnService.select("getMessagesDetail", map);
                }

                infos.put("messages", list);

                rtnVal.put("infos", infos);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="/setScheduleConfirmed", method = RequestMethod.POST)
    @ApiOperation(value = "운동일정 승인 요청",
        notes = "{\"PTScheduleIdx\":\"15\", \"confirmed\":\"1\", \"messageIdx\":\"3\", " +
            "\"receiverIdx\":\"1\"}" +
            "\n\n여기서 receiverIdx는 getScheduleMessagesDetail의 senderIdx" +
            "\n\nconfirmed 1:동의 2:비동의")
    public HashMap setScheduleConfirmed(@RequestBody String data, HttpServletRequest auth) {
        log.info("####setScheduleConfirmed##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            int result = dbConnService.update("setScheduleConfirmed", map);

            if(result == 0) {
                error = "Message index " +jsonData.values() + " messages not found";
            } else {
                Object confirmed = map.get("confirmed");

                // senderType&receiverType 1:User, 2:Store, 3:PTTrainer, 4:BBL Manager
                map.put("senderType", "1");
                map.put("receiverType", "3");
                map.put("messageType", "1");

                if(confirmed.equals("1")) {
                    // 승인 메세지 전송
                    map.put("title", "일정 승인 완료");
                    map.put("content", "일정 승인이 완료되었습니다.");

                    result = dbConnService.insert("sendScheduleConfirmed", map);

                    if(result == 0) {
                        error = "Message send failed";
                    }
                } else if(confirmed.equals("2")) {
                    // 거절 메세지 전송
                    map.put("title", "일정 승인 거절");
                    map.put("content", "일정 승인이 거절되었습니다.");

                    result = dbConnService.insert("sendScheduleConfirmed", map);

                    if(result == 0) {
                        error = "Message send failed";
                    }
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="user/checkId", method = RequestMethod.POST)
    @ApiOperation(value = "유저 - 이메일 중복 확인", notes = "{\"email\":\"gildong@naver.com\"}")
    public HashMap checkId(@RequestBody String data) {
        log.info("####checkId##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("checkId", map);

            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                infos.put("checkedId", false);
            } else {
                infos.put("checkedId", true);
            }

            rtnVal.put("infos", infos);

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="user/register", method = RequestMethod.POST)
    @ApiOperation(value = "유저 - 이메일로 시작하기",
        notes = "{\"email\":\"gildong@naver.com\", \"password\":\"12345\", \"name\":\"홍길동\", " +
            "\"nickName\":\"길동이\", \"birthYYYYMMDD\":\"19900101\", \"gender\":\"1\", " +
            "\"elDas\":\"1\", \"region\":\"1\", \"localArea\":\"1\", " +
            "\"telephone\":\"01012345678\", \"marketingYN\":\"1\"}")
    public HashMap registerUser(@RequestBody String data) {
        log.info("####registerUser##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("checkId", map);

            if(list.isEmpty()) {
                map.put("certType", "1");
                map.put("password", new PasswordCryptConverter().convertToDatabaseColumn((String) map.get("password")));

                Random random = new Random();

                // 프로필 기본 이미지 인덱스 초기단계 1~3
                int ranInt = random.nextInt(3) + 1;

                map.put("imgIdx", ranInt);

                dbConnService.insert("registerUser", map);
                dbConnService.insert("registerUser_authentication", map);
                dbConnService.insert("registerUser_info", map);

            } else {
                error = "이미 존재하는 이메일입니다.";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="user/setInterests", method = RequestMethod.POST)
    @ApiOperation(value = "유저 - 관심분야 설정",
        notes = "{\"interests\":[{\"interestCode\":\"1\"}, {\"interestCode\":\"2\"}]}")
    public HashMap setUsersInterests(@RequestBody String data, HttpServletRequest auth) {
        log.info("####setUsersInterests##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            JSONArray arr = (JSONArray) map.get("interests");

            for(int i=0; i<arr.size(); i++) {
                JSONObject obj = (JSONObject) arr.get(i);
                obj.forEach((key, value) -> map.put(key, value));

                List<HashMap> list = dbConnService.select("getInterestCode", map);

                if(list.isEmpty()) {
                    int result = dbConnService.insert("setInterests", map);

                    if(result == 0) {
                        error = "users interests insert failed";
                    }
                }
            }
            dbConnService.delete("delInterestCode", map);

            Integer numOfInterest = dbConnService.selectWithReturnInt("getInterestsCount", map);

            map.put("numOfInterest", numOfInterest);

            dbConnService.update("setNumOfInterest", map);

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="user/login", method = RequestMethod.POST)
    @ApiOperation(value = "유저 - 이메일로 로그인 ",
        notes = "{\"email\":\"gildong@daum.net\", \"password\":\"12345\"}")
    public HashMap loginUser(@RequestBody String data) {
        log.info("####loginUser##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("checkId", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                infos.put("checkedId", false);
                infos.put("checkedPw", false);
            } else {
                infos.put("checkedId", true);
                Object userIdx =  list.get(0).get("idx");

                map.put("userIdx", userIdx);
                map.put("password", new PasswordCryptConverter().convertToDatabaseColumn((String) map.get("password")));

                list = dbConnService.select("checkPw", map);

                if(list.isEmpty()) {
                    infos.put("checkedPw", false);
                } else {
                    infos.put("checkedPw", true);

                    list = dbConnService.select("getUserIdx", map);
                    map.put("userIdx", list.get(0).get("userIdx"));

                    int result = dbConnService.update("updateLastAccess", map);

                    list = dbConnService.select("getUsersInfo", map);

                    String token = new JwtProvider().jwtCreater(
                            0, Integer.parseInt(list.get(0).get("userIdx").toString()),0
                    );

                    rtnVal.put("token", token);

                    if(result == 0) {
                        error = "LastAccess update failed";
                    }
                }
            }

            rtnVal.put("infos", infos);
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    // PT톡
    @RequestMapping(value="getUsersNotes", method = RequestMethod.POST)
    @ApiOperation(value = "PT톡 목록 보기",
        notes = "")
    public HashMap getUsersNotes(HttpServletRequest auth) {
        log.info("####getUsersNotes#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            List<HashMap> list = dbConnService.select("getUsersNotes", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                error = "PT톡 목록을 불러올 수 없습니다.";
            } else {
//                for(int i=0; i<list.size(); i++) {
//                    int photoCount = (int) list.get(i).get("photoCount");
//
//                    if(photoCount > 0){
//                        map.put("photoCategory", 1);
//                        map.put("postIdx", list.get(i).get("noteIdx"));
//
//                        List<HashMap> list2 = dbConnService.select("getImagesInfo", map);
//
//                        list.get(i).put("imagesInfo", list2);
//                    }
//                }
                infos.put("usersNote", list);
            }

            rtnVal.put("infos", infos);

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="searchUsersNotes", method = RequestMethod.POST)
    @ApiOperation(value = "PT톡 검색",
        notes = "{\"keyword\":\"홍길동\"}")
    public HashMap searchUsersNotes(@RequestBody String data) {
        log.info("####searchUsersNotes#####");

        HashMap rtnVal = new HashMap();
        String error = null;
        JSONParser parser = new JSONParser();

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();

            if(jsonData.isEmpty()) {
                error = "Data is empty";
            } else {
                jsonData.forEach((key, value) -> map.put(key,value));

                List<HashMap> list = dbConnService.select("searchUsersNotes", map);
                HashMap infos = new HashMap();

                if(list.isEmpty()) {
                    error = "PT톡 목록을 불러올 수 없습니다.";
                } else {
                    infos.put("usersNote", list);
                    rtnVal.put("infos", infos);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="getUsersNotesDetail", method = RequestMethod.POST)
    @ApiOperation(value = "PT톡 상세 보기",
        notes = "{\"noteIdx\":\"1\"}")
    public HashMap getUsersNotesDetail(@RequestBody String data) {
        log.info("####getUsersNotesDetail##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            map.put("noteCategory", "2");


            List<HashMap> list = dbConnService.select("getUsersNotesDetail", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                error = "PT톡 상세보기를 불러올 수 없습니다.";
            } else {
                infos.put("usersNotesDetail", list);

                Integer photoCount = (Integer) list.get(0).get("photoCount");
                Integer replyCount = (Integer) list.get(0).get("replyCount");

                if(photoCount > 0) {
                    map.put("photoCategory", "1");
                    map.put("postIdx", map.get("noteIdx"));

                    list = dbConnService.select("getImagesInfo", map);

                    infos.put("imageInfo", list);
                }

                if(replyCount > 0) {
                    map.put("replyCategory", "1");
                    map.put("postIdx", map.get("noteIdx"));

                    list = dbConnService.select("getNotesReplies", map);

                    infos.put("replies", list);
                }
            }

            rtnVal.put("infos", infos);
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="writeGeneralReview", method = RequestMethod.POST)
    @ApiOperation(value = "PT톡 작성 - 일반 이용 후기",
        notes = "")
    public HashMap writeGeneralReview(
            ReviewWriteForm reviewWriteForm,
            @RequestPart(value = "multiFile", required = false) List<MultipartFile> multipartFiles,
            HttpServletRequest auth
    ) {
        log.info("####writeGeneralReview#####");

        Integer noteCategory = 3;
        HashMap rtnVal = new HashMap();
        String error = null;

        String token = auth.getHeader("token");
        int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

        reviewWriteForm.setNoteCategory(noteCategory);
        reviewWriteForm.setUserIdx(idx);

        try{
            String converJson = gson.toJson(reviewWriteForm);

            HashMap data = gson.fromJson(converJson, HashMap.class);

            int result = dbConnService.insert("writeReview", data);

            if(result == 0) {
                error = "후기 작성 실패";
            } else {
                String filename = null;
                S3Service.FileGroupType groupType = S3Service.FileGroupType.Board;

                if (multipartFiles != null) {
                    for (MultipartFile multipartFile : multipartFiles) {
                        if (!multipartFile.isEmpty()) {
                            try {
                                filename = s3Service.uploadWithUUID(multipartFile, groupType);

                                log.info("file upload to s3 : " + groupType.getValue() + " : " + filename);

                                S3Object imgFileInfo = s3Service.getFileInfo(groupType.getValue() + filename);
                                log.info("uploaded image file : " + imgFileInfo.toString());
                                S3Object imgThumbFileInfo = s3Service.getFileInfo(groupType.getValue() + S3Service.thumbPath + filename);
                                log.info("uploaded image thumb file : " + imgThumbFileInfo.toString());

                                log.info("url : " + imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                                BufferedImage imgBuf = null;
                                String base64 = null;
                                try {
                                    imgBuf = ImageIO.read(imgFileInfo.getObjectContent());
                                    base64 = S3Service.encodeBase64(imgBuf);
                                    rtnVal.put("img_data", base64);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    error = "이미지파일 base64 변환 실패!";
                                }

                                data.put("filename", filename);
                                data.put("category", "1");
                                data.put("fileurl", imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());

                                result = dbConnService.insert("boardUpload", data);

                                if (result > 0) {
                                    data.put("photoCount", dbConnService.selectWithReturnInt("getPhotoCount", data));
                                    dbConnService.update("updateNotesPhotoCount", data);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                error = "파일 업로드 실패";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="updateGeneralReview", method = RequestMethod.POST)
    @ApiOperation(value = "PT톡 수정 - 일반 이용 후기",
        notes = "")
    public HashMap updateGeneralReview(
        ReviewWriteForm reviewWriteForm,
        @RequestPart(value = "multiFile", required = false) List<MultipartFile> multipartFiles,
        HttpServletRequest auth) {
        log.info("####updateGeneralReview#####");

        Integer noteCategory = 3;
        HashMap rtnVal = new HashMap();
        String error = null;

        String token = auth.getHeader("token");
        int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

        reviewWriteForm.setNoteCategory(noteCategory);
        reviewWriteForm.setUserIdx(idx);

        try{
            String converJson = gson.toJson(reviewWriteForm);

            HashMap data = gson.fromJson(converJson, HashMap.class);

            data.put("postIdx", data.get("noteIdx"));

            int result = dbConnService.update("updateReview", data);

            if(result == 0) {
                error = "후기 작성 실패";
            } else {
                String filename = null;
                S3Service.FileGroupType groupType = S3Service.FileGroupType.Board;

                data.put("category", "1");
                result = dbConnService.update("updateBoardFile", data);

                if(multipartFiles != null) {
                    for (MultipartFile multipartFile : multipartFiles) {
                        if (!multipartFile.isEmpty()) {
                            try {
                                filename = s3Service.uploadWithUUID(multipartFile, groupType);

                                log.info("file upload to s3 : " + groupType.getValue() + " : " + filename);

                                S3Object imgFileInfo = s3Service.getFileInfo(groupType.getValue() + filename);
                                log.info("uploaded image file : " + imgFileInfo.toString());
                                S3Object imgThumbFileInfo = s3Service.getFileInfo(groupType.getValue() + S3Service.thumbPath + filename);
                                log.info("uploaded image thumb file : " + imgThumbFileInfo.toString());

                                log.info("url : " + imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                                BufferedImage imgBuf = null;
                                String base64 = null;
                                try {
                                    imgBuf = ImageIO.read(imgFileInfo.getObjectContent());
                                    base64 = S3Service.encodeBase64(imgBuf);
                                    rtnVal.put("img_data", base64);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    error = "이미지파일 base64 변환 실패!";
                                }

                                data.put("filename", filename);
                                data.put("fileurl", imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());

                                result = dbConnService.insert("boardUpload", data);
                            } catch (IOException e) {
                                e.printStackTrace();
                                error = "파일 업로드 실패";
                            }
                        }
                    }
                }

                data.put("photoCount", dbConnService.selectWithReturnInt("getPhotoCount", data));
                dbConnService.update("updateNotesPhotoCount", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="getPTTrainersPTUsers", method = RequestMethod.POST)
    @ApiOperation(value = "체험권 구매내역 조회",
        notes = "")
    public HashMap getPTTrainersPTUsers(HttpServletRequest auth) {
        log.info("####getPTTrainersPTUsers#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getPTTrainersPTUsers", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                infos.put("vouchersPurchaseHistory", false);
            } else {
                infos.put("vouchersPurchaseHistory", list);
            }

            rtnVal.put("infos", infos);

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="writeExperienceReview", method = RequestMethod.POST)
    @ApiOperation(value = "PT톡 작성 - 1회 체험 후기",
        notes = "")
    public HashMap writeExperienceReview(
        ReviewWriteForm reviewWriteForm,
        @RequestPart(value = "multiFile", required = false) List<MultipartFile> multipartFiles,
        HttpServletRequest auth) {
        log.info("####writeExperienceReview#####");

        Integer noteCategory = 2;
        HashMap rtnVal = new HashMap();
        String error = null;

        String token = auth.getHeader("token");
        int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

        reviewWriteForm.setNoteCategory(noteCategory);
        reviewWriteForm.setUserIdx(idx);

        try{
            String converJson = gson.toJson(reviewWriteForm);

            HashMap data = gson.fromJson(converJson, HashMap.class);

            int result = dbConnService.insert("writeReview", data);

            if(result == 0) {
                error = "후기 작성 실패";
            } else {
                String filename = null;
                S3Service.FileGroupType groupType = S3Service.FileGroupType.Board;

                if(multipartFiles != null) {
                    for(MultipartFile multipartFile : multipartFiles) {
                        if(!multipartFile.isEmpty()) {
                            try{
                                filename = s3Service.uploadWithUUID(multipartFile, groupType);

                                log.info("file upload to s3 : " + groupType.getValue() + " : " + filename);

                                S3Object imgFileInfo = s3Service.getFileInfo(groupType.getValue() + filename);
                                log.info("uploaded image file : " + imgFileInfo.toString());
                                S3Object imgThumbFileInfo = s3Service.getFileInfo(groupType.getValue() + S3Service.thumbPath + filename);
                                log.info("uploaded image thumb file : " + imgThumbFileInfo.toString());

                                log.info("url : " + imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                                BufferedImage imgBuf = null;
                                String base64 = null;
                                try {
                                    imgBuf = ImageIO.read(imgFileInfo.getObjectContent());
                                    base64 = S3Service.encodeBase64(imgBuf);
                                    rtnVal.put("img_data", base64);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    error = "이미지파일 base64 변환 실패!";
                                }

                                data.put("filename", filename);
                                data.put("category", "1");
                                data.put("fileurl", imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());

                                result = dbConnService.insert("boardUpload", data);

                                if(result > 0) {
                                    data.put("photoCount", dbConnService.selectWithReturnInt("getPhotoCount", data));
                                    dbConnService.update("updateNotesPhotoCount", data);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                error = "파일 업로드 실패";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="updateExperienceReview", method = RequestMethod.POST)
    @ApiOperation(value = "PT톡 수정 - 1회 체험 후기",
        notes = "")
    public HashMap updateExperienceReview(
        ReviewWriteForm reviewWriteForm,
        @RequestPart(value = "multiFile", required = false) List<MultipartFile> multipartFiles,
        HttpServletRequest auth) {
        log.info("####updateExperienceReview#####");

        Integer noteCategory = 2;
        HashMap rtnVal = new HashMap();
        String error = null;

        String token = auth.getHeader("token");
        int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

        reviewWriteForm.setNoteCategory(noteCategory);
        reviewWriteForm.setUserIdx(idx);

        try{
            String converJson = gson.toJson(reviewWriteForm);

            HashMap data = gson.fromJson(converJson, HashMap.class);

            data.put("postIdx", data.get("noteIdx"));

            int result = dbConnService.update("updateReview", data);

            if(result == 0) {
                error = "후기 작성 실패";
            } else {
                String filename = null;
                S3Service.FileGroupType groupType = S3Service.FileGroupType.Board;

                data.put("category", "1");
                result = dbConnService.update("updateBoardFile", data);

                if(multipartFiles != null) {
                    for(MultipartFile multipartFile : multipartFiles) {
                        if(!multipartFile.isEmpty()) {
                            try{
                                filename = s3Service.uploadWithUUID(multipartFile, groupType);

                                log.info("file upload to s3 : " + groupType.getValue() + " : " + filename);

                                S3Object imgFileInfo = s3Service.getFileInfo(groupType.getValue() + filename);
                                log.info("uploaded image file : " + imgFileInfo.toString());
                                S3Object imgThumbFileInfo = s3Service.getFileInfo(groupType.getValue() + S3Service.thumbPath + filename);
                                log.info("uploaded image thumb file : " + imgThumbFileInfo.toString());

                                log.info("url : " + imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                                BufferedImage imgBuf = null;
                                String base64 = null;
                                try {
                                    imgBuf = ImageIO.read(imgFileInfo.getObjectContent());
                                    base64 = S3Service.encodeBase64(imgBuf);
                                    rtnVal.put("img_data", base64);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    error = "이미지파일 base64 변환 실패!";
                                }

                                data.put("filename", filename);
                                data.put("fileurl", imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());

                                data.put("exposeYN", '1');
                                result = dbConnService.insert("boardUpload", data);
                            } catch (IOException e) {
                                e.printStackTrace();
                                error = "파일 업로드 실패";
                            }
                        }
                    }
                }

                data.put("photoCount", dbConnService.selectWithReturnInt("getPhotoCount", data));
                dbConnService.update("updateNotesPhotoCount", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="deleteNote", method = RequestMethod.POST)
    @ApiOperation(value = "후기 삭제",
        notes = "{\"noteIdx\":\"1\"}")
    public HashMap deleteNote(@RequestBody String data) {
        log.info("####deleteNote##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            int result = dbConnService.update("deleteNote", map);

            if(result == 0) {
                error = "후기 삭제 실패";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="writeReplyToNote", method = RequestMethod.POST)
    @ApiOperation(value = "후기 댓글 작성",
        notes = "{\"noteIdx\":\"2\", \"content\":\"후기 댓글 작성\"," +
            " \"hiddenYN\":\"0\"\n}")
    public HashMap writeReplyToNote(@RequestBody String data, HttpServletRequest auth) {
        log.info("####writeReplyToNote##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);
            map.put("category", "1");
            map.put("targetIdx", map.get("noteIdx"));

            int result = dbConnService.insert("writeReply", map);

            if(result == 0) {
                error = "후기 댓글 작성 실패";
            } else {
                dbConnService.update("updateNotesReplyCount", map);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="updateReply", method = RequestMethod.POST)
    @ApiOperation(value = "댓글 수정",
        notes = "{\"replyIdx\":\"1\", \"content\":\"댓글 수정하기\", \"hiddenYN\":\"0\"}")
    public HashMap updateReply(@RequestBody String data, HttpServletRequest auth) {
        log.info("####updateReply##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            int result = dbConnService.update("updateReply", map);

            if(result == 0) {
                error = "댓글 수정 실패";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="deleteReply", method = RequestMethod.POST)
    @ApiOperation(value = "댓글 삭제",
        notes = "{\"replyIdx\":\"1\"}")
    public HashMap deleteReply(@RequestBody String data) {
        log.info("####deleteReply##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            int result = dbConnService.update("deleteReply", map);

            if(result == 0) {
                error = "댓글 삭제 실패";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="likeNotes", method = RequestMethod.POST)
    @ApiOperation(value = "후기 좋아요",
        notes = "{\"noteIdx\":\"2\"}")
    public HashMap likeNotes(@RequestBody String data, HttpServletRequest auth) {
        log.info("####likeNotes##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);
            map.put("category", "1");
            map.put("targetIdx", map.get("noteIdx"));

            List<HashMap> list = dbConnService.select("getUsersLikes", map);

            if(list.isEmpty()) {
                int result = dbConnService.insert("likePosts", map);

                if(result == 0) {
                    error = "후기 게시글 좋아요 실패";
                } else {
                    int likeCount = dbConnService.selectWithReturnInt("getLikeCount", map);
                    map.put("likeCount", likeCount);

                    dbConnService.update("updateNotesLikeCount", map);
                }
            } else {
                error = "이미 좋아요된 게시글입니다.";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="cancelLikeNotes", method = RequestMethod.POST)
    @ApiOperation(value = "후기 좋아요 취소",
        notes = "{\"noteIdx\":\"2\"}")
    public HashMap cancelLikeNotes(@RequestBody String data, HttpServletRequest auth) {
        log.info("####cancelLikeNotes##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);
            map.put("category", "1");
            map.put("targetIdx", map.get("noteIdx"));

            List<HashMap> list = dbConnService.select("getUsersLikes", map);

            if(!list.isEmpty()) {
                int result = dbConnService.delete("deleteLikePosts", map);

                if(result == 0) {
                    error = "후기 게시글 좋아요 취소 실패";
                } else {
                    int likeCount = dbConnService.selectWithReturnInt("getLikeCount", map);
                    map.put("likeCount", likeCount);

                    dbConnService.update("updateNotesLikeCount", map);
                }
            } else {
                error = "좋아요 되지 않은 게시글입니다.";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="usersNotesLikesIt", method = RequestMethod.POST)
    @ApiOperation(value = "사용자의 후기 게시글 좋아요 여부",
        notes = "{\"noteIdx\":\"1\"}")
    public HashMap usersNotesLikesIt(@RequestBody String data, HttpServletRequest auth) {
        log.info("####usersNotesLikesIt##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);
            map.put("category", "1");
            map.put("targetIdx", map.get("noteIdx"));

            List<HashMap> list = dbConnService.select("getUsersLikes", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                infos.put("usersLikesYN", false);
            } else {
                infos.put("usersLikesYN", true);
            }

            rtnVal.put("infos", infos);
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    // 자유톡
    @RequestMapping(value="getFreeTalks", method = RequestMethod.POST)
    @ApiOperation(value = "자유톡 목록 보기",
        notes = "")
    public HashMap getFreeTalks(HttpServletRequest auth) {
        log.info("####getFreeTalks#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            List<HashMap> list = dbConnService.select("getFreeTalks", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                error = "자유톡 목록을 불러올 수 없습니다.";
            } else {
//                for(int i=0; i<list.size(); i++) {
//                    int photoCount = (int) list.get(i).get("photoCount");
//
//                    if(photoCount > 0){
//                        map.put("photoCategory", 2);
//                        map.put("postIdx", list.get(i).get("freeTalkIdx"));
//
//                        List<HashMap> list2 = dbConnService.select("getImagesInfo", map);
//
//                        list.get(i).put("imagesInfo", list2);
//                    }
//                }
                infos.put("freeTalks", list);
            }

            rtnVal.put("infos", infos);

        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="getFreeTalkDetail", method = RequestMethod.POST)
    @ApiOperation(value = "자유톡 상세 보기",
        notes = "{\"freeTalkIdx\":\"1\"}")
    public HashMap getFreeTalkDetail(@RequestBody String data) {
        log.info("####getFreeTalkDetail##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getFreeTalkDetail", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                error = "자유톡 상세보기를 불러올 수 없습니다.";
            } else {
                infos.put("freeTalkDetail", list);

                Integer photoCount = (Integer) list.get(0).get("photoCount");
                Integer replyCount = (Integer) list.get(0).get("replyCount");

                if (photoCount > 0) {
                    map.put("photoCategory", "2");
                    map.put("postIdx", map.get("freeTalkIdx"));

                    list = dbConnService.select("getImagesInfo", map);

                    infos.put("imageInfo", list);
                }

                if(replyCount > 0) {
                    map.put("replyCategory", "2");
                    map.put("postIdx", map.get("freeTalkIdx"));

                    list = dbConnService.select("getNotesReplies", map);

                    infos.put("replies", list);
                }
            }

            rtnVal.put("infos", infos);
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="writeFreeTalks", method = RequestMethod.POST)
    @ApiOperation(value = "자유톡 작성",
        notes = "")
    public HashMap writeFreeTalks(
        FreeTalksWriteForm freeTalksWriteForm,
        @RequestPart(value = "multiFile", required = false) List<MultipartFile> multipartFiles,
        HttpServletRequest auth) {
        log.info("####writeFreeTalks#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        String token = auth.getHeader("token");
        int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));
        freeTalksWriteForm.setWriterIdx(idx);

        HashMap map = dbConnService.selectIdx("getUsersInfo", idx);


        String writerName = String.valueOf(map.get("userName"));
        freeTalksWriteForm.setWriterName(writerName);

        try{
            String converJson = gson.toJson(freeTalksWriteForm);

            HashMap data = gson.fromJson(converJson, HashMap.class);

            int result = dbConnService.insert("writeFreeTalk", data);

            if(result == 0) {
                error = "자유톡 작성 실패";
            } else {
                String filename = null;
                S3Service.FileGroupType groupType = S3Service.FileGroupType.Board;

                if (multipartFiles != null) {
                    for (MultipartFile multipartFile : multipartFiles) {
                        if (!multipartFile.isEmpty()) {
                            try {
                                filename = s3Service.uploadWithUUID(multipartFile, groupType);

                                log.info("file upload to s3 : " + groupType.getValue() + " : " + filename);

                                S3Object imgFileInfo = s3Service.getFileInfo(groupType.getValue() + filename);
                                log.info("uploaded image file : " + imgFileInfo.toString());
                                S3Object imgThumbFileInfo = s3Service.getFileInfo(groupType.getValue() + S3Service.thumbPath + filename);
                                log.info("uploaded image thumb file : " + imgThumbFileInfo.toString());

                                log.info("url : " + imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                                BufferedImage imgBuf = null;
                                String base64 = null;
                                try {
                                    imgBuf = ImageIO.read(imgFileInfo.getObjectContent());
                                    base64 = S3Service.encodeBase64(imgBuf);
                                    rtnVal.put("img_data", base64);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    error = "이미지파일 base64 변환 실패!";
                                }

                                data.put("filename", filename);
                                data.put("category", "2");
                                data.put("fileurl", imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                                data.put("exposeYN", '1');

                                result = dbConnService.insert("boardUpload", data);

                                if (result > 0) {
                                    data.put("photoCount", dbConnService.selectWithReturnInt("getPhotoCount", data));
                                    dbConnService.update("updateFreeTalksPhotoCount", data);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                error = "파일 업로드 실패";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="updateFreeTalks", method = RequestMethod.POST)
    @ApiOperation(value = "자유톡 수정",
        notes = "")
    public HashMap updateFreeTalks(
        FreeTalksWriteForm freeTalksWriteForm,
        @RequestPart(value = "multiFile", required = false) List<MultipartFile> multipartFiles,
        HttpServletRequest auth) {
        log.info("####updateFreeTalks#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        String token = auth.getHeader("token");
        int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

        freeTalksWriteForm.setWriterIdx(idx);

        try{
            String converJson = gson.toJson(freeTalksWriteForm);

            HashMap data = gson.fromJson(converJson, HashMap.class);

            data.put("postIdx", data.get("freeTalkIdx"));

            int result = dbConnService.update("updateFreeTalk", data);

            if(result == 0) {
                error = "후기 작성 실패";
            } else {
                String filename = null;
                S3Service.FileGroupType groupType = S3Service.FileGroupType.Board;

                data.put("category", "2");
                result = dbConnService.update("updateBoardFile", data);

                if(multipartFiles != null) {
                    for (MultipartFile multipartFile : multipartFiles) {
                        if (!multipartFile.isEmpty()) {
                            try {
                                filename = s3Service.uploadWithUUID(multipartFile, groupType);

                                log.info("file upload to s3 : " + groupType.getValue() + " : " + filename);

                                S3Object imgFileInfo = s3Service.getFileInfo(groupType.getValue() + filename);
                                log.info("uploaded image file : " + imgFileInfo.toString());
                                S3Object imgThumbFileInfo = s3Service.getFileInfo(groupType.getValue() + S3Service.thumbPath + filename);
                                log.info("uploaded image thumb file : " + imgThumbFileInfo.toString());

                                log.info("url : " + imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                                BufferedImage imgBuf = null;
                                String base64 = null;
                                try {
                                    imgBuf = ImageIO.read(imgFileInfo.getObjectContent());
                                    base64 = S3Service.encodeBase64(imgBuf);
                                    rtnVal.put("img_data", base64);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    error = "이미지파일 base64 변환 실패!";
                                }

                                data.put("filename", filename);
                                data.put("fileurl", imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                                data.put("exposeYN", '1');

                                result = dbConnService.insert("boardUpload", data);
                            } catch (IOException e) {
                                e.printStackTrace();
                                error = "파일 업로드 실패";
                            }
                        }
                    }
                }

                data.put("photoCount", dbConnService.selectWithReturnInt("getPhotoCount", data));
                dbConnService.update("updateNotesPhotoCount", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @RequestMapping(value="deleteFreeTalk", method = RequestMethod.POST)
    @ApiOperation(value = "자유톡 삭제",
        notes = "{\"freeTalkIdx\":\"1\"}")
    public HashMap deleteFreeTalk(@RequestBody String data) {
        log.info("####deleteFreeTalk##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            int result = dbConnService.update("deleteFreeTalk", map);

            if(result == 0) {
                error = "자유톡 삭제 실패";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="writeReplyToFreeTalk", method = RequestMethod.POST)
    @ApiOperation(value = "자유톡 댓글 작성",
        notes = "{\"freeTalkIdx\":\"2\", \"content\":\"자유톡 댓글 작성\"," +
            " \"hiddenYN\":\"0\"\n}")
    public HashMap writeReplyToFreeTalk(@RequestBody String data, HttpServletRequest auth) {
        log.info("####writeReplyToFreeTalk##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);
            map.put("category", "2");
            map.put("targetIdx", map.get("freeTalkIdx"));

            int result = dbConnService.insert("writeReply", map);

            if(result == 0) {
                error = "후기 댓글 작성 실패";
            } else {
                dbConnService.update("updateFreeTalksReplyCount", map);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="likeFreeTalks", method = RequestMethod.POST)
    @ApiOperation(value = "자유톡 좋아요",
        notes = "{\"freeTalkIdx\":\"2\"}")
    public HashMap likeFreeTalks(@RequestBody String data, HttpServletRequest auth) {
        log.info("####likeFreeTalks##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);
            map.put("category", "2");
            map.put("targetIdx", map.get("freeTalkIdx"));

            List<HashMap> list = dbConnService.select("getUsersLikes", map);

            if(list.isEmpty()) {
                int result = dbConnService.insert("likePosts", map);

                if(result == 0) {
                    error = "자유톡 게시글 좋아요 실패";
                } else {
                    int likeCount = dbConnService.selectWithReturnInt("getLikeCount", map);
                    map.put("likeCount", likeCount);

                    dbConnService.update("updateFreeTalksLikeCount", map);
                }
            } else {
                error = "이미 좋아요된 게시글입니다.";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="cancelLikeFreeTalks", method = RequestMethod.POST)
    @ApiOperation(value = "자유톡 좋아요 취소",
        notes = "{\"freeTalkIdx\":\"2\"}")
    public HashMap cancelLikeFreeTalks(@RequestBody String data, HttpServletRequest auth) {
        log.info("####cancelLikeFreeTalks##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);
            map.put("category", "2");
            map.put("targetIdx", map.get("freeTalkIdx"));

            List<HashMap> list = dbConnService.select("getUsersLikes", map);

            if(!list.isEmpty()) {
                int result = dbConnService.delete("deleteLikePosts", map);

                if(result == 0) {
                    error = "자유톡 게시글 좋아요 취소 실패";
                } else {
                    int likeCount = dbConnService.selectWithReturnInt("getLikeCount", map);
                    map.put("likeCount", likeCount);

                    dbConnService.update("updateFreeTalksLikeCount", map);
                }
            } else {
                error = "좋아요 되지 않은 게시글입니다.";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="usersFreeTalksLikesIt", method = RequestMethod.POST)
    @ApiOperation(value = "사용자의 자유톡 게시글 좋아요 여부",
        notes = "{\"freeTalkIdx\":\"1\"}")
    public HashMap usersFreeTalksLikesIt(@RequestBody String data, HttpServletRequest auth) {
        log.info("####usersLikesIt##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);
            map.put("category", "2");
            map.put("targetIdx", map.get("freeTalkIdx"));

            List<HashMap> list = dbConnService.select("getUsersLikes", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                infos.put("usersLikesYN", false);
            } else {
                infos.put("usersLikesYN", true);
            }

            rtnVal.put("infos", infos);
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    // 나의 톡톡
    @RequestMapping(value="getUserLikesList", method = RequestMethod.POST)
    @ApiOperation(value = "나의 톡톡 - 좋아요",
        notes = "")
    public HashMap getUserLikesList(HttpServletRequest auth) {
        log.info("####getUserLikesList#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            HashMap infos = new HashMap();

            // 자유톡
            map.put("category", "2");

            List<HashMap> list = dbConnService.select("getUserLikesFreeTalks", map);

            if(list.isEmpty()) {
                error = "좋아요한 내역이 없어요.";
            } else {
                infos.put("userLikeFreeTalks", list);
            }

            // 운동톡
            map.put("category", "1");
            list = dbConnService.select("getUserLikesNotes", map);

            if(list.isEmpty()) {
                error = "좋아요한 내역이 없어요.";
            } else {
                infos.put("userLikeNotes", list);
            }

            // 성형톡 추가


            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="getUserPostsList", method = RequestMethod.POST)
    @ApiOperation(value = "나의 톡톡 - 톡톡",
        notes = "")
    public HashMap getUserPostsList(HttpServletRequest auth) {
        log.info("####getUserPostsList#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            HashMap infos = new HashMap();

            // 자유톡
            List<HashMap> list = dbConnService.select("getUserWriteFreeTalks", map);

            if(list.isEmpty()) {
                error = "작성한 게시글이 없어요.";
            } else {
                infos.put("userWriteFreeTalks", list);
            }

            // 운동톡
            list = dbConnService.select("getUserWriteNotes", map);

            if(list.isEmpty()) {
                error = "작성한 게시글이 없어요.";
            } else {
                infos.put("userWriteNotes", list);
            }

            // 성형톡 추가


            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="getUserWriteReply", method = RequestMethod.POST)
    @ApiOperation(value = "나의 톡톡 - 댓글",
        notes = "")
    public HashMap getUserWriteReply(HttpServletRequest auth) {
        log.info("####getUserWriteReply#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            HashMap infos = new HashMap();

            // 자유톡
            List<HashMap> list = dbConnService.select("getUserWriteReply", map);

            if(list.isEmpty()) {
                error = "작성한 댓글이 없어요.";
            } else {
                infos.put("userWriteReply", list);
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="getReplyPost", method = RequestMethod.POST)
    @ApiOperation(value = "나의 톡톡 - 댓글 상세보기",
        notes = "{\"boardCategory\":\"1\", \"targetIdx\":\"2\"}")
    public HashMap getReplyPost(@RequestBody String data) {
        log.info("####getReplyPost##### : " + data);

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));
            HashMap infos = new HashMap();

            if(map.get("boardCategory").equals("1")) {
                // PT톡(후기)
                map.put("noteIdx", map.get("targetIdx"));

                List<HashMap> list = dbConnService.select("getUsersNotesDetail", map);

                if(list.isEmpty()) {
                    error = "인덱스" + map.get("noteIdx") + "번 게시글을 찾을 수 없습니다.";
                } else {
                    infos.put("usersNotesDetail", list);

                    Integer photoCount = (Integer) list.get(0).get("photoCount");
                    Integer replyCount = (Integer) list.get(0).get("replyCount");

                    if(photoCount > 0) {
                        map.put("photoCategory", "1");
                        map.put("postIdx", map.get("noteIdx"));

                        list = dbConnService.select("getImagesInfo", map);

                        infos.put("imageInfo", list);
                    }

                    if(replyCount > 0) {
                        map.put("replyCategory", "1");
                        map.put("postIdx", map.get("noteIdx"));

                        list = dbConnService.select("getNotesReplies", map);

                        infos.put("replies", list);
                    }
                }
            } else if(map.get("boardCategory").equals("2")) {
                // 자유톡
                map.put("freeTalkIdx", map.get("targetIdx"));

                List<HashMap> list = dbConnService.select("getFreeTalkDetail", map);
                if(list.isEmpty()) {
                    error = "자유톡 상세보기를 불러올 수 없습니다.";
                } else {
                    infos.put("freeTalkDetil", list);

                    Integer photoCount = (Integer) list.get(0).get("photoCount");
                    Integer replyCount = (Integer) list.get(0).get("replyCount");

                    if (photoCount > 0) {
                        map.put("photoCategory", "2");
                        map.put("postIdx", map.get("freeTalkIdx"));

                        list = dbConnService.select("getImagesInfo", map);

                        infos.put("imageInfo", list);
                    }

                    if (replyCount > 0) {
                        map.put("replyCategory", "2");
                        map.put("postIdx", map.get("freeTalkIdx"));

                        list = dbConnService.select("getNotesReplies", map);

                        infos.put("replies", list);
                    }
                }
            } else {
                error = "존재하지 않는 게시판 카테고리입니다.";
            }

            rtnVal.put("infos", infos);
        } catch (ParseException e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="modifyUserInfo", method = RequestMethod.POST)
    @ApiOperation(value = "마이 페이지 - 내 정보 수정 화면",
            notes = "")
    public HashMap modifyUserInfo(HttpServletRequest auth) {
        log.info("####getUserWriteReply#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            HashMap infos = new HashMap();

            List<HashMap> list = dbConnService.select("getUsersInfo", map);

            if(list.isEmpty()) {
                error = "유저 정보가 없어요.";
            } else {
                HashMap data = new HashMap();

                data.put("email", list.get(0).get("userId"));
                data.put("userName", list.get(0).get("userName"));
                data.put("nickName", list.get(0).get("userNickName"));
                data.put("telephone", list.get(0).get("telephone"));
                data.put("userPw", list.get(0).get("userPw"));

                if(Integer.parseInt(list.get(0).get("numOfInterest").toString()) > 0) {
                    List<HashMap> interestsCode = dbConnService.select("getInterests", map);

                    data.put("interestsCode", interestsCode);
                }

                infos.put("userInfo", data);
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="modifyUserInfo_photo", method = RequestMethod.POST)
    @ApiOperation(value = "마이 페이지 - 내 정보 수정 - 프로필 사진 변경",
            notes = "")
    public HashMap modifyUserInfo_photo(HttpServletRequest auth,
                                        @RequestPart MultipartFile multipartFile) {
        log.info("####modifyUserInfo_photo#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            if(!multipartFile.isEmpty()) {
                String filename = null;
                S3Service.FileGroupType groupType = S3Service.FileGroupType.User;

                try {
                    filename = s3Service.uploadWithUUID(multipartFile, groupType);

                    log.info("file upload to s3 : " + groupType.getValue() + " : " + filename);

                    S3Object imgFileInfo = s3Service.getFileInfo(groupType.getValue() + filename);
                    log.info("uploaded image file : " + imgFileInfo.toString());
                    S3Object imgThumbFileInfo = s3Service.getFileInfo(groupType.getValue() + S3Service.thumbPath + filename);
                    log.info("uploaded image thumb file : " + imgThumbFileInfo.toString());

                    log.info("url : " + imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                    BufferedImage imgBuf = null;
                    String base64 = null;
                    try {
                        imgBuf = ImageIO.read(imgFileInfo.getObjectContent());
                        base64 = S3Service.encodeBase64(imgBuf);
                        rtnVal.put("img_data", base64);

                    } catch (IOException e) {
                        e.printStackTrace();
                        error = "이미지파일 base64 변환 실패!";
                    }

                    map.put("filename", filename);
                    map.put("fileurl", imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());

                    int result = dbConnService.insert("userProfileImgUpload", map);

                    if (result > 0) {
                        map.put("defaultImg", 0);
                        result = dbConnService.update("modifyUserImgIdx", map);

                        if (result == 0) {
                            error = "프로필 이미지 변경 실패";
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    error = "파일 업로드 실패";
                }
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="modifyUserInfo_defaultPhoto", method = RequestMethod.POST)
    @ApiOperation(value = "마이 페이지 - 내 정보 수정 - 프로필 사진 기본 이미지로 변경",
            notes = "")
    public HashMap modifyUserInfo_defaultPhoto(HttpServletRequest auth) {
        log.info("####modifyUserInfo_defaultPhoto#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();
            Random random = new Random();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            int ranInt = random.nextInt(3) + 1;

            map.put("userImgIdx", ranInt);

            map.put("defaultImg", 1);
            int result = dbConnService.update("modifyUserImgIdx", map);

            if (result == 0) {
                error = "프로필 기본 이미지로 변경 실패";
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }


    @RequestMapping(value="modifyUserInfo_nickname", method = RequestMethod.POST)
    @ApiOperation(value = "마이 페이지 - 내 정보 수정 - 닉네임 변경",
            notes = "{\"nickName\":\"동동이\"}")
    public HashMap modifyUserInfo_nickname(HttpServletRequest auth, @RequestBody String data) {
        log.info("####modifyUserInfo_nickname#####");

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            JSONObject jsonData = (JSONObject) parser.parse(data);
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            int result = dbConnService.update("modifyUserNickname", map);

            if(result == 0) {
                error = "닉네임 변경 실패";
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="modifyUserInfo_telephone", method = RequestMethod.POST)
    @ApiOperation(value = "마이 페이지 - 내 정보 수정 - 휴대폰 변경",
            notes = "{\"telephone\":\"01098764321\"}")
    public HashMap modifyUserInfo_telephone(HttpServletRequest auth, @RequestBody String data) {
        log.info("####modifyUserInfo_telephone#####");

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            JSONObject jsonData = (JSONObject) parser.parse(data);
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            int result = dbConnService.update("modifyUserTelephone", map);

            if(result == 0) {
                error = "휴대폰 변경 실패";
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="modifyUserInfo_password", method = RequestMethod.POST)
    @ApiOperation(value = "마이 페이지 - 내 정보 수정 - 비밀번호 재설정",
            notes = "{\"oldPassword\":\"12345\", \"newPassword\":\"67890\"}")
    public HashMap modifyUserInfo_password(HttpServletRequest auth, @RequestBody String data) {
        log.info("####modifyUserInfo_password#####");

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            JSONObject jsonData = (JSONObject) parser.parse(data);
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            map.put("password", new PasswordCryptConverter().convertToDatabaseColumn((String) map.get("oldPassword")));

            List<HashMap> list = dbConnService.select("checkPw", map);

            if(list.isEmpty()) {
                error = "현재 비밀번호가 일치하지 않습니다.";
            } else {
                map.put("newPassword", new PasswordCryptConverter().convertToDatabaseColumn((String) map.get("newPassword")));

                int result = dbConnService.update("modifyUserPassword", map);

                if(result == 0) {
                    error = "비밀번호 변경 실패";
                }
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="writeInquiries", method = RequestMethod.POST)
    @ApiOperation(value = "설정 - 문의 - 문의글 작성",
            notes = "{\"title\":\"1회 체험이 뭔가요?\", \"content\":\"1회 체험이 뭔지 자세한 설명이 궁금해요.\"}")
    public HashMap writeInquiries(HttpServletRequest auth, @RequestBody String data) {
        log.info("####writeInquiries#####");

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            JSONObject jsonData = (JSONObject) parser.parse(data);
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getUsersInfo", map);
            String userName = String.valueOf(list.get(0).get("userName"));
            map.put("writerName", userName);

            int result = dbConnService.insert("writeInquiries", map);

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="getInquiriesList", method = RequestMethod.POST)
    @ApiOperation(value = "설정 - 문의 - 문의 및 답변",
            notes = "")
    public HashMap getInquiriesList(HttpServletRequest auth) {
        log.info("####getInquiriesList#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getInquiriesList", map);

            if(list.isEmpty()){
                error = "문의 및 답변을 불러올 수 없습니다.";
            } else {
                for(int i = 0; i < list.size(); i++) {
                    map.put("inquiriesIdx", list.get(i).get("idx"));

                    if("3".equals(list.get(i).get("processingStatus").toString())) { // 3: 답변 완료 상태
                        List<HashMap> list2 = dbConnService.select("getAnswersToInquiries", map);

                        list.get(i).put("answers", list2);
                    }
                }
                infos.put("inquiries", list);
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="getNotices", method = RequestMethod.POST)
    @ApiOperation(value = "설정 - 공지사항",
            notes = "")
    public HashMap getNotices(HttpServletRequest auth) {
        log.info("####getNotices#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getNotices", map);

            if(list.isEmpty()){
                error = "공지사항을 불러올 수 없습니다.";
            } else {
                for(int i = 0; i < list.size(); i++) {
                    map.put("noticeIdx", list.get(i).get("idx"));

                    if("1".equals(list.get(i).get("setPeriod").toString())) { // 1: 게시 기간 설정 true
                        LocalDate noticeStart = LocalDate.parse(list.get(i).get("noticeStart").toString());
                        LocalDate noticeEnd = LocalDate.parse(list.get(i).get("noticeEnd").toString());
                        LocalDate now = LocalDate.now();

                        if(!((now.isAfter(noticeStart) || now.isEqual(noticeStart)) && (now.isBefore(noticeEnd) || now.isEqual(noticeEnd)))) {
                            list.remove(i);
                            i--;
                        }
                    }
                }
                infos.put("notice", list);
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="deleteAccount", method = RequestMethod.POST)
    @ApiOperation(value = "설정 - 회원탈퇴",
            notes = "{\"deletedReason\":\"5\", \"reasonDetail\":\"가나다라마바사\"}")
    public HashMap deleteAccount(HttpServletRequest auth, @RequestBody String data) {
        log.info("####deleteAccount#####");

        HashMap rtnVal = new HashMap();
        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            jsonData.forEach((key, value) -> map.put(key,value));

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            int result = dbConnService.insert("insertDeletedUsers", map);

            if(result != 0) {
                result = dbConnService.update("updateDeletedUsers", map);
                if(result != 0) {
                    // 데이터 삭제 진행
                    // if(데이터 삭제가 완료)
                    // users 테이블에서 해당 인덱스 삭제 deletedUsers
                }
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="myPTTrainers", method = RequestMethod.POST)
    @ApiOperation(value = "마이 페이지 - 트레이너관리",
            notes = "")
    public HashMap myPTTrainers(HttpServletRequest auth) {
        log.info("####myPTTrainers#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            List<HashMap> list = dbConnService.select("getMyPTTrainers", map);

            if(list.isEmpty()) {
                error = "나의 트레이너를 찾을 수 없습니다.";
            } else {
                infos.put("myTrainers", list);
            }

            rtnVal.put("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);
        return rtnVal;
    }

    @RequestMapping(value="endOfExercise", method = RequestMethod.POST)
    @ApiOperation(value = "마이 페이지 - 트레이너관리 - 운동 종료 신청",
            notes = "{\"sellerIdx\":\"1\"}")
    public HashMap endOfExercise(HttpServletRequest auth, @RequestBody String Data) {
        log.info("####endOfExercise#####");

        HashMap rtnVal = new HashMap();
        String error = null;

        try{
            HashMap map = new HashMap();
            HashMap infos = new HashMap();

            String token = auth.getHeader("token");
            int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("userIdx")));

            map.put("userIdx", idx);

            HashMap user = dbConnService.selectOne("getUsersInfo", map);

            int result = dbConnService.delete("endOfExercise", map);

            if(result == 0) {
                error = "트레이너 운동 종료 신청 실패";
            } else {
                String userName = user.get("userName").toString();

                map.put("title", "운동 종료신청");
                map.put("content", "[ "+userName+" ] 회원님이 운동종료 신청하셨습니다.");
                map.put("receiverType", "3");
                map.put("receiverIdx", map.get("sellerIdx"));
                map.put("messageType", "2");
//                map.put("typeIdx", "트랜잭션인덱스");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
           rtnVal.put("errorMsg", error);

        return rtnVal;
    }
}
