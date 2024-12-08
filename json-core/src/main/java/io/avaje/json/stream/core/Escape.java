package io.avaje.json.stream.core;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

final class Escape {

  private static final byte QUOTE = '"';
  private static final byte ESCAPE = '\\';

  /**
   * Return as quoted escaped bytes.
   */
  static byte[] quoteEscape(final CharSequence value) {
    final int len = value.length();
    final ByteArrayOutputStream ba = new ByteArrayOutputStream(len << 2);
    ba.write(QUOTE);
    for (int i = 0; i < len; i++) {
      final char c = value.charAt(i);
      if (c > 31 && c != '"' && c != '\\' && c < 126) {
        ba.write((byte) c);
      } else {
        writeQuotedString(value, i, len, ba);
        ba.write(QUOTE);
        return ba.toByteArray();
      }
    }
    ba.write(QUOTE);
    return ba.toByteArray();
  }

  private static void writeQuotedString(final CharSequence str, int i, final int len, ByteArrayOutputStream ba) {
    for (; i < len; i++) {
      final char c = str.charAt(i);
      if (c == '"') {
        ba.write(ESCAPE);
        ba.write(QUOTE);
      } else if (c == '\\') {
        ba.write(ESCAPE);
        ba.write(ESCAPE);
      } else if (c < 32) {
        switch (c) {
          case 8:
            ba.write(ESCAPE);
            ba.write('b');
            break;
          case 9:
            ba.write(ESCAPE);
            ba.write('t');
            break;
          case 10:
            ba.write(ESCAPE);
            ba.write('n');
            break;
          case 12:
            ba.write(ESCAPE);
            ba.write('f');
            break;
          case 13:
            ba.write(ESCAPE);
            ba.write('r');
            break;
          default:
            ba.write(ESCAPE);
            ba.write('u');
            ba.write('0');
            ba.write('0');
            switch (c) {
              case 0:
                ba.write('0');
                ba.write('0');
                break;
              case 1:
                ba.write('0');
                ba.write('1');
                break;
              case 2:
                ba.write('0');
                ba.write('2');
                break;
              case 3:
                ba.write('0');
                ba.write('3');
                break;
              case 4:
                ba.write('0');
                ba.write('4');
                break;
              case 5:
                ba.write('0');
                ba.write('5');
                break;
              case 6:
                ba.write('0');
                ba.write('6');
                break;
              case 7:
                ba.write('0');
                ba.write('7');
                break;
              case 11:
                ba.write('0');
                ba.write('B');
                break;
              case 14:
                ba.write('0');
                ba.write('E');
                break;
              case 15:
                ba.write('0');
                ba.write('F');
                break;
              case 16:
                ba.write('1');
                ba.write('0');
                break;
              case 17:
                ba.write('1');
                ba.write('1');
                break;
              case 18:
                ba.write('1');
                ba.write('2');
                break;
              case 19:
                ba.write('1');
                ba.write('3');
                break;
              case 20:
                ba.write('1');
                ba.write('4');
                break;
              case 21:
                ba.write('1');
                ba.write('5');
                break;
              case 22:
                ba.write('1');
                ba.write('6');
                break;
              case 23:
                ba.write('1');
                ba.write('7');
                break;
              case 24:
                ba.write('1');
                ba.write('8');
                break;
              case 25:
                ba.write('1');
                ba.write('9');
                break;
              case 26:
                ba.write('1');
                ba.write('A');
                break;
              case 27:
                ba.write('1');
                ba.write('B');
                break;
              case 28:
                ba.write('1');
                ba.write('C');
                break;
              case 29:
                ba.write('1');
                ba.write('D');
                break;
              case 30:
                ba.write('1');
                ba.write('E');
                break;
              default:
                ba.write('1');
                ba.write('F');
                break;
            }
            break;
        }
      } else if (c < 0x007F) {
        ba.write((byte) c);
      } else {
        final int cp = Character.codePointAt(str, i);
        if (Character.isSupplementaryCodePoint(cp)) {
          i++;
        }
        if (cp == 0x007F) {
          ba.write((byte) cp);
        } else if (cp <= 0x7FF) {
          ba.write((byte) (0xC0 | ((cp >> 6) & 0x1F)));
          ba.write((byte) (0x80 | (cp & 0x3F)));
        } else if ((cp < 0xD800) || (cp > 0xDFFF && cp <= 0xFFFF)) {
          ba.write((byte) (0xE0 | ((cp >> 12) & 0x0F)));
          ba.write((byte) (0x80 | ((cp >> 6) & 0x3F)));
          ba.write((byte) (0x80 | (cp & 0x3F)));
        } else if (cp >= 0x10000 && cp <= 0x10FFFF) {
          ba.write((byte) (0xF0 | ((cp >> 18) & 0x07)));
          ba.write((byte) (0x80 | ((cp >> 12) & 0x3F)));
          ba.write((byte) (0x80 | ((cp >> 6) & 0x3F)));
          ba.write((byte) (0x80 | (cp & 0x3F)));
        } else {
          throw new IllegalArgumentException("Unknown unicode codepoint in string! " + Integer.toHexString(cp));
        }
      }
    }
  }

  static int nameHash(String name) {
    int hash = 0x811c9dc5;
    for (byte b : name.getBytes(StandardCharsets.UTF_8)) {
      hash ^= b;
      hash *= 0x1000193;
    }
    return hash;
  }
}
