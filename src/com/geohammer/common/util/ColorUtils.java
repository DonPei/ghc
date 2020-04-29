package com.geohammer.common.util;

import java.awt.Color;
import java.util.Random;

import edu.mines.jtk.awt.ColorMap;

//http://stackoverflow.com/questions/470690/how-to-automatically-generate-n-distinct-colors
/**
 * Contains a method to generate N visually distinct colors and helper methods.
 * @author Melinda Green
 */
public class ColorUtils {
    private ColorUtils() {} // To disallow instantiation.
    private final static float
        U_OFF = .436f,
        V_OFF = .615f;
    private static final long RAND_SEED = 0;
    private static Random rand = new Random(RAND_SEED);

	//http://stackoverflow.com/questions/470690/how-to-automatically-generate-n-distinct-colors
	public static final Color[] KELLY_COLORS = {
	    new Color(255, 179, 0),    	// Vivid Yellow
	    new Color(128, 62, 117),   	// Strong Purple
	    new Color(255, 104, 0),    	// Vivid Orange
	    new Color(166, 189, 215),   // Very Light Blue
	    new Color(193, 0, 32),    	// Vivid Red
	    new Color(206, 162, 98),    // Grayish Yellow
	    new Color(129, 112, 102),   // Medium Gray

	    //these aren't good for people with defective color vision:
	    new Color(0, 125, 52),    	// Vivid Green
	    new Color(246, 118, 142),   // Strong Purplish Pink
	    new Color(0, 83, 138),    	// Strong Blue
	    new Color(255, 122, 92),    // Strong Yellowish Pink
	    new Color(83, 55, 122),    	// Strong Violet
	    new Color(255, 142, 0),    	// Vivid Orange Yellow
	    new Color(179, 40, 81),    	// Strong Purplish Red
	    new Color(244, 200, 0),    	// Vivid Greenish Yellow
	    new Color(127, 24, 13),    	// Strong Reddish Brown
	    new Color(147, 170, 0),    	// Vivid Yellowish Green
	    new Color(89, 51, 21),    	// Deep Yellowish Brown
	    new Color(241, 58, 19),    	// Vivid Reddish Orange
	    new Color(35, 44, 22),    	// Dark Olive Green
	};
	
	public static final double [] Red = new double[]{1.0, 0.0, 0.0};
	public static final double [] Green = new double[]{0.0, 1.0, 0.0};
	public static final double [] Blue = new double[]{0.0, 0.0, 1.0};
	public static final double [] Yellow = new double[]{1.0, 1.0, 0.0};
	public static final double [] Cyan = new double[]{0.0, 1.0, 1.0};
	public static final double [] Magenta = new double[]{1.0, 0.0, 1.0};
	public static final double [] White = new double[]{1.0, 1.0, 1.0};
	public static final double [] Black = new double[]{0.0, 0.0, 0.0};
	
	// These grays are useful for fine-tuning lighting color values
	// and for other areas where subtle variations of grays are needed.
	// PERCENTAGE GRAYS:
	public static final double [] Gray05 = new double[]{0.05, 0.05, 0.05};
	public static final double [] Gray10 = new double[]{0.10, 0.10, 0.10};
	public static final double [] Gray15 = new double[]{0.15, 0.15, 0.15};
	public static final double [] Gray20 = new double[]{0.20, 0.20, 0.20};
	public static final double [] Gray25 = new double[]{0.25, 0.25, 0.25};
	public static final double [] Gray30 = new double[]{0.30, 0.30, 0.30};
	public static final double [] Gray35 = new double[]{0.35, 0.35, 0.35};
	public static final double [] Gray40 = new double[]{0.40, 0.40, 0.40};
	public static final double [] Gray45 = new double[]{0.45, 0.45, 0.45};
	public static final double [] Gray50 = new double[]{0.50, 0.50, 0.50};
	public static final double [] Gray55 = new double[]{0.55, 0.55, 0.55};
	public static final double [] Gray60 = new double[]{0.60, 0.60, 0.60};
	public static final double [] Gray65 = new double[]{0.65, 0.65, 0.65};
	public static final double [] Gray70 = new double[]{0.70, 0.70, 0.70};
	public static final double [] Gray75 = new double[]{0.75, 0.75, 0.75};
	public static final double [] Gray80 = new double[]{0.80, 0.80, 0.80};
	public static final double [] Gray85 = new double[]{0.85, 0.85, 0.85};
	public static final double [] Gray90 = new double[]{0.99, 0.90, 0.90};
	public static final double [] Gray95 = new double[]{0.05, 0.95, 0.95};

