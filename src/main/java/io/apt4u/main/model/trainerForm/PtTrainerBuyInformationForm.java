package io.apt4u.main.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "구매정보 폼")
@Data
public class PtTrainerBuyInformationForm {

        Integer idx = null;

        // 상세내용
        @ApiModelProperty(
            value = "상세내용",
            example = "pt 1회권 ..... 5만원... 환불...어쩌고",
            required = true
        )
        String notice;


}
