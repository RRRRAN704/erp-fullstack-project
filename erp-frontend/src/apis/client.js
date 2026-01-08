import request from '@/utils/http'

export const clientPageQueryAPI = (params)=>{
    return request({
       url: 'employee/client/page',
       method: 'GET',
       params
     })
   }

export const archivedClientPageQueryAPI = (params)=>{
    return request({
       url: 'employee/client//page/archived',
       method: 'GET',
       params
     })
   }

 
export const addClientAPI = (params)=>{
    return request({
       url: 'employee/client',
       method: 'POST',
       data: params
     })
   }


  export const editClientAPI = (params)=>{
  return request({
       url: 'employee/client',
       method: 'PUT',
       data:params
     })
   }   

   export const archiveClientAPI = (clientId) => {
    return request({
      url: `employee/client/archive`,
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      data: [clientId] 
    })
  }


  export const restoreClientAPI = (clientId) => {
    return request({
      url: `employee/client/restore`, 
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      data: [clientId] 
    })
  }

  export const getAllEmployeeAPI = ()=>{
    return request({
       url: 'employee/common/employee',
       method: 'GET',
     })
   }

   export const getAllPositionAPI = ()=>{
    return request({
       url: 'employee/common/position',
       method: 'GET',
     })
   }

   export const getAllDepartmentAPI = ()=>{
    return request({
       url: 'employee/common/department',
       method: 'GET',
     })
}