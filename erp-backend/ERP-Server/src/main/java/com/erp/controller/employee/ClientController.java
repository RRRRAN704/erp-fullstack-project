package com.erp.controller.employee;

import com.erp.constant.MessageConstant;
import com.erp.context.BaseContext;
import com.erp.dto.ClientDTO;
import com.erp.dto.ClientPageQueryDTO;
import com.erp.dto.OrderDTO;
import com.erp.dto.OrderPageQueryDTO;
import com.erp.exception.CannotBeAdminException;
import com.erp.exception.NoAccessException;
import com.erp.result.PageResult;
import com.erp.result.Result;
import com.erp.service.ClientService;
import com.erp.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employee/client")
@Tag(name = "Client related controller")
@Slf4j
public class ClientController {

    @Autowired
    ClientService clientService;


    /**
     * add client
     * @param clientDTO
     * @return
     */

    @PostMapping
    @Operation(summary = "Add Client")
    public Result addClient(@RequestBody ClientDTO clientDTO) {
        if (BaseContext.getCurrentRole().equals("System Admin")) {
            throw new CannotBeAdminException(MessageConstant.ADMIN_NO_ACCESS);
        }

        log.info("Add Client: {}", clientDTO );
        clientService.addClient(clientDTO);
        return Result.success();

    }



    /**
     * Edit clients
     * @param clientDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "Edit Client")
    public Result editClient(@RequestBody ClientDTO clientDTO) {
        if (BaseContext.getCurrentRole().equals("System Admin")) {
            throw new CannotBeAdminException(MessageConstant.ADMIN_NO_ACCESS);
        }
        log.info("Edit client: {}", clientDTO);
        clientService.update(clientDTO);
        return Result.success();

    }


    /**
     * Archive clients in batch
     * @param ids
     * @return
     */

    @PutMapping("/archive")
    @Operation(summary = "Archive client in batch")
    public Result archiveBatch(@RequestBody List<Long> ids){
        if (BaseContext.getCurrentRole().equals("System Admin")) {
            throw new CannotBeAdminException(MessageConstant.ADMIN_NO_ACCESS);
        }
        clientService.archiveBatch(ids);
        return Result.success();
    }


    /**
     * Restore clients in batch
     * @param ids
     * @return
     */
    @PutMapping("/restore")
    @Operation(summary = "Restore clients in batch")
    public Result restoreBatch(@RequestBody List<Long> ids){
        if (BaseContext.getCurrentRole().equals("System Admin")) {
            throw new CannotBeAdminException(MessageConstant.ADMIN_NO_ACCESS);
        }
        clientService.restoreBatch(ids);
        return Result.success();
    }


    /**
     * client page query
     * @param clientPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "Client page query")
    public Result<PageResult> pageClient(ClientPageQueryDTO clientPageQueryDTO){
        if (BaseContext.getCurrentRole().equals("System Admin")) {
            throw new CannotBeAdminException(MessageConstant.ADMIN_NO_ACCESS);
        }
        log.info("Client page query {}", clientPageQueryDTO);
        PageResult pageResult = clientService.clientPageQuery(clientPageQueryDTO);
        return Result.success(pageResult);
    }



    /**
     * archived client page query
     * @param clientPageQueryDTO
     * @return
     */
    @GetMapping("/page/archived")
    @Operation(summary = "Archived Client page query")
    public Result<PageResult> pageClientArchived (ClientPageQueryDTO clientPageQueryDTO){
        if (BaseContext.getCurrentRole().equals("System Admin")) {
            throw new CannotBeAdminException(MessageConstant.ADMIN_NO_ACCESS);
        }
        log.info("Archived Client page query {}", clientPageQueryDTO);
        PageResult pageResult = clientService.clientArchivedPageQuery(clientPageQueryDTO);
        return Result.success(pageResult);
    }
}
