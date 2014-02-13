// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from msg.proto

package proto.msg;

import java.io.IOException;

import com.dyuproject.protostuff.ByteString;
import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.UninitializedMessageException;

public class DeclareHandSchema
       implements Schema<de.pokerno.protocol.msg.DeclareHand> {


    static final de.pokerno.protocol.msg.DeclareHand DEFAULT_INSTANCE = new de.pokerno.protocol.msg.DeclareHand();
    static final Schema<de.pokerno.protocol.msg.DeclareHand> SCHEMA = new DeclareHandSchema();

    public static de.pokerno.protocol.msg.DeclareHand getDefaultInstance() { return DEFAULT_INSTANCE; }
    public static Schema<de.pokerno.protocol.msg.DeclareHand> getSchema() { return SCHEMA; }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_POS = 1;
    public static final int FIELD_PLAYER = 2;
    public static final int FIELD_HAND = 3;
    public static final int FIELD_CARDS = 4;

    public DeclareHandSchema() {}


    public de.pokerno.protocol.msg.DeclareHand newMessage() {
        return new de.pokerno.protocol.msg.DeclareHand();
    }

    public Class<de.pokerno.protocol.msg.DeclareHand> typeClass() {
        return de.pokerno.protocol.msg.DeclareHand.class;
    }

    public String messageName() {
        return de.pokerno.protocol.msg.DeclareHand.class.getSimpleName();
    }

    public String messageFullName() {
        return de.pokerno.protocol.msg.DeclareHand.class.getName();
    }


    public boolean isInitialized(de.pokerno.protocol.msg.DeclareHand message) {
        return true;
    }


    public void mergeFrom(Input input, de.pokerno.protocol.msg.DeclareHand message) throws IOException {
        for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
            mergeFrom(input, message, fieldIx);
        }
    }

    public void mergeFrom(Input input, de.pokerno.protocol.msg.DeclareHand message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                return;
            case FIELD_POS:
                message.setPos(input.readInt32());
                break;
            case FIELD_PLAYER:
                message.setPlayer(input.readString());
                break;
            case FIELD_HAND:
                message.setHand(input.mergeObject(message.getHand(), proto.wire.HandSchema.getSchema()));
                break;

            case FIELD_CARDS:
                message.setCards(input.readBytes());
                break;
            default:
                input.handleUnknownField(fieldIx, this);
        }
    }


    private static int[] FIELDS_TO_WRITE = { FIELD_POS, FIELD_PLAYER, FIELD_HAND, FIELD_CARDS };

    public int[] getWriteFields() { return FIELDS_TO_WRITE; }

    public void writeTo(Output output, de.pokerno.protocol.msg.DeclareHand message) throws IOException {
        int[] toWrite = getWriteFields();
        for (int i = 0; i < toWrite.length; i++) {
            writeTo(output, message, toWrite[i]);
        }
    }

    public void writeTo(Output output, de.pokerno.protocol.msg.DeclareHand message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                break;
            case FIELD_POS:
                output.writeInt32(FIELD_POS, message.getPos(), false);
                break;
            case FIELD_PLAYER:
                output.writeString(FIELD_PLAYER, message.getPlayer(), false);
                break;
            case FIELD_HAND:
                output.writeObject(FIELD_HAND, message.getHand(), proto.wire.HandSchema.getSchema(), false);

                break;
            case FIELD_CARDS:
                if (message.getCards() != null)
                    output.writeBytes(FIELD_CARDS, message.getCards(), false);
                break;
            default:
                break;
        }
    }

    public String getFieldName(int number) {
        switch(number) {
            case FIELD_POS: return "pos";
            case FIELD_PLAYER: return "player";
            case FIELD_HAND: return "hand";
            case FIELD_CARDS: return "cards";
            default: return null;
        }
    }

    public int getFieldNumber(String name) {
        final Integer number = fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    final java.util.Map<String, Integer> fieldMap = new java.util.HashMap<String, Integer>(); {
        fieldMap.put("pos", FIELD_POS);
        fieldMap.put("player", FIELD_PLAYER);
        fieldMap.put("hand", FIELD_HAND);
        fieldMap.put("cards", FIELD_CARDS);
    }
}