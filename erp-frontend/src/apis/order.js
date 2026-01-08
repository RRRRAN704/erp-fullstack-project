import request from '@/utils/http'

export const assignOrderAPI = (params)=>{
    return request({
       url: 'employee/order/assign',
       method: 'PUT',
       data:params
     })
   }

   export const OrderPageQueryAPI = (params)=>{
    return request({
       url: 'employee/order/page',
       method: 'GET',
       params
     })
   }


export const archivedOrderPageQueryAPI = (params)=>{
    return request({
       url: 'employee/order/page/archived',
       method: 'GET',
       params
     })
   }

 

   export const archiveOrderAPI = (orderId) => {
    return request({
      url: `employee/order/archive`,
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      data: [orderId] 
    })
  }


  export const restoreOrderAPI = (orderId) => {
    return request({
      url: `employee/order/restore`, 
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      data: [orderId] 
    })
  }


  export const getAllEmployeeAPI = ()=>{
    return request({
       url: 'employee/common/employee',
       method: 'GET',
     })
   }