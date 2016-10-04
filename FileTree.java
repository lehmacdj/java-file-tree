import java.io.File;
import java.util.Arrays;
import java.nio.file.Files;
import java.lang.reflect.*;
import java.util.stream.Collectors;
import java.lang.annotation.*;

/** 
 * Has some small methods that show off some filesystem apis and some
 * reflection apis in addition to a command line interface to those.
 */
public class FileTree {

    public static void main(String[] args) {
        try {
            Class<?> c = FileTree.class;

            Method m = c.getDeclaredMethod(args[0], String[].class);

            if (!m.isAnnotationPresent(External.class)) {
                // ugly hack to reuse the code in my method handling
                throw new NoSuchMethodException();
            }

            String[] margs = Arrays.copyOfRange(args, 1, args.length);

            // Print that you are invoking the method
            System.out.printf("invoking %s(%s)%n", m.getName(),
                    Arrays.asList(margs)
                    .stream()
                    .map(s-> '"' + s + '"')
                    .collect(Collectors.joining(", ")));

            m.invoke(null, (Object) margs);

        } catch(NoSuchMethodException me) {
            System.err.println(args[0] + " is not a valid option.");
            System.out.println("Usage: java FileTree <method-name>");
            System.out.println("where <method-name> is one of:");
            listExternalMethodsWithPrefix("    ");
        } catch(IllegalAccessException ae) {
            ae.printStackTrace();
        } catch(InvocationTargetException te) {
            te.printStackTrace();
        }
    }

    /**
     * List all the methods that are available to the external command line
     * interface.
     */
    @External
    public static void list(String... args) {
        System.out.println("Listing methods that may be called:");
        listExternalMethodsWithPrefix("- ");
    }

    /**
     * Prints to std::out the list of &#64;External methods in this class using
     * a prefix of [prefix].
     */
    private static void listExternalMethodsWithPrefix(String prefix){
        try {
            Class<?> c = FileTree.class;
            for (Method m : c.getDeclaredMethods()) {
                if (m.isAnnotationPresent(External.class)) {
                    System.out.println(prefix + m.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Printthe maximum depth of the FileTree with the root directory passed
     * as an argument passed as an argument.
     */
    @External
    public static void getMaxDepth(String... args) {
        try {
            File f = new File(args[0]);
            System.out.println("Computing max filetree depth...");
            System.out.println(getMaxDepthOf(f));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the maximum depth of the specified file tree.
     */
    private static int getMaxDepthOf(File path) {
        if (path == null) return 1;
        if (path.isFile()) return 1;
        if (Files.isSymbolicLink(path.toPath())) return 1;
        if (path.listFiles() == null) return 1;
        if (path.listFiles().length == 0) return 1;

        return 1 + Arrays.asList(path.listFiles())
            .stream()
            .mapToInt(f->getMaxDepthOf(f))
            .max()
            .getAsInt();
    }

}

/**
 * Used to mark methods that are visible to the command line interface of this
 * program.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface External {}
