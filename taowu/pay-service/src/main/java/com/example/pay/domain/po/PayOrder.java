package com.example.pay.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.example.common.enums.PayType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 支付订单
 * </p>
 *
 * @author author
 * @since 2024-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_order")
@ApiModel(value="PayOrder对象", description="支付订单")
public class PayOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "业务订单号")
    private Long bizOrderNo;

    @ApiModelProperty(value = "支付单号")
    private Long payOrderNo;

    @ApiModelProperty(value = "支付用户id")
    private Long bizUserId;

    @ApiModelProperty(value = "支付渠道编码")
    private String payChannelCode;

    @ApiModelProperty(value = "支付金额，单位分")
    private Integer amount;

    @ApiModelProperty(value = "支付类型，1：h5,2:小程序，3：公众号，4：扫码，5：余额支付")
    private Integer payType;

    @ApiModelProperty(value = "支付状态，0：待提交，1:待支付，2：支付超时或取消，3：支付成功")
    private Integer status;

    @ApiModelProperty(value = "拓展字段，用于传递不同渠道单独处理的字段")
    private String expandJson;

    @ApiModelProperty(value = "第三方返回业务码")
    private String resultCode;

    @ApiModelProperty(value = "第三方返回提示信息")
    private String resultMsg;

    @ApiModelProperty(value = "支付成功时间")
    private LocalDateTime paySuccessTime;

    @ApiModelProperty(value = "支付超时时间")
    private LocalDateTime payOverTime;

    @ApiModelProperty(value = "支付二维码链接")
    private String qrCodeUrl;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private Long creater;

    @ApiModelProperty(value = "更新人")
    private Long updater;

    @ApiModelProperty(value = "逻辑删除")
    private Boolean isDelete;


}
