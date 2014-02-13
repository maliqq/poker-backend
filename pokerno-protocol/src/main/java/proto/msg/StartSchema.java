// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from msg.proto

package proto.msg;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.UninitializedMessageException;

public class StartSchema
       implements Schema<de.pokerno.protocol.msg.Start> {


    static final de.pokerno.protocol.msg.Start DEFAULT_INSTANCE = new de.pokerno.protocol.msg.Start();
    static final Schema<de.pokerno.protocol.msg.Start> SCHEMA = new StartSchema();

    public static de.pokerno.protocol.msg.Start getDefaultInstance() { return DEFAULT_INSTANCE; }
    public static Schema<de.pokerno.protocol.msg.Start> getSchema() { return SCHEMA; }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_TABLE = 1;
    public static final int FIELD_VARIATION = 2;
    public static final int FIELD_STAKE = 3;
    public static final int FIELD_PLAY = 4;

    public StartSchema() {}


    public de.pokerno.protocol.msg.Start newMessage() {
        return new de.pokerno.protocol.msg.Start();
    }

    public Class<de.pokerno.protocol.msg.Start> typeClass() {
        return de.pokerno.protocol.msg.Start.class;
    }

    public String messageName() {
        return de.pokerno.protocol.msg.Start.class.getSimpleName();
    }

    public String messageFullName() {
        return de.pokerno.protocol.msg.Start.class.getName();
    }


    public boolean isInitialized(de.pokerno.protocol.msg.Start message) {
        return true;
    }


    public void mergeFrom(Input input, de.pokerno.protocol.msg.Start message) throws IOException {
        for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
            mergeFrom(input, message, fieldIx);
        }
    }

    public void mergeFrom(Input input, de.pokerno.protocol.msg.Start message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                return;
            case FIELD_TABLE:
                message.setTable(input.mergeObject(message.getTable(), proto.wire.TableSchema.getSchema()));
                break;

            case FIELD_VARIATION:
                message.setVariation(input.mergeObject(message.getVariation(), proto.wire.VariationSchema.getSchema()));
                break;

            case FIELD_STAKE:
                message.setStake(input.mergeObject(message.getStake(), proto.wire.StakeSchema.getSchema()));
                break;

            case FIELD_PLAY:
                message.setPlay(input.mergeObject(message.getPlay(), PlaySchema.getSchema()));
                break;

            default:
                input.handleUnknownField(fieldIx, this);
        }
    }


    private static int[] FIELDS_TO_WRITE = { FIELD_TABLE, FIELD_VARIATION, FIELD_STAKE, FIELD_PLAY };

    public int[] getWriteFields() { return FIELDS_TO_WRITE; }

    public void writeTo(Output output, de.pokerno.protocol.msg.Start message) throws IOException {
        int[] toWrite = getWriteFields();
        for (int i = 0; i < toWrite.length; i++) {
            writeTo(output, message, toWrite[i]);
        }
    }

    public void writeTo(Output output, de.pokerno.protocol.msg.Start message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                break;
            case FIELD_TABLE:
                output.writeObject(FIELD_TABLE, message.getTable(), proto.wire.TableSchema.getSchema(), false);

                break;
            case FIELD_VARIATION:
                output.writeObject(FIELD_VARIATION, message.getVariation(), proto.wire.VariationSchema.getSchema(), false);

                break;
            case FIELD_STAKE:
                output.writeObject(FIELD_STAKE, message.getStake(), proto.wire.StakeSchema.getSchema(), false);

                break;
            case FIELD_PLAY:
                output.writeObject(FIELD_PLAY, message.getPlay(), PlaySchema.getSchema(), false);

                break;
            default:
                break;
        }
    }

    public String getFieldName(int number) {
        switch(number) {
            case FIELD_TABLE: return "table";
            case FIELD_VARIATION: return "variation";
            case FIELD_STAKE: return "stake";
            case FIELD_PLAY: return "play";
            default: return null;
        }
    }

    public int getFieldNumber(String name) {
        final Integer number = fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    final java.util.Map<String, Integer> fieldMap = new java.util.HashMap<String, Integer>(); {
        fieldMap.put("table", FIELD_TABLE);
        fieldMap.put("variation", FIELD_VARIATION);
        fieldMap.put("stake", FIELD_STAKE);
        fieldMap.put("play", FIELD_PLAY);
    }
}