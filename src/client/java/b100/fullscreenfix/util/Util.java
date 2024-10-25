package b100.fullscreenfix.util;

import java.util.List;
import java.util.function.Function;

public class Util {
	
	public static <E> E findFirstElement(List<E> list, boolean backwards, Function<E, Boolean> condition) {
		int start, dir;
		if(backwards) {
			start = list.size() - 1;
			dir = -1;
		}else {
			start = 0;
			dir = 1;
		}
		for(int i = start; i >= 0 && i < list.size(); i += dir) {
			E element = list.get(i);
			if(condition.apply(element)) {
				return element;
			}
		}
		return null;
	}

}
