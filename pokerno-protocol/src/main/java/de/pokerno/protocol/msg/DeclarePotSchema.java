// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from msg.proto

package de.pokerno.protocol.msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.UninitializedMessageException;

public class DeclarePotSchema
       implements Schema<DeclarePot> {


    static final DeclarePot DEFAULT_INSTANCE = new DeclarePot();
    static final Schema<DeclarePot> SCHEMA = new DeclarePotSchema();

    public static DeclarePot getDefaultInstance() { return DEFAULT_INSTANCE; }
    public static Schema<DeclarePot> getSchema() { return SCHEMA; }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_POT = 1;
    public static final int FIELD_SIDE = 2;
    public static final int FIELD_RAKE = 3;

    public DeclarePotSchema() {}


    public DeclarePot newMessage() {
        return new DeclarePot();
    }

    public Class<DeclarePot> typeClass() {
        return DeclarePot.class;
    }

    public String messageName() {
        return DeclarePot.class.getSimpleName();
    }

    public String messageFullName() {
        return DeclarePot.class.getName();
    }


    public boolean isInitialized(DeclarePot message) {
        return true;
    }


    public void mergeFrom(Input input, DeclarePot message) throws IOException {
        for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
            mergeFrom(input, message, fieldIx);
        }
    }

    public void mergeFrom(Input input, DeclarePot message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                return;
            case FIELD_POT:
                message.setPot(input.readDouble());
                break;
            case FIELD_SIDE:
                if (message.getSide() == null)
                    message.setSide(new ArrayList<Double>());
                message.getSide().add(input.readDouble());
                break;
            case FIELD_RAKE:
                message.setRake(input.readDouble());
                break;
            default:
                input.handleUnknownField(fieldIx, this);
        }
    }


    private static int[] FIELDS_TO_WRITE = { FIELD_POT, FIELD_SIDE, FIELD_RAKE };

    public int[] getWriteFields() { return FIELDS_TO_WRITE; }

    public void writeTo(Output output, DeclarePot message) throws IOException {
        int[] toWrite = getWriteFields();
        for (int i = 0; i < toWrite.length; i++) {
            writeTo(output, message, toWrite[i]);
        }
    }

    public void writeTo(Output output, DeclarePot message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                break;
            case FIELD_POT:
                output.writeDouble(FIELD_POT, message.getPot(), false);
                break;
            case FIELD_SIDE:
                if (message.getSide() != null) {
                    for (Double sideEntry : message.getSide()) {
                        if (sideEntry != null)
                            output.writeDouble(FIELD_SIDE, sideEntry, true);
                    }
                }
                break;
            case FIELD_RAKE:
                if (message.getRake() != null)
                    output.writeDouble(FIELD_RAKE, message.getRake(), false);
                break;
            default:
                break;
        }
    }

    public String getFieldName(int number) {
        switch(number) {
            case FIELD_POT: return "pot";
            case FIELD_SIDE: return "side";
            case FIELD_RAKE: return "rake";
            default: return null;
        }
    }

    public int getFieldNumber(String name) {
        final Integer number = fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    final java.util.Map<String, Integer> fieldMap = new java.util.HashMap<String, Integer>(); {
        fieldMap.put("pot", FIELD_POT);
        fieldMap.put("side", FIELD_SIDE);
        fieldMap.put("rake", FIELD_RAKE);
    }
}
