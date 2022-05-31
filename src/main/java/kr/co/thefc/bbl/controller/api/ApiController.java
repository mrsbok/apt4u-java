package kr.co.thefc.bbl.controller.api;

import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;
import kr.co.thefc.bbl.service.DBConnService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private DBConnService dbConnService;

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

            List<HashMap> list = dbConnService.select("getPTLessionVouchars", map);
            HashMap infos = new HashMap();
            infos.put("products", list);
            for (Object key:map.keySet()) {
                infos.put(key,map.get(key));
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

    @RequestMapping(value="/getPTLessionVoucharDetail", method = RequestMethod.POST)
    @ApiOperation(value = "PT 상품 상세 조회",
            notes = "{\"idx\":\"1\"}")
    public HashMap getPTLessionVoucharDetail(@RequestBody String data) {
        log.info("####getPTLessionVoucharDetail##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            // 넘어온 data가 있는지 확인
            if(jsonData.isEmpty()) {
                error = "Data is empty";
            } else {
                HashMap map = new HashMap();
                Set set = jsonData.keySet();
                jsonData.forEach((key, value) -> map.put(key,value));

                List<HashMap> list = dbConnService.select("getPTLessionVoucharsDetail", map);

                // 검색해서 가져온 list에 값이 들어있는지 확인
                if(list.isEmpty()) {
                    error = "Product index " +jsonData.values() + " not found";
                } else {
                    HashMap infos = new HashMap();
                    infos.put("productInfo", list);
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

            List<HashMap> list = dbConnService.select("getPTTrainers", map);
            HashMap infos = new HashMap();
            infos.put("PTTrainers", list);

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

    @RequestMapping(value="/getPTTrainerDetail", method = RequestMethod.POST)
    @ApiOperation(value = "PT 트레이너 상세 조회",
            notes = "{\"idx\":\"1\"}")
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

                if (list.isEmpty()) {
                    error = "Trainer index " +jsonData.values() + " not found";
                } else {
                    HashMap infos = new HashMap();
                    infos.put("PTTrainerInfo", list);

                    list = dbConnService.select("getPTTrainerDetail_workExperience", map);
                    infos.put("workExperience", list);

                    list = dbConnService.select("getPTTrainerDetail_awardWinning", map);
                    infos.put("awardWinning", list);

                    list = dbConnService.select("getPTTrainerDetail_qualification", map);
                    infos.put("qualification", list);

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

            HashMap infos = new HashMap();
            infos.put("usersShoppingBasketProducts", list);


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

            // 에러 추가
            // 1. productIdx가 없음
            // 2. userIdx가 없음
            // 3. quantity가 없음
            
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
                list = dbConnService.select("getUserPick_PTTrainers", map);
            }

            if(list.isEmpty()) {
                error = "User index number " + map.get("userIdx") + " and category number " + map.get("category") +  " is not found";
            } else {
                HashMap infos = new HashMap();
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

    @RequestMapping(value="/getUserPickDetail", method = RequestMethod.POST)
    @ApiOperation(value = "사용자 찜 상품 상세 보기",
            notes = "{\"idx\":\"1\", \"category\":\"1\"}" +
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
            } else if (map.get("category").equals("2")) {
                list = dbConnService.select("getUsersPickDetail_Product", map);

                if(list.isEmpty()) {
                    error = "Product index number " + map.get("idx") + " is not found";
                } else {
                    infos.put("productInfo", list);
                }
            } else if (map.get("category").equals("3")) {
                list = dbConnService.select("getUsersPickDetail_PTTrainer", map);

                if(list.isEmpty()) {
                    error = "PTTrainer index number " + map.get("idx") + " is not found";
                } else {
                    infos.put("PTTrainerInfo", list);

                    list = dbConnService.select("getPTTrainerDetail_workExperience", map);
                    infos.put("workExperience", list);

                    list = dbConnService.select("getPTTrainerDetail_awardWinning", map);
                    infos.put("awardWinning", list);

                    list = dbConnService.select("getPTTrainerDetail_qualification", map);
                    infos.put("qualification", list);
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

    @RequestMapping(value="/addUserPick", method = RequestMethod.POST)
    @ApiOperation(value = "사용자 찜 상품 추가",
            notes = "{\"userIdx\":\"1\", \"category\":\"2\", \"pickItemIdx\":\"1\"}" +
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

            int result = dbConnService.insert("addUserPick", map);

            if(result == 0) {
                error = "Failed to add to user pick list";
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
            notes = "{\"usersPicksIdx\":\"1\"}")
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

            int result = dbConnService.delete("deleteUserPick", map);

            if(result == 0) {
                error = "Failed to delete from user pick list";
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

            HashMap infos = new HashMap();
            infos.put("Transactions", list);

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
                rtnVal.put("infos", infos);
                
                // product 데이터 가져오기
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

//    @RequestMapping(value="/cancelTransaction", method = RequestMethod.POST)
//    @ApiOperation(value = "구매 취소", notes = "구매 취소")
//    public HashMap cancelTransaction(@RequestBody String data) {
//        log.info("####cancelTransaction##### : " + data);
//        HashMap rtnVal = new HashMap();
//
//        JSONParser parser = new JSONParser();
//        String error = null;
//
//        try{
//            JSONObject jsonData = (JSONObject) parser.parse(data);
//
//            HashMap map = new HashMap();
//            Set set = jsonData.keySet();
//            jsonData.forEach((key, value) -> map.put(key,value));
//
//        } catch (ParseException e) {
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
}
