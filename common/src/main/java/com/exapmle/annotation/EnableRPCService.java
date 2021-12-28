package com.exapmle.annotation;

import com.exapmle.core.RPCAutoWiredRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RPCAutoWiredRegistrar.class)
public @interface EnableRPCService {

}
