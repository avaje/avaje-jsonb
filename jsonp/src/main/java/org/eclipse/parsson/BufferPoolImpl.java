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

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * char[] pool that pool instances of char[] which are expensive to create.
 *
 * @author Jitendra Kotamraju
 */
final class BufferPoolImpl implements BufferPool {

    // volatile since multiple threads may access queue reference
    //private volatile WeakReference<ConcurrentLinkedQueue<char[]>> queue;
    private final AtomicReferenceArray<char[]> _charBuffers;
    //private final static int[] CHAR_BUFFER_LENGTHS = new int[]{4000, 4000, 200, 200};

    BufferPoolImpl() {
        this(4);
    }

    BufferPoolImpl(int cbCount) {
        _charBuffers = new AtomicReferenceArray<>(cbCount);
    }

    @Override
    public void recycle(char[] buffer) {
        _charBuffers.set(0, buffer);
    }

    @Override
    public char[] take() {
        char[] buffer = _charBuffers.getAndSet(0, null);
        return buffer == null ? new char[50] : buffer;
    }

//    /**
//     * Gets a new object from the pool.
//     *
//     * <p>
//     * If no object is available in the pool, this method creates a new one.
//     *
//     * @return
//     *      always non-null.
//     */
//    @Override
//    public char[] take() {
//        char[] t = getQueue().poll();
//        if (t==null)
//            return new char[4096];
//        return t;
//    }
//
//    private ConcurrentLinkedQueue<char[]> getQueue() {
//        WeakReference<ConcurrentLinkedQueue<char[]>> q = queue;
//        if (q != null) {
//            ConcurrentLinkedQueue<char[]> d = q.get();
//            if (d != null)
//                return d;
//        }
//
//        // overwrite the queue
//        ConcurrentLinkedQueue<char[]> d = new ConcurrentLinkedQueue<>();
//        queue = new WeakReference<>(d);
//
//        return d;
//    }
//
//    /**
//     * Returns an object back to the pool.
//     */
//    @Override
//    public void recycle(char[] t) {
//        getQueue().offer(t);
//    }

}
