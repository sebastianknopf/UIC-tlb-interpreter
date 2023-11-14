package org.uic.interpreter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.uic.barcode.staticFrame.StaticFrame;
import org.uic.barcode.staticFrame.ticketLayoutBarcode.TicketLayout;
import org.uic.interpreter.exception.TlbConstraintException;
import org.uic.interpreter.exception.TlbInterpreterException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TlbInterpreter {

    private Interpreter interpreter;

    public TlbInterpreter() {
    }

    public TlbInterpreter(String tlbInterpreterJson) throws TlbInterpreterException {
        this.loadInterpreter(tlbInterpreterJson);
    }

    public void loadInterpreter(String tlbInterpreterJson) throws TlbInterpreterException {
        try {
            // extract primary interpreter
            JSONObject interpreterObject = new JSONObject(tlbInterpreterJson);

            this.interpreter = new Interpreter();
            this.interpreter.setName(interpreterObject.getString("name"));
            this.interpreter.setVersion(interpreterObject.getString("version"));

            // extract constraints
            JSONArray conditionsArray = interpreterObject.getJSONArray("constraints");
            for (int c = 0; c < conditionsArray.length(); c++) {
                JSONObject conditionObject = (JSONObject) conditionsArray.get(c);

                Constraint constraint = new Constraint();
                constraint.setKey(conditionObject.getString("key"));

                JSONArray conditionValuesArray = conditionObject.getJSONArray("values");
                for (int v = 0; v < conditionValuesArray.length(); v++) {
                    constraint.addValue(String.valueOf(conditionValuesArray.get(v)));
                }

                this.interpreter.addCondition(constraint);
            }

            // extract instructions
            JSONArray instructionsArray = interpreterObject.getJSONArray("instructions");
            for (int i = 0; i < instructionsArray.length(); i++) {
                JSONObject instructionObject = (JSONObject) instructionsArray.get(i);

                Instruction instruction = new Instruction();
                instruction.setType(instructionObject.getString("type"));
                instruction.setDelimiter(instructionObject.getString("delimiter"));
                instruction.setFormat(interpreterObject.getString("format"));

                JSONArray instructionFieldsArray = instructionObject.getJSONArray("fields");
                for (int f = 0; f < instructionFieldsArray.length(); f++) {
                    JSONObject fieldObject = instructionFieldsArray.getJSONObject(f);

                    Field field = new Field();
                    field.setLine(fieldObject.getString("line"));
                    field.setColumn(fieldObject.getString("column"));

                    JSONArray fieldSubstringArray = instructionObject.getJSONArray("substring");
                    field.setSubstringStart(fieldSubstringArray.getInt(0));
                    field.setSubstringLength(fieldSubstringArray.getInt(1));

                    instruction.addField(field);
                }

                this.interpreter.addInstruction(instruction);
            }
        } catch (Exception exception) {
            throw new TlbInterpreterException(exception);
        }
    }

    public Map<String, Object> processData(StaticFrame uicStaticFrame, TicketLayout uicTicketLayout) throws TlbInterpreterException, TlbConstraintException {
        if (this.interpreter == null) {
            throw new TlbInterpreterException("no interpreter loaded");
        }

        if (uicStaticFrame == null || uicStaticFrame.getuTlay() == null || uicTicketLayout == null) {
            throw new TlbInterpreterException("ticket does not represent a TLB barcode");
        }

        for(Constraint constraint : this.interpreter.getConditions()) {
            if (constraint.getKey().equalsIgnoreCase("ricsCode")) {
                if (!constraint.getValues().contains(uicStaticFrame.getHeaderRecord().getIssuer())) {
                    this.raiseConstraintException(constraint.getKey());
                }
            } else if (constraint.getKey().equalsIgnoreCase("messageTypeVersion")) {
                if (!constraint.getValues().contains(String.valueOf(uicStaticFrame.getVersion()))) {
                    this.raiseConstraintException(constraint.getKey());
                }
            } else if (constraint.getKey().equalsIgnoreCase("recordVersion")) {
                if (!constraint.getValues().contains(uicStaticFrame.getuTlay().getVersionId())) {
                    this.raiseConstraintException(constraint.getKey());
                }
            } else if (constraint.getKey().equalsIgnoreCase("layoutStandard")) {
                if (!constraint.getValues().contains(uicTicketLayout.getLayoutStandard())) {
                    this.raiseConstraintException(constraint.getKey());
                }
            } else {
                throw new TlbInterpreterException(String.format("unknown condition key %s", constraint.getKey()));
            }
        }

        Map<String, Object> result = new HashMap<>();

        for (Instruction instruction : this.interpreter.getInstructions()) {
            if (instruction.getType().equalsIgnoreCase("productName")) {
                result.put(instruction.getType(), this.productNameInstruction());
            } else if (instruction.getType().equalsIgnoreCase("validFrom")) {
                result.put(instruction.getType(), this.validFromInstruction());
            } else if (instruction.getType().equalsIgnoreCase("validUntil")) {
                result.put(instruction.getType(), this.validUntilInstruction());
            } else {
                throw new TlbInterpreterException(String.format("unknown instruction type %s", instruction.getType()));
            }
        }

        return result;
    }

    private String productNameInstruction() {
        return null;
    }

    private Date validFromInstruction() {
        return new Date();
    }

    private Date validUntilInstruction() {
        return new Date();
    }

    private void raiseConstraintException(String conditionKey) throws TlbConstraintException {
        throw new TlbConstraintException(String.format("constraint %s failed", conditionKey));
    }
}
