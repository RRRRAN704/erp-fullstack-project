//axios basic encapsulation
import axios from "axios";
import { ElMessage } from 'element-plus'
import 'element-plus/theme-chalk/el-message.css'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const httpInstance = axios.create( 

    {
    // baseURL: '/api',
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 50000
    }

)

// axios request interceptor
httpInstance.interceptors.request.use(config => {

   // 1. get token from pinia
   const userStore = useUserStore()
   // 2. add token to request header
   const token = userStore.userInfo?.token
   if (token) {
    config.headers.token = token
   }
    return config
  }, e => Promise.reject(e))
  
  // axios respnse interceptor
  httpInstance.interceptors.response.use(res => res.data, e => {
    const userStore = useUserStore()
    //error maessage
    ElMessage({
      type: 'warning',
      //message: e.response.data.message
      message: e.response?.data?.message || 'API request failed, please try again later.',
    })

    //401 token expired
    if (e.response?.status === 401) {
      // clear user info
      userStore.clearUserInfo()
      // push to login page
      router.push('/login') 
    }
    return Promise.reject(e)
  })


export default httpInstance
