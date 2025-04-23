package com.woow.serviceprovider.api;

import lombok.Data;

import java.util.Date;

@Data
public class TelemedicineDTO {
    private String NOMBRE_ASEGURADO;   //mandatory
    private Date FECHA_NACIMIENTO;
    private String SEXO;
    private boolean FAMILY_CARD;     // mandatory
    private int ACCESS_LEVEL;
    private String FECHA_VIGENCIA_DESDE;
    private String FECHA_VIGENCIA_HASTA;     // mandatory  DD/MM/AAAA
    private String NUMERO_POLIZA;
    private String CORREO_ELECTRONICO;
    private String TELEFONO;
    private String CIUDAD;
    private String CODIGO_PAIS;
}