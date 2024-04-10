package org.uic.interpreter.command;

import org.uic.barcode.staticFrame.StaticFrame;
import org.uic.interpreter.TlbInterpreter;
import org.uic.interpreter.console.Console;
import org.uic.interpreter.exception.TlbConstraintException;
import org.uic.interpreter.exception.TlbInterpreterException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class CommandTest {

    private TlbInterpreter interpreter;

    public CommandTest(String interpreterFilename) throws IOException, TlbInterpreterException {
        byte[] interpreterFileEncoded = Files.readAllBytes(Paths.get(interpreterFilename));
        String interpreterFileContent = new String(interpreterFileEncoded, Charset.defaultCharset());

        this.interpreter = new TlbInterpreter();
        this.interpreter.loadInterpreter(interpreterFileContent);
    }


    public void execute(StaticFrame staticFrame) throws TlbConstraintException, TlbInterpreterException {
        Console.writeLine("");
        Console.writeLine("#", 100);
        Console.writeLine("Der Interpreter hat folgende Ergebnisse zurückgeliefert:");
        Console.writeLine("#", 100);

        Map<String, Object> interpreterResult = interpreter.processData(staticFrame, staticFrame.getuTlay().getLayout());

        for (Map.Entry<String, Object> entry : interpreterResult.entrySet()) {
            Console.writeLine(String.format("%s: %s", entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null));
            Console.writeLine("-", 100);
        }
    }

}
