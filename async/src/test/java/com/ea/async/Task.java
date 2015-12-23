package com.ea.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class Task<T> extends CompletableFuture<T>
{
    public static <T> Task<T> fromValue(T value)
    {
        final Task<T> task = new Task<>();
        task.complete(value);
        return task;
    }

    public static Task<Void> done()
    {
        final Task<Void> task = new Task<>();
        task.complete(null);
        return task;
    }

    public static <T> Task<T> fromException(final Exception ex)
    {
        final Task<T> task = new Task<>();
        task.completeExceptionally(ex);
        return task;
    }

    public static <T> Task<T> from(CompletionStage<T> stage)
    {
        Task a;
        if (stage instanceof Task)
        {
            a = (Task<T>) stage;
        }
        else
        {
            final Task<T> t = new Task<>();
            stage.whenComplete((T v, Throwable ex) -> {
                if (ex != null)
                {
                    t.completeExceptionally(ex);
                }
                else
                {
                    t.complete(v);
                }
            });
            a  = t;
        }
        return a;
    }


    /**
     * Wraps a CompletionStage as a Task or just casts it if it is already a Task.
     *
     * @param stage the stage to be wrapped or casted to Task
     * @return stage cast as Task of a new Task that is dependent on the completion of that stage.
     */
    public static <T> Task<T> cast(CompletionStage<T> stage)
    {
        if (stage instanceof Task)
        {
            return (Task<T>) stage;
        }

        final Task<T> t = new Task<>();
        stage.whenComplete((T v, Throwable ex) -> {
            if (ex != null)
            {
                t.completeExceptionally(ex);
            }
            else
            {
                t.complete(v);
            }
        });
        return t;
    }


    public <U> Task<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn)
    {
        return Task.from(super.thenCompose(fn));
    }

}
