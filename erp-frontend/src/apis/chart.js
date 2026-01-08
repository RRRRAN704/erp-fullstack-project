import request from '@/utils/http'

export const getContractNumberStatistics = (params) => {
  return request({
    url: 'employee/report/contractNumber',  
    method: 'GET',                          
    params                                  
  })
}

export const getContractAmountStatistics = (params)=>{
    return request({
       url: 'employee/report/contractAmount',
       method: 'GET',
       params
     })
   }

 
export const getOrderNumberStatistics = (params)=>{
    return request({
       url: 'employee/report/orderNumber',
       method: 'GET',
       params
     })
}

 

export const getTopEmployeeStatistics = (params)=>{
  return request({
     url: 'employee/report/topEmployee',
     method: 'GET',
     params
   })
}   

