import { defineStore } from "pinia";
import { ref } from "vue";
import { loginAPI } from "@/apis/user";
import { sendCodeAPI } from "@/apis/user";
import { resetPasswordAPI } from "@/apis/user";

export const useUserStore = defineStore("user", () => {
    const userInfo = ref({});
    //ger user login information
    const getUserInfo = async ({ username, password }) => {
        const res = await loginAPI({ username, password })
        userInfo.value = res.data
        return res
    }


    //send verification code to email
    const sendCode = async ({ email }) => {
        const res = await sendCodeAPI({ email })
        return res
    }
    
    //reset password
    const resetPassword = async ({ email, verifyCode, newPassword, confirmPassword }) => {
        const res = await resetPasswordAPI({ email, verifyCode, newPassword, confirmPassword })
        return res
    }
    
    //logout and clear user information
    const clearUserInfo = () => {
        userInfo.value = {}
    }

    return {
        userInfo,
        getUserInfo,
        clearUserInfo,
        sendCode,
        resetPassword
    }

}, // logout and clear user information
    {
        persist: true,
    },
);
