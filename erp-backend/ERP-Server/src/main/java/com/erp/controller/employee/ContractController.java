package com.erp.controller.employee;

import com.erp.annotation.RequireRole;
import com.erp.enumeration.Role;
import com.erp.dto.ClientDTO;
import com.erp.dto.ClientPageQueryDTO;
import com.erp.dto.ContractDTO;
import com.erp.dto.ContractPageQueryDTO;
import com.erp.result.PageResult;
import com.erp.result.Result;
import com.erp.service.ContractService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequireRole(Role.STAFF)
@RequestMapping("/employee/contract")
@Tag(name = "Contract related controller")
@Slf4j
public class ContractController {

    @Autowired
    ContractService contractService;


    /**
     * add contract
     * @param contractDTO
     * @return
     */

    @PostMapping
    @Operation(summary = "Add Contract")
    public Result addContract(@RequestBody ContractDTO contractDTO) {

        log.info("Add Contract: {}", contractDTO);
        contractService.addContract(contractDTO);
        return Result.success();

    }



    /**
     * Edit clients
     * @param contractDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "Edit Contract")
    public Result editContract(@RequestBody ContractDTO contractDTO) {
        log.info("Edit contract: {}", contractDTO);
        contractService.update(contractDTO);
        return Result.success();

    }


    /**
     * Archive contracts in batch
     * @param ids
     * @return
     */

    @PutMapping("/archive")
    @Operation(summary = "Archive contracts in batch")
    public Result archiveBatch(@RequestBody List<Long> ids){
        contractService.archiveBatch(ids);
        return Result.success();
    }


    /**
     * Restore contracts in batch
     * @param ids
     * @return
     */
    @PutMapping("/restore")
    @Operation(summary = "Restore contracts in batch")
    public Result restoreBatch(@RequestBody List<Long> ids){
        contractService.restoreBatch(ids);
        return Result.success();
    }


    /**
     * contract page query
     * @param contractPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "Contract page query")
    public Result<PageResult> pageClient(ContractPageQueryDTO contractPageQueryDTO){
        log.info("Contract page query {}", contractPageQueryDTO);
        PageResult pageResult = contractService.contractPageQuery(contractPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * archived contract page query
     * @param contractPageQueryDTO
     * @return
     */
    @GetMapping("/page/archived")
    @Operation(summary = "Archived Contract page query")
    public Result<PageResult> pageArchivedClient(ContractPageQueryDTO contractPageQueryDTO){
        log.info("Contract page query {}", contractPageQueryDTO);
        PageResult pageResult = contractService.contractArchivedPageQuery(contractPageQueryDTO);
        return Result.success(pageResult);
    }
}
