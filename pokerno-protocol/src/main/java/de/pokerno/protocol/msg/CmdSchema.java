// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from msg.proto

package de.pokerno.protocol.msg;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.UninitializedMessageException;

public class CmdSchema
       implements Schema<Cmd> {

    public enum CmdType implements com.dyuproject.protostuff.EnumLite<CmdType>
    {
        JOIN_TABLE(1),
        LEAVE_TABLE(2),
        SIT_OUT(3),
        COME_BACK(4),
        ACTION(5);
        
        public final int number;
        
        private CmdType (int number)
        {
            this.number = number;
        }
        
        public int getNumber()
        {
            return number;
        }
        
        public static CmdType valueOf(int number)
        {
            switch(number) 
            {
                case 1: return JOIN_TABLE;
                case 2: return LEAVE_TABLE;
                case 3: return SIT_OUT;
                case 4: return COME_BACK;
                case 5: return ACTION;
                default: return null;
            }
        }
    }


    static final Cmd DEFAULT_INSTANCE = new Cmd();
    static final Schema<Cmd> SCHEMA = new CmdSchema();

    public static Cmd getDefaultInstance() { return DEFAULT_INSTANCE; }
    public static Schema<Cmd> getSchema() { return SCHEMA; }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_TYPE = 1;
    public static final int FIELD_JOIN_TABLE = 2;
    public static final int FIELD_ACTION_EVENT = 3;

    public CmdSchema() {}


    public Cmd newMessage() {
        return new Cmd();
    }

    public Class<Cmd> typeClass() {
        return Cmd.class;
    }

    public String messageName() {
        return Cmd.class.getSimpleName();
    }

    public String messageFullName() {
        return Cmd.class.getName();
    }


    public boolean isInitialized(Cmd message) {
        return true;
    }


    public void mergeFrom(Input input, Cmd message) throws IOException {
        for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
            mergeFrom(input, message, fieldIx);
        }
    }

    public void mergeFrom(Input input, Cmd message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                return;
            case FIELD_TYPE:
                message.setType(CmdType.valueOf(input.readEnum()));
                break;
            case FIELD_JOIN_TABLE:
                message.setJoinTable(input.mergeObject(message.getJoinTable(), JoinTableSchema.getSchema()));
                break;

            case FIELD_ACTION_EVENT:
                message.setActionEvent(input.mergeObject(message.getActionEvent(), ActionEventSchema.getSchema()));
                break;

            default:
                input.handleUnknownField(fieldIx, this);
        }
    }


    private static int[] FIELDS_TO_WRITE = { FIELD_TYPE, FIELD_JOIN_TABLE, FIELD_ACTION_EVENT };

    public int[] getWriteFields() { return FIELDS_TO_WRITE; }

    public void writeTo(Output output, Cmd message) throws IOException {
        int[] toWrite = getWriteFields();
        for (int i = 0; i < toWrite.length; i++) {
            writeTo(output, message, toWrite[i]);
        }
    }

    public void writeTo(Output output, Cmd message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                break;
            case FIELD_TYPE:
                output.writeEnum(FIELD_TYPE, message.getType().number, false);    break;
            case FIELD_JOIN_TABLE:
                if (message.getJoinTable() != null)
                    output.writeObject(FIELD_JOIN_TABLE, message.getJoinTable(), JoinTableSchema.getSchema(), false);

                break;
            case FIELD_ACTION_EVENT:
                if (message.getActionEvent() != null)
                    output.writeObject(FIELD_ACTION_EVENT, message.getActionEvent(), ActionEventSchema.getSchema(), false);

                break;
            default:
                break;
        }
    }

    public String getFieldName(int number) {
        switch(number) {
            case FIELD_TYPE: return "type";
            case FIELD_JOIN_TABLE: return "joinTable";
            case FIELD_ACTION_EVENT: return "actionEvent";
            default: return null;
        }
    }

    public int getFieldNumber(String name) {
        final Integer number = fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    final java.util.Map<String, Integer> fieldMap = new java.util.HashMap<String, Integer>(); {
        fieldMap.put("type", FIELD_TYPE);
        fieldMap.put("joinTable", FIELD_JOIN_TABLE);
        fieldMap.put("actionEvent", FIELD_ACTION_EVENT);
    }
}