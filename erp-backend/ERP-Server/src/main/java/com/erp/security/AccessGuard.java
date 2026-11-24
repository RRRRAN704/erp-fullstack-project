package com.erp.security;

import com.erp.constant.MessageConstant;
import com.erp.context.BaseContext;
import com.erp.entity.Client;
import com.erp.entity.Contract;
import com.erp.entity.Employee;
import com.erp.exception.NoAccessException;
import com.erp.mapper.ClientMapper;
import com.erp.mapper.ContractMapper;
import com.erp.mapper.EmployeeMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Record-level access checks (against IDOR: changing someone else's record by sending its id).
 * Rule: you may only change what you can see in your list.
 * - Consultant: sees and changes only their own records, and may only assign records to themselves.
 * - Manager: sees and changes records of their own department, and may assign them to anyone in it.
 * The department of a record is always taken from its employee, never from the request.
 */
@Component
public class AccessGuard {

    private static final String MANAGER = "Manager";

    private final EmployeeMapper employeeMapper;
    private final ClientMapper clientMapper;
    private final ContractMapper contractMapper;

    public AccessGuard(EmployeeMapper employeeMapper, ClientMapper clientMapper, ContractMapper contractMapper) {
        this.employeeMapper = employeeMapper;
        this.clientMapper = clientMapper;
        this.contractMapper = contractMapper;
    }

    /**
     * Checks that the current user may assign a record to this employee and returns that employee
     * (the caller copies its departmentId onto the record). No employee given = the current user.
     */
    public Employee resolveAssignee(Long employeeId) {
        Long me = BaseContext.getCurrentId();
        Long target = employeeId != null ? employeeId : me;

        if (!isManager() && !target.equals(me)) {
            throw new NoAccessException(MessageConstant.NO_PERMISSION_ASSIGN);
        }
        Employee employee = employeeMapper.getById(target);
        if (employee == null
                || (isManager() && !Objects.equals(employee.getDepartmentId(), BaseContext.getCurrentDepartment()))) {
            throw new NoAccessException(MessageConstant.NO_PERMISSION_ASSIGN);
        }
        return employee;
    }

    /** Returns the client if the current user can see it, otherwise throws. */
    public Client checkClient(Long clientId) {
        Client client = clientId == null ? null : clientMapper.getById(clientId);
        if (client == null || !canSee(client.getEmployeeId(), client.getDepartmentId())) {
            throw new NoAccessException(MessageConstant.NO_PERMISSION_RECORD);
        }
        return client;
    }

    /** Returns the contract if the current user can see it, otherwise throws. */
    public Contract checkContract(Long contractId) {
        Contract contract = contractId == null ? null : contractMapper.getById(contractId);
        if (contract == null || !canSee(contract.getEmployeeId(), contract.getDepartmentId())) {
            throw new NoAccessException(MessageConstant.NO_PERMISSION_RECORD);
        }
        return contract;
    }

    /** Same visibility as the list pages: manager = own department, consultant = own records. */
    private boolean canSee(Long ownerId, Long departmentId) {
        if (isManager()) {
            return Objects.equals(departmentId, BaseContext.getCurrentDepartment());
        }
        return Objects.equals(ownerId, BaseContext.getCurrentId());
    }

    private boolean isManager() {
        return MANAGER.equals(BaseContext.getCurrentRole());
    }
}