	// OTHER GRAYS
	public static final double [] DimGray = new double[]{0.329412, 0.329412, 0.329412};
	public static final double [] DimGrey = new double[]{0.329412, 0.329412, 0.329412};
	public static final double [] Gray = new double[]{0.752941, 0.752941, 0.752941};
	public static final double [] Grey = new double[]{0.752941, 0.752941, 0.752941};
	public static final double [] LightGray = new double[]{0.658824, 0.658824, 0.658824};
	public static final double [] LightGrey = new double[]{0.658824, 0.658824, 0.658824};
	public static final double [] VLightGray = new double[]{0.80, 0.80, 0.80};
	public static final double [] VLightGrey = new double[]{0.80, 0.80, 0.80};
	
	//other color
	public static final double [] Aquamarine	 = new double[]{0.439216, 0.858824, 0.576471}; 
	public static final double [] BlueViolet	 = new double[]{0.62352, 0.372549, 0.623529}; 
	public static final double [] Brown			 = new double[]{0.647059, 0.164706, 0.164706}; 
	public static final double [] CadetBlue		 = new double[]{0.372549, 0.623529, 0.623529}; 
	public static final double [] Coral			 = new double[]{1.0, 0.498039, 0.0}; 
	public static final double [] CornflowerBlue = new double[]{0.258824, 0.258824, 0.435294}; 
	public static final double [] DarkGreen		 = new double[]{0.184314, 0.309804, 0.184314}; 
	public static final double [] DarkOliveGreen = new double[]{0.309804, 0.309804, 0.184314}; 
	public static final double [] DarkOrchid	 = new double[]{0.6, 0.196078, 0.8}; 
	public static final double [] DarkSlateBlue	 = new double[]{0.419608, 0.137255, 0.556863}; 
	public static final double [] DarkSlateGray	 = new double[]{0.184314, 0.309804, 0.309804}; 
	public static final double [] DarkSlateGrey	 = new double[]{0.184314, 0.309804, 0.309804}; 
	public static final double [] DarkTurquoise	 = new double[]{0.439216, 0.576471, 0.858824}; 
	public static final double [] Firebrick		 = new double[]{0.556863, 0.137255, 0.137255}; 
	public static final double [] ForestGreen	 = new double[]{0.137255, 0.556863, 0.137255}; 
	public static final double [] Gold			 = new double[]{0.8, 0.498039, 0.196078}; 
	public static final double [] Goldenrod		 = new double[]{0.858824, 0.858824, 0.439216}; 
	public static final double [] GreenYellow	 = new double[]{0.576471, 0.858824, 0.439216}; 
	public static final double [] IndianRed		 = new double[]{0.309804, 0.184314, 0.184314}; 
	public static final double [] Khaki			 = new double[]{0.623529, 0.623529, 0.372549}; 
	public static final double [] LightBlue		 = new double[]{0.74902, 0.847059, 0.847059}; 
	public static final double [] LightSteelBlue = new double[]{0.560784, 0.560784, 0.737255}; 
	public static final double [] LimeGreen		 = new double[]{0.196078, 0.8, 0.196078}; 
	public static final double [] Maroon		 = new double[]{0.556863, 0.137255, 0.419608}; 
	public static final double [] MediumAquamarine	 = new double[]{0.196078, 0.8, 0.6}; 
	public static final double [] MediumBlue		 = new double[]{0.196078, 0.196078, 0.8}; 
	public static final double [] MediumForestGreen	 = new double[]{0.419608, 0.556863, 0.137255}; 
	public static final double [] MediumGoldenrod	 = new double[]{0.917647, 0.917647, 0.678431}; 
	public static final double [] MediumOrchid		 = new double[]{0.576471, 0.439216, 0.858824}; 
	public static final double [] MediumSeaGreen	 = new double[]{0.258824, 0.435294, 0.258824}; 
	public static final double [] MediumSlateBlue	 = new double[]{0.0, 0.498039, 1.0}; 
	public static final double [] MediumSpringGreen	 = new double[]{0.0, 0.498039, 1.0}; 
	public static final double [] MediumTurquoise	 = new double[]{0.439216, 0.858824, 0.858824}; 
	public static final double [] MediumVioletRed	 = new double[]{0.858824, 0.439216, 0.576471}; 
	public static final double [] MidnightBlue		 = new double[]{0.184314, 0.184314, 0.309804}; 
	public static final double [] Navy		 = new double[]{0.137255, 0.137255, 0.556863}; 
	public static final double [] NavyBlue	 = new double[]{0.137255, 0.137255, 0.556863}; 
	public static final double [] Orange	 = new double[]{1, 0.5, 0.0}; 
	public static final double [] OrangeRed	 = new double[]{1.0, 0.25, 0.0}; 
	public static final double [] Orchid	 = new double[]{0.858824, 0.439216, 0.858824}; 
	public static final double [] PaleGreen	 = new double[]{0.560784, 0.737255, 0.560784}; 
	public static final double [] Pink		 = new double[]{0.737255, 0.560784, 0.560784}; 
	public static final double [] Plum		 = new double[]{0.917647, 0.678431, 0.917647}; 
	public static final double [] Salmon	 = new double[]{0.435294, 0.258824, 0.258824}; 
	public static final double [] SeaGreen	 = new double[]{0.137255, 0.556863, 0.419608}; 
	public static final double [] Sienna	 = new double[]{0.556863, 0.419608, 0.137255}; 
	public static final double [] SkyBlue	 = new double[]{0.196078, 0.6, 0.8}; 
	public static final double [] SlateBlue	 = new double[]{0.0, 0.498039, 1.0}; 
	public static final double [] SpringGreen= new double[]{0.0, 1.0, 0.498039}; 
	public static final double [] SteelBlue	 = new double[]{0.137255, 0.419608, 0.556863}; 
	public static final double [] Tan		 = new double[]{0.858824, 0.576471, 0.439216}; 
	public static final double [] Thistle	 = new double[]{0.847059, 0.74902, 0.847059}; 
	public static final double [] Turquoise	 = new double[]{0.678431, 0.917647, 0.917647}; 
	public static final double [] Violet	 = new double[]{0.309804, 0.184314, 0.309804}; 
	public static final double [] VioletRed	 = new double[]{0.8, 0.196078, 0.6}; 
	public static final double [] Wheat		 = new double[]{0.847059, 0.847059, 0.74902}; 
	public static final double [] YellowGreen= new double[]{0.6, 0.8, 0.196078}; 
	public static final double [] SummerSky	 = new double[]{0.22, 0.69, 0.87}; 
	public static final double [] RichBlue	 = new double[]{0.35, 0.35, 0.67}; 
	public static final double [] Brass	 	 = new double[]{0.71, 0.65, 0.26}; 
	public static final double [] Copper	 = new double[]{0.72, 0.45, 0.20}; 
	public static final double [] Bronze	 = new double[]{0.55, 0.47, 0.14}; 
	public static final double [] Bronze2	 = new double[]{0.65, 0.49, 0.24}; 
	public static final double [] Silver	 = new double[]{0.90, 0.91, 0.98}; 
	public static final double [] BrightGold = new double[]{0.85, 0.85, 0.10}; 
	public static final double [] OldGold	 = new double[]{0.81, 0.71, 0.23}; 
	public static final double [] Feldspar	 = new double[]{0.82, 0.57, 0.46}; 
	public static final double [] Quartz	 = new double[]{0.85, 0.85, 0.95}; 
	public static final double [] Mica	 	 = new double[]{0.0, 0.0, 0.0};
	public static final double [] NeonPink	 = new double[]{1.00, 0.43, 0.78}; 
	public static final double [] DarkPurple = new double[]{0.53, 0.12, 0.47}; 
	public static final double [] NeonBlue	 = new double[]{0.30, 0.30, 1.00}; 
	public static final double [] CoolCopper = new double[]{0.85, 0.53, 0.10}; 
	public static final double [] MandarinOrange	 = new double[]{0.89, 0.47, 0.20}; 
	public static final double [] LightWood	 = new double[]{0.91, 0.76, 0.65}; 
	public static final double [] MediumWood = new double[]{0.65, 0.50, 0.39}; 
	public static final double [] DarkWood	 = new double[]{0.52, 0.37, 0.26}; 
	public static final double [] SpicyPink	 = new double[]{1.00, 0.11, 0.68}; 
	public static final double [] SemiSweetChoc	 = new double[]{0.42, 0.26, 0.15}; 
	public static final double [] BakersChoc = new double[]{0.36, 0.20, 0.09}; 
	public static final double [] Flesh	 	 = new double[]{0.96, 0.80, 0.69}; 
	public static final double [] NewTan	 = new double[]{0.92, 0.78, 0.62}; 
	public static final double [] NewMidnightBlue = new double[]{0.00, 0.00, 0.61}; 
	public static final double [] VeryDarkBrown	 = new double[]{0.35, 0.16, 0.14}; 
	public static final double [] DarkBrown		 = new double[]{0.36, 0.25, 0.20}; 
	public static final double [] DarkTan		 = new double[]{0.59, 0.41, 0.31}; 
	public static final double [] GreenCopper	 = new double[]{0.32, 0.49, 0.46}; 
	public static final double [] DkGreenCopper	 = new double[]{0.29, 0.46, 0.43}; 
	public static final double [] DustyRose		 = new double[]{0.52, 0.39, 0.39}; 
	public static final double [] HuntersGreen	 = new double[]{0.13, 0.37, 0.31}; 
	public static final double [] Scarlet		 = new double[]{0.55, 0.09, 0.09}; 
	public static final double [] Med_Purple	 = new double[]{0.73, 0.16, 0.96}; 
	public static final double [] Light_Purple	 = new double[]{0.87, 0.58, 0.98}; 
	public static final double [] Very_Light_Purple	 = new double[]{0.94, 0.81, 0.99};

