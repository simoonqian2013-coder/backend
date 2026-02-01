package com.qss.pet.dto;

import jakarta.validation.constraints.NotNull;

public class AdoptionReviewRequest {
    @NotNull
    private Integer status;

    private String remark;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
