package kr.co.thefc.bbl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private DBConnService dbConnService;
    public HashMap getPTTrainerDetail(HashMap map) {
        HashMap data = new HashMap();
        String error = null;

        try{
            if(map == null) {
                error = "Data is empty";
            } else {
                // imageType : 프로필, 근무경력, 수상경력, 자격증 등등
                map.put("imageType", "프로필");

                data = dbConnService.selectOne("getPTTrainerDetail", map);

                if(data == null) {
                    error = "PTTrainer index not found";
                } else {
                    Integer workExperience = Integer.parseInt(String.valueOf(data.get("workExperienceCount")));
                    Integer awardWinning= Integer.parseInt(String.valueOf(data.get("awardWinningCount")));
                    Integer qualification= Integer.parseInt(String.valueOf(data.get("qualificationCount")));
                    Integer photoCount= Integer.parseInt(String.valueOf(data.get("photoCount")));

                    List<HashMap> list = new ArrayList();

                    if (workExperience > 0) {
                        list = dbConnService.select("getPTTrainerDetail_workExperience", map);

                        if(!list.isEmpty()) {
                            data.put("workExperience", list);
                        }
                    }

                    if (awardWinning > 0) {
                        list = dbConnService.select("getPTTrainerDetail_awardWinning", map);

                        if(!list.isEmpty()) {
                            data.put("awardWinning", list);
                        }
                    }

                    if (qualification > 0) {
                        list = dbConnService.select("getPTTrainerDetail_qualification", map);

                        if(!list.isEmpty()) {
                            data.put("qualification", list);
                        }
                    }

                    if (photoCount > 0) {
                        list = dbConnService.select("getPTTrainerDetail_photo", map);

                        if(!list.isEmpty()) {
                            data.put("trainerPhoto", list);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "정보를 파싱하지 못했습니다.";
        }

        if(error != null) {
            data.put("errorMsg", error);
        }

        return data;
    }

    @Override
    public HashMap getPTLessonVoucherDetail(HashMap map) {
        HashMap temp = new HashMap();
        String error = null;

        try{
            map.put("noteCategory", "2");
            map.put("imageType", "프로필");

            temp = dbConnService.selectOne("getPTLessonVoucherDetail", map);
            HashMap temp2 = new HashMap();

            if(temp != null) {
                if("1".equals(temp.get("sellerType").toString())) {
                    // 업체정보
                    map.put("storeIdx", temp.get("sellerIdx"));
                    temp2 = dbConnService.selectOne("getPTLessonVouchersOfStore", map);

                    if("1".equals(temp2.get("storeType").toString())) {
                        //storeType이 1(fitnessGym)이면
                        HashMap image = dbConnService.selectOne("getFitnessGymImage", map);

                        if(image != null) {
                            temp2.put("imagePath", image.get("imagePath"));
                            temp2.put("imageFileName", image.get("imageFileName"));
                        }

                        temp.put("storeInfo", temp2);
                    }
                } else if("2".equals(temp.get("sellerType").toString())) {
                    // PT트레이너 정보
                    map.put("PTTrainerIdx", temp.get("sellerIdx"));

                    temp.put("PTTrainerInfo", getPTTrainerDetail(map));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return temp;
    }

}