	public static ColorMap 		_colorMap 			= new ColorMap(0, 1.0, ColorMap.JET);
	public static final double [] 	_colorIndex 		= new double[] { 0.99,
			0.63302, 0.21656, 0.46257, 0.73081, 0.37328, 0.87629, 0.25430, 0.45968, 0.60556, 0.73623, 0.24818, 0.17734, 0.99210, 0.83660, 0.26743, 0.49099, 0.08557, 0.11326, 0.55592, 0.22390, 
			0.85439, 0.28428, 0.03373, 0.35700, 0.34755, 0.68872, 0.00034, 0.90159, 0.05593, 0.93143, 0.96176, 0.36830, 0.05496, 0.16142, 0.98976, 0.79892, 0.19851, 0.69130, 0.90844, 0.03573, 
			0.47446, 0.45126, 0.64783, 0.01748, 0.12943, 0.46362, 0.30420, 0.41137, 0.32373, 0.12249, 0.80321, 0.81728, 0.88685, 0.56789, 0.57470, 0.11354, 0.20987, 0.85517, 0.31705, 0.25269, 
			0.43310, 0.93749, 0.75386, 0.96132, 0.86560, 0.70299, 0.26768, 0.34965, 0.00791, 0.43832, 0.43260, 0.39471, 0.63999, 0.12586, 0.76205, 0.62509, 0.18353, 0.92494, 0.43787, 0.10589, 
			0.95108, 0.03546, 0.39347, 0.99908, 0.95399, 0.89760, 0.88650, 0.55758, 0.65714, 0.34214, 0.61523, 0.40075, 0.44022, 0.87351, 0.50843, 0.31417, 0.55384, 0.74129, 0.54951, 0.61867, 
			0.04339, 0.83550, 0.38375, 0.36145, 0.45411, 0.72765, 0.00124, 0.19633, 0.12821, 0.37142, 0.48824, 0.84073, 0.55796, 0.07568, 0.69640, 0.42381, 0.56842, 0.36761, 0.69172, 0.87307, 
			0.06916, 0.27435, 0.01418, 0.72607, 0.73645, 0.36959, 0.10408, 0.39943, 0.26582, 0.65461, 0.73278, 0.99892, 0.76918, 0.97343, 0.91306, 0.21263, 0.48192, 0.19790, 0.55809, 0.69331, 
			0.19201, 0.86083, 0.10879, 0.60664, 0.70688, 0.26380, 0.45499, 0.69871, 0.23312, 0.34151, 0.73927, 0.31931, 0.76003, 0.72528, 0.05072, 0.32652, 0.41001, 0.51591, 0.59146, 0.69404, 
			0.24723, 0.24975, 0.62989, 0.94401, 0.22040, 0.42160, 0.44258, 0.23634, 0.75652, 0.71422, 0.49057, 0.05989, 0.01938, 0.77676, 0.27593, 0.99099, 0.99987, 0.99280, 0.68112, 0.91206, 
			0.41502, 0.18381, 0.10607, 0.41560, 0.85806, 0.60486, 0.38721, 0.85842, 0.76434, 0.90668, 0.30755, 0.67635, 0.53579, 0.96218, 0.48854, 0.12146, 0.60171, 0.58486, 0.59207, 0.54172 	
	};
	public static Color getColor(double v) 	{ return _colorMap.getColor(v); }
	public static Color getColor(int index) { int k = index%_colorIndex.length; return getColor(_colorIndex[k]); }

