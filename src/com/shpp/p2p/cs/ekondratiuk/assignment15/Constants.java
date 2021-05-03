package com.shpp.p2p.cs.ekondratiuk.assignment15;

/** Constants for archiving/unArchiving files processes */
public final class Constants {
    /** Default folder with test files */
    protected static final String PATH_TO_FILE = "assets/";

    /** Default file name with extension */
    protected static final String DEFAULT_FILE_NAME = "test.txt";

    /** Extension which identifies a file as archived */
    protected static final String ARCHIVED_FILE_EXT = ".par";

    public static final String INIT_STRING = "";

    /** Bytes amount to read a file as a part of a stream */
    public static final int BUFFER_SIZE = 512;

    /** "0" means  - to go left on the tree */
    public static  final String GO_LEFT = "0";

    /** "1" means  - to go right on the tree */
    public static  final String GO_RIGHT = "1";

    /** Bytes amount to represent a tree size */
    public static final int TREE_SIZE_IN_BYTES = 2;

    /** Bites amount to represent a tree shape */
    public static final int TREE_SHAPE_IN_BITES = 2;

    /** Bytes amount to represent a tree leaves */
    public static final int TREE_LEAVES_NUMBER_IN_BYTES = 2;

    /**
     * Bytes amount in which must be represented a file size
     * in a file header
     */
    public static final int FILE_SIZE_IN_BYTES = 8;

    /** Byte size(in bits) to represent a num as a binary one or vice versa */
    public static final int BYTE_SIZE = 8;

    /** Bytes amount in the integer type of a num */
    public static final int BYTES_IN_LONG = 8;

    /** The message about unsuccessful archiving/unArchiving operation */
    protected static final String FAIL_PROCESS_MESSAGE = "Fail! Something went wrong.";

    /** The message about unsuccessful archiving due to a little unique bytes amount */
    public static final String NOT_ALLOWED_FILE_MESSAGE =
            "The specified file cannot be processed. It has less than two unique symbols";

    /** Minimal allowed unique bytes in file to start archiving */
    public static final int ALLOWED_DIFFERENT_BYTES_IN_FILE = 2;

    /** Message about successful archiving/unArchiving operation */
    protected static final String SUCCESSFUL_PROCESS_MESSAGE =
            "Success! File processing was completed in";
}

