package b100.fullscreenfix;

public enum FullscreenMode {
	OFF,
	ON,
	BORDERLESS;
	
	public FullscreenMode next() {
		return next(this);
	}
	
	public int id() {
		return getId(this);
	}
	
	public static FullscreenMode next(FullscreenMode mode) {
		return get((getId(mode) + 1) % 3);
	}
	
	public static int getId(FullscreenMode mode) {
		if(mode == BORDERLESS) return 2;
		if(mode == ON) return 1;
		return 0;
	}
	
	public static FullscreenMode get(int id) {
		return values()[id];
	}
}