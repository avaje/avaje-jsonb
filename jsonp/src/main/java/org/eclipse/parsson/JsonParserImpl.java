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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

import jakarta.json.JsonException;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;

import org.eclipse.parsson.JsonTokenizer.JsonToken;
import org.eclipse.parsson.api.BufferPool;

/**
 * JSON parser implementation. NoneContext, ArrayContext, ObjectContext is used
 * to go to next parser state.
 *
 * @author Jitendra Kotamraju
 * @author Kin-man Chung
 */
public final class JsonParserImpl implements JsonParser {

    private final Context contextNone = new NoneContext();
    private final Context contextObject = new ObjectContext();
    private final Context contextArray = new ArrayContext();
    private Context currentContext = contextNone;
    private Event currentEvent;

    private final Deque<Context> stack = new ArrayDeque<>();
    private final JsonTokenizer tokenizer;
    private boolean closed = false;

    public JsonParserImpl(Reader reader, BufferPool bufferPool) {
        this(reader, bufferPool, false, Collections.emptyMap());
    }

    public JsonParserImpl(Reader reader, BufferPool bufferPool, boolean rejectDuplicateKeys, Map<String, ?> config) {
        tokenizer = new JsonTokenizer(reader, bufferPool);
    }

    public JsonParserImpl(InputStream in, BufferPool bufferPool) {
        this(in, bufferPool, false, Collections.emptyMap());
    }

    public JsonParserImpl(InputStream in, BufferPool bufferPool, boolean rejectDuplicateKeys, Map<String, ?> config) {
        UnicodeDetectingInputStream uin = new UnicodeDetectingInputStream(in);
        tokenizer = new JsonTokenizer(new InputStreamReader(uin, uin.getCharset()), bufferPool);
    }

    public JsonParserImpl(InputStream in, Charset encoding, BufferPool bufferPool) {
        this(in, encoding, bufferPool, false, Collections.emptyMap());
    }

    public JsonParserImpl(InputStream in, Charset encoding, BufferPool bufferPool, boolean rejectDuplicateKeys, Map<String, ?> config) {
        tokenizer = new JsonTokenizer(new InputStreamReader(in, encoding), bufferPool);
    }

    @Override
    public String getString() {
        if (currentEvent == Event.KEY_NAME || currentEvent == Event.VALUE_STRING
                || currentEvent == Event.VALUE_NUMBER) {
            return tokenizer.getValue();
        }
        throw new IllegalStateException(
                JsonMessages.PARSER_GETSTRING_ERR(currentEvent));
    }

    @Override
    public boolean isIntegralNumber() {
        if (currentEvent != Event.VALUE_NUMBER) {
            throw new IllegalStateException(
                    JsonMessages.PARSER_ISINTEGRALNUMBER_ERR(currentEvent));
        }
        return tokenizer.isIntegral();
    }

    @Override
    public int getInt() {
        if (currentEvent != Event.VALUE_NUMBER) {
            throw new IllegalStateException(
                    JsonMessages.PARSER_GETINT_ERR(currentEvent));
        }
        return tokenizer.getInt();
    }

    boolean isDefinitelyInt() {
        return tokenizer.isDefinitelyInt();
    }

    boolean isDefinitelyLong() {
        return tokenizer.isDefinitelyLong();
    }

    @Override
    public long getLong() {
        if (currentEvent != Event.VALUE_NUMBER) {
            throw new IllegalStateException(
                    JsonMessages.PARSER_GETLONG_ERR(currentEvent));
        }
        return tokenizer.getLong();
    }

    @Override
    public BigDecimal getBigDecimal() {
        if (currentEvent != Event.VALUE_NUMBER) {
            throw new IllegalStateException(
                    JsonMessages.PARSER_GETBIGDECIMAL_ERR(currentEvent));
        }
        return tokenizer.getBigDecimal();
    }

    @Override
    public void skipChildren() {
      if (currentEvent == Event.START_ARRAY) {
        skipChildrenArray();
        currentContext = stack.pop();
        currentEvent = Event.END_ARRAY;
      } else if (currentEvent == Event.START_OBJECT) {
        skipChildrenObject();
        currentContext = stack.pop();
        currentEvent = Event.END_OBJECT;
      }
    }

    @Override
    public void skipArray() {
        if (currentEvent == Event.START_ARRAY) {
            skipChildrenArray();
            currentContext = stack.pop();
            currentEvent = Event.END_ARRAY;
        }
    }

    @Override
    public void skipObject() {
        if (currentEvent == Event.START_OBJECT) {
            skipChildrenObject();
            currentContext = stack.pop();
            currentEvent = Event.END_OBJECT;
        }
    }

    void skipChildrenObject() {
      JsonToken token;
      int depth = 1;
      do {
        token = tokenizer.nextToken();
        switch (token) {
          case CURLYCLOSE:
            depth--;
            break;
          case CURLYOPEN:
            depth++;
            break;
        }
      } while (!(token == JsonToken.CURLYCLOSE && depth == 0));
    }

    void skipChildrenArray() {
      JsonToken token;
      int depth = 1;
      do {
        token = tokenizer.nextToken();
        switch (token) {
          case SQUARECLOSE:
            depth--;
            break;
          case SQUAREOPEN:
            depth++;
            break;
        }
      } while (!(token == JsonToken.SQUARECLOSE && depth == 0));
    }

    private CharSequence getCharSequence() {
      if (currentEvent == Event.KEY_NAME || currentEvent == Event.VALUE_STRING
              || currentEvent == Event.VALUE_NUMBER) {
          return tokenizer.getCharSequence();
      }
      throw new IllegalStateException(JsonMessages.PARSER_GETSTRING_ERR(currentEvent));
  }

