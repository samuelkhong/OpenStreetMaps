import java.util.ArrayList;
import java.util.List;

public class Trie {
    private TrieNode root;
    public Trie() {
        root = new TrieNode();
    }

    public List<String> findByPrefix(String prefix) {
        List<String> result = new ArrayList<>();

        TrieNode currentNode = root;
        // iterate through Trie to see any matching
        for (int i = 0; i < prefix.length(); i++) {
            char ch = prefix.charAt(i);
            TrieNode childNode = currentNode.getChild(ch);
            if (childNode == null) {
                return result; // No matching prefix found
            }
            currentNode = childNode;
        }
        collectWords(currentNode, result);
        return result;
    }

    private void collectWords(TrieNode node, List<String> result) {
        if (node.isEndOfWord()) {
            result.addAll(node.getWords());
        }
        // It then recursively calls collectWords() on each non-null child node to collect words from the child nodes
        for (TrieNode child : node.getChildren()) {
            if (child != null) {
                collectWords(child, result);
            }
        }
    }
    public void insert(String word) {

        TrieNode currentNode = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            TrieNode childNode = currentNode.getChild(ch);
            if (childNode == null) {
                childNode = new TrieNode();
                currentNode.setChild(ch, childNode);
            }
            currentNode = childNode;
        }
        currentNode.setEndOfWord(true);
        currentNode.addWord(word);

    }
}