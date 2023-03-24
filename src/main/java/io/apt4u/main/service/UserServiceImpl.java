package io.apt4u.main.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public List<HashMap> getPTLessonVoucherSellerInfo(List<HashMap> list, HashMap map) {
        try {
            if(!list.isEmpty()) {
                HashMap data = new HashMap();
                HashMap temp = new HashMap();

                for (int i = 0; i < list.size(); i++) {
                    if ("1".equals(list.get(i).get("sellerType").toString())) {
                        // 업체정보
                        map.put("storeIdx", list.get(i).get("sellerIdx"));

                        data = dbConnService.selectOne("getPTLessonVouchersOfStore", map);

                        if ("1".equals(data.get("storeType").toString())) {
                            //storeType이 1(fitnessGym)이면
                            temp = dbConnService.selectOne("getFitnessGymMainImage", map);

                            if (temp != null) {
                                data.put("imagePath", temp.get("imagePath"));
                                data.put("imageFileName", temp.get("imageFileName"));
                            }

                            list.get(i).put("storeInfo", data);
                        }
                    } else if ("2".equals(list.get(i).get("sellerType").toString())) {
                        // PT트레이너 정보
                        map.put("PTTrainerIdx", list.get(i).get("sellerIdx"));

                        data = dbConnService.selectOne("getPTLessonVouchersOfPTTrainer", map);

                        list.get(i).put("PTTrainerInfo", data);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

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

    @Override
    public String phoneFormat(String number) {
        String regEx = "(\\d{3})(\\d{3,4})(\\d{4})";

        return number.replaceAll(regEx, "$1-$2-$3");
    }

}
