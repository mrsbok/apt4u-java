package io.apt4u.main.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "사용 요금")
@Data
public class PtUsePriceForm extends PtUsePriceDetailForm{

        Integer idx = null;


        @ApiModelProperty(
            value = "제목 타이틀",
           example = "별도 결제함",
            required = true
        )
        String title;

        @ApiModelProperty(
            value = "회차 구분",
            required = true,
            example = "1회"
        )
        String round;

        @ApiModelProperty(
            value = "사용 요금 상세",
            required = true
        )
        List<PtUsePriceDetailForm> ptUsePriceDetailForm;
}
