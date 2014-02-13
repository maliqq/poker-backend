// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from wire.proto

package proto.wire;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.UninitializedMessageException;

public class BetSchema
       implements Schema<de.pokerno.protocol.wire.Bet> {

    public enum BetType implements com.dyuproject.protostuff.EnumLite<BetType>
    {
        ANTE(1),
        BRING_IN(2),
        SB(3),
        BB(4),
        GUEST_BLIND(5),
        STRADDLE(6),
        RAISE(7),
        CALL(8),
        CHECK(9),
        FOLD(10),
        ALLIN(11);
        
        public final int number;
        
        private BetType (int number)
        {
            this.number = number;
        }
        
        public int getNumber()
        {
            return number;
        }
        
        public static BetType valueOf(int number)
        {
            switch(number) 
            {
                case 1: return ANTE;
                case 2: return BRING_IN;
                case 3: return SB;
                case 4: return BB;
                case 5: return GUEST_BLIND;
                case 6: return STRADDLE;
                case 7: return RAISE;
                case 8: return CALL;
                case 9: return CHECK;
                case 10: return FOLD;
                case 11: return ALLIN;
                default: return null;
            }
        }
    }


    static final de.pokerno.protocol.wire.Bet DEFAULT_INSTANCE = new de.pokerno.protocol.wire.Bet();
    static final Schema<de.pokerno.protocol.wire.Bet> SCHEMA = new BetSchema();

    public static de.pokerno.protocol.wire.Bet getDefaultInstance() { return DEFAULT_INSTANCE; }
    public static Schema<de.pokerno.protocol.wire.Bet> getSchema() { return SCHEMA; }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_TYPE = 1;
    public static final int FIELD_AMOUNT = 2;

    public BetSchema() {}


    public de.pokerno.protocol.wire.Bet newMessage() {
        return new de.pokerno.protocol.wire.Bet();
    }

    public Class<de.pokerno.protocol.wire.Bet> typeClass() {
        return de.pokerno.protocol.wire.Bet.class;
    }

    public String messageName() {
        return de.pokerno.protocol.wire.Bet.class.getSimpleName();
    }

    public String messageFullName() {
        return de.pokerno.protocol.wire.Bet.class.getName();
    }


    public boolean isInitialized(de.pokerno.protocol.wire.Bet message) {
        return true;
    }


    public void mergeFrom(Input input, de.pokerno.protocol.wire.Bet message) throws IOException {
        for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
            mergeFrom(input, message, fieldIx);
        }
    }

    public void mergeFrom(Input input, de.pokerno.protocol.wire.Bet message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                return;
            case FIELD_TYPE:
                message.setType(BetType.valueOf(input.readEnum()));
                break;
            case FIELD_AMOUNT:
                message.setAmount(input.readDouble());
                break;
            default:
                input.handleUnknownField(fieldIx, this);
        }
    }


    private static int[] FIELDS_TO_WRITE = { FIELD_TYPE, FIELD_AMOUNT };

    public int[] getWriteFields() { return FIELDS_TO_WRITE; }

    public void writeTo(Output output, de.pokerno.protocol.wire.Bet message) throws IOException {
        int[] toWrite = getWriteFields();
        for (int i = 0; i < toWrite.length; i++) {
            writeTo(output, message, toWrite[i]);
        }
    }

    public void writeTo(Output output, de.pokerno.protocol.wire.Bet message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                break;
            case FIELD_TYPE:
                output.writeEnum(FIELD_TYPE, message.getType().number, false);    break;
            case FIELD_AMOUNT:
                if (message.getAmount() != null)
                    output.writeDouble(FIELD_AMOUNT, message.getAmount(), false);
                break;
            default:
                break;
        }
    }

    public String getFieldName(int number) {
        switch(number) {
            case FIELD_TYPE: return "type";
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
        fieldMap.put("amount", FIELD_AMOUNT);
    }
}