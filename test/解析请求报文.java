import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class 解析请求报文 {
    public static void main(String[] args) {
        String data = "------WebKitFormBoundaryLS68SUFyApZlK4Xy\n" +
                "Content-Disposition: form-data; name=\"asd\"\n\n" +
                "12\n" +
                "------WebKitFormBoundaryLS68SUFyApZlK4Xy\n" +
                "Content-Disposition: form-data; name=\"vbn\"\n\n" +
                "asdasddas\n" +
                "------WebKitFormBoundaryLS68SUFyApZlK4Xy--\n";


        String splitStr = "------WebKitFormBoundaryLS68SUFyApZlK4Xy";
        Map<String, String> map = new HashMap<>();
        Scanner sc = new Scanner(data);
        String line = sc.nextLine();
        String key = null;
        StringBuilder value = new StringBuilder();
        while (sc.hasNextLine()) {
            if (key != null && value.length() != 0) {
                map.put(key, value.toString());
                key = null;
                value.delete(0, value.length());
            }
            if (line.equals(splitStr) && (line = sc.nextLine()).startsWith("Content-Disposition")) {
                key = line.split("; ")[1].split("=")[1].replaceAll("\"", "");
                while (sc.hasNextLine()) {
                    line = sc.nextLine();
                    if (line.isEmpty()) continue;
                    if (line.startsWith(splitStr)) break;
                    value.append(line);
                }
            }
        }
        if (key != null && value.length() != 0) {
            map.put(key, value.toString());
        }

        System.out.println(1);
    }
}
