package com.erp.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;


/**
 * reset password info
 */
@Data
public class ResetPasswordDTO implements Serializable {

    private String email;
    private String verifyCode;
    private String newPassword;
    private String confirmPassword;
}
