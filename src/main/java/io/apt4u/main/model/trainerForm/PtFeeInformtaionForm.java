package io.apt4u.main.model.trainerForm;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "1회 체험가 저장 폼")
@Data
public class PtFeeInformtaionForm {

        Integer idx = null;

        @ApiModelProperty(
            value = "센터 회원권 별도 결제 유무",
           example = "결제",
            required = true
        )
        String separatePaymentYN;

        @ApiModelProperty(
            value = "레슨 1회 이용시간",
            example = "10분",
            required = true
        )
        String roundTime;

        @ApiModelProperty(
            value = "가격 참고 사항",
            required = true,
            example = "가격은 1회권이며 ...."
        )
        String notice;

}
