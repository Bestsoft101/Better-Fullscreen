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
			Class<?> cls = MacWindowUtil.class;
			
			List<Method> allMethods = new ArrayList<>();
			
			Method[] declaredMethods = cls.getDeclaredMethods();
			System.out.println("Declared Methods: " + declaredMethods.length);
			for(int i=0; i < declaredMethods.length; i++) {
				allMethods.add(declaredMethods[i]);
			}
			
//			Method[] methods = cls.getMethods();
//			System.out.println("Methods: " + methods.length);
//			for(int i=0; i < methods.length; i++) {
//				Method meth = methods[i];
//				allMethods.add(meth);
//				System.out.println("    " + toString(meth));
//			}

			allMethods.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));
			
			for(int i=0; i < allMethods.size(); i++) {
				System.out.println("    " + toString(allMethods.get(i)));
			}
			
			List<Method> possibleMethods = new ArrayList<>();
			
			for(int i=0; i < allMethods.size(); i++) {
				Method meth = allMethods.get(i);
				int mod = meth.getModifiers();
				
				if(Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
					Parameter[] params = meth.getParameters();
					if(params != null && params.length == 1 && params[0].getType() == long.class) {
						possibleMethods.add(meth);
					}	
				}
			}
			
			System.out.println("Possible Methods: " + possibleMethods.size());
			
			toggleFullscreenMethod = possibleMethods.get(0);
			fixStyleMaskMethod = possibleMethods.get(1);
			
			System.out.println("Toggle Fullscreen: " + toString(toggleFullscreenMethod));
			System.out.println("Fix Style Mask: " + toString(fixStyleMaskMethod));
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
	
	public static String toString(Method method) {
		try {
			StringBuilder str = new StringBuilder();
			
			int mod = method.getModifiers();
			if(Modifier.isPublic(mod)) str.append("public ");
			if(Modifier.isStatic(mod)) str.append("static ");
			if(Modifier.isPrivate(mod)) str.append("private ");
			if(Modifier.isProtected(mod)) str.append("protected ");
			
			
			str.append(method.getReturnType().getName());
			str.append(' ');
			str.append(method.getName());
			str.append('(');
			
			Parameter[] params = method.getParameters();
			for(int i=0; i < params.length; i++) {
				if(i > 0) {
					str.append(", ");
				}
				str.append(params[i].getType().getName());
				str.append(' ');
				str.append(params[i].getName());
			}
			
			str.append(')');
			return str.toString();
		}catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	public static void main(String[] args) {
		
	}
	
}
