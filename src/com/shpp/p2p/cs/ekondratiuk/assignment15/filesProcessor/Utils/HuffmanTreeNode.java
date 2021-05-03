package com.shpp.p2p.cs.ekondratiuk.assignment15.filesProcessor.Utils;

/**
 * Represents a node of the Huffman tree
 */
public class HuffmanTreeNode {
    /** Byte value as a leaf of the tree value */
    public byte byteValue;

    /** Frequency of a separate byte encountered in the source */
    public int frequencyValue;

    /** Huffman node object as a left child of another object of the same instance */
    public HuffmanTreeNode leftChild;

    /** Huffman node object as a right child of another object of the same instance */
    public HuffmanTreeNode rightChild;

    /** Constructor for start leaves during building the Huffman tree */
    public HuffmanTreeNode(byte byteValue, int frequencyValue){
        this.byteValue = byteValue;
        this.frequencyValue = frequencyValue;
    }

    /** Constructor for nodes with frequencies and children features */
    public HuffmanTreeNode(int frequencyValue, HuffmanTreeNode leftChild, HuffmanTreeNode rightChild) {
        this.frequencyValue = frequencyValue;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    /** Constructor for leaves which are not needed frequencies(unpacking tree) */
    public HuffmanTreeNode(byte byteValue) {
        this.byteValue = byteValue;
    }

    /** Constructor just for an empty object which are not needed any features (unpacking tree) */
    public HuffmanTreeNode() {
    }

    /** Checks if the current node is a leaf(doesn't have children). Returns true if it is */
    public boolean isLeaf() {
        return leftChild == null || rightChild == null;
    }
}