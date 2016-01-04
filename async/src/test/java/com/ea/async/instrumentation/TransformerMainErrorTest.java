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

package com.ea.async.instrumentation;

import com.ea.async.test.BaseTest;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.objectweb.asm.Opcodes.*;

public class TransformerMainErrorTest extends BaseTest
{

    public static final String COM_EA_ASYNC_TASK = "com/ea/async/Task";

    // sanity check of the creator
    @Test
    @SuppressWarnings("unchecked")
    public void transformerError() throws Exception
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(out);
        final PrintStream err = System.err;
        System.setErr(printStream);
        try
        {
            final ClassNode cn = createClassNode(Object.class, cw -> {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", null, new String[]{ "java/lang/Exception" });
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, COM_EA_ASYNC_TASK);
                mv.visitMethodInsn(INVOKESTATIC, ASYNC_NAME, "await", "(Ljava/util/concurrent/CompletableFuture;)Ljava/lang/Object;", false);
                mv.visitInsn(POP);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, COM_EA_ASYNC_TASK);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 2);
                mv.visitEnd();
            });
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            final byte[] bytes = new Transformer().transform(getClass().getClassLoader(), new ClassReader(cw.toByteArray()));

            final Main main = new Main();
            final Path path = Paths.get("target/transformerError/classes2").resolve(cn.name + ".class");
            Files.deleteIfExists(path);
            Files.createDirectories(path.getParent());
            Files.write(path, cw.toByteArray());

            // still not transformed
            final byte[] original = Files.readAllBytes(path);
            assertFalse(Arrays.equals(bytes, original));

            final Path path3 = Paths.get("target/transformerError/classes3").resolve(cn.name + ".class");
            Files.deleteIfExists(path3);
            // in output dir
            main.doMain(new String[]{ "-d", "target/transformerError/classes3", path.toString() });
            printStream.flush();
            assertTrue(new String(out.toByteArray()).startsWith("Invalid use of"));
            out.reset();
            assertArrayEquals(bytes, Files.readAllBytes(path3));
            // still unchanged
            assertArrayEquals(original, Files.readAllBytes(path));

            // passing a dir as parameter
            final Path path4 = Paths.get("target/transformerError/classes4").resolve(cn.name + ".class");
            Files.deleteIfExists(path4);
            main.doMain(new String[]{ "-d", "target/transformerError/classes4", path.getParent().toString() });
            printStream.flush();
            assertTrue(new String(out.toByteArray()).startsWith("Invalid use of"));
            out.reset();
            assertTrue("Can't find file: " + path4, Files.exists(path4));
            assertArrayEquals(bytes, Files.readAllBytes(path4));
            // still unchanged
            assertArrayEquals(original, Files.readAllBytes(path));

            // in place
            main.doMain(new String[]{ path.toString() });
            printStream.flush();
            assertTrue(new String(out.toByteArray()).startsWith("Invalid use of"));
            out.reset();
            assertArrayEquals(bytes, Files.readAllBytes(path));
            assertFalse(Arrays.equals(original, Files.readAllBytes(path)));
        }
        finally
        {
            System.setErr(err);
        }

    }


}
