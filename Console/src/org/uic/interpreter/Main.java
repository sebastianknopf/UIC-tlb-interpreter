package org.uic.interpreter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uic.barcode.Decoder;
import org.uic.barcode.staticFrame.StaticFrame;
import org.uic.barcode.ticket.EncodingFormatException;
import org.uic.interpreter.TlbInterpreter;
import org.uic.interpreter.command.CommandView;
import org.uic.interpreter.console.Console;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.DataFormatException;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0] != null) {
            // extract selected command
            String command = args[0].toLowerCase().trim();

            // display welcome message
            Console.writeLine("#", 100);
            Console.writeLine("Willkommen in der uic-tlb-interpreter Konsole!");
            Console.writeLine("#", 100);
            Console.writeLine("");

            // try reading a (((eTicketInfo file at first
            StaticFrame uicStaticFrame = null;
            while (uicStaticFrame == null) {
                String eTicketInfoFileName = Console.askForStringResult("Bitte geben Sie den Pfad zur (((eTicketInfo-Datei an:");

                try {
                    JSONObject ticketInfoObject = new JSONObject(loadFile(eTicketInfoFileName));
                    JSONArray ticketInfoExpertViews = ticketInfoObject.getJSONArray("expertview");
                    JSONObject ticketInfoExpertView = ticketInfoExpertViews.getJSONObject(0);

                    byte[] uicData = hexToBin(ticketInfoExpertView.getString("base16"));
                    Decoder uicDecoder = new Decoder(uicData);

                    uicStaticFrame = uicDecoder.getStaticFrame();

                    if (uicStaticFrame.getuTlay() == null) {
                        Console.writeLine("Das Ticket enthält keinen U_TLAY-Record.");

                        uicStaticFrame = null;
                    }
                } catch (DataFormatException | EncodingFormatException  e) {
                    Console.writeLine("Die (((eTicketInfo-Datei konnte nicht gelesen werden. Möglicherweise handelt es sich nicht um ein UIC-Ticket.");
                } catch (JSONException e) {
                    Console.writeLine("Die (((eTicketInfo-Datei konnte nicht gelesen werden. Möglicherweise handelt es sich nicht um eine (((eTicketInfo-Datei.");
                }
                catch (IOException e) {
                    Console.writeLine("Die (((eTicketInfo-Datei konnte nicht gelesen werden. Bitte versuchen Sie es erneut.");
                }
            }

            // check for requested command here and call the correct command
            if (command.equals("view")) {
                // display ticket info and content
                CommandView commandView = new CommandView();
                commandView.execute(uicStaticFrame);
            } else if (command.equals("assistant")) {
                // display ticket info and content at first
                CommandView commandView = new CommandView();
                commandView.execute(uicStaticFrame);

                // run assistant command next
            }
        }
    }

    private static String loadFile(String fileName) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(fileName));
        return new String(encoded, Charset.defaultCharset());
    }

    private static byte[] hexToBin(String hex) {
        hex = hex.replace("0x", "");

        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i+1), 16));
        }

        return data;
    }
}