/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.eclipse.parsson;

import jakarta.json.*;
import jakarta.json.stream.JsonGenerationException;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.parsson.api.BufferPool;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * @author Jitendra Kotamraju
 */
class JsonGeneratorImpl implements JsonGenerator {

    private static final char[] INT_MIN_VALUE_CHARS = "-2147483648".toCharArray();
    private static final int[] INT_CHARS_SIZE_TABLE = { 9, 99, 999, 9999, 99999,
            999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

    private static final char [] DIGIT_TENS = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    } ;

    private static final char [] DIGIT_ONES = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    } ;

    /**
     * All possible chars for representing a number as a String
     */
    private static final char[] DIGITS = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9'
    };
    private static final char QUOTE = '"';
    private static final char COMMA = ',';
    private static final char CLOSE_ARRAY = ']';
    private static final char CLOSE_OBJECT = '}';
    private static final char COLON = ':';
    private static final char OPEN_ARRAY = '[';
    private static final char OPEN_OBJECT = '{';

    private enum Scope {
        IN_NONE,
        IN_OBJECT,
        //IN_FIELD,
        IN_ARRAY
    }

    /**
     * A key that is already escaped so then can just be written directly.
     */
    static class PreparedKey implements JsonGenerator.Key {

        private final char[] escapedName;

        PreparedKey(String escapedName) {
            this.escapedName = escapedName.toCharArray();
        }

        @Override
        public char[] toCharArray() {
            return escapedName;
        }
    }

    private static final int[] _outputEscapes = CharTypes.get7BitOutputEscapes();
    private final BufferPool bufferPool;
    private final Writer writer;
    //private Context currentContext = new Context(Scope.IN_NONE);
    private Scope currentScope = Scope.IN_NONE;
    private boolean notEmpty;
    private boolean startArray;
    private boolean startObject;
    private boolean startKey;
    private Deque<Scope> stack;// = new ArrayDeque<>();

    // Using own buffering mechanism as JDK's BufferedWriter uses synchronized
    // methods. Also, flushBuffer() is useful when you don't want to actually
    // flush the underlying output source
    private final char buf[];     // capacity >= INT_MIN_VALUE_CHARS.length
    private int len = 0;
    private final int capacity;

    JsonGeneratorImpl(Writer writer, BufferPool bufferPool) {
        this.writer = writer;
        this.bufferPool = bufferPool;
        this.buf = bufferPool.take();
        this.capacity = buf.length;
    }

    JsonGeneratorImpl(OutputStream out, BufferPool bufferPool) {
        this(out, StandardCharsets.UTF_8, bufferPool);
    }

    JsonGeneratorImpl(OutputStream out, Charset encoding, BufferPool bufferPool) {
        this(new OutputStreamWriter(out, encoding), bufferPool);
    }

    static Key key(String name) {
        return new PreparedKey(EscapeKey.quoteEscape(name));
    }

    @Override
    public void flush() {
        flushBuffer();
        try {
            writer.flush();
        } catch (IOException ioe) {
            throw new JsonException(JsonMessages.GENERATOR_FLUSH_IO_ERR(), ioe);
        }
    }

    @Override
    public JsonGenerator writeStartObject() {
        if (startObject && currentScope == Scope.IN_OBJECT) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
        }
        if (currentScope == Scope.IN_NONE && notEmpty) { //!currentContext.first
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_MULTIPLE_TEXT());
        }
        writeArrayComma();
        return pushNewObject();
    }

    @Override
    public JsonGenerator writeStartObject(String name) {
        if (currentScope != Scope.IN_OBJECT) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
        }
        writeName(name);
        return pushNewObject();
    }

    private JsonGenerator pushNewObject() {
        writeChar(OPEN_OBJECT);
        writeAfterObjectStart();
        if (currentScope != Scope.IN_NONE) {
            pushScope();
        }
        currentScope = Scope.IN_OBJECT;
        startObject = true;
        startKey = false;
        return this;
    }

    private void pushScope() {
        if (stack == null) {
            stack = new ArrayDeque<>();
        }
        stack.push(currentScope);
    }

    private JsonGenerator writeName(String name) {
        writeNameComma();
        writeEscapedString(name);
        writeColon();
        return this;
    }

    private JsonGenerator writeName(Key name) {
        writeNameComma();
        final char[] escaped = name.toCharArray();
        final int required = escaped.length;
        if (len + required >= capacity) {
            flushBuffer();
        }
        System.arraycopy(escaped, 0, buf, len, required);
        len += required;
        writeColon();
        return this;
    }

    @Override
    public JsonGenerator write(String name, String value) {
        writeKey(name);
        return write(value);
//        write(name, (CharSequence) value);
//        return this;
    }

    @Override
    public JsonGenerator write(String name, int value) {
        writeKey(name);
        return write(value);
////        startKey = false;
////        writeInt(value);
////        return this;
//
//        if (currentScope != Scope.IN_OBJECT) {
//            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
//        }
//        writeName(name);
//        startKey = false;
//        writeInt(value);
//        return this;
    }

    @Override
    public JsonGenerator write(String name, long value) {
        writeKey(name);
        return write(value);
//        if (currentScope != Scope.IN_OBJECT) {
//            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
//        }
//        writeName(name);
//        startKey = false;
//        writeString(String.valueOf(value));
//        return this;
    }

    @Override
    public JsonGenerator write(String name, double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new NumberFormatException(JsonMessages.GENERATOR_DOUBLE_INFINITE_NAN());
        }
        writeKey(name);
        return write(value);
