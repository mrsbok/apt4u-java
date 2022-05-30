package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "1회 체험가 저장 폼")
@Data
public class PtUsePriceDetailForm {

        Integer idx = null;

        @ApiModelProperty(
            value = "회",
            required = true,
            example = "1회"
        )
        String round;

        @ApiModelProperty(
            value = "가격",
            required = true,
            example = "100000"
        )
        Integer amount;

}
