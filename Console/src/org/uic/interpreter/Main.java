package org.uic.interpreter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.uic.barcode.Decoder;
import org.uic.interpreter.TlbInterpreter;
import org.uic.interpreter.console.Console;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        /*System.out.println();

        try {
            Scanner scanner = new Scanner(System.in);

            String ticketFilename;
            if (args.length > 0) {
                ticketFilename = args[0];
            } else {
                System.out.print("(((eTicket-Info-Datei: ");
                ticketFilename = scanner.next();
            }

            String interpreterFilename;
            if (args.length > 2) {
                interpreterFilename = args[1];
            } else {
                System.out.print("Interpreter-Datei: ");
                interpreterFilename = scanner.next();
            }

            System.out.println();

            // extract data from (((eTicket Info App
            JSONObject ticketInfoObject = new JSONObject(loadFile(ticketFilename));
            JSONArray ticketInfoExpertViews = ticketInfoObject.getJSONArray("expertview");
            JSONObject ticketInfoExpertView = ticketInfoExpertViews.getJSONObject(0);

            byte[] uicData = hexToBin(ticketInfoExpertView.getString("base16"));
            Decoder uicDecoder = new Decoder(uicData);

            TlbInterpreter interpreter = new TlbInterpreter();
            interpreter.loadInterpreter(loadFile(interpreterFilename));

            System.out.println(interpreter.getInterpreterName());
            System.out.println(interpreter.getInterpreterVersion());

            Map<String, Object> interpreterResult = interpreter.processData(uicDecoder.getStaticFrame(), uicDecoder.getLayout());

            System.out.println();
            for (Map.Entry<String, Object> entry : interpreterResult.entrySet()) {
                System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null));
            }

            boolean result = Console.askForBooleanResult("This is a test. Do you want to continue or not?", true);
            Console.write(String.format("%s", result));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

        if (args.length > 0 && args[0] != null) {
            String command = args[0].toLowerCase().trim();

            if (command.equals("view")) {

            } else if (command.equals("assistant")) {

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