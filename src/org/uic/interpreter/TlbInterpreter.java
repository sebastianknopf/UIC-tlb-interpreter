package org.uic.interpreter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.uic.barcode.staticFrame.StaticFrame;
import org.uic.barcode.staticFrame.ticketLayoutBarcode.LayoutElement;
import org.uic.barcode.staticFrame.ticketLayoutBarcode.TicketLayout;
import org.uic.interpreter.exception.TlbConstraintException;
import org.uic.interpreter.exception.TlbInterpreterException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TlbInterpreter {

    private Interpreter interpreter;

    public TlbInterpreter() {
    }

    public TlbInterpreter(String tlbInterpreterJson) throws TlbInterpreterException {
        this.loadInterpreter(tlbInterpreterJson);
    }

    public String getInterpreterName() {
        if (this.interpreter != null) {
            return this.interpreter.getName();
        } else {
            return null;
        }
    }

    public String getInterpreterVersion() {
        if (this.interpreter != null) {
            return this.interpreter.getVersion();
        } else {
            return null;
        }
    }

    public void loadInterpreter(String tlbInterpreterJson) throws TlbInterpreterException {
        try {
            // extract primary interpreter
            JSONObject interpreterObject = new JSONObject(tlbInterpreterJson);

            this.interpreter = new Interpreter();
            this.interpreter.setName(interpreterObject.getString("name"));
            this.interpreter.setVersion(interpreterObject.getString("version"));
            this.interpreter.setTimezone(interpreterObject.has("timezone") ? interpreterObject.getString("timezone") : null);

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

                this.interpreter.addConstraint(constraint);
            }

            // extract elements
            JSONArray instructionsArray = interpreterObject.getJSONArray("elements");
            for (int i = 0; i < instructionsArray.length(); i++) {
                JSONObject instructionObject = (JSONObject) instructionsArray.get(i);

                Element element = new Element();
                element.setType(instructionObject.getString("type"));
                element.setDelimiter(instructionObject.has("delimiter") ? instructionObject.getString("delimiter") : null);
                element.setFormat(instructionObject.has("format") ? instructionObject.getString("format") : null);

                JSONArray instructionFieldsArray = instructionObject.getJSONArray("fields");
                if (instructionFieldsArray.length() > 1 && element.getDelimiter() == null) {
                    throw new TlbInterpreterException("instruction delimiter property must not be null when number of fields > 1");
                }

                for (int f = 0; f < instructionFieldsArray.length(); f++) {
                    JSONObject fieldObject = instructionFieldsArray.getJSONObject(f);

                    Field field = new Field();
                    field.setLine(fieldObject.getInt("line"));
                    field.setColumn(fieldObject.getInt("column"));

                    field.setOptional(fieldObject.has("optional") && fieldObject.getBoolean("optional"));

                    if (fieldObject.has("substring")) {
                        JSONArray fieldSubstringArray = fieldObject.getJSONArray("substring");
                        if (fieldSubstringArray.length() == 2) {
                            field.setSubstringStart(fieldSubstringArray.getInt(0));
                            field.setSubstringLength(fieldSubstringArray.getInt(1));
                        }
                    }

                    field.setPrefix(fieldObject.has("prefix") ? fieldObject.getString("prefix") : null);
                    field.setSuffix(fieldObject.has("suffix") ? fieldObject.getString("suffix") : null);

                    if (field.getPrefix() != null && element.getDelimiter() == null) {
                        throw new TlbInterpreterException("delimiter must not be null when using a prefix");
                    }

                    if (field.getSuffix() != null && element.getDelimiter() == null) {
                        throw new TlbInterpreterException("delimiter must not be null when using a suffix");
                    }

                    element.addField(field);
                }

                this.interpreter.addElement(element);
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

        for(Constraint constraint : this.interpreter.getConstraints()) {
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
                throw new TlbConstraintException(String.format("unknown constraint key %s", constraint.getKey()));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();

        for (Element element : this.interpreter.getElements()) {
            if (element.getType().equalsIgnoreCase("productName")) {
                result.put(element.getType(), this.stringInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("validFrom")) {
                result.put(element.getType(), this.dateTimeInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("validUntil")) {
                result.put(element.getType(), this.dateTimeInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("startStationName")) {
                result.put(element.getType(), this.stringInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("destinationStationName")) {
                result.put(element.getType(), this.stringInstruction(element, uicTicketLayout));
            }  else if (element.getType().equalsIgnoreCase("returnValidFrom")) {
                result.put(element.getType(), this.dateTimeInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("returnValidUntil")) {
                result.put(element.getType(), this.dateTimeInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("returnStartStationName")) {
                result.put(element.getType(), this.stringInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("returnDestinationStationName")) {
                result.put(element.getType(), this.stringInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("passengerName")) {
                result.put(element.getType(), this.stringInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("serviceClass")) {
                result.put(element.getType(), this.stringInstruction(element, uicTicketLayout));
            } else if (element.getType().equalsIgnoreCase("infoText")) {
                result.put(element.getType(), this.stringInstruction(element, uicTicketLayout));
            } else {
                throw new TlbInterpreterException(String.format("unknown element type %s", element.getType()));
            }
        }

        if (result.containsKey("validFrom") && result.containsKey("validUntil")) {
            Date validFrom = (Date) result.get("validFrom");
            Date validUntil = (Date) result.get("validUntil");

            if (validUntil.before(validFrom)) {
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(validUntil);
                calendar.add(Calendar.YEAR, 1);

                result.replace("validUntil", calendar.getTime());
            }
        }

        return result;
    }

    private String stringInstruction(Element element, TicketLayout uicTicketLayout) throws TlbInterpreterException {
        return this.extractInstructionBaseData(element, uicTicketLayout);
    }

    private Date dateTimeInstruction(Element element, TicketLayout uicTicketLayout) throws TlbInterpreterException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(element.getFormat());

            if (this.interpreter.getTimezone() != null) {
                sdf.setTimeZone(TimeZone.getTimeZone(this.interpreter.getTimezone()));
            }

            String instructionBaseData = this.extractInstructionBaseData(element, uicTicketLayout);
            return sdf.parse(instructionBaseData);
        } catch(ParseException exception) {
            throw new TlbInterpreterException(exception);
        }
    }

    private String extractInstructionBaseData(Element element, TicketLayout uicTicketLayout) throws TlbInterpreterException {
        List<String> baseStringList = new ArrayList<>();
        for (Field field : element.getFields()) {
            String fieldValue = this.findLayoutField(uicTicketLayout, field.getLine(), field.getColumn());

            if (!field.isOptional() && fieldValue == null) {
                throw new TlbInterpreterException(String.format("field at line %d and column %d does not exist", field.getLine(), field.getColumn()));
            } else if (field.isOptional() && fieldValue == null) {
                continue;
            }

            if (field.getSubstringStart() > -1) {
                if (field.getSubstringLength() < 1) {
                    fieldValue = fieldValue.substring(field.getSubstringStart());
                } else {
                    fieldValue = fieldValue.substring(field.getSubstringStart(), field.getSubstringStart() + field.getSubstringLength());
                }
            }

            if (field.getPrefix() != null) {
                fieldValue = String.format("%s%s%s", field.getPrefix(), element.getDelimiter(), fieldValue);
            }

            if (field.getSuffix() != null) {
                fieldValue = String.format("%s%s%s", fieldValue, element.getDelimiter(), field.getSuffix());
            }

            baseStringList.add(fieldValue);
        }

        if (element.getDelimiter() == null) {
            if (!baseStringList.isEmpty()) {
                return baseStringList.get(0);
            } else {
                return null;
            }
        } else {
            if (!baseStringList.isEmpty()) {
                return String.join(element.getDelimiter(), baseStringList);
            } else {
                return null;
            }
        }
    }

    private String findLayoutField(TicketLayout uicTicketLayout, int line, int column) {
        for (LayoutElement layoutElement : uicTicketLayout.getElements()) {
            if (layoutElement.getLine() == line && layoutElement.getColumn() == column) {
                return layoutElement.getText();
            }
        }

        return null;
    }

    private void raiseConstraintException(String conditionKey) throws TlbConstraintException {
        throw new TlbConstraintException(String.format("constraint %s failed", conditionKey));
    }
}
