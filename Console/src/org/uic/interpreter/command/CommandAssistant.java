package org.uic.interpreter.command;

import org.uic.barcode.staticFrame.StaticFrame;
import org.uic.interpreter.Interpreter;
import org.uic.interpreter.SpecConstraint;
import org.uic.interpreter.console.Console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandAssistant {

    private String interpreterFilename;
    private Interpreter interpreter;

    public CommandAssistant(String interpreterFilename) {
        this.interpreterFilename = interpreterFilename;
        this.interpreter = new Interpreter();
    }

    public void execute(StaticFrame staticFrame) throws IOException {
        Console.writeLine("");
        Console.writeLine("#", 100);
        Console.writeLine("Nun beginnt die eigentliche Arbeit. Der Assistent fragt der Reihe nach alle relevanten Infos ab und\nerstellt am Ende eine gültige Interpreter-Datei. Los geht es mit einigen Kopfdaten.");
        Console.writeLine("#", 100);

        Console.writeLine("");

        // request carrier code
        String carrierCodeValue = Console.askForStringResult("Für welchen Carrier-Code soll der Interpreter gelten?", staticFrame.getHeaderRecord().getIssuer());

        SpecConstraint carrierConstraint = new SpecConstraint();
        carrierConstraint.setKey("ricsCode");
        carrierConstraint.setValues(this.generateStringList(carrierCodeValue));

        this.interpreter.addSpecConstraint(carrierConstraint);

        // request message version
        String messageTypeVersionValue = Console.askForStringResult("Für welche Message-Version soll der Interpreter gelten?", String.valueOf(staticFrame.getVersion()));

        SpecConstraint messageTypeVersionConstraint = new SpecConstraint();
        messageTypeVersionConstraint.setKey("messageTypeVersionValue");
        messageTypeVersionConstraint.setValues(this.generateStringList(messageTypeVersionValue));

        this.interpreter.addSpecConstraint(messageTypeVersionConstraint);

        // request record version
        String recordVersionValue = Console.askForStringResult("Für welche Record-Version soll der Interpreter gelten?", staticFrame.getuTlay().getVersionId());

        SpecConstraint recordVersionConstraint = new SpecConstraint();
        recordVersionConstraint.setKey("recordVersion");
        recordVersionConstraint.setValues(this.generateStringList(recordVersionValue));

        this.interpreter.addSpecConstraint(recordVersionConstraint);

        // request layout standard
        String layoutStandardValue = Console.askForStringResult("Für welchen Layoutstandard soll der Interpreter gelten?", staticFrame.getuTlay().getLayout().getLayoutStandard());

        SpecConstraint layoutStandardConstraint = new SpecConstraint();
        layoutStandardConstraint.setKey("recordVersion");
        layoutStandardConstraint.setValues(this.generateStringList(layoutStandardValue));

        this.interpreter.addSpecConstraint(layoutStandardConstraint);
    }

    private List<String> generateStringList(String inputString) {
        List<String> resultList = new ArrayList<>();

        if (inputString.contains(",")) {
            for (String str : inputString.split(",")) {
                resultList.add(str.trim());
            }
        } else {
            resultList.add(inputString.trim());
        }

        return resultList;
    }
}
