import java.util.ArrayList;
import java.util.List;

public class TrieNode  {
    private TrieNode[] children;
    private boolean isEndOfWord;
    private List<String> words;


    public TrieNode() {
        children = new TrieNode[27]; // Assuming only lowercase alphabetical characters and a space
        isEndOfWord = false;
        words = words = new ArrayList<>();
    }

    public TrieNode getChild(char ch) {
        if (ch == ' ') {
            return children[26]; // Index 26 represents space
        }
        return children[ch - 'a'];
    }

    public void setChild(char ch, TrieNode node) {
        if (ch == ' ') {
            children[26] = node; // Index 26 represents space
        }
        else{
            children[ch - 'a'] = node;
        }

    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setEndOfWord(boolean isEndOfWord) {
        this.isEndOfWord = isEndOfWord;
    }
    public void addWord(String word) {
        words.add(word);
    }

    public List<String> getWords() {
        return words;
    }

    public TrieNode[] getChildren()  {
        return children;
    }
}