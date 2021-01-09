package mcp.mobius.waila.api;

import java.util.regex.Pattern;

public class SpecialChars {
    
    // Minecraft values
    public static String MCStyle = "\u00A7";
    public static final Pattern patternMinecraft = Pattern.compile("(?i)" + MCStyle + "[0-9A-FK-OR]");
    @Deprecated // Use TextFormatting
    public static String BLACK = MCStyle + "0";
    @Deprecated // Use TextFormatting
    public static String DBLUE = MCStyle + "1";
    @Deprecated // Use TextFormatting
    public static String DGREEN = MCStyle + "2";
    @Deprecated // Use TextFormatting
    public static String DAQUA = MCStyle + "3";
    @Deprecated // Use TextFormatting
    public static String DRED = MCStyle + "4";
    @Deprecated // Use TextFormatting
    public static String DPURPLE = MCStyle + "5";
    @Deprecated // Use TextFormatting
    public static String GOLD = MCStyle + "6";
    @Deprecated // Use TextFormatting
    public static String GRAY = MCStyle + "7";
    @Deprecated // Use TextFormatting
    public static String DGRAY = MCStyle + "8";
    @Deprecated // Use TextFormatting
    public static String BLUE = MCStyle + "9";
    @Deprecated // Use TextFormatting
    public static String GREEN = MCStyle + "a";
    @Deprecated // Use TextFormatting
    public static String AQUA = MCStyle + "b";
    @Deprecated // Use TextFormatting
    public static String RED = MCStyle + "c";
    @Deprecated // Use TextFormatting
    public static String LPURPLE = MCStyle + "d";
    @Deprecated // Use TextFormatting
    public static String YELLOW = MCStyle + "e";
    @Deprecated // Use TextFormatting
    public static String WHITE = MCStyle + "f";
    @Deprecated // Use TextFormatting
    public static String OBF = MCStyle + "k";
    @Deprecated // Use TextFormatting
    public static String BOLD = MCStyle + "l";
    @Deprecated // Use TextFormatting
    public static String STRIKE = MCStyle + "m";
    @Deprecated // Use TextFormatting
    public static String UNDER = MCStyle + "n";
    @Deprecated // Use TextFormatting
    public static String ITALIC = MCStyle + "o";
    @Deprecated // Use TextFormatting
    public static String RESET = MCStyle + "r";
    
    // Waila values
    public static String WailaStyle = "\u00A4";
    public static final Pattern patternWaila = Pattern.compile("(?i)(" + WailaStyle + "(?<type>..))");
    public static String WailaIcon = "\u00A5";
    public static final Pattern patternIcon = Pattern.compile("(?i)(" + WailaStyle + WailaIcon + "(?<type>[0-9a-z]))");
    public static String WailaRenderer = "\u00A6";
    public static final Pattern patternLineSplit = Pattern
        .compile("(?i)(" + WailaStyle + WailaStyle + "[^" + WailaStyle + "]+|" + WailaStyle + WailaIcon + "[0-9A-Z]|" + WailaStyle + WailaRenderer + "a\\{([^,}]*),?([^}].*)\\}$|[^" + WailaStyle + "]+)");
    public static String TAB = WailaStyle + WailaStyle + "a";
    public static final Pattern patternTab = Pattern.compile("(?i)" + TAB);
    public static String WailaSplitter = WailaStyle + "\u03D6";
    public static String ALIGNRIGHT = WailaStyle + WailaStyle + "b";
    public static final Pattern patternRight = Pattern.compile("(?i)" + ALIGNRIGHT);
    public static String ALIGNCENTER = WailaStyle + WailaStyle + "c";
    public static final Pattern patternCenter = Pattern.compile("(?i)" + ALIGNCENTER);
    public static String HEART = WailaStyle + WailaIcon + "a";
    public static String HHEART = WailaStyle + WailaIcon + "b";
    public static String EHEART = WailaStyle + WailaIcon + "c";
    public static String RENDER = WailaStyle + WailaRenderer + "a";
    public static final Pattern patternRender = Pattern.compile("(?i)(" + RENDER + "\\{(?<name>[^,}]*)\\+,?(?<args>.*)\\}$)");
    
    /** Helper method to get a proper RENDER string. Just put the name of the renderer and the params in, and it will give back a directly usable String for the tooltip. */
    public static String getRenderString(String name, String... params) {
        String result = RENDER + "{" + name;
        for (String s : params) {
            result += "+," + s;
        }
        result += "}" + WailaSplitter;
        return result;
    }
}
