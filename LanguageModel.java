import java.util.HashMap;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */

    // Trains the language model using text from a file
    public void train(String fileName) {
            String window = "";
            char c;
            In in = new In(fileName);
            // Reads just enough characters to form the first window
            for (int i = 0; i < windowLength; i++) {
                c = (char) in.readChar();
                window += c;
            }
            // Processes the entire text, one character at a time
            while (!in.isEmpty()) {
            // Gets the next character
            c = in.readChar();
            // Checks if the window is already in the map
            List probs = CharDataMap.get(window);
            // If the window was not found in the map
            if (probs == null) {
                // Creates a new empty list, and adds (window, list) to the map
                probs = new List();
                CharDataMap.put(window, probs);
            }
            // Calculates the counts of the current character.
            probs.update(c);
            // Advances the window: adds c to the window’s end, and deletes the
            // window's first character.
            window = window.substring(1) + c;
            }
            // The entire file has been processed, and all the characters have been counted.
            // Proceeds to compute and set the p and cp fields of all the CharData objects
            // in each linked list in the map.
            for (List probs : CharDataMap.values()){
            calculateProbabilities(probs);
            }
            }
        
    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {	
        // Step 1: Compute total number of characters
         // Step 1: Compute total number of characters
         int totalChars = 0;
         for (int i = 0; i < probs.getSize(); i++) {
             CharData cd = (CharData) probs.get(i);
             totalChars += cd.count;
         }
        // Step 2: Compute and set the probabilities and cumulative probabilities
        double cumulativeProbability = 0.0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = (CharData) probs.get(i);
            double probability = (double) cd.count / totalChars;
            cumulativeProbability += probability;

            cd.p=probability;
            cd.cp=cumulativeProbability;
        }
    }

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		 // Generate a random number between 0 and 1 (exclusive).
         double r = randomGenerator.nextDouble();
         // Iterate through the list and find the character corresponding to the random number.
         for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            if (r < cd.cp) {
                return cd.chr;
            }
        }

         // If no character is found (this should not happen), return null.
         return '\0'; // return null character.
     }
 

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        // If the length of initial text is less than windowLength, return initialText
        if (initialText.length() < windowLength) {
            return initialText;
        }
        // Use a StringBuilder to store the generated text
        StringBuilder generatedText = new StringBuilder(initialText);
        // Set the initial window to the last windowLength characters of initialText
        String window = initialText.substring(initialText.length() - windowLength);
        // Loop until the generated text reaches the desired length
        while (generatedText.length() < textLength) {
            // Get the list of probabilities for the current window
            List probs = CharDataMap.get(window);
            // If the window is not in the map, stop the process and return the generated text
            if (probs == null) {
                break;
            }
            // Get a random character from the probabilities list and append it to the generated text
            char nextChar = getRandomChar(probs);
            generatedText.append(nextChar);
            // Update the window by removing the first character
            window = window.substring(1) + nextChar;
        }
        // Return the generated text
        return generatedText.toString();
    }

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
