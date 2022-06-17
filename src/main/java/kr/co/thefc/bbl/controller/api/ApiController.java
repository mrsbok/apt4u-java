package kr.co.thefc.bbl.controller.api;

import com.amazonaws.services.s3.model.S3Object;
import io.swagger.annotations.ApiOperation;
import kr.co.thefc.bbl.converter.PasswordCryptConverter;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private DBConnService dbConnService;

    @Autowired
    private S3Service s3Service;

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
                                // imageType : 프로필, 근무경력, 수상경력, 자격증 등등
                                map.put("imageType", "프로필");

                                list = dbConnService.select("getPTTrainerDetail_photo", map);
                                infos.put("trainerPhoto", list);
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
                            // imageType : 프로필, 근무경력, 수상경력, 자격증 등등
                            map.put("imageType", "프로필");

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
            notes = "{\"userIdx\":\"1\"}")
    public HashMap getShoppingItems(@RequestBody String data) {
        log.info("####getShoppingItems##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getShoppingItems", map);

            if(list.isEmpty()) {
                error = "PTTrainer index not found";
            } else {
                HashMap infos = new HashMap();
                infos.put("usersShoppingBasketProducts", list);

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

    @RequestMapping(value="/addShoppingItems", method = RequestMethod.POST)
    @ApiOperation(value = "장바구니 상품 추가",
            notes = "{\"userIdx\":\"1\", \"productIdx\":\"1\", \"productCategory\":\"1\", \"quantity\":\"1\"}" +
                    "\n\nproductCategory(=CD_ProductClassification) 1: PTVoucher")
    public HashMap addShoppingItems(@RequestBody String data) {
        log.info("####addShoppingItems##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));
            
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
            notes = "{\"userIdx\":\"1\", \"category\":\"1\"}" +
                    "\n\ncategory 1:업체, 2:상품, 3:트레이너")
    public HashMap getUserPick(@RequestBody String data) {
        log.info("####getUserPick##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

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
                        // imageType 중 "프로필" 사진인 데이터만 가져옴
                        map.put("imageType", "프로필");
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
            notes = "{\"userIdx\":\"1\", \"category\":\"2\", \"pickedItemIdx\":\"1\"}" +
                    "\n\ncategory 1:업체, 2:상품, 3:트레이너")
    public HashMap getUsersPicks(@RequestBody String data) {
        log.info("####getUsersPicks##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

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
            notes = "{\"userIdx\":\"1\", \"category\":\"2\", \"pickedItemIdx\":\"1\"}" +
                    "\n\ncategory 1:업체, 2:상품, 3:트레이너")
    public HashMap addUserPick(@RequestBody String data) {
        log.info("####addUserPick##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

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
            notes = "{\"userIdx\":\"1\", \"category\":\"2\", \"pickedItemIdx\":\"1\"}")
    public HashMap deleteUserPick(@RequestBody String data) {
        log.info("####deleteUserPick##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

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
            notes = "{\"userIdx\":\"1\", \"pointUse\":\"10000\", \"billingMethod\":\"1\", " +
                    "\"totalAmount\":\"25000\", \"billingAmount\":\"15000\"," +
                    "\n\n\"products\":[\n\n{\"productCategory\":\"1\", \"productIdx\":\"8\", \"price\":\"25000\", " +
                    "\"quantity\":\"1\", \"amount\":\"25000\", \"sellerIdx\":\"16\"}\n\n]}" +
                    "\n\nbillingMethod 1: 무통장입금, 2: 신용/체크카드, 3: 카카오페이, 4: 삼성페이, 5: 페이코, 6: 토스" +
                    "\n\nproductCategory 1: PTVoucher, ... " +
                    "\n\nproducts: 여러 개의 데이터가 될 수 있음" +
                    "\n\nsellerIdx와 storeIdx 중 하나의 데이터가 필요")
    public HashMap buyProduct(@RequestBody String data) {
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
            notes = "{\"userIdx\":\"1\"}")
    public HashMap getTransactions(@RequestBody String data) {
        log.info("####getTransactions##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getTransactions", map);

            if(list.isEmpty()) {
                error = "User index number " + map.get("userIdx") + " and category number " + map.get("category") +  " is not found";
            } else {
                HashMap infos = new HashMap();
                infos.put("Transactions", list);

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
            notes = "{\"userIdx\":\"1\"}")
    public HashMap getCancelTransactions(@RequestBody String data) {
        log.info("####getCancelTransactions##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getCancelTransactions", map);

            if(list.isEmpty()) {
                error = "User index number " + map.get("userIdx") + " and category number " + map.get("category") +  " is not found";
            } else {
                HashMap infos = new HashMap();
                infos.put("CancelTransactions", list);

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

    @RequestMapping(value="/addUserPTRecords", method = RequestMethod.POST)
    @ApiOperation(value = "개인 운동 일정 등록",
            notes = "{\"userIdx\":\"1\", \"date\":\"2022-03-24\", \"exerciseCategory\":\"1\", \"exerciseType\":\"2\", " +
                    "\"exerciseName\":\"푸쉬업\", \"exerciseDetails\":\"70,0,10,5\"}" +
                    "\n\nexerciseCategory : 운동 종목(1: PT&헬스, 2: 필라테스&요가)" +
                    "\n\nexerciseType : PT 운동 구분(열거형 데이터 정의 필요)" +
                    "\n\nexerciseName : 운동 명칭" +
                    "\n\nexerciseDetails : 중량,지속시간,운동횟수,세트수 순서대로 기입 만약 데이터가 없으면 0이 들어가게")
    public HashMap addUserPTRecords(@RequestBody String data) {
        log.info("####addUserPTRecords##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

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
    @ApiOperation(value = "개인 운동 일정 보기", notes = "{\"userIdx\":\"1\"}")
    public HashMap getUserPTRecords(@RequestBody String data) {
        log.info("####getUserPTRecords##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getUserPTRecords", map);

            if(list.isEmpty()) {
                error = "User index " +jsonData.values() + " not found";
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
    @ApiOperation(value = "트레이너가 등록한 운동 일정 보기", notes = "{\"userIdx\":\"1\"}")
    public HashMap getUserPTRecordsWithTrainer(@RequestBody String data) {
        log.info("####getUserPTRecordsWithTrainer##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getUserPTRecordsWithTrainer", map);

            if(list.isEmpty()) {
                error = "User index " +jsonData.values() + " not found";
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

    @RequestMapping(value="/checkedUnreadMessage", method = RequestMethod.POST)
    @ApiOperation(value = "읽지 않은 알림 확인", notes = "{\"userIdx\":\"1\"}")
    public HashMap checkedUnreadMessage(@RequestBody String data) {
        log.info("####checkedUnreadMessage##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

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

    @RequestMapping(value="/getScheduleMessages", method = RequestMethod.POST)
    @ApiOperation(value = "일정 알림 메세지 보기", notes = "{\"userIdx\":\"1\"}")
    public HashMap getScheduleMessages(@RequestBody String data) {
        log.info("####getScheduleMessages##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            // IDType 1:User, 2:Store, 3:PTTrainer, 4:BBL Manager
            map.put("IDType", "1");
            // messageType 1:PT 일정 관리
            map.put("messageType", "1");

            List<HashMap> list = dbConnService.select("getMessages", map);

            if(list.isEmpty()) {
                error = "User index " +jsonData.values() + " messages not found";
            } else {
                HashMap infos = new HashMap();
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
                    "\"userIdx\":\"1\", \"receiverIdx\":\"1\"}" +
                    "\n\n여기서 receiverIdx는 getScheduleMessagesDetail의 senderIdx" +
            "\n\nconfirmed 1:동의 2:비동의")
    public HashMap setScheduleConfirmed(@RequestBody String data) {
        log.info("####setScheduleConfirmed##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

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
            notes = "{\"userIdx\":\"15\", " +
                    "\"interests\":[{\"interestCode\":\"1\"}, {\"interestCode\":\"2\"}]}")
    public HashMap setUsersInterests(@RequestBody String data) {
        log.info("####setUsersInterests##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

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

    @RequestMapping(value="user/getUserInfo", method = RequestMethod.POST)
    @ApiOperation(value = "유저 - 유저 정보 가져오기 ",
            notes = "{\"email\":\"gildong@daum.net\", \"password\":\"12345\"}")
    public HashMap getUserInfo(@RequestBody String data) {
        log.info("####getUserInfo##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            map.put("password", new PasswordCryptConverter().convertToDatabaseColumn((String) map.get("password")));

            List<HashMap> list = dbConnService.select("getUserIdx", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                error = "email : " + map.get("email") + ", password : " + map.get("password") + " Not Found";
            } else {
                map.put("userIdx", list.get(0).get("userIdx"));

                list = dbConnService.select("getUsersInfo", map);
                infos.put("userInfo", list);

                Integer numOfInterest = (Integer) list.get(0).get("numOfInterest");
                if(numOfInterest > 0) {
                    list = dbConnService.select("getInterests", map);
                    infos.put("userInterests", list);
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

            map.put("noteCategory", "2");

            List<HashMap> list = dbConnService.select("getUsersNotes", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                error = "PT톡 목록을 불러올 수 없습니다.";
            } else {
                infos.put("usersNotes", list);
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
            map.put("replyCategory", "1");

            List<HashMap> list = dbConnService.select("getUsersNotesDetail", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                error = "PT톡 상세보기를 불러올 수 없습니다.";
            } else {
                infos.put("usersNotesDetail", list);

                list = dbConnService.select("getNotesReplies", map);

                if(list.isEmpty()) {
                    infos.put("replies", "등록된 댓글이 없습니다.");
                } else {
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
            notes = "{\"PTTrainerIdx\":\"1\", \"userSatisfaction\":\"5\", \"useStartDate\":\"2022-06-15\", " +
                    "\"useEndDate\":\"2022-06-22\", \"content\":\"일반 이용 후기 작성 테스트\", " +
                    "\"hashtag\":\"#일반이용,#해시태그,#테스트\", \"userIdx\":\"1\"}")
    public HashMap writeGeneralReview(@RequestBody String data) {
        log.info("####writeGeneralReview##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            map.put("noteCategory", "3");

            int result = dbConnService.insert("writeReview", map);

            if(result == 0) {
                error = "후기 작성 실패";
            } else {
                //사진 등록 + photoCout 진행

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

    @RequestMapping(value="getPTTrainersPTUsers", method = RequestMethod.POST)
    @ApiOperation(value = "체험권 구매내역 조회",
            notes = "{\"userIdx\":\"15\"}")
    public HashMap getPTTrainersPTUsers(@RequestBody String data) {
        log.info("####getPTTrainersPTUsers##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getPTTrainersPTUsers", map);
            HashMap infos = new HashMap();

            if(list.isEmpty()) {
                infos.put("vouchersPurchaseHistory", false);
            } else {
                infos.put("vouchersPurchaseHistory", list);
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

    @RequestMapping(value="writeExperienceReview", method = RequestMethod.POST)
    @ApiOperation(value = "PT톡 작성 - 1회 체험 후기",
            notes = "{ \"PTTrainerIdx\":\"1\"," +
                    " \"userSatisfaction\":\"5\"," +
                    " \"dateStart\":\"2022-02-03\"," +
                    " \"dateEnd\":\"2022-02-04\"," +
                    " \"content\":\"1회 체험 후기 작성 테스트\"," +
                    " \"hashtag\":\"#1회체험후기작성\"," +
                    " \"userIdx\":\"15\"}" +
                    "\n\nPTTrainerIdx, dateStart, dateEnd는 getPTTrainersPTUsers에서 반환된 값")
    public HashMap writeExperienceReview(@RequestBody String data) {
        log.info("####writeExperienceReview##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            map.put("noteCategory", "2");
            map.put("useStartDate", map.get("dateStart"));
            map.put("useEndDate", map.get("dateEnd"));

            int result = dbConnService.insert("writeReview", map);

            if(result == 0) {
                error = "후기 작성 실패";
            } else {
                //사진 등록 + photoCount 진행
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
            notes = "{\"noteIdx\":\"2\", \"userIdx\":\"13\", \"content\":\"후기 댓글 작성\"," +
                    " \"hiddenYN\":\"0\"\n}")
    public HashMap writeReplyToNote(@RequestBody String data) {
        log.info("####writeReplyToNote##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            map.put("category", "1");
            map.put("targetIdx", map.get("noteIdx"));

            int result = dbConnService.insert("writeReply", map);

            if(result == 0) {
                error = "후기 댓글 작성 실패";
            } else {
                dbConnService.update("updateReplyCount", map);
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

            int result = dbConnService.delete("deleteReply", map);

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
            notes = "{\"noteIdx\":\"2\", \"userIdx\":\"13\"}")
    public HashMap likeNotes(@RequestBody String data) {
        log.info("####likeNotes##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            map.put("category", "1");
            map.put("targetIdx", map.get("noteIdx"));

            int result = dbConnService.insert("likeNotes", map);

            if(result == 0) {
                error = "후기 게시글 좋아요 실패";
            } else {
                dbConnService.update("updateLikeCount", map);
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

    @RequestMapping(value="usersLikesIt", method = RequestMethod.POST)
    @ApiOperation(value = "사용자의 후기 게시글 좋아요 여부",
            notes = "{\"userIdx\":\"13\", \"idx\":\"1\"}")
    public HashMap usersLikesIt(@RequestBody String data) {
        log.info("####usersLikesIt##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);
            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            map.put("category", "1");
            map.put("targetIdx", map.get("idx"));

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
}
