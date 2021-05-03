package com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Auxiliary class with separate methods to help of
 * files archiving/unArchiving
 */
public class Tools {

    public static boolean checkArgs(String[] args) {
        if (args.length > 3) {
            throw new IllegalArgumentException("Too many arguments");
        }
        if (args.length == 3 && !("-a, -u".contains(args[0])) ||
                (args.length == 3 && (!isAllowedFileName(args[1]) || !isAllowedFileName(args[2]))) ||
                (args.length == 1 && !isAllowedFileName(args[0]) && !("-a, -u".contains(args[0])))
        ) {
            throw new IllegalArgumentException("Wrong argument(s) Syntax");
        }
        if (args.length == 2 && ("-a, -u".contains(args[0])) ||
                (args.length == 1 && ("-a, -u".contains(args[0])))
        ) {
            throw new IllegalArgumentException("Specify both the source and target files");
        }

        /* Uncomment if it is needed to restrict the following operations:
         * flag -u, + two arguments and the second one(source file) doesn't have the .par extension
         * flag -a, + two arguments and the third one (target file) doesn't have the .par extension
         */

        /*if (args.length == 3 && isAllowedFileName(args[1]) && isAllowedFileName(args[2])) {
            if (args[0].equals("-u") && !fileHasParExtension(args[1])) {
                throw new IllegalArgumentException("The source file must be type of .par");
            }
        }*/
        /*if (args.length == 2 && !fileHasParExtension(args[0]) && !fileHasParExtension(args[1]) ||
                (args.length == 3 && args[0].equals("-a") && !fileHasParExtension(args[2]))
        ) {
            throw new IllegalArgumentException("Target file must be type of .par in archiving process");
        }*/

        return true;
    }

    /**
     * Checks if the specified string is correct file name
     * Correct name may content alphabet russian and english symbols
     * in both low and upper cases with numbers and points.
     */
    private static boolean isAllowedFileName(String s) {
        return s.matches("^[а-яА-ЯёЁa-zA-Z0-9.]*\\.?[a-zA-Z]*$");
    }

    /**
     * Gets specified file's size in bytes
     */
    public static long getFileSize(String file) throws IOException {
        return Files.size(Paths.get(file));
    }

    /**
     * Gets specified file's name without its last extension
     * A file may contain more than one extension in its name
     * or not to contain any extension at all
     */
    public static String getFileNameWithoutLastExt(String sourceFile) {
        int pos = sourceFile.lastIndexOf(".");
        if (pos == -1) return sourceFile;
        return sourceFile.substring(0, pos);
    }

    /**
     * Checks if the file has .par extension, which means the file is archived
     */
    public static boolean fileHasParExtension(String arg) {
        return arg.matches("[а-яА-ЯёЁa-zA-Z0-9.]+\\.par");
    }

}
