package il.co.idocare.www.idocare;


public class UtilMethods {

    /**
     * Count lines in string.
     * @param string it is assumed that line terminator is either \n or \r or \r\n, but not a mix of them.
     * @return number of lines in this string
     */
    public static int countLines(String string) {
        int lines = 1;
        int pos = 0;
        while ((pos = Math.max(string.indexOf("\n", pos), string.indexOf("\r", pos)) + 1) != 0) {
            lines++;
        }
        return lines;
    }
}
