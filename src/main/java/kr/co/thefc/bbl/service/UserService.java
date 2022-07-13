package kr.co.thefc.bbl.service;

import java.util.HashMap;
import java.util.List;

public interface UserService {
    public List<HashMap> getPTLessonVoucherSellerInfo(List<HashMap> list, HashMap map);

    public HashMap getPTTrainerDetail(HashMap map);

    public HashMap getPTLessonVoucherDetail(HashMap map);

    public String phoneFormat(String number);
}
