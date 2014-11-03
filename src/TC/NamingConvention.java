package TC;

public class NamingConvention {
    public String toCamelCase(String variableName) {
        String ret = "";
        for (int i = 0; i < variableName.length(); ) {
            if (variableName.charAt(i) == '_') {
                ret += (char)('A' - 'a' + variableName.charAt(i + 1));
                i += 2; continue;
            }
            ret += variableName.charAt(i);
            ++i;
        }
        return ret;
    }
}
