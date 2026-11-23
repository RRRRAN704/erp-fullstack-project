package com.erp.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * employee login info
 */
@Data
@Schema(description = "data model of login detail")
public class EmployeeLoginDTO implements Serializable {

    @Schema(description = "username")
    private String username;

    @Schema(description = "password")
    private String password;

}
