package com.erp.controller.employee;

import com.erp.annotation.RequireRole;
import com.erp.enumeration.Role;

import com.erp.dto.DepartmentDTO;
import com.erp.dto.DepartmentPageQueryDTO;
import com.erp.result.PageResult;
import com.erp.result.Result;
import com.erp.service.DepartmentService;
import com.erp.service.EmployeeService;
import com.github.pagehelper.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequireRole(Role.ADMIN)
@RequestMapping("/employee/department")
@Slf4j
@Tag(name = "Department related api")
public class DepartmentController {

    @Autowired
    DepartmentService departmentService;



    /**
     * Add new department
     * @param departmentDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "Add new department")
    public Result saveDepartment(@RequestBody DepartmentDTO departmentDTO) {

        log.info("Add new department{}", departmentDTO);
        departmentService.saveDepartment(departmentDTO);
        return Result.success();

    }


    /**
     * department page query
     * @param departmentPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "department page query")
    public Result<PageResult> pageDepartment(DepartmentPageQueryDTO departmentPageQueryDTO){
        log.info("Department page query {}", departmentPageQueryDTO);
        PageResult pageResult = departmentService.departmentPageQurey(departmentPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * Edit department info
     * @param departmentDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "Edit department info")
    public Result updateDepartment(@RequestBody DepartmentDTO departmentDTO) {
        log.info("Update department {}", departmentDTO);
        departmentService.updateDepartment(departmentDTO);
        return Result.success();

    }


    /**
     * delete department in batch
     * @param ids
     * @return
     */
    @DeleteMapping
    @Operation(summary = "Delete department in batch")
    public Result deleteDepartment(@RequestParam List<Long> ids){
        log.info("Delete department in batch {}", ids);
        departmentService.deleteBatch(ids);
        return Result.success();
    }




}
