package com.erp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login result object that will be sent to the front end ")
public class EmployeeLoginVO implements Serializable {

    @Schema(description = "Employee Id")
    private Long id;

    @Schema(description = "username")
    private String username;

    @Schema(description = "fullname")
    private String name;

    @Schema(description = "jwt token")
    private String token;

    @Schema(description = "employee role")
    private String position;

    @Schema(description = "department Id")
    private Long departmentId;

}

