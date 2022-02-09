/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

final class EscapeKey {

    static String quoteEscape(CharSequence string) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
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
                builder.append(string, begin, end);
                if (i == len) {
                    break;
                }
            }

            switch (c) {
                case '"':
                case '\\':
                    builder.append('\\'); builder.append(c);
                    break;
                case '\b':
                    builder.append('\\'); builder.append('b');
                    break;
                case '\f':
                    builder.append('\\'); builder.append('f');
                    break;
                case '\n':
                    builder.append('\\'); builder.append('n');
                    break;
                case '\r':
                    builder.append('\\'); builder.append('r');
                    break;
                case '\t':
                    builder.append('\\'); builder.append('t');
                    break;
                default:
                    String hex = "000" + Integer.toHexString(c);
                    builder.append("\\u" + hex.substring(hex.length() - 4));
            }
        }
        return builder.append('"').toString().intern();
    }
}
