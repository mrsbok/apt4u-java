package kr.co.thefc.bbl.controller.api;

import kr.co.thefc.bbl.service.DBConnService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    //PT 상품 리스트 조회
    @RequestMapping(value="/getPTLessionVouchars", method = RequestMethod.POST)
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
    public HashMap getPTLessionVoucharDetail(@RequestBody String data) {
        log.info("####getPTLessionVoucharDetail##### : " + data);
        HashMap rtnVal = new HashMap();

        JSONParser parser = new JSONParser();
        String error = null;

        try{
            JSONObject jsonData = (JSONObject) parser.parse(data);

            HashMap map = new HashMap();
            Set set = jsonData.keySet();
            jsonData.forEach((key, value) -> map.put(key,value));

            List<HashMap> list = dbConnService.select("getPTLessionVoucharsDetail", map);

            // 검색해서 가져온 list에 값이 들어있는지 확인
            if(list.isEmpty()) {
                error = jsonData.values() + "번 상품은 존재하지 않습니다.";
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

    @RequestMapping(value="/getPTTrainers", method = RequestMethod.POST)
    public HashMap getPTTrainers(@RequestBody String data) {
        log.info("####getPTLessionVoucharDetail##### : " + data);
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
}
