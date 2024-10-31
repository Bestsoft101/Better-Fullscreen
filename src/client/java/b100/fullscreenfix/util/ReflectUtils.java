package b100.fullscreenfix.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ReflectUtils {
	
	public static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		
		while(true) {
			Util.addArrayContentToList(fields, clazz.getDeclaredFields());
			
			clazz = clazz.getSuperclass();
			if(clazz == null) {
				break;
			}
		}
		
		return fields;
	}
	
	public static Field getField(Class<?> clazz, Function<Field, Boolean> condition) {
		for(Field field : getAllFields(clazz)) {
			if(condition.apply(field)) {
				return field;
			}
		}
		return null;
	}
	
	public static Object getValue(Field field, Object instance) {
		try {
			field.setAccessible(true);
			return field.get(instance);
		}catch (Exception e) {
			throw new RuntimeException("Getting value of field " + field, e);
		}
	}

}
