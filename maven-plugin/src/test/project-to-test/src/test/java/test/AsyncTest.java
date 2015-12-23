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

package test;

import com.ea.async.Async;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.*;

public class AsyncTest
{
    static
    {
        Async.init();
    }

    @Test(timeout = 5_000L)
    public void testThenCompose()
    {
        CompletableFuture<Integer> futureA = new CompletableFuture<>();
        CompletableFuture<Integer> futureB = new CompletableFuture<>();

        // calling the instrumented function
        // without async this would block
        CompletableFuture<Integer> result = asyncAdd(futureA, futureB);

        // it is not done because futureA and futureB are not completed
        assertFalse(result.isDone());

        futureA.complete(1);
        futureB.complete(2);

        // now the result is complete because we have completed futureA and futureB
        assertTrue(result.isDone());

        // and here is the result
        assertEquals((Integer) 3, result.join());

    }

    public CompletableFuture<Integer> asyncAdd(CompletableFuture<Integer> a, CompletableFuture<Integer> b)
    {
        return completedFuture(await(a) + await(b));
    }
}