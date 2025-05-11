package tum.i2.cma;

import java.io.IOException;
import java.util.Arrays;

public class Helpers {

    public static CMa fromCMaCodeFile(String filePath) throws IOException {
        CmaParser parser = new CmaParser();
        CMaInstruction[] instructions = parser.parseFile(filePath);
        return new CMa(Arrays.stream(instructions).toList());
    }
}
