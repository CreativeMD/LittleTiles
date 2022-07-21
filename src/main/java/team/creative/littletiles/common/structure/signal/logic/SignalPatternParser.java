package team.creative.littletiles.common.structure.signal.logic;

import java.text.ParseException;

public class SignalPatternParser {
    
    public final String pattern;
    private int pos = -1;
    
    public SignalPatternParser(String pattern) {
        this.pattern = pattern;
    }
    
    public boolean hasNext() {
        return pos < pattern.length() - 1;
    }
    
    public char next(boolean skipSpace) throws ParseException {
        try {
            if (skipSpace) {
                char next = ' ';
                while (Character.isWhitespace(next))
                    next = pattern.charAt(++pos);
                return next;
            } else
                return pattern.charAt(++pos);
        } catch (IndexOutOfBoundsException e) {
            throw exception("Invalid end of pattern");
        }
    }
    
    public char lookForNext(boolean skipSpace) {
        if (skipSpace) {
            char next = ' ';
            int index = pos;
            while (hasNext() && Character.isWhitespace(next))
                next = pattern.charAt(++index);
            return next;
        }
        if (hasNext())
            return pattern.charAt(pos + 1);
        return '\n';
    }
    
    public int parseNumber() throws ParseException {
        String digit = "";
        boolean first = true;
        while (Character.isDigit(lookForNext(first))) {
            digit += next(first);
            first = false;
        }
        
        try {
            return Integer.parseInt(digit);
        } catch (NumberFormatException e) {
            throw exception("Invalid number " + digit);
        }
    }
    
    public int position() {
        return pos;
    }
    
    public ParseException exception(String text) {
        return new ParseException(text + " '" + pattern + "'", pos);
    }
    
    public ParseException invalidChar(char character) {
        return exception("Invalid char " + character);
    }
    
    public char current() {
        return pattern.charAt(pos);
    }
    
}
