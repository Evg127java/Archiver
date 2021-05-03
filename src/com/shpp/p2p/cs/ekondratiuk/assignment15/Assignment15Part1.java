package com.shpp.p2p.cs.ekondratiuk.assignment15;

import static com.shpp.p2p.cs.ekondratiuk.assignment15.Constants.*;

import com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor.Archiver;
import com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor.UnArchiver;
import com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor.Utils.Tools;


import java.io.IOException;

/**
 * Archives and UnArchives files due to input program argument
 * using the Huffman algorithm
 */
public class Assignment15Part1 {

    public static void main(String[] args) {
        try {
            if (Tools.checkArgs(args)) {
                if (args.length == 0) {
                    fileToArchive(null, null);
                }
                if (args.length == 1) {
                    if (Tools.fileHasParExtension(args[0])) {
                        archiveToFile(args[0], null);
                    } else {
                        fileToArchive(args[0], null);
                    }
                }
                if (args.length == 2) {
                    if (Tools.fileHasParExtension(args[1])) {
                        fileToArchive(args[0], args[1]);
                    } else {
                        archiveToFile(args[0], args[1]);
                    }
                }
                if (args.length == 3) {
                    if (args[0].equals("-a")) {
                        fileToArchive(args[1], args[2]);
                    }
                    if (args[0].equals("-u")) {
                        archiveToFile(args[1], args[2]);
                    }
                }
            }
        } catch (IllegalArgumentException | IOException exception) {
            System.out.println("\nFAIL!: " + exception.getMessage());
        }
    }

    /**
     * Runs archiving process of specified file as source to specified file as target
     * Counts the whole operation's time and prints result of the process if it was successful.
     */
    private static void fileToArchive(String source, String target) throws IOException {
        String sourceFile = source == null ? PATH_TO_FILE + DEFAULT_FILE_NAME : PATH_TO_FILE + source;
        String targetFile = target == null ? sourceFile + ARCHIVED_FILE_EXT : PATH_TO_FILE + target;
        Archiver archiver = new Archiver();

        long startTime = System.currentTimeMillis();
        int process = archiver.archive(sourceFile, targetFile);
        long finishTime = System.currentTimeMillis();

        if (process == 0) {
            System.out.println("\n" + SUCCESSFUL_PROCESS_MESSAGE + " " + (finishTime - startTime) + "ms");
            archiver.printProcessResults();
        } else {
            System.out.println("\n" + FAIL_PROCESS_MESSAGE);
        }
    }

    /**
     * Runs unArchiving process of specified file as source to specified file as target
     * Counts the whole operation's time and prints result of the process if it was successful.
     */
    private static void archiveToFile(String source, String target) throws IOException {
        String sourceFile = PATH_TO_FILE + source;
        String targetFile = target == null ? Tools.getFileNameWithoutLastExt(sourceFile) : PATH_TO_FILE + target;
        UnArchiver unArchiver = new UnArchiver();

        long startTime = System.currentTimeMillis();
        int process = unArchiver.unArchive(sourceFile, targetFile);
        long finishTime = System.currentTimeMillis();

        if (process == 0) {
            System.out.println("\n" + SUCCESSFUL_PROCESS_MESSAGE + " " + (finishTime - startTime) + "ms");
            unArchiver.printProcessResults();
        } else {
            System.out.println("\n" + FAIL_PROCESS_MESSAGE);
        }
    }
}