//        if (currentScope != Scope.IN_OBJECT) {
//            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
//        }
//        writeName(name);
//        startKey = false;
//        writeString(String.valueOf(value));
//        return this;
    }

    @Override
    public JsonGenerator write(String name, BigInteger value) {
        writeKey(name);
        return write(value);
//        if (currentScope != Scope.IN_OBJECT) {
//            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
//        }
//        writeName(name);
//        startKey = false;
//        writeString(String.valueOf(value));
//        return this;
    }

    @Override
    public JsonGenerator write(String name, BigDecimal value) {
        writeKey(name);
        return write(value);
//        if (currentScope != Scope.IN_OBJECT) {
//            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
//        }
//        writeName(name);
//        startKey = false;
//        writeString(String.valueOf(value));
//        return this;
    }

    @Override
    public JsonGenerator write(String name, boolean value) {
        writeKey(name);
        return write(value);
//        if (currentScope != Scope.IN_OBJECT) {
//            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
//        }
//        writeName(name);
//        startKey = false;
//        writeString(value? "true" : "false");
//        return this;
    }

    @Override
    public JsonGenerator writeNull(String name) {
        if (currentScope != Scope.IN_OBJECT) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
        }
        writeName(name);
        startKey = false;
        writeString("null");
        return this;
    }

    @Override
    public JsonGenerator write(JsonValue value) {
        switch (value.getValueType()) {
            case ARRAY:
                JsonArray array = (JsonArray)value;
                writeStartArray();
                for(JsonValue child: array) {
                    write(child);
                }
                writeEnd();
                break;
            case OBJECT:
                JsonObject object = (JsonObject)value;
                writeStartObject();
                for(Map.Entry<String, JsonValue> member: object.entrySet()) {
                    write(member.getKey(), member.getValue());
                }
                writeEnd();
                break;
            case STRING:
                JsonString str = (JsonString)value;
                write(str.getString());
                break;
            case NUMBER:
                JsonNumber number = (JsonNumber)value;
                writeValue(number.toString());
                //popFieldContext();
                break;
            case TRUE:
                write(true);
                break;
            case FALSE:
                write(false);
                break;
            case NULL:
                writeNull();
                break;
        }

        return this;
    }

    @Override
    public JsonGenerator writeStartArray() {
        if (currentScope == Scope.IN_OBJECT && startObject) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
        }
        if (currentScope == Scope.IN_NONE && notEmpty) { //currentContext.first) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_MULTIPLE_TEXT());
        }
        writeArrayComma();
        return pushNewArray();
    }

    @Override
    public JsonGenerator writeStartArray(String name) {
//        if (currentScope != Scope.IN_OBJECT) {
//            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
//        }
//        writeName(name);
        writeKey(name);
        return writeStartArray();
        //return pushNewArray();
    }

    private JsonGenerator pushNewArray() {
        writeChar(OPEN_ARRAY);
        writeAfterArrayStart();
        if (currentScope != Scope.IN_NONE) {
            pushScope();
        }
        currentScope = Scope.IN_ARRAY;
        startArray = true;
        startKey = false;
        return this;
    }

    @Override
    public JsonGenerator write(String name, JsonValue value) {
        if (currentScope != Scope.IN_OBJECT) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
        }
        switch (value.getValueType()) {
            case ARRAY:
                JsonArray array = (JsonArray)value;
                writeStartArray(name);
                for(JsonValue child: array) {
                    write(child);
                }
                writeEnd();
                break;
            case OBJECT:
                JsonObject object = (JsonObject)value;
                writeStartObject(name);
                for(Map.Entry<String, JsonValue> member: object.entrySet()) {
                    write(member.getKey(), member.getValue());
                }
                writeEnd();
                break;
            case STRING:
                JsonString str = (JsonString)value;
                write(name, str.getChars());
                break;
            case NUMBER:
                JsonNumber number = (JsonNumber)value;
                writeValue(name, number.toString());
                break;
            case TRUE:
                write(name, true);
                break;
            case FALSE:
                write(name, false);
                break;
            case NULL:
                writeNull(name);
                break;
        }
        return this;
    }

    @Override
    public JsonGenerator write(String value) {
        //checkContextForValue();
        writeArrayComma();
        writeEscapedString(value);
        //popFieldContext();
        return this;
    }


    @Override
    public JsonGenerator write(int value) {
        //checkContextForValue();
        writeArrayComma();
        writeInt(value);
        //popFieldContext();
        return this;
    }

    @Override
    public JsonGenerator write(long value) {
        //checkContextForValue();
        writeValue(String.valueOf(value));
        //popFieldContext();
        return this;
    }

    @Override
    public JsonGenerator write(double value) {
        //checkContextForValue();
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new NumberFormatException(JsonMessages.GENERATOR_DOUBLE_INFINITE_NAN());
        }
        writeValue(String.valueOf(value));
        //popFieldContext();
        return this;
    }

    @Override
    public JsonGenerator write(BigInteger value) {
        //checkContextForValue();
        writeValue(value.toString());
        //popFieldContext();
        return this;
    }

