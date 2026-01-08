package com.erp.aspect;


import com.erp.annotation.AutoFill;
import com.erp.constant.AutoFIllConstant;
import com.erp.context.BaseContext;
import com.erp.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;


@Aspect // Marks this class as an AOP aspect
@Component
@Slf4j
public class AutoFillAspect {
    /* Pointcut definition: match methods in com.erp.mapper.* that are annotated with @AutoFill */
    @Pointcut("execution(* com.erp.mapper.*.*(..)) && @annotation(com.erp.annotation.AutoFill)")
    public void autoFillPointCut(){
    }

    /*Pre-notification, assign public fields in the notification*/
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("Start executing public field autofill");


        //  Obtain the method signature and read the @AutoFill annotation to know operation type
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //Method signature object
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//Obtain method annotation objects
        OperationType operationType = autoFill.value();// INSERT or UPDATE


        // Fetch the target method’s arguments;
        Object[] args = joinPoint.getArgs();
        if (args == null && args.length == 0){
            return;

        }

        // assume the first arg is the entity to be filled.
        Object entity = args[0];


        //Prepare data for assignment
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // Use reflection to invoke setters on the entity depending on operation type.
        if (operationType == OperationType.INSERT){
            //Assign values to four public field
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFIllConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFIllConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFIllConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFIllConstant.SET_UPDATE_USER, Long.class);

                //Assign values to object properties through reflection
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (operationType == OperationType.UPDATE){
            //Assign values to four public field
            try {

                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFIllConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFIllConstant.SET_UPDATE_USER, Long.class);

                //Assign values to object properties through reflection
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