    @Override
    public JsonLocation getLocation() {
        return tokenizer.getLocation();
    }

    public JsonLocation getLastCharLocation() {
        return tokenizer.getLastCharLocation();
    }

    boolean eof;
    @Override
    public boolean hasNext() {
        if (stack.isEmpty() && (currentEvent != null && currentEvent.compareTo(Event.KEY_NAME) > 0)) {
            JsonToken token = tokenizer.nextToken();
            if (token != JsonToken.EOF) {
                throw new JsonParsingException(JsonMessages.PARSER_EXPECTED_EOF(token),getLastCharLocation());
            }
            eof = true;
            return false;
        } else if (!stack.isEmpty() && !tokenizer.hasNextToken()) {
            currentEvent = currentContext.getNextEvent();
            eof = true;
            return false;
        }
        return true;
    }

    @Override
    public Event next() {
        if (eof) {
            throw new NoSuchElementException();
        }
        return currentEvent = currentContext.getNextEvent();
    }

    @Override
    public Event currentEvent() {
        return currentEvent;
    }

    @Override
    public void close() {
        if (!closed) {
            try {
                tokenizer.close();
                closed = true;
            } catch (IOException e) {
                throw new JsonException(JsonMessages.PARSER_TOKENIZER_CLOSE_IO(), e);
            }
        }
    }

//    // Using the optimized stack impl as we don't require other things
//    // like iterator etc.
//    private static final class Stack {
//        private Context head;
//
//        private void push(Context context) {
//            context.next = head;
//            head = context;
//        }
//
//        private Context pop() {
//            if (head == null) {
//                throw new NoSuchElementException();
//            }
//            Context temp = head;
//            head = head.next;
//            return temp;
//        }
//
//        private boolean isEmpty() {
//            return head == null;
//        }
//    }

    private abstract class Context {
        //Context next;
        abstract Event getNextEvent();
    }

    private final class NoneContext extends Context {
        @Override
        public Event getNextEvent() {
            // Handle 1. {   2. [   3. value
            JsonToken token = tokenizer.nextToken();
            if (token == JsonToken.CURLYOPEN) {
                stack.push(currentContext);
                currentContext = contextObject;
                return Event.START_OBJECT;
            } else if (token == JsonToken.SQUAREOPEN) {
                stack.push(currentContext);
                currentContext = contextArray;
                return Event.START_ARRAY;
            } else if (token.isValue()) {
                return token.getEvent();
            }
            throw parsingException(token, "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]");
        }
    }

    private JsonParsingException parsingException(JsonToken token, String expectedTokens) {
        JsonLocation location = getLastCharLocation();
        return new JsonParsingException(JsonMessages.PARSER_INVALID_TOKEN(token, location, expectedTokens), location);
    }

    private final class ObjectContext extends Context {

        /*
         * Some more things could be optimized. For example, instead
         * tokenizer.nextToken(), one could use tokenizer.matchColonToken() to
         * match ':'. That might optimize a bit, but will fragment nextToken().
         * I think the current one is more readable.
         *
         */
        @Override
        public Event getNextEvent() {
            // Handle 1. }   2. name:value   3. ,name:value
            JsonToken token = tokenizer.nextToken();
            if (token == JsonToken.EOF) {
                switch (currentEvent) {
                    case START_OBJECT:
                        throw parsingException(token, "[STRING, CURLYCLOSE]");
                    case KEY_NAME:
                        throw parsingException(token, "[COLON]");
                    default:
                        throw parsingException(token, "[COMMA, CURLYCLOSE]");
                }
            } else if (currentEvent == Event.KEY_NAME) {
                // Handle 1. :value
                if (token != JsonToken.COLON) {
                    throw parsingException(token, "[COLON]");
                }
                token = tokenizer.nextToken();
                if (token.isValue()) {
                    return token.getEvent();
                } else if (token == JsonToken.CURLYOPEN) {
                    stack.push(currentContext);
                    currentContext = contextObject;
                    return Event.START_OBJECT;
                } else if (token == JsonToken.SQUAREOPEN) {
                    stack.push(currentContext);
                    currentContext = contextArray;
                    return Event.START_ARRAY;
                }
                throw parsingException(token, "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]");
            } else {
                // Handle 1. }   2. name   3. ,name
                if (token == JsonToken.CURLYCLOSE) {
                    currentContext = stack.pop();
                    return Event.END_OBJECT;
                }
                if (token == JsonToken.COMMA) {
                  token = tokenizer.nextToken();
                }
                if (token == JsonToken.STRING) {
                    return Event.KEY_NAME;
                }
                throw parsingException(token, "[STRING]");
            }
        }

    }

    private final class ArrayContext extends Context {

        // Handle 1. ]   2. value   3. ,value
        @Override
        public Event getNextEvent() {
            JsonToken token = tokenizer.nextToken();
            if (token == JsonToken.EOF) {
                switch (currentEvent) {
                    case START_ARRAY:
                        throw parsingException(token, "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]");
                    default:
                        throw parsingException(token, "[COMMA, CURLYCLOSE]");
                }
            }
            if (token == JsonToken.SQUARECLOSE) {
                currentContext = stack.pop();
                return Event.END_ARRAY;
            }
            if (token == JsonToken.COMMA) {
              token = tokenizer.nextToken();
            }
            if (token.isValue()) {
                return token.getEvent();
            } else if (token == JsonToken.CURLYOPEN) {
                stack.push(currentContext);
                currentContext = contextObject;
                return Event.START_OBJECT;
            } else if (token == JsonToken.SQUAREOPEN) {
                stack.push(currentContext);
                currentContext = contextArray;
                return Event.START_ARRAY;
            }
            throw parsingException(token, "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]");
        }
    }

}
