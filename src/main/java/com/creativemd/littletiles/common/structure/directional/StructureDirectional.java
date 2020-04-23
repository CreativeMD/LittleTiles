package com.creativemd.littletiles.common.structure.directional;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;

@Retention(RUNTIME)
@Target(FIELD)
public @interface StructureDirectional {
	
	String saveKey() default "";
	
	int color() default ColorUtils.WHITE;
	
}
