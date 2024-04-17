package com.example.item.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.example.common.enums.ItemStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author author
 * @since 2024-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("item")
@ApiModel(value="Item对象", description="商品表")
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品id")
    private Long id;

    @ApiModelProperty(value = "SKU名称")
    private String name;

    @ApiModelProperty(value = "价格（分）")
    private Integer price;

    @ApiModelProperty(value = "库存数量")
    private Integer stock;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "类目名称")
    private String category;

    @ApiModelProperty(value = "品牌名称")
    private String brand;

    @ApiModelProperty(value = "规格")
    private String spec;

    @ApiModelProperty(value = "销量")
    private Integer sold;

    @ApiModelProperty(value = "评论数")
    private Integer commentCount;

    @ApiModelProperty(value = "是否是推广广告，true/false")
    @TableField(value = "isAD")
    private Boolean isAD;

    @ApiModelProperty(value = "商品状态 1-正常，2-下架，3-删除")
    private ItemStatus status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private Long creater;

    @ApiModelProperty(value = "修改人")
    private Long updater;


}
