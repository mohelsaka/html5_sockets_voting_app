import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Main {

    public static void main(String[] args) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
        	
        	System.out.println("Enter your regex: ");
            Pattern pattern = Pattern.compile(br.readLine());
            
            System.out.println("Enter input string to search: ");
            Matcher matcher = pattern.matcher(br.readLine());

            boolean found = false;
            while (matcher.find()) {
                System.out.println(String.format("I found the text" +
                    " \"%s\" starting at " +
                    "index %d and ending at index %d.%n",
                    matcher.group(),
                    matcher.start(),
                    matcher.end()));
                found = true;
            }
            if(!found){
                System.out.println("No match found.");
            }
        }
    }
}