//    private void checkContextForValue() {
//        //if ((!currentContext.first && currentScope != Scope.IN_ARRAY ) // && currentScope != Scope.IN_FIELD
//        //        || (currentContext.first && currentScope == Scope.IN_OBJECT)) {
//        if (currentContext.first && currentScope == Scope.IN_OBJECT) {
//            throw new JsonGenerationException(
//                    JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
//        }
//    }

    @Override
    public JsonGenerator write(BigDecimal value) {
        //checkContextForValue();
        writeValue(value.toString());
        //popFieldContext();
        return this;
    }

//    private void popFieldContext() {
//        if (currentScope == Scope.IN_FIELD) {
//            currentContext = stack.pop();
//        }
//    }

    @Override
    public JsonGenerator write(boolean value) {
        //checkContextForValue();
        writeArrayComma();
        writeString(value ? "true" : "false");
        //popFieldContext();
        return this;
    }

    @Override
    public JsonGenerator writeNull() {
        //checkContextForValue();
        writeArrayComma();
        writeString("null");
        //popFieldContext();
        return this;
    }

    private void writeValue(String value) {
        writeArrayComma();
        writeString(value);
    }

    private void writeValue(String name, String value) {
        writeNameComma();
        writeEscapedString(name);
        writeColon();
        writeString(value);
        startKey = false;
    }

    @Override
    public JsonGenerator writeKey(String name) {
        if (currentScope != Scope.IN_OBJECT) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
        }
        writeName(name);
        //stack.push(currentContext);
        //currentContext = new Context(Scope.IN_FIELD);
        //startObject = false;
        //currentContext.first = false;
        return this;
    }

    @Override
    public JsonGenerator writeKey(JsonGenerator.Key name) {
        if (currentScope != Scope.IN_OBJECT) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
        }
        writeName(name);
        //stack.push(currentContext);
        //currentContext = new Context(Scope.IN_FIELD);
        //startObject = false;
        //currentContext.first = false;
        return this;
    }

    @Override
    public JsonGenerator writeEnd() {
        if (currentScope == Scope.IN_NONE) {
            throw new JsonGenerationException("writeEnd() cannot be called in no context");
        }
        if (currentScope == Scope.IN_ARRAY) {
            writeChar(CLOSE_ARRAY);
            notEmpty = true;
            startArray = false;
        } else {
            writeChar(CLOSE_OBJECT);
            notEmpty = true;
            startObject = false;
        }
        currentScope = stack == null || stack.isEmpty() ? Scope.IN_NONE : stack.pop();
        //currentScope = stack.pop();
        //popFieldContext();
        return this;
    }

    void write(String name, CharSequence fieldValue) {
      if (currentScope != Scope.IN_OBJECT) {
          throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
      }
      writeName(name);
      startKey = false;
      writeEscapedString(fieldValue);
    }

