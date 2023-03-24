package io.apt4u.main.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "거래내역 폼ㅁ")
@Data
public class TransactionForm {

        Integer idx = null;


        @ApiModelProperty(
            value = "트레이너 idx",
            example = "1",
            required = true
        )
        Integer trainerIdx;

        @ApiModelProperty(
            value = "트레이너 idx",
            example = "1",
            required = true
        )
        Integer userIdx;

        @ApiModelProperty(
            value = "상품 카테고리(1 = PT 상품권)",
            example = "1",
            required = true
        )
        Integer productCategory;


}
