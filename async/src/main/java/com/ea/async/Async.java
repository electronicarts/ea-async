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

package com.ea.async;

import com.ea.async.instrumentation.InitializeAsync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This class together with bytecode instrumentation implements the async-await functionality in the JVM.
 * <p/>
 * To use it call <code>Async.await(future)</code> from within methods whose return type is
 * CompletionStage, CompletableFuture or subclasses of CompletableFuture.
 * <p/>
 * <code>Async.await(future)</code> won't block. It is semantically equivalent to call <code>future.join()</code>
 * But instead of blocking the code in instrumented to become a state machine that evolves as the futures passed
 * to <code>await</code> are completed.
 * <p/>
 * This is equivalent to use CompletableFuture composition methods (ex: thenApply, handle, thenCompose).
 * The advantage of using <code>await</code> is that the code will resemble sequential blocking code.
 * <p/>
 * Example:
 * <pre><code>
 * import static com.ea.async.Async.await;
 * import static java.util.concurrent.CompletableFuture.completedFuture;*
 *
 * public class Store
 * {
 *     public CompletableFuture<Boolean> buyItem(String itemTypeId, int cost)
 *     {
 *         if(!await(bank.decrement(cost))) {
 *             return completedFuture(false);
 *         }
 *         await(inventory.giveItem(itemTypeId));
 *         return completedFuture(true);
 *     }
 * }
 * </code></pre>
 */
public class Async
{
    private Async(){}

    /**
     * Ensure that if no pre instrumentation was done, that the Async runtime instrumentation is running.
     * <p/>
     * Attention! The build time instrumentation will remove calls to this method.
     */
    public static void init()
    {
        InitializeAsync.init();
    }

    private static Logger logger;

    /**
     * This method will behave as a <code>CompletableFuture.join()</code> but will actually cause the
     * caller to return a promise instead of blocking.
     *
     * Calls to this method are replaced by the EA Async instrumentation.
     *
     * @param future a future to wait for.
     * @param <T>    the future value type.
     * @return the return value of the future
     * @throws java.util.concurrent.CompletionException wrapping the actual exception if an exception occured.
     */
    public static <T, F extends CompletionStage<T>> T await(F future)
    {
        String warning;
        if (!InitializeAsync.isRunning())
        {
            warning = "Warning: Illegal call to await, static { Async.init(); } must be added to the main program class and the method invoking await must return a CompletableFuture";
        }
        else
        {
            warning = "Warning: Illegal call to await, the method invoking await must return a CompletableFuture";
        }
        LoggerFactory.getLogger(Async.class);
        if (logger == null)
        {
            logger = LoggerFactory.getLogger(Async.class);
        }
        if (logger.isDebugEnabled())
        {
            logger.warn(warning, new Throwable());
        }
        else
        {
            logger.warn(warning);
        }
        if (future instanceof CompletableFuture)
        {
            //noinspection unchecked
            return ((CompletableFuture<T>) future).join();
        }
        return future.toCompletableFuture().join();
    }
}