    public static void hsv2rgb(float h, float s, float v, float[] rgb) {
        // H is given on [0->6] or -1. S and V are given on [0->1]. 
        // RGB are each returned on [0->1]. 
        float m, n, f;
        int i;

        float[] hsv = new float[3];

        hsv[0] = h;
        hsv[1] = s;
        hsv[2] = v;
        System.out.println("H: " + h + " S: " + s + " V:" + v);
        if(hsv[0] == -1) {
            rgb[0] = rgb[1] = rgb[2] = hsv[2];
            return;
        }
        i = (int) (Math.floor(hsv[0]));
        f = hsv[0] - i;
        if(i % 2 == 0)
            f = 1 - f; // if i is even 
        m = hsv[2] * (1 - hsv[1]);
        n = hsv[2] * (1 - hsv[1] * f);
        switch(i) {
            case 6:
            case 0:
                rgb[0] = hsv[2];
                rgb[1] = n;
                rgb[2] = m;
                break;
            case 1:
                rgb[0] = n;
                rgb[1] = hsv[2];
                rgb[2] = m;
                break;
            case 2:
                rgb[0] = m;
                rgb[1] = hsv[2];
                rgb[2] = n;
                break;
            case 3:
                rgb[0] = m;
                rgb[1] = n;
                rgb[2] = hsv[2];
                break;
            case 4:
                rgb[0] = n;
                rgb[1] = m;
                rgb[2] = hsv[2];
                break;
            case 5:
                rgb[0] = hsv[2];
                rgb[1] = m;
                rgb[2] = n;
                break;
        }
    }

