import request from '@/utils/http'

export const departmentPageQueryAPI = (params)=>{
    return request({
       url: 'employee/department/page',
       method: 'GET',
       params
     })
   }
 
export const addDepartmentAPI = ({departmentName})=>{
    return request({
       url: 'employee/department',
       method: 'POST',
       data: {
           departmentName
       }
     })
   }

   export const deleteDepartmentAPI = (ids) => {
    return request({
      url: 'employee/department',
      method: 'DELETE',
      params: {
        ids: ids
      },
      paramsSerializer: {
        indexes: null //serialise array
      }
    })
  }


  export const editDepartmentAPI = (params)=>{
  return request({
       url: 'employee/department',
       method: 'PUT',
       data:params
     })
   }   