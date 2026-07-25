package jforgame.commons.trie;


import jforgame.commons.thread.ThreadSafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Trie tree, also known as prefix tree, is a common data structure in algorithms. It is mainly used to solve the problem of associating complete words through prefixes.
 * Can be used for dirty word detection, friend fuzzy query, etc.
 *
 * @since 2.4.0
 */
@ThreadSafe
public class TrieDictionary {

    /**
     * Threshold, when the number of child nodes is less than or equal to the threshold, convert map container to array
     */
    private static final int THRESHOLD = 3;
    /**
     * Prefix root node
     */
    private final TrieNode root;

    /**
     * Read-write lock for thread-safe access
     * <p>
     * Read lock: allow concurrent multi-thread query operations
     * Write lock: exclusive lock for add/delete/rebuild structure modification operations
     * </p>
     */
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public TrieDictionary() {
        this.root = new TrieNode((char) 0);
    }

    /**
     * Add single word to trie dictionary
     * <p>
     * Not recommended for batch initialization scenarios.
     * Each call competes for write lock individually, which causes performance loss.
     * For mass word loading on startup, please use {@link #addAllNode(Collection)} instead.
     * </p>
     *
     * @param word single word to add
     */
    public void addNode(String word) {
        writeLock.lock();
        try {
            word = normalize(word);
            if (word.isEmpty()) {
                return;
            }
            root.addChild(word, 0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Batch add multiple words to trie dictionary
     * <p>
     * Optimized for startup mass initialization scenario.
     * Only acquire write lock ONCE for the entire batch, greatly reduce lock competition overhead.
     * <b>Recommended</b> for full dictionary loading on application startup.
     * </p>
     *
     * @param words collection of words to add in batch
     */
    public void addAllNode(Collection<String> words) {
        if (words == null || words.isEmpty()) {
            return;
        }
        writeLock.lock();
        try {
            for (String word : words) {
                String normWord = normalize(word);
                if (!normWord.isEmpty()) {
                    root.addChild(normWord, 0);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Deletes a word node
     * <p>
     * Exclusive write lock is applied, suitable for rare runtime deletion scenarios
     * </p>
     *
     * @param word the word to delete
     * @return whether the deletion was successful
     * @since 2.5.0
     */
    public boolean deleteNode(String word) {
        writeLock.lock();
        try {
            word = normalize(word);
            if (word.isEmpty()) {
                return false;
            }
            return deleteNodeRecursive(root, word, 0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Recursively deletes a word node
     *
     * @param node  the current node
     * @param word  the word to delete
     * @param index the current character index being processed
     * @return whether the deletion was successful
     */
    private boolean deleteNodeRecursive(TrieNode node, String word, int index) {
        // If all characters have been processed
        if (index >= word.length()) {
            // If the current node is a leaf node, remove the leaf marker
            if (node.isLeaf()) {
                node.setLeaf(false);
                return true;
            }
            return false;
        }

        char currentChar = word.charAt(index);
        TrieNode childNode = node.getChild(currentChar);

        if (childNode == null) {
            // Word does not exist
            return false;
        }

        // Recursively delete the next character
        boolean deleted = deleteNodeRecursive(childNode, word, index + 1);

        if (deleted) {
            // If the child node was deleted and the current child node has no other child nodes and is not a leaf node, delete the current child node
            if (!childNode.isLeaf() && childNode.getChildren().isEmpty()) {
                node.removeChild(currentChar);
            }
        }

        return deleted;
    }

    /**
     * Checks if the specified string contains words
     * <p>
     * Shared read lock, supports high-concurrency query scenarios
     * </p>
     *
     * @param word the string to check
     * @return whether it contains words
     */
    public boolean containsWords(String word) {
        readLock.lock();
        try {
            word = normalize(word);
            for (int i = 0; i < word.length(); i++) {
                if (root.hasPrefix(word, i) != -1) {
                    return true;
                }
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if the dictionary exactly matches a word
     * For example, if "张无" is a word, but "张无忌" should not be
     * <p>
     * Shared read lock, supports high-concurrency query scenarios
     * </p>
     *
     * @param word the word to check
     * @return whether it exactly matches
     * @since 2.5.0
     */
    public boolean containsExactWord(String word) {
        readLock.lock();
        try {
            word = normalize(word);
            if (word.isEmpty()) {
                return false;
            }
            return root.hasExactWord(word, 0);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Replaces words with character '*', if any
     * <p>
     * Shared read lock, supports high-concurrency sensitive word replacement
     * </p>
     *
     * @param content the string to process
     * @return the converted string
     */
    public String replaceWords(String content) {
        readLock.lock();
        try {
            String normalizedString = normalize(content);
            List<int[]> indexList = new ArrayList<>();
            int end = -1, len = normalizedString.length();
            for (int i = 0; i < len; ) {
                if ((end = root.hasPrefix(normalizedString, i)) != -1) {
                    indexList.add(new int[]{i, end});
                    i = end;
                } else {
                    i++;
                }
            }
            if (indexList.isEmpty()) {
                return content;
            } else {
                StringBuilder sb = new StringBuilder(normalizedString);
                for (int[] indexArray : indexList) {
                    for (int i = indexArray[0]; i < indexArray[1]; i++) {
                        sb.setCharAt(i, '*');
                    }
                }
                return sb.toString();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * String preprocessing, convert English to lowercase (remove special symbols, keep only letters, numbers, Chinese)
     *
     * @param dirtyWord original string
     * @return the converted string
     */
    private String normalize(String dirtyWord) {
        if (dirtyWord == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dirtyWord.length(); i++) {
            char c = dirtyWord.charAt(i);
            if (Character.isLetterOrDigit(c) || isChineseCharacter(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * Judge whether the character is a Chinese character
     *
     * @param c target character
     * @return true if chinese character, otherwise false
     */
    private boolean isChineseCharacter(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS;
    }

    /**
     * After the entire tree is built, restructure the child nodes
     * If the number of child nodes of a node is less than the threshold, convert the map container to array
     * <p>
     * Exclusive write lock, used for offline optimization of trie structure
     * </p>
     */
    public void rebuild() {
        writeLock.lock();
        try {
            rebuildChildren(root);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Recursively restructure trie node children container
     *
     * @param node current trie node
     */
    private void rebuildChildren(TrieNode node) {
        if (node.children instanceof MapNodeContainer) {
            if (node.children.size() <= THRESHOLD) {
                node.children = node.children.transform();
            }
        }
        // Recursively process child nodes
        node.children.getAll().forEach(this::rebuildChildren);
    }

    /**
     * Get trie root node
     *
     * @return root node
     */
    public TrieNode getRoot() {
        return root;
    }

}