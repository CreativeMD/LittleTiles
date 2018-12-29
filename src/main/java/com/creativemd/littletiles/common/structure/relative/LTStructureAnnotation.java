package com.creativemd.littletiles.common.structure.relative;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;

@Retention(RUNTIME)
@Target(FIELD)
public @interface LTStructureAnnotation {
	
	String saveKey() default "";
	
	int color() default ColorUtils.WHITE;
	
}
