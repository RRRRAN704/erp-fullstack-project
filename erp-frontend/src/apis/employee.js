import request from '@/utils/http'

export const employeePageQueryAPI = (params)=>{
    return request({
       url: 'employee/info/page',
       method: 'GET',
       params
     })
   }



export const addEmployeeAPI = (params)=>{
    return request({
       url: 'employee/info',
       method: 'POST',
       data: params
     })
}



export const enableOrDisableAPI = (status, id) => {
    return request({
      url: `employee/info/status/${status}`,
      method: 'POST',  
      params: {
        id: id
      }
    });
  };



export const editEmployeeAPI = (params)=>{
    return request({
         url: 'employee/info',
         method: 'PUT',
         data:params
       })
     }  

export const getAllDepartmentAPI = ()=>{
        return request({
           url: 'employee/common/department',
           method: 'GET',
         })
}

export const getAllPositionAPI = ()=>{
    return request({
       url: 'employee/common/position',
       method: 'GET',
     })
   }

