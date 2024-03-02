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
     /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName)
    {
       In in = new In(fileName);
       String window = "";
       char c;
       for (int i=0;i<windowLength;i++)
       {
           window= window+in.readChar();
       }
       while (!in.isEmpty())
       {
           c = in.readChar();
           List probs = CharDataMap.get(window);
           if (probs == null)
           {
               probs = new List();
               CharDataMap.put(window, probs);
           }
           probs.update(c);
           window=window.substring(1) + c; 
       }
       for (List probs : CharDataMap.values()) 
       {
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
		String generatedText = initialText;
        List probs = new List();
        String currWindow = initialText;
        char chr = ' ';

        if (initialText.length() < windowLength)
            return initialText;

        while (generatedText.length() < textLength + initialText.length()) {

            probs = CharDataMap.get(currWindow);

            if (probs == null)
                return generatedText;

            chr = getRandomChar(probs);

		    generatedText = generatedText + chr;

            // Advances the window: 
            currWindow = currWindow.substring(1,windowLength) + chr;
        }
        return generatedText;
	}
	

    public static void main(String[] args) {
		int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
        lm = new LanguageModel(windowLength);
        else
        lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
