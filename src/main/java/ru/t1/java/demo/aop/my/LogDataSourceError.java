package ru.t1.java.demo.aop.my;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // для кого
@Retention(RetentionPolicy.RUNTIME)
@Documented // должна отображаться в JavaDoc
public @interface LogDataSourceError { }