	// assumes hue [0, 360), saturation [0, 100), lightness [0, 100)
	//The saturation and brightness components should be floating-point 
	//values between zero and one (numbers in the range 0.0-1.0). 
	//The hue component can be any floating-point number. 
	//The floor of this number is subtracted from it to create 
	//a fraction between 0 and 1. This fractional number is then 
	//multiplied by 360 to produce the hue angle in the HSB color model.
	private Color [] genColorArray(int numColors) {
		float dhue = 0.95f/numColors;
		Color [] colors = new Color[numColors];
		float saturation = 0.2f;
		float brightness = 0.8f;
		float hue = 0.01f;
		for(int i=0; i<numColors; i++) {
			hue += dhue;
			int rgb = Color.HSBtoRGB(hue, saturation, brightness); 
			int red = (rgb>>16)&0xFF; 
			int green = (rgb>>8)&0xFF; 
			int blue = rgb&0xFF; 
			colors[i] = new Color(red, green, blue);
		}
		return colors;
	}

    // From http://en.wikipedia.org/wiki/YUV#Mathematical_derivations_and_formulas
    public static void yuv2rgb(float y, float u, float v, float[] rgb) {
        rgb[0] = 1 * y + 0 * u + 1.13983f * v;
        rgb[1] = 1 * y + -.39465f * u + -.58060f * v;
        rgb[2] = 1 * y + 2.03211f * u + 0 * v;
    }

    public static void rgb2yuv(float r, float g, float b, float[] yuv) {
        yuv[0] = .299f * r + .587f * g + .114f * b;
        yuv[1] = -.14713f * r + -.28886f * g + .436f * b;
        yuv[2] = .615f * r + -.51499f * g + -.10001f * b;
    }

