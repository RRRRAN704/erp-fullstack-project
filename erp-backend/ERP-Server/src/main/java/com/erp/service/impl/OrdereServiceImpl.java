package com.erp.service.impl;

import com.erp.context.BaseContext;
import com.erp.dto.AddOrderDTO;
import com.erp.dto.ChangeOrderStatusDTO;
import com.erp.dto.OrderDTO;
import com.erp.dto.OrderPageQueryDTO;
import com.erp.entity.Department;
import com.erp.entity.Order;
import com.erp.mapper.OrderMapper;
import com.erp.result.PageResult;
import com.erp.service.OrderService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.erp.constant.MessageConstant;
import com.erp.exception.NoAccessException;
import com.erp.security.AccessGuard;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class OrdereServiceImpl implements OrderService {


    @Autowired
    OrderMapper orderMapper;

    @Autowired
    AccessGuard accessGuard;

//    /**
//     *archive order/unarchive
//     * @param orderDTO
//     */
//    @Override
//    public void update(OrderDTO orderDTO) {
//        Order order = new Order();
//        BeanUtils.copyProperties(orderDTO, order);
//        orderMapper.update(order);
//    }


    /**
     * archive orders in batch
     * @param ids
     */
    @Override
    @Transactional
    public void archiveBatch(List<Long> ids) {
        // check every id first, so a batch with one foreign id changes nothing
        ids.forEach(accessGuard::checkOrder);
        for (Long id : ids) {
            Order order = new Order();
            order.setId(id);
            order.setIsArchived(1);
            orderMapper.update(order);
        }
    }


    /**
     * restore orders in batch
     * @param ids
     */
    @Override
    @Transactional
    public void restoreBatch(List<Long> ids) {
        // check every id first, so a batch with one foreign id changes nothing
        ids.forEach(accessGuard::checkOrder);
        for (Long id : ids) {
            Order order = new Order();
            order.setId(id);
            order.setIsArchived(0);
            orderMapper.update(order);
        }
    }


    /**
     * order page query
     * @param orderPageQueryDTO
     * @return
     */
    @Override
    public PageResult orderPageQurey(OrderPageQueryDTO orderPageQueryDTO) {

        PageHelper.startPage(orderPageQueryDTO.getPage(),orderPageQueryDTO.getPageSize());

        if(BaseContext.getCurrentRole().equals("Manager")){
            Page<Order> order = orderMapper.managerPageQuery(orderPageQueryDTO);
            long total = order.getTotal();
            List<Order> records = order.getResult();
            return new PageResult(total,records);
        }else{
            Long employeeId = BaseContext.getCurrentId();
            Page<Order> order = orderMapper.consultantPageQuery(orderPageQueryDTO, employeeId);
            long total = order.getTotal();
            List<Order> records = order.getResult();
            return new PageResult(total,records);
        }

    }


    /**
     * archived order page query
     * @param orderPageQueryDTO
     * @return
     */
    @Override
    public PageResult orderArchivedPageQurey(OrderPageQueryDTO orderPageQueryDTO) {
        PageHelper.startPage(orderPageQueryDTO.getPage(),orderPageQueryDTO.getPageSize());

        if(BaseContext.getCurrentRole().equals("Manager")){
            Page<Order> order = orderMapper.managerArchivedPageQuery(orderPageQueryDTO);
            long total = order.getTotal();
            List<Order> records = order.getResult();
            return new PageResult(total,records);
        }else{
            Long employeeId = BaseContext.getCurrentId();
            Page<Order> order = orderMapper.consultantArchivedPageQuery(orderPageQueryDTO, employeeId);
            long total = order.getTotal();
            List<Order> records = order.getResult();
            return new PageResult(total,records);
        }
    }


    /**
     * generate order from client's end
     * @param addOrderDTO
     */
    @Override
    public void addOrder(AddOrderDTO addOrderDTO) {
        Order order = new Order();
        BeanUtils.copyProperties(addOrderDTO, order);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

    }


    /**
     * assign order
     * @param changeOrderStatusDTO
     */
    @Override
    public void assignOrder(ChangeOrderStatusDTO changeOrderStatusDTO) {
        accessGuard.checkOrder(changeOrderStatusDTO.getId());
        if (changeOrderStatusDTO.getEmployeeId() == null) {
            throw new NoAccessException(MessageConstant.NO_PERMISSION_ASSIGN);
        }
        accessGuard.resolveAssignee(changeOrderStatusDTO.getEmployeeId());

        Order order = new Order();
        BeanUtils.copyProperties(changeOrderStatusDTO, order);
        order.setAssignTime(LocalDateTime.now());
        orderMapper.assignOrder(order);
    }
}
