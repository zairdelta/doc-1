package com.woow.serviceprovider.api;

import lombok.Data;

@Data
public class TelemedicineResponse {
    int code;
    private String message;
    private TelemedicineDTO payload;
}
