package com.legendsofsw.battleservice.dto;

public class ActionRequest {

    private String actionType;
    private Integer targetIndex;

    public ActionRequest() {
    }

    public ActionRequest(String actionType, Integer targetIndex) {
        this.actionType = actionType;
        this.targetIndex = targetIndex;
    }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Integer getTargetIndex() { return targetIndex; }
    public void setTargetIndex(Integer targetIndex) { this.targetIndex = targetIndex; }
}
