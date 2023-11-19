# UIC TLB Interpreter
Interpreter for UIC railway tickets based on UIC918.3 layouts.

## Documentation
To use the TLB interpreter, you need a interpreter definition and a reference to the [uic-barcode library](https://github.com/UnionInternationalCheminsdeFer/UIC-barcode) at least. Implementation then works as follows:

```java
try {
    String interpreterDefinition = null; // load JSON string here
    TlbInterpreter interpreter = new TlbInterpreter(interpreterDefinition);

    Map<String, Object> results = interpreter.processData(staticFrame, ticketLayout);
    // process results
} catch (TlbConstraintException ex) {
    // thrown if a constraint within the interpreter definition fails
} catch (TlbInterpreterException ex) {
    // thrown if another error happened while running the interpreter
}
```

Take a look into JSON schema [uic-tlb-interpreter.json](uic-tlb-interpreter.json) to see how a interpreter definition must look like and examples in [samples directory](/samples) for reference.

## Result Types
Currently, the interpreter supports following result types:

- productName
- validFrom
- validUntil
- startStationName
- destinationStationName
- returnValidFrom
- returnValidUntil
- returnStartStationName
- returnDestinationStationName
- passengerName
- passengerBirthday
- serviceClass
- infoText

## License
See [LICENSE.md](/LICENSE.md) for license information.