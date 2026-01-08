import request from '@/utils/http'
export const addOrderAPI = (params)=>{
    return request({
       url: 'employee/order',
       method: 'POST',
       data: params
     })
   }