package com.java2nb.novel.vo;

import lombok.Data;

import java.util.Date;

/**
 * 小说搜索参数
 *
 * @author 10253
 */
@Data
public class SearchDataVO {

    private Integer curr;

    private Integer offset;

    private Integer limit;

    private String keyword;

    private Byte workDirection;

    private Integer catId;

    private Byte isVip;

    private Byte bookStatus;

    private Integer wordCountMin;

    private Integer wordCountMax;

    private Date updateTimeMin;

    private Long updatePeriod;

    private String sort;

    public void calculateOffset(){
        offset = (curr - 1) * limit;
    }


}
