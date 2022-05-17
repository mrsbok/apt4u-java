package kr.co.thefc.bbl.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@Transactional
public class TransactionService {
    @Autowired
    DBConnService dbConnService;

/*********************** 참고용 소스 ************************************************************************************/
    public HashMap insertCleanerData(HashMap data) {
        HashMap rtnval = new HashMap();

        int inserted_id = dbConnService.insertWithReturnInt("insertCleanerInfo", data);

        if (inserted_id > 0) {
            if (data.get("cleaner_work_day_type").toString().equals("3")) {
                try {
                    List<HashMap> list = new ArrayList<>();
                    String[] list_sch_date = data.get("sch_date").toString().split(",");
                    for (int i = 0; i < list_sch_date.length; i++) {
                        HashMap map = new HashMap();
                        map.put("cleaner_id", inserted_id);
                        map.put("sch_date", list_sch_date[i].trim());
                        list.add(map);
                    }
                    dbConnService.insertList("insertSchDate", list);
                }catch (Exception ex) {
                    rtnval.put("error", "클리너 스케줄정보를 저장하지 못했습니다.");
                    ex.printStackTrace();
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                }
            }

            rtnval.put("inserted_id", inserted_id);
        }
        else {
            rtnval.put("error", "클리너 정보를 저장하지 못했습니다.");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return rtnval;
    }
}
