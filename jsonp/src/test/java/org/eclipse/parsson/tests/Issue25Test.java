/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.eclipse.parsson.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParser;

public class Issue25Test {

    @Test
    public void doubleClose() throws IOException {
        byte[] content = "[\"test\"]".getBytes();
        JsonProvider json = JsonProvider.provider();
        for (int i = 0; i < 3; i++) {
            try (InputStream in = new ByteArrayInputStream(content)) {
                try (JsonParser parser = json.createParser(in)) {
                    JsonParser.Event firstEvent = parser.next();
                    assertEquals(JsonParser.Event.START_ARRAY, firstEvent);
                    while (parser.hasNext()) {
                        JsonParser.Event event = parser.next();
                        if (event == JsonParser.Event.START_OBJECT) {
                          parser.skipObject();
                            //JsonObject object = parser.getObject();
                            //object.toString();
                        }
                    }
                    parser.close();
                }
            }
        }
    }

    @Test
    public void doubleCloseWithMoreContent() throws IOException {
        byte[] content = loadResource("/comments.json");
        JsonProvider json = JsonProvider.provider();
        //for (int i = 0; i < 1; i++) {
            try (InputStream in = new ByteArrayInputStream(content)) {
                try (JsonParser parser = json.createParser(in)) {
                    JsonParser.Event firstEvent = parser.next();
                    assertEquals(JsonParser.Event.START_ARRAY, firstEvent);
                    while (parser.hasNext()) {
                        JsonParser.Event event = parser.next();
                        if (event == JsonParser.Event.START_OBJECT) {
                          parser.skipObject();
                            //JsonObject object = parser.getObject();
                            //object.toString();
                        }
                    }
                    // Closing
                    parser.close();
                }
            }
       // }
    }

    private byte[] loadResource(String name) throws IOException {
        try (InputStream in = openResource(name)) {
            return in.readAllBytes();
        }
    }

    private InputStream openResource(String name) throws FileNotFoundException {
        InputStream in = Issue25Test.class.getResourceAsStream(name);
        if (in == null) throw new FileNotFoundException(name);
        return in;
    }
}
