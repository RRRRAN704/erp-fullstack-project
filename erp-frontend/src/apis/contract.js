import request from '@/utils/http'

export const contractPageQueryAPI = (params) => {
  return request({
    url: 'employee/contract/page',
    method: 'GET',
    params
  })
}


export const archivedContractPageQueryAPI = (params) => {
  return request({
    url: 'employee/contract/page/archived',
    method: 'GET',
    params
  })
}


export const addContractAPI = (params) => {
  return request({
    url: 'employee/contract',
    method: 'POST',
    data: params
  })
}



export const archiveContractAPI = (contractId) => {
  return request({
    url: `employee/contract/archive`,
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    data: [contractId]
  });
};




export const restoreContractAPI = (contractId) => {
  return request({
    url: `employee/contract/restore`,
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    data: [contractId]
  });
};


export const editContractAPI = (params) => {
  return request({
    url: 'employee/contract',
    method: 'PUT',
    data: params
  })
}



export const getAllEmployeeAPI = () => {
  return request({
    url: 'employee/common/employee',
    method: 'GET',
  })
}

export const getAllPositionAPI = () => {
  return request({
    url: 'employee/common/position',
    method: 'GET',
  })
}

export const getAllDepartmentAPI = () => {
  return request({
    url: 'employee/common/department',
    method: 'GET',
  })

}

export const getAllClientAPI = () => {
  return request({
    url: 'employee/common/client',
    method: 'GET',
  })

}



export const uploadAPI = (file) => {
  const formData = new FormData();
  formData.append('file', file);

  return request({
    url: 'employee/common/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

