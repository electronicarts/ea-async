/*
 Copyright (C) 2017 Electronic Arts Inc.  All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1.  Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
 2.  Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
 3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
     its contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ea.async.test;

import com.ea.async.Task;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static com.ea.async.Async.await;
import static org.junit.Assert.assertEquals;

/**
 * Created by joeh on 2017-04-06.
 */
public class TryWithResourcesTest extends BaseTest
{
    private class CloseableResource implements AutoCloseable {
        private boolean closed = false;

        @Override
        public void close()
        {
            closed = true;
        }

        public boolean isClosed()
        {
            return closed;
        }
    }

    @Test
    public void testTryWithResources()
    {
        final Task<Integer> res = doTestTryWithResources();
        completeFutures();
        assertEquals((Integer) 3456, res.join());
    }

    private Task<Integer> doTestTryWithResources()
    {
        try(CloseableResource cr = new CloseableResource())
        {
            await(getBlockedFuture());
            if(cr.isClosed()) {
                throw new RuntimeException("Closed early");
            }
        }

        return Task.fromValue(3456);
    }
}