    private static float[] randYUVinRGBRange(float minComponent, float maxComponent) {
        while(true) {
            float y = rand.nextFloat(); // * YFRAC + 1-YFRAC);
            float u = rand.nextFloat() * 2 * U_OFF - U_OFF;
            float v = rand.nextFloat() * 2 * V_OFF - V_OFF;
            float[] rgb = new float[3];
            yuv2rgb(y, u, v, rgb);
            float r = rgb[0], g = rgb[1], b = rgb[2];
            if(0 <= r && r <= 1 &&
                0 <= g && g <= 1 &&
                0 <= b && b <= 1 &&
                (r > minComponent || g > minComponent || b > minComponent) && // don't want all dark components
                (r < maxComponent || g < maxComponent || b < maxComponent)) // don't want all light components

                return new float[]{y, u, v};
        }
    }

    /*
     * Returns an array of ncolors RGB triplets such that each is as unique from the rest as possible
     * and each color has at least one component greater than minComponent and one less than maxComponent.
     * Use min == 1 and max == 0 to include the full RGB color range.
     * 
     * Warning: O N^2 algorithm blows up fast for more than 100 colors.
     */
    public static Color[] generateVisuallyDistinctColors(int ncolors, float minComponent, float maxComponent) {
        rand.setSeed(RAND_SEED); // So that we get consistent results for each combination of inputs

        float[][] yuv = new float[ncolors][3];

        // initialize array with random colors
        for(int got = 0; got < ncolors;) {
            System.arraycopy(randYUVinRGBRange(minComponent, maxComponent), 0, yuv[got++], 0, 3);
        }
        // continually break up the worst-fit color pair until we get tired of searching
        for(int c = 0; c < ncolors * 1000; c++) {
            float worst = 8888;
            int worstID = 0;
            for(int i = 1; i < yuv.length; i++) {
                for(int j = 0; j < i; j++) {
                    float dist = sqrdist(yuv[i], yuv[j]);
                    if(dist < worst) {
                        worst = dist;
                        worstID = i;
                    }
                }
            }
            float[] best = randYUVBetterThan(worst, minComponent, maxComponent, yuv);
            if(best == null)
                break;
            else
                yuv[worstID] = best;
        }

        Color[] rgbs = new Color[yuv.length];
        for(int i = 0; i < yuv.length; i++) {
            float[] rgb = new float[3];
            yuv2rgb(yuv[i][0], yuv[i][1], yuv[i][2], rgb);
            rgbs[i] = new Color(rgb[0], rgb[1], rgb[2]);
            //System.out.println(rgb[i][0] + "\t" + rgb[i][1] + "\t" + rgb[i][2]);
        }

        return rgbs;
    }

    private static float sqrdist(float[] a, float[] b) {
        float sum = 0;
        for(int i = 0; i < a.length; i++) {
            float diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }

    private static double worstFit(Color[] colors) {
        float worst = 8888;
        float[] a = new float[3], b = new float[3];
        for(int i = 1; i < colors.length; i++) {
            colors[i].getColorComponents(a);
            for(int j = 0; j < i; j++) {
                colors[j].getColorComponents(b);
                float dist = sqrdist(a, b);
                if(dist < worst) {
                    worst = dist;
                }
            }
        }
        return Math.sqrt(worst);
    }

    private static float[] randYUVBetterThan(float bestDistSqrd, float minComponent, float maxComponent, float[][] in) {
        for(int attempt = 1; attempt < 100 * in.length; attempt++) {
            float[] candidate = randYUVinRGBRange(minComponent, maxComponent);
            boolean good = true;
            for(int i = 0; i < in.length; i++)
                if(sqrdist(candidate, in[i]) < bestDistSqrd)
                    good = false;
            if(good)
                return candidate;
        }
        return null; // after a bunch of passes, couldn't find a candidate that beat the best.
    }


    /**
     * Simple example program.
     */
    public static void main(String[] args) {
        final int ncolors = 10;
        Color[] colors = generateVisuallyDistinctColors(ncolors, .8f, .3f);
        for(int i = 0; i < colors.length; i++) {
            System.out.println(colors[i].toString());
        }
        System.out.println("Worst fit color = " + worstFit(colors));
    }

}


