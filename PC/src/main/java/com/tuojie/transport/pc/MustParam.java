package com.tuojie.transport.pc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/10/24 15:11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface MustParam {
}
