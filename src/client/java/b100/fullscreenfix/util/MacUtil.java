package b100.fullscreenfix.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.util.MacWindowUtil;

public class MacUtil {
	
	// Fabric doesn't want to build the mod if any method in MacWindowUtil is
	// referenced directly, so we need to do some reflection nonsense.
	
	private static Method toggleFullscreenMethod;
	private static Method fixStyleMaskMethod;
	
	static {
		try {
			Method[] methods = MacWindowUtil.class.getDeclaredMethods();
			List<Method> possibleMethods = new ArrayList<>();
			
			for(int i=0; i < methods.length; i++) {
				Method meth = methods[i];
				int mod = meth.getModifiers();
				
				if(Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
					Parameter[] params = meth.getParameters();
					if(params != null && params.length == 1) {
						Class<?> paramClass = params[0].getClass();
						Class<?> longClass1 = long.class;
						Class<?> longClass2 = Long.class;
						if(paramClass == longClass1 || paramClass == longClass2) {
							possibleMethods.add(meth);
						}
					}	
				}
			}
			
			toggleFullscreenMethod = possibleMethods.get(0);
			fixStyleMaskMethod = possibleMethods.get(1);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void toggleFullscreen(long handle) {
		try {
			toggleFullscreenMethod.invoke(null, handle);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void fixStyleMask(long handle) {
		try {
			fixStyleMaskMethod.invoke(null, handle);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
