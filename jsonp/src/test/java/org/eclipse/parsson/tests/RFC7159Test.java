/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved.
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

import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.io.StringReader;

/**
 * @author Kin-man Chung
 */
public class RFC7159Test {

    @Test
    public void testGeneratorValues() {
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = Json.createGenerator(stringWriter);
        generator.write("someString").close();
        assertEquals("\"someString\"", stringWriter.toString());

        stringWriter = new StringWriter();
        generator = Json.createGenerator(stringWriter);
        generator.write(100).close();
        assertEquals("100", stringWriter.toString());

        stringWriter = new StringWriter();
        generator = Json.createGenerator(stringWriter);
        generator.write(12345.6789).close();
        assertEquals("12345.6789", stringWriter.toString());
    }
}
