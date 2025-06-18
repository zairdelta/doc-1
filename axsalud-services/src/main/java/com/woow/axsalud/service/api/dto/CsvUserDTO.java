package com.woow.axsalud.service.api.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class CsvUserDTO {
    @CsvBindByName(column = "hid", required = true)
    private String hid;

    @CsvBindByName(column = "name", required = true)
    private String name;

    @CsvBindByName(column = "last_name", required = true)
    private String lastName;
}
