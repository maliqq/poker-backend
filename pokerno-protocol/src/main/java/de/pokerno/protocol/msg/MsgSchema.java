// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from msg.proto

package de.pokerno.protocol.msg;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.UninitializedMessageException;

public class MsgSchema
       implements Schema<Msg> {

    public enum MsgType implements com.dyuproject.protostuff.EnumLite<MsgType>
    {
        ERROR(1),
        CHAT(2),
        DEALER(3);
        
        public final int number;
        
        private MsgType (int number)
        {
            this.number = number;
        }
        
        public int getNumber()
        {
            return number;
        }
        
        public static MsgType valueOf(int number)
        {
            switch(number) 
            {
                case 1: return ERROR;
                case 2: return CHAT;
                case 3: return DEALER;
                default: return null;
            }
        }
    }


    static final Msg DEFAULT_INSTANCE = new Msg();
    static final Schema<Msg> SCHEMA = new MsgSchema();

    public static Msg getDefaultInstance() { return DEFAULT_INSTANCE; }
    public static Schema<Msg> getSchema() { return SCHEMA; }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_TYPE = 1;
    public static final int FIELD_BODY = 2;

    public MsgSchema() {}


    public Msg newMessage() {
        return new Msg();
    }

    public Class<Msg> typeClass() {
        return Msg.class;
    }

    public String messageName() {
        return Msg.class.getSimpleName();
    }

    public String messageFullName() {
        return Msg.class.getName();
    }


    public boolean isInitialized(Msg message) {
        return true;
    }


    public void mergeFrom(Input input, Msg message) throws IOException {
        for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
            mergeFrom(input, message, fieldIx);
        }
    }

    public void mergeFrom(Input input, Msg message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                return;
            case FIELD_TYPE:
                message.setType(MsgType.valueOf(input.readEnum()));
                break;
            case FIELD_BODY:
                message.setBody(input.readString());
                break;
            default:
                input.handleUnknownField(fieldIx, this);
        }
    }


    private static int[] FIELDS_TO_WRITE = { FIELD_TYPE, FIELD_BODY };

    public int[] getWriteFields() { return FIELDS_TO_WRITE; }

    public void writeTo(Output output, Msg message) throws IOException {
        int[] toWrite = getWriteFields();
        for (int i = 0; i < toWrite.length; i++) {
            writeTo(output, message, toWrite[i]);
        }
    }

    public void writeTo(Output output, Msg message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                break;
            case FIELD_TYPE:
                output.writeEnum(FIELD_TYPE, message.getType().number, false);    break;
            case FIELD_BODY:
                output.writeString(FIELD_BODY, message.getBody(), false);
                break;
            default:
                break;
        }
    }

    public String getFieldName(int number) {
        switch(number) {
            case FIELD_TYPE: return "type";
            case FIELD_BODY: return "body";
            default: return null;
        }
    }

    public int getFieldNumber(String name) {
        final Integer number = fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    final java.util.Map<String, Integer> fieldMap = new java.util.HashMap<String, Integer>(); {
        fieldMap.put("type", FIELD_TYPE);
        fieldMap.put("body", FIELD_BODY);
    }
}