//    protected void writeComma() {
//        if (isCommaAllowed()) {
//            writeChar(',');
//        }
//        currentContext.first = false;
//    }

    private void writeArrayComma() {
        startKey = false;
        if (startObject) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
        }
        if (startArray) {
            if (currentScope == Scope.IN_OBJECT) {
                throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
            }
            startArray = false;
        } else if (currentScope == Scope.IN_ARRAY) {
            writeChar(COMMA);
            writeAfterComma();
        } else {
            notEmpty = true;
        }
    }

    protected void writeAfterObjectStart() {
        // do nothing
    }

    protected void writeAfterArrayStart() {
        // do nothing
    }

    protected void writeAfterComma() {
        // do nothing
    }

    private void writeNameComma() {
        if (startKey) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_ILLEGAL_METHOD(currentScope));
        }
        if (startObject) {
            startObject = false;
        } else { // && currentScope == Scope.IN_OBJECT) { //currentContext.first
            writeChar(COMMA);
            writeAfterComma();
        }
//        if (isCommaAllowed()) {
//            //writeChar(',');
//            buf[len++] = ',';
//        }
        //currentContext.first = false;
    }

    protected boolean inNone() {
        return currentScope == Scope.IN_NONE;
    }

//    boolean isCommaAllowed() {
//        return !currentContext.first && currentScope == Scope.IN_ARRAY;// && currentScope != Scope.IN_FIELD;
//    }

    protected void writeColon() {
        writeChar(COLON);
        startKey = true;
    }

