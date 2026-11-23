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
public class EmployeeMappingVO implements Serializable {

    //employee ID
    private Long id;

    //Full name
    private String name;

    private Long positionId;

    private Long departmentId;

}

