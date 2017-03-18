package cs.njit.edu.cs756.luaengine;

/**
 *
 * @author Chris O
 */
public class LuaScripts {

    private static final String splitStr =
            "function splitStr(str, pat) "
            + "local t = {} "
            + "local fpat = '(.-)' .. pat "
            + "local last_end = 1 "
            + "local s, e, cap = str:find(fpat, 1) "
            + ""
            + "while s do "
            + " if s ~= 1 or cap ~= '' then "
            + "     table.insert(t,cap) "
            + " end "
            + " last_end = e+1 "
            + " s, e, cap = str:find(fpat, last_end) "
            + "end "
            + "if last_end <= #str then "
            + " cap = str:sub(last_end) "
            + " table.insert(t, cap) "
            + "end "
            + "return t "
            + "end ";
    
    private static final String sendOutput = "function sendOutput(out) "
            + " local output = out "
            + " rawset(_G, 'output', output) "
            + "end ";
    
    private static final String getInput = "function getInput() "
            + " local input = rawget(_G, 'input') "
            + " return input "
            + "end ";
    
    private static final String getSensorInput = "function getSensorInput(type) "
            + " local input = rawget(_G, 'sensorInput') "
            + " return input[type] "
            + "end ";
    
    public static String createInsomniaScript(String taskScript) {
        taskScript = taskScript.replace("\n", " \n");
        
        String finalTaskScript = 
                splitStr + "\n\n" +
                sendOutput + "\n\n" +
                getInput + "\n\n" + 
                getSensorInput + "\n\n" +
                taskScript;
        
        return finalTaskScript;
    }
}
