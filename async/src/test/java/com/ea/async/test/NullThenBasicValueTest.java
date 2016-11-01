package com.ea.async.test;

import com.ea.async.Task;

import org.junit.Test;

import static com.ea.async.Async.await;
import static org.junit.Assert.assertNull;

public class NullThenBasicValueTest extends BaseTest
{
    @Test
    public void nullThenBasicValueTest() throws Exception
    {
        debugTransform(NullThenBasicValueTest.class.getName() + "$NullThenBasicVal");
        final Task<Void> res = NullThenBasicVal.doIt(getBlockedTask());
        completeFutures();
        assertNull(res.join());
    }

    public static class NullThenBasicVal
    {
        public static Task<Void> doIt(Task<Void> task)
        {
            String nullString = null; //this variable must be a string and initialized to null
            int basicInt = 0; //this variable must be numeric and initialized to anything

            await(task);
            return Task.done();
        }
    }
}