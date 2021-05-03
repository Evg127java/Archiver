package com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor;

import com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor.Utils.HuffmanTreeNode;
import com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor.Utils.Tools;

import static com.shpp.p2p.cs.ekondratiuk.assignment15.Constants.*;

import java.io.*;
import java.util.*;

public class UnArchiver {
    /** Size of the file that was packed to current(fileIn) archive file */
    protected long originalFileSize;

    /** Input file to unArchive */
    private String fileIn;

    /** Output file for the result of unArchiving */
    private String fileOut;

    /** Temporary bytes counter from one buffer for writing to the fileOut */
    private int bufferBytesCounter;

    /** Global bytes counter from all buffers for writing to the fileOut */
    private int globalBytesCounter;

    /** Technical data offset(bytes) from the begin of the file to the data itself */
    private int offset;

    /** Huffman tree from the packed file to process the packed data */
    private HuffmanTreeNode huffmanTree;

    /** Tree shape as the boolean queue */
    private final Deque<Boolean> treeShape = new LinkedList<>();

    /** Leaves of the tree as the Byte queue */
    private final Queue<Byte> treeLeaves = new LinkedList<>();

    /** String constructor used as a dynamic binary stack */
    private final StringBuilder binaryStack = new StringBuilder();

    /**
     * Makes the UnArchived file(target) from the source one
     *
     * @return 0 if operation is successful
     */
    public int unArchive(String fileIn, String fileOut) throws IOException {
        this.fileIn = fileIn;
        this.fileOut = fileOut;
        restoreFileHeaderData();
        huffmanTree = unpackHuffmanTree();
        processFile();
        return 0;
    }

    /** Restores data for a current file from its header */
    private void restoreFileHeaderData() throws IOException {
        DataInputStream dis;
        dis = new DataInputStream(new FileInputStream(fileIn));
        /* Read eight bytes as a long number from the file header */
        originalFileSize = dis.readLong();
        /* The length of the original tree shape in bites (2 bytes) */
        int treeSizeInBites = dis.readShort();
        /* The length of the packed tree shape in bytes (2 bytes) */
        int treeSizeInBytes = dis.readShort();
        /* The packed tree leaves amount (2 bytes) */
        int treeLeavesAmount = dis.readShort();

        byte[] buffer = new byte[treeSizeInBytes + treeLeavesAmount];
        int bytesInBuffer;
        if ((bytesInBuffer = dis.read(buffer)) != -1) {
            /* Get the tree shape */
            getTreeShapeAsBooleanDequeue(Arrays.copyOfRange(
                    buffer, 0, treeSizeInBytes), treeSizeInBites
            );
            /* Get the tree leaves */
            byte[] leaves = Arrays.copyOfRange(buffer, treeSizeInBytes, bytesInBuffer);
            for (Byte leaf: leaves) {
                treeLeaves.add(leaf);
            }
        }
        int fileHeaderSize = FILE_SIZE_IN_BYTES +
                TREE_SHAPE_IN_BITES +
                TREE_SIZE_IN_BYTES +
                TREE_LEAVES_NUMBER_IN_BYTES;
        /* Define the current file pointer after its technical data to read */
        offset = fileHeaderSize + treeSizeInBytes + treeLeavesAmount;
    }

    /** Gets unpacked Huffman tree from the packed file */
    private HuffmanTreeNode unpackHuffmanTree() {
        HuffmanTreeNode root = null;
        boolean next = false;
        /* Check each element if it is a node of the tree */
        if (treeShape.peek() != null) {
            next = treeShape.poll();
        }
        if (next) {
            /* if the current element is a node */
            root = new HuffmanTreeNode();
            root.leftChild = unpackHuffmanTree();
            root.rightChild = unpackHuffmanTree();
        } else if (treeLeaves.peek() != null) {
            /* if the current element is a Leaf */
            root = new HuffmanTreeNode(treeLeaves.poll());
        }
        /* set the current node as a root for the next pass */
        return root;
    }

