package mcjty.theoneprobe.api;

public record Color(int value) {
	static final int ALPHA = 0xFF << 24;

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        this((a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255));
    }
    
    public Color darker() {
    	return darker(0.7D);
    }
    
    public Color darker(double factor) {
        return new Color(darker(value, factor));
    }
    
    public Color brighter() {
    	return brighter(0.7D);
    }
    
    public Color brighter(double factor) {
    	return new Color(brighter(value, factor));
    }
        
    public Color mix(Color other, double factor) {
    	return new Color(mix(value, other.getRGB(), factor));
    }
    
    public int getRed() {
        return this.getRGB() >> 16 & 255;
    }

    public int getGreen() {
        return this.getRGB() >> 8 & 255;
    }

    public int getBlue() {
        return this.getRGB() & 255;
    }

    public int getAlpha() {
        return this.getRGB() >> 24 & 255;
    }

    public int getRGB() {
        return this.value;
    }
    
    public static int rgb(int r, int g, int b) {
    	return rgb(r, g, b, 255);
    }
    
    public static int rgb(int r, int g, int b, int a) {
    	return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }
    
    /// type specific mix function so people don't have to create a Object if they want to mix something.
	public static int mix(int from, int to, double factor) {
	    double weight0 = (1D - factor);
        int r = (int)((((from >> 16) & 0xFF) * weight0) + (((to >> 16) & 0xFF) * factor));
	    int g = (int)((((from >> 8) & 0xFF) * weight0) + (((to >> 8) & 0xFF) * factor));
	    int b = (int)(((from & 0xFF) * weight0) + ((to & 0xFF) * factor));
	    int a = (int)((((from >> 24) & 0xFF) * weight0) + (((to >> 24) & 0xFF) * factor));
	    return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | b & 0xFF;
	}
    
    /// type specific darker function so people don't have to create a Object if they want to darken something.
	public static int darker(int color, double factor) {
		int r = Math.max(0, (int)(((color >> 16) & 0xFF) * factor));
		int g = Math.max(0, (int)(((color >> 8) & 0xFF) * factor));
		int b = Math.max(0, (int)((color & 0xFF) * factor));
		return (color & ALPHA) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
	}
    
    /// type specific brighter function so people don't have to create a Object if they want to brighten something.
	public static int brighter(int color, double factor) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int i = (int)(1.0 / (1.0 - factor));
		if(r == 0 && g == 0 && b == 0) {
			return (color & ALPHA) | ((i & 0xFF) << 16) | ((i & 0xFF) << 8) | (i & 0xFF);
		}
		if (r > 0 && r < i) {
            r = i;
        }
		if (g > 0 && g < i) {
            g = i;
        }
		if (b > 0 && b < i) {
            b = i;
        }
		return (color & ALPHA) | Math.min(255, (int)(r / factor)) << 16 | Math.min(255, (int)(g / factor)) << 8 | Math.min(255, (int)(b / factor));
	}
}
