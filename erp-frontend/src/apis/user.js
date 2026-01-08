import request from '@/utils/http'

export const loginAPI = ({username,password})=>{
    return request({
       url: 'employee/info/login',
       method: 'POST',
       data:{
           username,
           password
       }
     })
   }


   
export const sendCodeAPI = ({email})=>{
    return request({
       url: 'employee/info/send-email-code',
       method: 'POST',
       data:{
            email
        }
     })
   } 


export const resetPasswordAPI = ({email,verifyCode,newPassword,confirmPassword})=>{
  return request({
      url: 'employee/info/reset-password',
      method: 'POST',
      data:{
          email,
          verifyCode,
          newPassword,
          confirmPassword
      }
    })
} 