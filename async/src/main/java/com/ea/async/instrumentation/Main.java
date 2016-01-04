package com.ea.async.instrumentation;


import org.objectweb.asm.ClassReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool class to perform build time instrumentation.
 */
public class Main
{
    private boolean verbose;
    private List<Path> fileList;
    private Path outputDirectory;
    private ClassLoader classLoader = getClass().getClassLoader();

    public static void main(String args[]) throws IOException
    {
        final int ret = new Main().doMain(args);
        if (ret != 0)
        {
            System.exit(-1);
        }
    }

    public int doMain(final String[] args) throws IOException
    {
        outputDirectory = null;

        fileList = new ArrayList<>();
        if (args.length == 0)
        {
            printUsage();
            return 0;
        }
        for (int i = 0; i < args.length; i++)
        {
            final String arg = args[i];
            if ("--help".equals(arg))
            {
                printUsage();
                continue;
            }
            if ("-verbose".equals(arg))
            {
                verbose = true;
                continue;
            }
            if ("-d".equals(arg))
            {
                if (i + 1 == args.length)
                {
                    error("Invalid usage of the -d option");
                    return 1;
                }
                outputDirectory = Paths.get(args[++i]);
                continue;
            }
            final Path path = Paths.get(arg);
            if (Files.isDirectory(path))
            {
                Files.walk(path)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".class"))
                        .forEach(fileList::add);
                continue;
            }
            if (Files.isRegularFile(path))
            {
                fileList.add(path);
                continue;
            }
            error("Invalid argument or file: " + arg);
            return 1;
        }
        return transform() >= 0 ? 0 : 1;
    }

    public int transform() throws IOException
    {
        final Transformer transformer = new Transformer();
        transformer.setErrorListener(System.err::println);
        boolean error = false;
        int count = 0;
        final Path outputDir = getOutputDirectory();
        for (Path path : fileList)
        {
            byte[] bytes = null;
            try (InputStream in = new FileInputStream(path.toFile()))
            {
                bytes = transformer.instrument(classLoader, in);
            }
            catch (Exception e)
            {
                error("Error instrumenting " + path, e);
                error = true;
            }
            if (bytes != null)
            {
                if (verbose)
                {
                    info("instrumented: " + path);
                }
                if (outputDir != null)
                {
                    // writing to the output directory, using the package name as path.
                    final Path outPath = outputDir.resolve(new ClassReader(bytes).getClassName() + ".class");
                    final Path outParent = outPath.getParent();
                    if (!Files.exists(outParent))
                    {
                        Files.createDirectories(outParent);
                    }
                    Files.write(outPath, bytes);
                }
                else
                {
                    // replacing the original file
                    Files.write(path, bytes);
                }
                count++;
            }
        }
        return error ? -1 : count;
    }

    private void error(final String msg, final Exception e)
    {
        System.err.println(msg);
        e.printStackTrace();
    }


    protected void error(String msg)
    {
        System.err.println(msg);
    }

    protected void info(String msg)
    {
        System.err.println(msg);
    }

    protected void printUsage()
    {
        System.out.println("usage:");
        System.out.println(" java -cp project-class-path -jar ea-async.jar -d output-directory file1 input-dir1 input-dir2");
        System.out.println("options:");
        System.out.println("-d directory");
        System.out.println("   Set the destination directory for class files. ");
        System.out.println("   If not specified the original files will be modified in place");
        System.out.println("-help");
        System.out.println("   Shows this help. ");
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    public void setVerbose(final boolean verbose)
    {
        this.verbose = verbose;
    }

    public List<Path> getFileList()
    {
        return fileList;
    }

    public void setFileList(final List<Path> fileList)
    {
        this.fileList = fileList;
    }

    public Path getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory(final Path outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public void setClassLoader(final ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }
}
