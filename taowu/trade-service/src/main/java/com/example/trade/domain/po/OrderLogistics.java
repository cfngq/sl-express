package com.example.trade.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author author
 * @since 2024-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_logistics")
@ApiModel(value="OrderLogistics对象", description="")
public class OrderLogistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id，与订单表一对一")
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    @ApiModelProperty(value = "物流单号")
    private String logisticsNumber;

    @ApiModelProperty(value = "物流公司名称")
    private String logisticsCompany;

    @ApiModelProperty(value = "收件人")
    private String contact;

    @ApiModelProperty(value = "收件人手机号码")
    private String mobile;

    @ApiModelProperty(value = "省")
    private String province;

    @ApiModelProperty(value = "市")
    private String city;

    @ApiModelProperty(value = "区")
    private String town;

    @ApiModelProperty(value = "街道")
    private String street;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