    /** Reads, archives and writes a file buffer by buffer using a stream */
    private void processFile() throws IOException {
        BufferedInputStream fis;
        BufferedOutputStream fos;
        fis = new BufferedInputStream(new FileInputStream(fileIn));
        fos = new BufferedOutputStream(new FileOutputStream(fileOut));
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesInBuffer;
        if (fis.skip(offset) != offset) {
            throw new IOException("Header handling error");
        }
        /* Read the data to process by specified buffers */
        while ((bytesInBuffer = fis.read(buffer)) != -1) {
            byte[] bytesToProcess = Arrays.copyOfRange(buffer, 0, bytesInBuffer);
            byte[] processedData = getProcessedData(bytesToProcess);
            /* Write the processed data by specified buffers */
            fos.write(processedData, 0, bufferBytesCounter);
        }
        if (binaryStack.length() != 0) {
            //System.out.println("test append" + binaryStack);
            byte[] appendData = getProcessedData(null);
            fos.write(appendData, 0, bufferBytesCounter);
        }
        fos.flush();
        fis.close();
    }

    /** Implements the unArchiving process */
    private byte[] getProcessedData(byte[] bytesToProcess) {
        bufferBytesCounter = 0;
        /* Counts every bit in a pass from the root to a leaf */
        int bitsCounter = 0;
        char temp;

        /* Build binary string with input bytes if the input array exists */
        if (bytesToProcess != null) {
            for (byte aByte : bytesToProcess) {
                binaryStack.append(getFullBinaryFormOfByte(aByte));
            }
        }
        /* Form the processed data in the output array */
        byte[] processedData = new byte[binaryStack.length()];
        HuffmanTreeNode currentNode = huffmanTree;
        /* Going through the tree to find leaves */
        while (
                binaryStack.length() > 0 &&
                bitsCounter < binaryStack.length() &&
                globalBytesCounter < originalFileSize
        ) {
            temp = binaryStack.charAt(bitsCounter);
            bitsCounter++;
            /* Check each bit from the binary stack where to go
            * '0' - to the left, '1' - to the right */
            currentNode = temp == '0' ? currentNode.leftChild : currentNode.rightChild;

            if (currentNode.isLeaf()) {
                processedData[bufferBytesCounter++] = currentNode.byteValue;
                globalBytesCounter++;
                /* Delete processed bits from the binary stack*/
                binaryStack.delete(0, bitsCounter);
                bitsCounter = 0;
                /* Set the root of the Huffman tree as the start for the next pass */
                currentNode = huffmanTree;
            }
        }
        return processedData;
    }

    /** Returns binary(in 8 bit) representation of the passed byte */
    private String getFullBinaryFormOfByte(byte aByte) {
        return Integer.toBinaryString((aByte & 0xff) + 0x100).substring(1);
    }

    /**
     * Gets the Huffman tree shape from passed bytes as a boolean queue
     *
     * @param compressedTreeShape Bytes array representation of the Huffman tree
     * @param treeSizeInBites     Bits amount which must contain the unpacked Huffman tree
     */
    private void getTreeShapeAsBooleanDequeue(byte[] compressedTreeShape, int treeSizeInBites) {
        /* Build binary string representation of the Huffman tree with input bytes */
        for (byte aByte : compressedTreeShape) {
            String binaryWord = getFullBinaryFormOfByte(aByte);
            for (int i = 0; i < binaryWord.length(); i++) {
                char temp = binaryWord.charAt(i);
                treeShape.add(temp == '1');
            }
        }
        /* Check the length of the gotten tree according to the original one */
        int res = treeShape.size() - treeSizeInBites;
        if (treeShape.size() - treeSizeInBites > 0) {
            for (int i = 0; i < res; i++) {
                treeShape.pollLast();
            }
        }
    }

    /** Prints results of the process to the console */
    public void printProcessResults() throws IOException {
        System.out.println("Compressed file name..: " + fileIn);
        System.out.println(
                "Compressed file size..: " + (String.format("%,dB",
                        Tools.getFileSize(fileIn)))
        );
        System.out.println(
                "UnCompressed file size: " + (String.format("%,dB",
                        Tools.getFileSize(fileOut)))
        );
        System.out.println("UnCompressed file name: " + fileOut);
    }
}