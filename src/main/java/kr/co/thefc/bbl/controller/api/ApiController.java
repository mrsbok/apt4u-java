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
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private DBConnService dbConnService;

    @Autowired
    private S3Service s3Service;

    private Gson gson = new Gson();

    @RequestMapping(value="/now", method = RequestMethod.POST)
    public HashMap now() {
        log.info("####now#####");
        HashMap rtnVal = new HashMap();

        List<HashMap> list = dbConnService.select("select_now_api", null);
        rtnVal.put("now", list.get(0).get("now"));

        return rtnVal;
    }

    @RequestMapping(value="/getPTLessionVouchars", method = RequestMethod.POST)
    @ApiOperation(value = "PT 상품 목록 조회",
            notes = "{}")
    public HashMap getPTLessionVouchars(@RequestBody String data) {
        log.info("####getPTLessionVouchars##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try {
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            // category 1:store, 2:product, 3:trainer
            map.put("noteCategory", "2");

            // 프로필, 근무경력, 수상경력, 자격증 등등
            map.put("imageType", "프로필");

            List<HashMap> list = dbConnService.select("getPTLessionVouchars", map);

            if(list.isEmpty()) {
                error = "Product index " +jsonData.values() + " not found";
            } else {
                HashMap infos = new HashMap();
                infos.put("products", list);

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

    @RequestMapping(value="/getPTLessionVoucharDetail", method = RequestMethod.POST)
    @ApiOperation(value = "PT 상품 상세 조회",
            notes = "{\"productIdx\":\"1\"}")
    public HashMap getPTLessionVoucharDetail(@RequestBody String data) {
        log.info("####getPTLessionVoucharDetail##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            if(jsonData.isEmpty()) {
                error = "Data is empty";
            } else {
                HashMap map = new HashMap();
                Set set = jsonData.keySet();
                jsonData.forEach((key, value) -> map.put(key,value));

                // 1:store, 2:product, 3:trainer
                map.put("noteCategory", "2");

                // imageType : 프로필, 근무경력, 수상경력, 자격증 등등
                map.put("imageType", "프로필");

                List<HashMap> list = dbConnService.select("getPTLessionVoucharsDetail", map);

                if(list.isEmpty()) {
                    error = "Product index " +jsonData.values() + " not found";
                } else {
                    HashMap infos = new HashMap();
                    infos.put("productInfo", list);

                    if(list.get(0).get("sellerIdx") == null) {
                        error = "Seller index is null";
                    } else {
                        map.put("PTTrainerIdx", list.get(0).get("sellerIdx"));

                        list = dbConnService.select("getPTTrainerDetail", map);

                        if (list.isEmpty()) {
                            error = "PTTrainer index not found";
                        } else {
                            infos.put("PTTrainerInfo", list);

                            Integer workExperience = Integer.parseInt(String.valueOf(list.get(0).get("workExperienceCount")));
                            Integer awardWinning = Integer.parseInt(String.valueOf(list.get(0).get("awardWinningCount")));
                            Integer qualification = Integer.parseInt(String.valueOf(list.get(0).get("qualificationCount")));
                            Integer photoCount = Integer.parseInt(String.valueOf(list.get(0).get("photoCount")));

                            Integer affiliatedCenterIdx = (Integer) list.get(0).get("affilatedCenterIdx");

                            map.put("storeIdx", affiliatedCenterIdx);

                            if (workExperience > 0) {
                                list = dbConnService.select("getPTTrainerDetail_workExperience", map);
                                infos.put("workExperience", list);
                            }

                            if (awardWinning > 0) {
                                list = dbConnService.select("getPTTrainerDetail_awardWinning", map);
                                infos.put("awardWinning", list);
                            }

                            if (qualification > 0) {
                                list = dbConnService.select("getPTTrainerDetail_qualification", map);
                                infos.put("qualification", list);
                            }

                            if (photoCount > 0) {
                                list = dbConnService.select("getPTTrainerDetail_photo", map);
                                infos.put("trainerPhoto", list);
                            }

                            if (affiliatedCenterIdx != null) {
                                list = dbConnService.select("getAffiliatedCenterDetail", map);
                                infos.put("storeInfo", list);

                                photoCount = Integer.parseInt(String.valueOf(list.get(0).get("photoCount")));

                                if (photoCount > 0) {
                                    list = dbConnService.select("getAffiliatedCenter_photo", map);
                                    infos.put("storePhoto", list);
                                }
                            }
                        }
                    }

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

    @RequestMapping(value="/getPTTrainers", method = RequestMethod.POST)
    @ApiOperation(value = "PT 트레이너 목록 조회",
        notes = "{}")
    public HashMap getPTTrainers(@RequestBody String data) {
        log.info("####getPTTrainers##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

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

    @RequestMapping(value="/getPTTrainerDetail", method = RequestMethod.POST)
    @ApiOperation(value = "PT 트레이너 상세 조회",
        notes = "{\"PTTrainerIdx\":\"1\"}")
    public HashMap getPTTrainerDetail(@RequestBody String data) {
        log.info("####getPTTrainerDetail##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            if(jsonData.isEmpty()) {
                error = "Data is empty";
            } else {
                HashMap map = new HashMap();
                Set set = jsonData.keySet();
                jsonData.forEach((key, value) -> map.put(key, value));

                // imageType : 프로필, 근무경력, 수상경력, 자격증 등등
                map.put("imageType", "프로필");

                List<HashMap> list = dbConnService.select("getPTTrainerDetail", map);

                if(list.isEmpty()) {
                    error = "PTTrainer index not found";
                } else {
                    if (list.get(0).get("PTTrainerIdx") == null) {
                        error = "Trainer index " +jsonData.values() + " not found";
                    } else {
                        HashMap infos = new HashMap();
                        infos.put("PTTrainerInfo", list);

                        Integer workExperience = Integer.parseInt(String.valueOf(list.get(0).get("workExperienceCount")));
                        Integer awardWinning= Integer.parseInt(String.valueOf(list.get(0).get("awardWinningCount")));
                        Integer qualification= Integer.parseInt(String.valueOf(list.get(0).get("qualificationCount")));
                        Integer photoCount= Integer.parseInt(String.valueOf(list.get(0).get("photoCount")));

                        if(workExperience > 0) {
                            list = dbConnService.select("getPTTrainerDetail_workExperience", map);
                            infos.put("workExperience", list);
                        }

                        if(awardWinning > 0) {
                            list = dbConnService.select("getPTTrainerDetail_awardWinning", map);
                            infos.put("awardWinning", list);
                        }

                        if(qualification > 0) {
                            list = dbConnService.select("getPTTrainerDetail_qualification", map);
                            infos.put("qualification", list);
                        }

                        if(photoCount > 0) {
                            list = dbConnService.select("getPTTrainerDetail_photo", map);
                            infos.put("trainerPhoto", list);
                        }
                        rtnVal.put("infos", infos);
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
            "\n\ncategory 1:업체, 2:상품, 3:트레이너")
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
                list = dbConnService.select("getUserPick_Stores", map);
            } else if (map.get("category").equals("2")) {
                list = dbConnService.select("getUserPick_Products", map);
            } else if (map.get("category").equals("3")) {
                map.put("imageType", "프로필");
                list = dbConnService.select("getUserPick_PTTrainers", map);
            }

            if(list.isEmpty()) {
                error = "User index number " + map.get("userIdx") + " and category number " + map.get("category") +  " is not found";
            } else {
                HashMap infos = new HashMap();

                if(map.get("category").equals("1")) {
                    infos.put("stores", list);
                }
                else if (map.get("category").equals("2")) {
                    infos.put("products", list);
                }
                else if (map.get("category").equals("3")) {
                    infos.put("PTTrainers", list);
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
            "\n\ncategory 1:업체, 2:상품, 3:트레이너")
    public HashMap getUserPickDetail(@RequestBody String data) {
        log.info("####getUserPickDetail##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = null;
            HashMap infos = new HashMap();

            if(map.get("category").equals("1")) {
                // 1:store, 2:product, 3:trainer
                map.put("noteCategory", "1");

                list = dbConnService.select("getUsersPickDetail_Store", map);

                if(list.isEmpty()) {
                    error = "Store index number " + map.get("idx") + " is not found";
                } else {
                    infos.put("storeInfo", list);

                    if((Integer.parseInt(list.get(0).get("bizHour").toString())) > 0) {
                        List<HashMap> bizHour = dbConnService.select("getStoreBizHours", map);
                        infos.put("storeBizHours", bizHour);
                    }

                    if((Integer.parseInt(list.get(0).get("programs").toString())) > 0) {
                        List<HashMap> program = dbConnService.select("getStoreBizHours", map);
                        infos.put("storePrograms", program);
                    }
                }
            }
            else if (map.get("category").equals("2")) {
                // 1:store, 2:product, 3:trainer
                map.put("noteCategory", "2");

                list = dbConnService.select("getUsersPickDetail_Product", map);

                // 검색해서 가져온 list에 값이 들어있는지 확인
                if(list.isEmpty()) {
                    error = "Product index " +jsonData.values() + " not found";
                } else {
                    infos = new HashMap();
                    infos.put("productInfo", list);
                    rtnVal.put("infos", infos);
                }
            }
            else if (map.get("category").equals("3")) {
                // 1:store, 2:product, 3:trainer
                map.put("noteCategory", "3");

                // imageType 중 "프로필" 사진인 데이터만 가져옴
                map.put("imageType", "프로필");

                list = dbConnService.select("getUsersPickDetail_PTTrainer", map);

                if (list.get(0).get("PTTrainerIdx") == null) {
                    error = "Trainer index " +jsonData.values() + " not found";
                } else {
                    infos.put("PTTrainerInfo", list);

                    Integer workExperience = Integer.parseInt(String.valueOf(list.get(0).get("workExperienceCount")));
                    Integer awardWinning= Integer.parseInt(String.valueOf(list.get(0).get("awardWinningCount")));
                    Integer qualification= Integer.parseInt(String.valueOf(list.get(0).get("qualificationCount")));
                    Integer photoCount= Integer.parseInt(String.valueOf(list.get(0).get("photoCount")));

                    map.put("PTTrainerIdx", list.get(0).get("PTTrainerIdx"));

                    if(workExperience > 0) {
                        list = dbConnService.select("getPTTrainerDetail_workExperience", map);
                        infos.put("workExperience", list);
                    }

                    if(awardWinning > 0) {
                        list = dbConnService.select("getPTTrainerDetail_awardWinning", map);
                        infos.put("awardWinning", list);
                    }

                    if(qualification > 0) {
                        list = dbConnService.select("getPTTrainerDetail_qualification", map);
                        infos.put("qualification", list);
                    }

                    if(photoCount > 0) {
                        list = dbConnService.select("getPTTrainerDetail_photo", map);
                        infos.put("trainerPhoto", list);
                    }
                    rtnVal.put("infos", infos);
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

    @RequestMapping(value="/buyProduct", method = RequestMethod.POST)
    @ApiOperation(value = "상품 구매",
        notes = "{\"pointUse\":\"10000\", \"billingMethod\":\"1\", " +
            "\"totalAmount\":\"25000\", \"billingAmount\":\"15000\"," +
            "\n\n\"products\":[\n\n{\"productCategory\":\"1\", \"productIdx\":\"8\", \"price\":\"25000\", " +
            "\"quantity\":\"1\", \"amount\":\"25000\", \"sellerIdx\":\"16\"}\n\n]}" +
            "\n\nbillingMethod 1: 무통장입금, 2: 신용/체크카드, 3: 카카오페이, 4: 삼성페이, 5: 페이코, 6: 토스" +
            "\n\nproductCategory 1: PTVoucher, ... " +
            "\n\nproducts: 여러 개의 데이터가 될 수 있음" +
            "\n\nsellerIdx와 storeIdx 중 하나의 데이터가 필요")
    public HashMap buyProduct(@RequestBody String data, HttpServletRequest auth) {
        log.info("####buyProduct##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
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
                }
            } else {
                error = "transaction insert failed";
            }

            int billingMethod = Integer.parseInt(map.get("billingMethod").toString());

            if(billingMethod == 1) {
                // 무통장 입금 안내 메세지
            } else if (billingMethod == 2) {
                // 카드 결제 시스템 호출
                // 카드 결제 성공 시 결제 여부와 결제 일시(tbl_transactions : billingYN, billingDate UPDATE)
                // 카드 결제 실패 시 재시도
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

    @RequestMapping(value="/getTransactions", method = RequestMethod.POST)
    @ApiOperation(value = "구매 목록 보기",
        notes = "")
    public HashMap getTransactions(HttpServletRequest auth) {
        log.info("####getTransactions#####");
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
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
    @ApiOperation(value = "구매 취소 신청", notes = "{\"transactionIdx\":\"10\", \"cancelReason\":\"3\", \"reasonDetail\":\"지역이 멀어요.\"}")
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

        JSONParser parser = new JSONParser();
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

        JSONParser parser = new JSONParser();
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

        JSONParser parser = new JSONParser();
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
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("checkId", map);

            if(list.isEmpty()) {
                map.put("certType", "1");
                map.put("password", new PasswordCryptConverter().convertToDatabaseColumn((String) map.get("password")));

                Random random = new Random();

                // 프로필 기본 이미지 인덱스 초기단계 1~3
                int ranInt = random.nextInt(3) + 1;
                System.out.println(ranInt);

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
                    int result = result = dbConnService.insert("setInterests", map);

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
        notes = "{}")
    public HashMap getUsersNotes(@RequestBody String data) {
        log.info("####getUsersNotes##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getUsersNotes", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                error = "PT톡 목록을 불러올 수 없습니다.";
            } else {
                infos.put("usersNote", list);
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
        HttpServletRequest auth) {
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
        log.info("test");
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
        notes = "{}")
    public HashMap getFreeTalks(@RequestBody String data) {
        log.info("####getFreeTalks##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getFreeTalks", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                error = "자유톡 목록을 불러올 수 없습니다.";
            } else {
                infos.put("freeTalks", list);
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
        notes = "{}")
    public HashMap getUserLikesList(HttpServletRequest auth) {
        log.info("####getUserLikesList#####");
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
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
        notes = "{}")
    public HashMap getUserPostsList(HttpServletRequest auth) {
        log.info("####getUserPostsList#####");
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
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
        notes = "{}")
    public HashMap getUserWriteReply(HttpServletRequest auth) {
        log.info("####getUserWriteReply#####");
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
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
}
