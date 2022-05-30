package kr.co.thefc.bbl.controller.api;

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
    @ApiOperation(value = "PT 상품 목록 조회", notes = "PT 상품 목록")
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
    @ApiOperation(value = "PT 상품 상세 조회", notes = "PT 상품 상세 조회")
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

    @RequestMapping(value="/addNewPTLessionVoucher", method = RequestMethod.POST)
    @ApiOperation(value = "PT 이용권 상품 등록", notes = "PT 이용권 등록")
    public HashMap addNewPTLessionVoucher(@RequestBody String data) {
        log.info("####addNewPTLessionVoucher##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            map.put("productType", "1");

            System.out.println(map);

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
    @ApiOperation(value = "PT 트레이너 목록 조회", notes = "PT 트레이너 목록")
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
    @ApiOperation(value = "PT 트레이너 상세 조회", notes = "PT 트레이너 상세 조회")
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
    @ApiOperation(value = "장바구니 조회", notes = "장바구니 조회")
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
    @ApiOperation(value = "장바구니 상품 추가", notes = "장바구니 상품 추가")
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
    @ApiOperation(value = "장바구니 상품 삭제", notes = "장바구니 상품 삭제")
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
    @ApiOperation(value = "사용자 찜 목록", notes = "사용자 찜 목록")
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
    @ApiOperation(value = "사용자 찜 상품 상세 보기", notes = "사용자 찜 상품 상세 보기")
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
    @ApiOperation(value = "사용자 찜 상품 추가", notes = "사용자 찜 상품 추가")
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
    @ApiOperation(value = "사용자 찜 상품 삭제", notes = "사용자 찜 상품 삭제")
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
            notes = "userIdx, productCategory, productIdx, voucherType, " +
                    "price, quantity, sellerIdx or storeIdx, kindOfItems, pointUse, billingMethod")
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
            int totalAmount = 0;
            int pointUse = Integer.parseInt(map.get("pointUse").toString());
            int billingAmount;
            int price, quantity, amount;

            for(int i=0; i<arr.size(); i++) {
                JSONObject obj = (JSONObject) arr.get(i);

                price = Integer.parseInt(obj.get("price").toString());
                quantity = Integer.parseInt(obj.get("quantity").toString());

                totalAmount = totalAmount + (price * quantity);
            }

            billingAmount = totalAmount - pointUse;

            map.put("kindOfItem", kindOfItem);
            map.put("totalAmount", totalAmount);
            map.put("billingAmount", billingAmount);

            int result = dbConnService.insert("insertTransaction", map);

            if(result > 0)  {
                for(int i=0; i<arr.size(); i++) {
                    JSONObject obj = (JSONObject) arr.get(i);
                    obj.forEach((key, value) -> map.put(key, value));

                    price = Integer.parseInt(obj.get("price").toString());
                    quantity = Integer.parseInt(obj.get("quantity").toString());

                    amount = price * quantity;

                    map.put("amount", amount);

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

//    @RequestMapping(value="/getTransactions", method = RequestMethod.POST)
//    @ApiOperation(value = "구매 목록 보기", notes = "구매 목록 보기")
//    public HashMap getTransactions(@RequestBody String data) {
//        log.info("####getTransactions##### : " + data);
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
//
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
