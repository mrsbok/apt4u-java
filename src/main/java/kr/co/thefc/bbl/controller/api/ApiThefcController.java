package kr.co.thefc.bbl.controller.api;

import kr.co.thefc.bbl.service.DBConnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiThefcController {
    @Autowired
    private DBConnService dbConnService;

    @RequestMapping(value="/now2", method = RequestMethod.POST)
    public HashMap now2() {
        log.info("####now2#####");
        HashMap rtnVal = new HashMap();

        List<HashMap> list = dbConnService.select("select_now_api_thefc", null);
        rtnVal.put("now", list.get(0).get("now"));

        return rtnVal;
    }
}

