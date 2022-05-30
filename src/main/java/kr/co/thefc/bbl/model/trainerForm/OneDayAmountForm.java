package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "1회 체험가 저장 폼")
@Data
public class OneDayAmountForm {

        Integer idx = null;


        @ApiModelProperty(
            value = "상품명",
            example = "1회 체험권",
            required = true
        )
        String name;

        @ApiModelProperty(
            value = "상세 설명",
            example = "PT",
            required = true
        )
        String description;

        @ApiModelProperty(
            value = "상품유형(Byte)",
            example = "1",
            required = true,
            dataType = "int"
        )
        Byte productType;

        @ApiModelProperty(
            value = "할인율",
           example = "20",
            required = true
        )
        Integer discountRate;

        @ApiModelProperty(
            value = "원가격",
            required = true,
            example = "100000"
        )
        Integer price;

        @ApiModelProperty(
            value = "1회 체험 가격",
            example = "8000",
            required = true
        )
        Integer discountPrice;


}
