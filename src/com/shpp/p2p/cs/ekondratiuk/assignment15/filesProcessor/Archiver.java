package com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor;

import com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor.Utils.HuffmanTreeNode;
import com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor.Utils.Tools;

import static com.shpp.p2p.cs.ekondratiuk.assignment15.Constants.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Archiver {

    /** Input file to archive */
    private String fileIn;

    /** Output file for the result of archiving */
    private String fileOut;

    /** Unique bytes - binary sequence table */
    public HashMap<Byte, String> codingTable = new HashMap<>();

    /** String constructor used as a dynamic binary stack */
    private final StringBuilder binaryStack = new StringBuilder();

    /** String constructor representing tree shape as a binary sequence */
    private final StringBuilder treeShape = new StringBuilder();

    /** List of unique bytes as leaves of the tree */
    private final List<Byte> treeLeaves = new ArrayList<>();

    /** Tree leaves processed for writing to the output file */
    private byte[] processedTreeLeaves;

    /** Tree shape processed for writing to the output file */
    private byte[] processedTreeShape;

    /**
     * Makes the archived file(target) from the source one
     *
     * @return 0 if operation is successful
     */
    public int archive(String fileIn, String fileOut) throws IOException {
        this.fileIn = fileIn;
        this.fileOut = fileOut;
        TreeMap<Byte, Integer> keysFrequencies = getFrequencies();
        HuffmanTreeNode huffmanTreeRoot = getHuffmanTree(keysFrequencies);
        buildCodingTable(huffmanTreeRoot, INIT_STRING);
        packHuffmanTree(huffmanTreeRoot);
        ProcessFile();
        return 0;
    }

    /**
     * Gets frequencies of bytes encountered in the source file to archive
     *
     * @return             The TreeMap of the file bytes with their frequencies
     * @throws IOException Exception if the file has less than two different bytes
     */
    private TreeMap<Byte, Integer> getFrequencies() throws IOException {
        BufferedInputStream fis;
        fis = new BufferedInputStream(new FileInputStream(fileIn));
        byte[] buffer = new byte[BUFFER_SIZE];
        int i, bytesInBuffer;
        byte tempByte;
        TreeMap<Byte, Integer> bytes = new TreeMap<>();
        while ((bytesInBuffer = fis.read(buffer)) != -1) {
            for (i = 0; i < bytesInBuffer; i++) {
                tempByte = buffer[i];
                bytes.put(tempByte, bytes.get(tempByte) == null ? 1 : bytes.get(tempByte) + 1);
            }
        }
        fis.close();
        /* Check if the source file has less than two different symbols */
        if (bytes.size() < ALLOWED_DIFFERENT_BYTES_IN_FILE) {
            throw new IOException(NOT_ALLOWED_FILE_MESSAGE);
        }
        return bytes;
    }

    /**
     * Gets the Huffman tree as a HuffmanTreeNode object with its structure and features
     *
     * @param frequencies TreeMap of frequencies of every byte in the source file
     * @return            Huffman tree
     */
    private HuffmanTreeNode getHuffmanTree(TreeMap<Byte, Integer> frequencies) {
        /* Use the priority queue with the comparator for convenient counting */
        PriorityQueue<HuffmanTreeNode> nodesQueue =
                new PriorityQueue<>(Comparator.comparingInt(huffmanTreeNode -> huffmanTreeNode.frequencyValue));
        for (Map.Entry<Byte, Integer> freq : frequencies.entrySet()) {
            nodesQueue.add(new HuffmanTreeNode(freq.getKey(), freq.getValue()));
        }
        /* One object in the queue means that it is the root of the Huffman tree */
        while (nodesQueue.size() > 1)
        {
            /* Poll two objects with minimal frequencies */
            HuffmanTreeNode first = nodesQueue.poll();
            HuffmanTreeNode second = nodesQueue.poll();
            if (first != null && second != null) {
                /* Get sum of th two minimal frequencies */
                int newFrequencyValue = first.frequencyValue + second.frequencyValue;
                /* Put a new object with gotten frequency and two children of polled objects to the same queue */
                nodesQueue.add(new HuffmanTreeNode(newFrequencyValue, first, second));
            }
        }
        return nodesQueue.peek();
    }

    /**
     * Gets recursively a set of correspondences  of bytes and their representations for file coding
     * If current is leaf, put to table its value and the gotten string as a path to the lief
     * If current is node, try to go deeper in the tree and write the corresponding part of a path
     *
     * @param currentNode The current node for processing. Initial value is the root of th Huffman tree
     * @param string      The current string of bits as a coding sequence as a path where to go in the tree
     */
    private void buildCodingTable(HuffmanTreeNode currentNode, String string) {
        if (currentNode != null) {
            if (currentNode.isLeaf()) {
                codingTable.put(currentNode.byteValue, string);
            }
            /* If node, run the same method recursively for the gotten children */
            buildCodingTable(currentNode.leftChild, string + GO_LEFT);
            buildCodingTable(currentNode.rightChild, string + GO_RIGHT);
        }
    }

    /**
     * Packs the Huffman tree as HuffmanTreeNode object to a StringBuilder
     * The initial value is the Huffman tree root
     *
     * @param currentNode The Huffman tree node. The root of the Huffman tree as initial
     */
    private void packHuffmanTree(HuffmanTreeNode currentNode) {
        if (currentNode.isLeaf()) {
            /* If a leaf, add its value to leaves list and "0" to tree shape string */
            treeLeaves.add(currentNode.byteValue);
            treeShape.append(0);
        } else {
            /* If a node, add "1" to tree shape string and relaunch itself for its children */
            treeShape.append(1);
            packHuffmanTree(currentNode.leftChild);
            packHuffmanTree(currentNode.rightChild);
        }
    }

    /** Reads, archives and writes a file buffer by buffer using a stream */
    private void ProcessFile() throws IOException {
        BufferedInputStream fis;
        BufferedOutputStream fos;
        fis = new BufferedInputStream(new FileInputStream(fileIn));
        fos = new BufferedOutputStream(new FileOutputStream(fileOut));
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesInBuffer;
        byte[] headerData = buildFileHeaderData();
        /* Write header data to the output file */
        fos.write(headerData);
        fos.write(processedTreeShape);
        fos.write(processedTreeLeaves);
        fos.flush();
        /* Read the data to process by specified buffers */
        while ((bytesInBuffer = fis.read(buffer)) != -1) {
            byte[] bytesToProcess = Arrays.copyOfRange(buffer, 0, bytesInBuffer);
            byte[] processedData = getProcessedData(bytesToProcess, binaryStack);
            /* Write the processed data by specified buffers */
            fos.write(processedData);
        }
        /* Process and write the append data if it is */
        if (binaryStack.length() != 0) {
            byte[] appendData = getProcessedData(null, binaryStack);
            fos.write(appendData);
        }
        fos.flush();
        fis.close();
    }

    /** Builds header data for a current file */
    private byte[] buildFileHeaderData() throws IOException {
        /* Tree shape size as the number of bits in the binary representation */
        int treeShapeSize = treeShape.length();
        /* Get tree shape as the bytes array */
        processedTreeShape = getProcessedData(null, treeShape);
        /* Get tree leaves as the bytes array */
        processedTreeLeaves = new byte[treeLeaves.size()];
        for (int i = 0; i < treeLeaves.size(); i++) {
            processedTreeLeaves[i] = treeLeaves.get(i);
        }
        /* Define the header's size */
        int fileHeaderSize = FILE_SIZE_IN_BYTES +
                TREE_SHAPE_IN_BITES +
                TREE_SIZE_IN_BYTES +
                TREE_LEAVES_NUMBER_IN_BYTES;
        byte[] headerData = new byte[fileHeaderSize];
        int bytesCounter = 0;
        /* File size  in 8 bytes*/
        for (byte num : intToBytesArray(Tools.getFileSize(fileIn))) {
            headerData[bytesCounter++] = num;
        }
        /* Tree bites size in 2 bytes */
        for (byte num : intToBytesArray(treeShapeSize)) {
            headerData[bytesCounter++] = num;
        }
        /* Tree bytes size in 2 bytes */
        for (byte num : intToBytesArray(processedTreeShape.length)) {
            headerData[bytesCounter++] = num;
        }
        /* Tree leaves amount in 2 bytes */
        for (Byte num : intToBytesArray(treeLeaves.size())) {
            headerData[bytesCounter++] = num;
        }
        return headerData;
    }

    /**
     * Gets processed input data in bytes array using binary form to process
     *
     * @param bytesToProcess Array of bytes to process
     * @param builder        StringBuilder with binary data to process
     * @return               Processed data bytes array
     */
    private byte[] getProcessedData(byte[] bytesToProcess, StringBuilder builder) {
        /* Build binary string with binary data from coding table corresponding
         to bytes from the source file */
        if (bytesToProcess != null) {
            for (byte num : bytesToProcess) {
                builder.append(codingTable.get(num));
            }
        } else {
            /* Check if the binary String has sufficient signs number */
            int left = builder.length() % BYTE_SIZE;
            if (left != 0) {
                left = BYTE_SIZE - left;
                while (left > 0) {
                    builder.append(0);
                    left--;
                }
            }
        }
        byte[] processedData = new byte[(builder.length() / BYTE_SIZE)];
        int start = 0;
        int word = BYTE_SIZE;
        int counter = 0;
        /* Fill the output buffer with new data values until binary stack has full words */
        while ((start + word) < builder.length() + 1) {
            int newByte = Integer.parseInt(builder.substring(start, start + word), 2);
            processedData[counter++] = ((byte) newByte);
            start += word;
        }
        builder.delete(0, start);
        return processedData;
    }

    /**
     * Converts long type to bytes array
     *
     * @param num Long number to convert
     * @return    Array of specified bytes amount
     */
    private byte[] intToBytesArray(long num) {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES_IN_LONG);
        buffer.putLong(num);
        return buffer.array();
    }

    /**
     * Converts integer type to two bytes array
     *
     * @param num Integer number to convert
     * @return    Array of bytes
     */
    private static byte[] intToBytesArray(int num) {
        return new byte[] {
                (byte)((num >> 8) & 0xff),
                (byte)((num) & 0xff),
        };
    }

    /** Gets the efficiency result of the process.*/
    private double getCompressingEfficiency(
            String fileIn, String fileOut
    ) throws IOException {
        return (double) (100-(Files.size(Paths.get(fileOut)) *
                100 / (Files.size(Paths.get(fileIn)))));
    }

    /** Prints results of the process to the console*/
    public void printProcessResults() throws IOException {
        double efficiency = getCompressingEfficiency(fileIn, fileOut);
        System.out.println("Original file name....: " + fileIn);
        System.out.println("Compression efficiency: " +
                String.format("%1$,.2f%%", efficiency));
        System.out.println("Input file size.......: " +
                (String.format("%,dB", Tools.getFileSize(fileIn))));
        System.out.println("Output file size......: " +
                (String.format("%,dB", Tools.getFileSize(fileOut))));
        System.out.println("Compressed file name..: " + fileOut);
    }
}