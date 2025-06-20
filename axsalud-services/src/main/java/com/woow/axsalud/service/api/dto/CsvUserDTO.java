package com.woow.axsalud.service.api.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class CsvUserDTO {
    @CsvBindByName(column = "hid", required = true)
    private String hid;

    @CsvBindByName(column = "nombre", required = true)
    private String name;

    @CsvBindByName(column = "apellido", required = true)
    private String lastName;
}
