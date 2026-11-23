package com.erp.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * department info
 */
@Data

public class DepartmentDTO implements Serializable {


    private Long id;
    private String departmentName;
}
