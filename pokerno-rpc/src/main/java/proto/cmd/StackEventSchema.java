// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from cmd.proto

package proto.cmd;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.UninitializedMessageException;

public class StackEventSchema
       implements Schema<de.pokerno.protocol.cmd.StackEvent> {

    public enum EventType implements com.dyuproject.protostuff.EnumLite<EventType>
    {
        BUYIN(1),
        REBUY(2),
        DOUBLE_REBUY(3),
        ADDON(4);
        
        public final int number;
        
        private EventType (int number)
        {
            this.number = number;
        }
        
        public int getNumber()
        {
            return number;
        }
        
        public static EventType valueOf(int number)
        {
            switch(number) 
            {
                case 1: return BUYIN;
                case 2: return REBUY;
                case 3: return DOUBLE_REBUY;
                case 4: return ADDON;
                default: return null;
            }
        }
    }


    static final de.pokerno.protocol.cmd.StackEvent DEFAULT_INSTANCE = new de.pokerno.protocol.cmd.StackEvent();
    static final Schema<de.pokerno.protocol.cmd.StackEvent> SCHEMA = new StackEventSchema();

    public static de.pokerno.protocol.cmd.StackEvent getDefaultInstance() { return DEFAULT_INSTANCE; }
    public static Schema<de.pokerno.protocol.cmd.StackEvent> getSchema() { return SCHEMA; }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_TYPE = 1;
    public static final int FIELD_PLAYER = 2;
    public static final int FIELD_AMOUNT = 3;

    public StackEventSchema() {}


    public de.pokerno.protocol.cmd.StackEvent newMessage() {
        return new de.pokerno.protocol.cmd.StackEvent();
    }

    public Class<de.pokerno.protocol.cmd.StackEvent> typeClass() {
        return de.pokerno.protocol.cmd.StackEvent.class;
    }

    public String messageName() {
        return de.pokerno.protocol.cmd.StackEvent.class.getSimpleName();
    }

    public String messageFullName() {
        return de.pokerno.protocol.cmd.StackEvent.class.getName();
    }


    public boolean isInitialized(de.pokerno.protocol.cmd.StackEvent message) {
        return true;
    }


    public void mergeFrom(Input input, de.pokerno.protocol.cmd.StackEvent message) throws IOException {
        for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
            mergeFrom(input, message, fieldIx);
        }
    }

    public void mergeFrom(Input input, de.pokerno.protocol.cmd.StackEvent message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                return;
            case FIELD_TYPE:
                message.setType(EventType.valueOf(input.readEnum()));
                break;
            case FIELD_PLAYER:
                message.setPlayer(input.readString());
                break;
            case FIELD_AMOUNT:
                message.setAmount(input.readDouble());
                break;
            default:
                input.handleUnknownField(fieldIx, this);
        }
    }


    private static int[] FIELDS_TO_WRITE = { FIELD_TYPE, FIELD_PLAYER, FIELD_AMOUNT };

    public int[] getWriteFields() { return FIELDS_TO_WRITE; }

    public void writeTo(Output output, de.pokerno.protocol.cmd.StackEvent message) throws IOException {
        int[] toWrite = getWriteFields();
        for (int i = 0; i < toWrite.length; i++) {
            writeTo(output, message, toWrite[i]);
        }
    }

    public void writeTo(Output output, de.pokerno.protocol.cmd.StackEvent message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                break;
            case FIELD_TYPE:
                output.writeEnum(FIELD_TYPE, message.getType().number, false);    break;
            case FIELD_PLAYER:
                output.writeString(FIELD_PLAYER, message.getPlayer(), false);
                break;
            case FIELD_AMOUNT:
                output.writeDouble(FIELD_AMOUNT, message.getAmount(), false);
                break;
            default:
                break;
        }
    }

    public String getFieldName(int number) {
        switch(number) {
            case FIELD_TYPE: return "type";
            case FIELD_PLAYER: return "player";
            case FIELD_AMOUNT: return "amount";
            default: return null;
        }
    }

    public int getFieldNumber(String name) {
        final Integer number = fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    final java.util.Map<String, Integer> fieldMap = new java.util.HashMap<String, Integer>(); {
        fieldMap.put("type", FIELD_TYPE);
        fieldMap.put("player", FIELD_PLAYER);
        fieldMap.put("amount", FIELD_AMOUNT);
    }
}