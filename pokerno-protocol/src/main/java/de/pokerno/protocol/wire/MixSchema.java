// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from wire.proto

package de.pokerno.protocol.wire;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.UninitializedMessageException;

public class MixSchema
       implements Schema<Mix> {

    public enum MixType implements com.dyuproject.protostuff.EnumLite<MixType>
    {
        HORSE(1),
        EIGHT_GAME(2);
        
        public final int number;
        
        private MixType (int number)
        {
            this.number = number;
        }
        
        public int getNumber()
        {
            return number;
        }
        
        public static MixType valueOf(int number)
        {
            switch(number) 
            {
                case 1: return HORSE;
                case 2: return EIGHT_GAME;
                default: return null;
            }
        }
    }


    static final Mix DEFAULT_INSTANCE = new Mix();
    static final Schema<Mix> SCHEMA = new MixSchema();

    public static Mix getDefaultInstance() { return DEFAULT_INSTANCE; }
    public static Schema<Mix> getSchema() { return SCHEMA; }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_TYPE = 1;
    public static final int FIELD_TABLE_SIZE = 2;

    public MixSchema() {}


    public Mix newMessage() {
        return new Mix();
    }

    public Class<Mix> typeClass() {
        return Mix.class;
    }

    public String messageName() {
        return Mix.class.getSimpleName();
    }

    public String messageFullName() {
        return Mix.class.getName();
    }


    public boolean isInitialized(Mix message) {
        return true;
    }


    public void mergeFrom(Input input, Mix message) throws IOException {
        for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
            mergeFrom(input, message, fieldIx);
        }
    }

    public void mergeFrom(Input input, Mix message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                return;
            case FIELD_TYPE:
                message.setType(MixType.valueOf(input.readEnum()));
                break;
            case FIELD_TABLE_SIZE:
                message.setTableSize(input.readInt32());
                break;
            default:
                input.handleUnknownField(fieldIx, this);
        }
    }


    private static int[] FIELDS_TO_WRITE = { FIELD_TYPE, FIELD_TABLE_SIZE };

    public int[] getWriteFields() { return FIELDS_TO_WRITE; }

    public void writeTo(Output output, Mix message) throws IOException {
        int[] toWrite = getWriteFields();
        for (int i = 0; i < toWrite.length; i++) {
            writeTo(output, message, toWrite[i]);
        }
    }

    public void writeTo(Output output, Mix message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                break;
            case FIELD_TYPE:
                output.writeEnum(FIELD_TYPE, message.getType().number, false);    break;
            case FIELD_TABLE_SIZE:
                output.writeInt32(FIELD_TABLE_SIZE, message.getTableSize(), false);
                break;
            default:
                break;
        }
    }

    public String getFieldName(int number) {
        switch(number) {
            case FIELD_TYPE: return "type";
            case FIELD_TABLE_SIZE: return "tableSize";
            default: return null;
        }
    }

    public int getFieldNumber(String name) {
        final Integer number = fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    final java.util.Map<String, Integer> fieldMap = new java.util.HashMap<String, Integer>(); {
        fieldMap.put("type", FIELD_TYPE);
        fieldMap.put("tableSize", FIELD_TABLE_SIZE);
    }
}
