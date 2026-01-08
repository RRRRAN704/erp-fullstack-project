import request from '@/utils/http'

export const positionPageQueryAPI = (params)=>{
    return request({
       url: 'employee/position/page',
       method: 'GET',
       params
     })
   }



export const addPositionAPI = ({positionName,departmentId})=>{
    return request({
       url: 'employee/position',
       method: 'POST',
       data: {
              positionName,
              departmentId
       }
     })
}



export const deletePositionAPI = (ids) => {
    return request({
      url: 'employee/position',
      method: 'DELETE',
      params: {
        ids: ids
      },
      paramsSerializer: {
        indexes: null // serialize arrays correctly
      }
    })
  }



  export const editPositionAPI = (params)=>{
    return request({
         url: 'employee/position',
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