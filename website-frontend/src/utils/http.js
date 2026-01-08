//axios basic encapsulation
import axios from "axios";
import { ElMessage } from 'element-plus'
import 'element-plus/theme-chalk/el-message.css'


const httpInstance = axios.create( 

    {
    // baseURL: '/api',
    baseURL: import.meta.env.VITE_API_URL, 
    timeout: 50000
    }

)

// axios response interceptor
httpInstance.interceptors.response.use(
  res => res.data, 
  e => {
      // error message
      ElMessage({
          type: 'warning',
          message: e.response?.data?.message || 'API request failed, please try again later.',
      })
      
      return Promise.reject(e)
  }
)

export default httpInstance


