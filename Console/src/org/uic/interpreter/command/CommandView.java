package org.uic.interpreter.command;

import org.uic.barcode.staticFrame.StaticFrame;
import org.uic.barcode.staticFrame.ticketLayoutBarcode.LayoutElement;
import org.uic.interpreter.console.Console;

import java.util.ArrayList;
import java.util.List;

public class CommandView {

    public void execute(StaticFrame staticFrame) {
        List<String> layoutFieldList = new ArrayList<>();

        for (LayoutElement layoutElement : staticFrame.getuTlay().getLayout().getElements()) {
            int row = layoutElement.getLine();
            int column = layoutElement.getColumn();

            layoutFieldList.add(String.format("Zeile: %d\tSpalte: %d\n%s", row, column, layoutElement.getText()));
        }

        Console.writeLine("");
        Console.writeLine("#", 100);
        Console.writeLine("Das Ticket enthält folgende Kopfdaten:");
        Console.writeLine("#", 100);
        Console.writeLine(String.format("Carrier: %s", staticFrame.getHeaderRecord().getIssuer()));
        Console.writeLine(String.format("Version: %d", staticFrame.getVersion()));
        Console.writeLine(String.format("U_TLAY-Version: %s", staticFrame.getuTlay().getVersionId()));
        Console.writeLine(String.format("Layoutstandard: %s", staticFrame.getuTlay().getLayout().getLayoutStandard()));

        Console.writeLine("");
        Console.writeLine("#", 100);
        Console.writeLine("Das Ticket enthält folgende Layout-Elemente:");
        Console.writeLine("#", 100);
        for (String layoutField : layoutFieldList) {
            Console.writeLine(layoutField);
            Console.writeLine("-", 100);
        }
    }
}
