package com.erp.aspect;

import com.erp.annotation.RequireRole;
import com.erp.constant.MessageConstant;
import com.erp.context.BaseContext;
import com.erp.enumeration.Role;
import com.erp.exception.CannotBeAdminException;
import com.erp.exception.NoAccessException;
import com.erp.exception.NotAdminException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

/**
 * Enforces @RequireRole on controllers, replacing the role checks that used to be copied into every method.
 * The role (position name) was put into BaseContext by the JWT interceptor.
 */
@Aspect
@Component
public class RoleCheckAspect {

    // any method in com.erp.controller whose class or method carries @RequireRole
    @Before("execution(* com.erp.controller..*.*(..)) && "
            + "(@within(com.erp.annotation.RequireRole) || @annotation(com.erp.annotation.RequireRole))")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // method annotation first, then the class one
        RequireRole required = AnnotatedElementUtils.findMergedAnnotation(signature.getMethod(), RequireRole.class);
        if (required == null) {
            required = AnnotatedElementUtils.findMergedAnnotation(joinPoint.getTarget().getClass(), RequireRole.class);
        }
        if (required == null) {
            return;
        }

        String position = BaseContext.getCurrentRole();
        boolean isAdmin = Role.ADMIN_POSITION.equals(position);

        switch (required.value()) {
            case ADMIN -> {
                if (!isAdmin) throw new NotAdminException(MessageConstant.NOT_ADMIN);
            }
            case STAFF -> {
                if (position == null || isAdmin) throw new CannotBeAdminException(MessageConstant.ADMIN_NO_ACCESS);
            }
            case MANAGER -> {
                if (!Role.MANAGER_POSITION.equals(position)) throw new NoAccessException(MessageConstant.MANAGER_ONLY);
            }
        }
    }
}