//    private static class Context {
//        //boolean first = true;
//        final Scope scope;
//        Context(Scope scope) {
//            this.scope = scope;
//        }
//    }

    @Override
    public void close() {
        if (startArray || startObject || (!notEmpty && currentScope == Scope.IN_NONE)) {
            throw new JsonGenerationException(JsonMessages.GENERATOR_INCOMPLETE_JSON());
        }
        flushBuffer();
        try {
            writer.close();
        } catch (IOException ioe) {
            throw new JsonException(JsonMessages.GENERATOR_CLOSE_IO_ERR(), ioe);
        }
        bufferPool.recycle(buf);
    }

    // begin, end-1 indexes represent characters that need not
    // be escaped
    //
    // XXXssssssssssssXXXXXXXXXXXXXXXXXXXXXXrrrrrrrrrrrrrrXXXXXX
    //    ^           ^                     ^             ^
    //    |           |                     |             |
    //   begin       end                   begin         end
    private void writeEscapedString(CharSequence string) {
        writeChar('"');
        int len = string.length();
        for(int i = 0; i < len; i++) {
            int begin = i, end = i;
            char c = string.charAt(i);
            // find all the characters that need not be escaped
            // unescaped = %x20-21 | %x23-5B | %x5D-10FFFF
            while(c >= 0x20 && c <= 0x10ffff && c != 0x22 && c != 0x5c) {
                i++; end = i;
                if (i < len) {
                    c = string.charAt(i);
                } else {
                    break;
                }
            }
            // Write characters without escaping
            if (begin < end) {
                writeString(string, begin, end);
                if (i == len) {
                    break;
                }
            }

            switch (c) {
                case '"':
                case '\\':
                    writeChar('\\'); writeChar(c);
                    break;
                case '\b':
                    writeChar('\\'); writeChar('b');
                    break;
                case '\f':
                    writeChar('\\'); writeChar('f');
                    break;
                case '\n':
                    writeChar('\\'); writeChar('n');
                    break;
                case '\r':
                    writeChar('\\'); writeChar('r');
                    break;
                case '\t':
                    writeChar('\\'); writeChar('t');
                    break;
                default:
                    String hex = "000" + Integer.toHexString(c);
                    writeString("\\u" + hex.substring(hex.length() - 4));
            }
        }
        writeChar('"');
    }

    void writeString(CharSequence str, int begin, int end) {
        while (begin < end) {       // source begin and end indexes
            int no = Math.min(capacity - len, end - begin);
            if (str instanceof String) {
              ((String)str).getChars(begin, begin + no, buf, len);
            } else {
              // if passed a non-string, assume this is deliberate
              getChars(str, begin, begin + no, buf, len);
            }
            begin += no;            // Increment source index
            len += no;              // Increment dest index
            if (len >= capacity) {
                flushBuffer();
            }
        }
    }

    void writeEscapedString(String string) {
        writeChar('"');
        int len = string.length();
        for(int i = 0; i < len; i++) {
            int begin = i, end = i;
            char c = string.charAt(i);
            // find all the characters that need not be escaped
            // unescaped = %x20-21 | %x23-5B | %x5D-10FFFF
            while(c >= 0x20 && c != 0x22 && c != 0x5c) { // && c <= 0x10ffff
                i++; end = i;
                if (i < len) {
                    c = string.charAt(i);
                } else {
                    break;
                }
            }
            // Write characters without escaping
            if (begin < end) {
                writeStringF(string, begin, end);
                if (i == len) {
                    break;
                }
            }

            switch (c) {
                case '"':
                case '\\':
                    writeChar('\\'); writeChar(c);
                    break;
                case '\b':
                    writeChar('\\'); writeChar('b');
                    break;
                case '\f':
                    writeChar('\\'); writeChar('f');
                    break;
                case '\n':
                    writeChar('\\'); writeChar('n');
                    break;
                case '\r':
                    writeChar('\\'); writeChar('r');
                    break;
                case '\t':
                    writeChar('\\'); writeChar('t');
                    break;
                default:
                    String hex = "000" + Integer.toHexString(c);
                    writeString("\\u" + hex.substring(hex.length() - 4));
            }
        }
        writeChar('"');
    }

    void writeString(CharSequence str) {
        writeString(str, 0, str.length());
    }

    void writeStringF(String text, int begin, int end) {
        int requiredLen = end - begin;
        if (requiredLen > capacity) {
            this.writeLongString(text, begin, end);
        } else {
            if (len + requiredLen > capacity) {
                flushBuffer();
            }
            // text.getChars(0, text.length(), buf, len);
            text.getChars(begin, end, buf, len);
            len += requiredLen;//text.length();
        }
    }

    private void writeLongString(String text, int begin, int end) {
        while (begin < end) {       // source begin and end indexes
            int no = Math.min(capacity - len, end - begin);
            text.getChars(begin, begin + no, buf, len);
            begin += no;            // Increment source index
            len += no;              // Increment dest index
            if (len >= capacity) {
                flushBuffer();
            }
        }
    }

//    private void _writeLongString(String text) throws IOException {
//        // First things first: let's flush the buffer to get some more room
//        flushBuffer();
//        // Then we can write
//        final int textLen = text.length();
//        int offset = 0;
//        do {
//            int max = _outputEnd;
//            int segmentLen = ((offset + max) > textLen)
//                    ? (textLen - offset) : max;
//            text.getChars(offset, offset+segmentLen, _outputBuffer, 0);
//            if (_characterEscapes != null) {
//                _writeSegmentCustom(segmentLen);
//            } else if (_maximumNonEscapedChar != 0) {
//                _writeSegmentASCII(segmentLen, _maximumNonEscapedChar);
//            } else {
//                _writeSegment(segmentLen);
//            }
//            offset += segmentLen;
//        } while (offset < textLen);
//    }

