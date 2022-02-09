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

import org.eclipse.parsson.api.BufferPool;

import java.lang.ref.SoftReference;

final class BufferPoolThreadLocal {

    private static final ThreadLocal<SoftReference<BufferPool>> recyclerRef = new ThreadLocal<>();

    /**
     */
    private static BufferPool get() {
        SoftReference<BufferPool> ref = recyclerRef.get();
        BufferPool br = (ref == null) ? null : ref.get();
        if (br == null) {
            br = new Local();
            //if (tracker != null) {
            //    ref = tracker.wrapAndTrack(br);
            //} else {
                ref = new SoftReference<>(br);
            //}
            recyclerRef.set(ref);
        }
        return br;
    }

    static BufferPool provide() {
        return new Pool();
    }


    static class Pool implements BufferPool {

        @Override
        public char[] take() {
            return BufferPoolThreadLocal.get().take();
        }

        @Override
        public void recycle(char[] buf) {
            BufferPoolThreadLocal.get().recycle(buf);
        }
    }

    static final class Local implements BufferPool {

        private char[] buffer = new char[40];

        @Override
        public void recycle(char[] buffer) {
            this.buffer = buffer;
        }

        @Override
        public char[] take() {
            final char[] buf = this.buffer;
            this.buffer = null;
            return buf == null ? new char[40] : buf;
        }

    }

}
