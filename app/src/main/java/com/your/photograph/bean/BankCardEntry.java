package com.your.photograph.bean;

import java.io.Serializable;

public class BankCardEntry implements Serializable {

    private String bankCardNumber;//银行卡号

    private String bankName;//银行卡名称


    private String bankType;//卡类型

    public String getBankType() {
        return bankType;
    }

    public void setBankType(String bankType) {
        this.bankType = bankType;
    }

    public String getBankCardNumber() {
        return bankCardNumber;
    }

    public void setBankCardNumber(String bankCardNumber) {
        this.bankCardNumber = bankCardNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
