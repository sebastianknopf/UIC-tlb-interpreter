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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            // extract spec based constraints
            JSONArray specConstraintsArray = interpreterObject.getJSONArray("specConstraints");
            for (int c = 0; c < specConstraintsArray.length(); c++) {
                JSONObject specConstraintObject = (JSONObject) specConstraintsArray.get(c);

                SpecConstraint specConstraint = new SpecConstraint();
                specConstraint.setKey(specConstraintObject.getString("key"));

                JSONArray conditionValuesArray = specConstraintObject.getJSONArray("values");
                for (int v = 0; v < conditionValuesArray.length(); v++) {
                    specConstraint.addValue(String.valueOf(conditionValuesArray.get(v)));
                }

                this.interpreter.addSpecConstraint(specConstraint);
            }

            // extract field based constraints
            if (interpreterObject.has("fieldConstraints")) {
                JSONArray fieldConstraintsArray = interpreterObject.getJSONArray("fieldConstraints");
                for (int c = 0; c < fieldConstraintsArray.length(); c++) {
                    JSONObject fieldConstraintObject = (JSONObject) fieldConstraintsArray.get(c);

                    FieldConstraint fieldConstraint = new FieldConstraint();
                    fieldConstraint.setLine(fieldConstraintObject.getInt("line"));
                    fieldConstraint.setColumn(fieldConstraintObject.getInt("column"));
                    fieldConstraint.setRegex(fieldConstraintObject.getString("regex"));

                    this.interpreter.addFieldConstraint(fieldConstraint);
                }
            }

            // extract elements
            JSONArray elementsArray = interpreterObject.getJSONArray("elements");
            for (int i = 0; i < elementsArray.length(); i++) {
                JSONObject elementObject = (JSONObject) elementsArray.get(i);

                Element element = new Element();
                element.setType(elementObject.getString("type"));
                element.setOptional(elementObject.has("optional") && elementObject.getBoolean("optional"));
                element.setDelimiter(elementObject.has("delimiter") ? elementObject.getString("delimiter") : null);
                element.setFormat(elementObject.has("format") ? elementObject.getString("format") : null);

                JSONArray elementFieldsArray = elementObject.getJSONArray("fields");
                if (elementFieldsArray.length() > 1 && element.getDelimiter() == null) {
                    throw new TlbInterpreterException("instruction delimiter property must not be null when number of fields > 1");
                }

                for (int f = 0; f < elementFieldsArray.length(); f++) {
                    JSONObject fieldObject = elementFieldsArray.getJSONObject(f);

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

                    field.setRegex(fieldObject.has("regex") ? fieldObject.getString("regex") : null);

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

        for(SpecConstraint specConstraint : this.interpreter.getSpecConstraints()) {
            if (specConstraint.getKey().equalsIgnoreCase("ricsCode")) {
                if (!specConstraint.getValues().contains(uicStaticFrame.getHeaderRecord().getIssuer())) {
                    this.raiseConstraintException(specConstraint.getKey());
                }
            } else if (specConstraint.getKey().equalsIgnoreCase("messageTypeVersion")) {
                if (!specConstraint.getValues().contains(String.valueOf(uicStaticFrame.getVersion()))) {
                    this.raiseConstraintException(specConstraint.getKey());
                }
            } else if (specConstraint.getKey().equalsIgnoreCase("recordVersion")) {
                if (!specConstraint.getValues().contains(uicStaticFrame.getuTlay().getVersionId())) {
                    this.raiseConstraintException(specConstraint.getKey());
                }
            } else if (specConstraint.getKey().equalsIgnoreCase("layoutStandard")) {
                if (!specConstraint.getValues().contains(uicTicketLayout.getLayoutStandard())) {
                    this.raiseConstraintException(specConstraint.getKey());
                }
            } else {
                throw new TlbConstraintException(String.format("unknown constraint key %s", specConstraint.getKey()));
            }
        }

        for (FieldConstraint fieldConstraint : this.interpreter.getFieldConstraints()) {
            String fieldValue = this.findLayoutField(uicTicketLayout, fieldConstraint.getLine(), fieldConstraint.getColumn());
            if (fieldValue != null) {
                Matcher matcher = Pattern.compile(fieldConstraint.getRegex()).matcher(fieldValue);
                if (!matcher.matches()) {
                    throw new TlbConstraintException(String.format("constraint field (%d/%d) does not match constraint regex", fieldConstraint.getLine(), fieldConstraint.getColumn()));
                }
            } else {
                throw new TlbConstraintException(String.format("constraint field (%d/%d) does not exist", fieldConstraint.getLine(), fieldConstraint.getColumn()));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();

        for (Element element : this.interpreter.getElements()) {
            if (element.getType().equalsIgnoreCase("productName")) {
                Object elementValue = this.stringElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("validFrom")) {
                Object elementValue = this.dateTimeElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("validUntil")) {
                Object elementValue = this.dateTimeElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("startStationName")) {
                Object elementValue = this.stringElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("destinationStationName")) {
                Object elementValue = this.stringElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            }  else if (element.getType().equalsIgnoreCase("returnValidFrom")) {
                Object elementValue = this.dateTimeElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("returnValidUntil")) {
                Object elementValue = this.dateTimeElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("returnStartStationName")) {
                Object elementValue = this.stringElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("returnDestinationStationName")) {
                Object elementValue = this.stringElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("passengerName")) {
                Object elementValue = this.stringElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("passengerBirthday")) {
                Object elementValue = this.dateTimeElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("serviceClass")) {
                Object elementValue = this.stringElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
            } else if (element.getType().equalsIgnoreCase("infoText")) {
                Object elementValue = this.stringElement(element, uicTicketLayout);
                this.addResultElement(result, element, elementValue);
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

    private String stringElement(Element element, TicketLayout uicTicketLayout) throws TlbInterpreterException {
        return this.extractElementBaseData(element, uicTicketLayout);
    }

    private Date dateTimeElement(Element element, TicketLayout uicTicketLayout) throws TlbInterpreterException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(element.getFormat());

            if (this.interpreter.getTimezone() != null) {
                sdf.setTimeZone(TimeZone.getTimeZone(this.interpreter.getTimezone()));
            }

            String elementBaseData = this.extractElementBaseData(element, uicTicketLayout);
            if (elementBaseData != null && !elementBaseData.equals("null")) {
                return sdf.parse(elementBaseData);
            } else {
                return null;
            }
        } catch(ParseException exception) {
            throw new TlbInterpreterException(exception);
        }
    }

    private void addResultElement(Map<String, Object> resultMap, Element element, Object elementValue) throws TlbInterpreterException {
        if (!element.isOptional() && elementValue == null) {
            throw new TlbInterpreterException(String.format("element of type %s could not be found but is required", element.getType()));
        }

        if (elementValue != null) {
            resultMap.put(element.getType(), elementValue);
        }
    }

    private String extractElementBaseData(Element element, TicketLayout uicTicketLayout) throws TlbInterpreterException {
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

            if (field.getRegex() != null) {
                Matcher matcher = Pattern.compile(field.getRegex(), Pattern.CASE_INSENSITIVE).matcher(fieldValue);

                fieldValue = null;
                while(matcher.find()) {
                    fieldValue = matcher.group(1);
                }
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
