/*
 Copyright (C) 2015 Electronic Arts Inc.  All rights reserved.

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

import static com.ea.async.Async.await;
import static org.junit.Assert.assertNull;

public class AConstNullTest extends BaseTest
{

    @Test
    public void nullInitialization() throws Exception
    {
        final Task<Void> res = NullInit.doIt(getBlockedTask());
        completeFutures();
        assertNull(res.join());

    }

    public static class NullInit
    {
        public static Task<Void> doIt(Task<Void> task)
        {
            String x = null;
            try
            {
                await(task);
                return Task.done();
            }
            catch (Exception ex)
            {
                return Task.done();
            }
        }
    }

    @Test
    public void nullInitialization2() throws Exception
    {
        final Task<Void> res = NullInit2.doIt(getBlockedTask());
        completeFutures();
        assertNull(res.join());

    }

    public static class NullInit2
    {
        public static Task<Void> doIt(Task<Void> task)
        {
            String x = null;
            String y = x;
            try
            {
                await(task);
                return Task.done();
            }
            catch (Exception ex)
            {
                return Task.done();
            }
        }
    }

    @Test
    public void nullInitialization3() throws Exception
    {
        final Task<Void> res = NullInit3.doIt(getBlockedTask());
        completeFutures();
        assertNull(res.join());

    }

    public static class NullInit3
    {
        public static Task<Void> doIt(Task<Void> task)
        {
            String x = null;
            call0(x, await(task));
            return Task.done();
        }
    }

    public static void call0(final Object a, final Object b)
    {
    }


    @Test
    public void nullInitialization4() throws Exception
    {
        final Task<Void> res = NullInit4.doIt(getBlockedTask());
        completeFutures();
        assertNull(res.join());

    }

    public static class NullInit4
    {
        public static Task<Void> doIt(Task<String> task)
        {
            String x = null;
            call0(await(task), x);
            return Task.done();
        }
    }


    @Test
    public void nullInTheStack() throws Exception
    {
        debugTransform(AConstNullTest.class.getName() + "$NullInTheStack");
        final Task<Void> res = NullInTheStack.doIt(getBlockedTask());
        completeFutures();
        assertNull(res.join());
    }

    public static class NullInTheStack
    {
        public static Task<Void> doIt(Task<Void> task)
        {
            call2(null, await(task));
            return Task.done();
        }

        private static void call2(final Object o, final Object p)
        {
        }
    }


    @Test
    public void nullInTheStack2() throws Exception
    {
        debugTransform(AConstNullTest.class.getName() + "$NullInTheStack2");
        final Task<Void> res = NullInTheStack2.doIt(getBlockedTask());
        completeFutures();
        assertNull(res.join());
    }

    public static class NullInTheStack2
    {
        public static Task<Void> doIt(Task<Void> task)
        {
            call2(null, await(task));
            return Task.done();
        }
        private static void call2(final String o, final Object p)
        {
        }

    }
}
