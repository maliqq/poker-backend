// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from wire.proto

package de.pokerno.protocol.wire;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.UninitializedMessageException;

public class GameSchema
       implements Schema<Game> {

    public enum GameType implements com.dyuproject.protostuff.EnumLite<GameType>
    {
        TEXAS(0),
        OMAHA(1),
        OMAHA_8(2),
        STUD(3),
        STUD_8(4),
        RAZZ(9),
        LONDON(10),
        FIVE_CARD(11),
        SINGLE_27(12),
        TRIPLE_27(13),
        BADUGI(14);
        
        public final int number;
        
        private GameType (int number)
        {
            this.number = number;
        }
        
        public int getNumber()
        {
            return number;
        }
        
        public static GameType valueOf(int number)
        {
            switch(number) 
            {
                case 0: return TEXAS;
                case 1: return OMAHA;
                case 2: return OMAHA_8;
                case 3: return STUD;
                case 4: return STUD_8;
                case 9: return RAZZ;
                case 10: return LONDON;
                case 11: return FIVE_CARD;
                case 12: return SINGLE_27;
                case 13: return TRIPLE_27;
                case 14: return BADUGI;
                default: return null;
            }
        }
    }
    public enum GameLimit implements com.dyuproject.protostuff.EnumLite<GameLimit>
    {
        NL(0),
        PL(1),
        FL(2);
        
        public final int number;
        
        private GameLimit (int number)
        {
            this.number = number;
        }
        
        public int getNumber()
        {
            return number;
        }
        
        public static GameLimit valueOf(int number)
        {
            switch(number) 
            {
                case 0: return NL;
                case 1: return PL;
                case 2: return FL;
                default: return null;
            }
        }
    }


    static final Game DEFAULT_INSTANCE = new Game();
    static final Schema<Game> SCHEMA = new GameSchema();

    public static Game getDefaultInstance() { return DEFAULT_INSTANCE; }
    public static Schema<Game> getSchema() { return SCHEMA; }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_TYPE = 1;
    public static final int FIELD_LIMIT = 2;
    public static final int FIELD_TABLE_SIZE = 3;

    public GameSchema() {}


    public Game newMessage() {
        return new Game();
    }

    public Class<Game> typeClass() {
        return Game.class;
    }

    public String messageName() {
        return Game.class.getSimpleName();
    }

    public String messageFullName() {
        return Game.class.getName();
    }


    public boolean isInitialized(Game message) {
        return true;
    }


    public void mergeFrom(Input input, Game message) throws IOException {
        for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
            mergeFrom(input, message, fieldIx);
        }
    }

    public void mergeFrom(Input input, Game message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                return;
            case FIELD_TYPE:
                message.setType(GameType.valueOf(input.readEnum()));
                break;
            case FIELD_LIMIT:
                message.setLimit(GameLimit.valueOf(input.readEnum()));
                break;
            case FIELD_TABLE_SIZE:
                message.setTableSize(input.readInt32());
                break;
            default:
                input.handleUnknownField(fieldIx, this);
        }
    }


    private static int[] FIELDS_TO_WRITE = { FIELD_TYPE, FIELD_LIMIT, FIELD_TABLE_SIZE };

    public int[] getWriteFields() { return FIELDS_TO_WRITE; }

    public void writeTo(Output output, Game message) throws IOException {
        int[] toWrite = getWriteFields();
        for (int i = 0; i < toWrite.length; i++) {
            writeTo(output, message, toWrite[i]);
        }
    }

    public void writeTo(Output output, Game message, int fieldIx) throws IOException {
        switch (fieldIx) {
            case FIELD_NONE:
                break;
            case FIELD_TYPE:
                output.writeEnum(FIELD_TYPE, message.getType().number, false);    break;
            case FIELD_LIMIT:
                output.writeEnum(FIELD_LIMIT, message.getLimit().number, false);    break;
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
            case FIELD_LIMIT: return "limit";
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
        fieldMap.put("limit", FIELD_LIMIT);
        fieldMap.put("tableSize", FIELD_TABLE_SIZE);
    }
}