//    private void writeEscapedString(String text) {
//        int textLen = text.length();
//        if (textLen > capacity) {
//            writeLongString(text);
//        } else {
//            if (len + textLen > capacity) {
//                flushBuffer();
//            }
//            buf[len++] = QUOTE;
//            text.getChars(0, textLen, buf, len);
//            _writeString2(textLen);
//            if (len + textLen > capacity) {
//                flushBuffer();
//            }
//            buf[len++] = QUOTE;
//        }
//    }
//
//    private void _writeString2(final int textLen) {
//        // And then we'll need to verify need for escaping etc:
//        final int end = len + textLen;
//        final int[] escCodes = _outputEscapes;
//        final int escLen = escCodes.length;
//
//        output_loop:
//        while (len < end) {
//            // Fast loop for chars not needing escaping
//            escape_loop:
//            while (true) {
//                char c = buf[len];
//                if (c < escLen && escCodes[c] != 0) {
//                    break escape_loop;
//                }
//                if (++len >= end) {
//                    break output_loop;
//                }
//            }
//
//            // Ok, bumped into something that needs escaping.
//            /* First things first: need to flush the buffer.
//             * Inlined, as we don't want to lose tail pointer
//             */
//            int flushLen = len;//(len - 0);
//            if (flushLen > 0) {
//                try {
//                    writer.write(buf, 0, flushLen);
//                } catch (IOException e) {
//                    throw new UncheckedIOException(e);
//                }
//            }
//            /* In any case, tail will be the new start, so hopefully
//             * we have room now.
//             */
//            char c = buf[len++];
//            _prependOrWriteCharacterEscape(c, escCodes[c]);
//        }
//    }
//
//    private void _prependOrWriteCharacterEscape(char c, int escCode) {
//
//    }

    private void writeChar(char c) {
        if (len >= capacity) {
            flushBuffer();
        }
        buf[len++] = c;
    }

    void writeCharacter(char c) {
        if (len >= capacity) {
            flushBuffer();
        }
        buf[len++] = c;
    }

    // Not using Integer.toString() since it creates intermediary String
    // Also, we want the chars to be copied to our buffer directly
    private void writeInt(int num) {
        int size;
        if (num == Integer.MIN_VALUE) {
            size = INT_MIN_VALUE_CHARS.length;
        } else {
            size = (num < 0) ? stringSize(-num) + 1 : stringSize(num);
        }
        if (len+size >= capacity) {
            flushBuffer();
        }
        if (num == Integer.MIN_VALUE) {
            System.arraycopy(INT_MIN_VALUE_CHARS, 0, buf, len, size);
        } else {
            fillIntChars(num, buf, len+size);
        }
        len += size;
    }

    // flushBuffer writes the buffered contents to writer. But incase of
    // byte stream, an OuputStreamWriter is created and that buffers too.
    // We may need to call OutputStreamWriter#flushBuffer() using
    // reflection if that is really required (commented out below)
    void flushBuffer() {
        try {
            if (len > 0) {
                writer.write(buf, 0, len);
                len = 0;
            }
        } catch (IOException ioe) {
            throw new JsonException(JsonMessages.GENERATOR_WRITE_IO_ERR(), ioe);
        }
    }

//    private static final Method flushBufferMethod;
//    static {
//        Method m = null;
//        try {
//            m = OutputStreamWriter.class.getDeclaredMethod("flushBuffer");
//            m.setAccessible(true);
//        } catch (Exception e) {
//            // no-op
//        }
//        flushBufferMethod = m;
//    }
//    void flushBufferOSW() {
//        flushBuffer();
//        if (writer instanceof OutputStreamWriter) {
//            try {
//                flushBufferMethod.invoke(writer);
//            } catch (Exception e) {
//                // no-op
//            }
//        }
//    }

    // Requires positive x
    private static int stringSize(int x) {
        for (int i=0; ; i++)
            if (x <= INT_CHARS_SIZE_TABLE[i])
                return i+1;
    }

    private void getChars(CharSequence str, int srcBegin, int srcEnd, char[] dst, int dstBegin) {
      int length = srcEnd - srcBegin;
      for (int i = 0 ; i < length ; i++) {
        int srcIdx = srcBegin + i;
        int dstIdx = dstBegin + i;
        dst[dstIdx] = str.charAt(srcIdx);
      }
    }

    /**
     * Places characters representing the integer i into the
     * character array buf. The characters are placed into
     * the buffer backwards starting with the least significant
     * digit at the specified index (exclusive), and working
     * backwards from there.
     *
     * Will fail if i == Integer.MIN_VALUE
     */
    private static void fillIntChars(int i, char[] buf, int index) {
        int q, r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf [--charPos] = DIGIT_ONES[r];
            buf [--charPos] = DIGIT_TENS[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = (i * 52429) >>> (16+3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buf [--charPos] = DIGITS[r];
            i = q;
            if (i == 0) break;
        }
        if (sign != 0) {
            buf [--charPos] = sign;
        }
    }

}
