import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LexicalAnalyzer {
    private static String sourceCode;
    private static List<Token> tokens = new ArrayList<>();
    private static int currentPosition;
    private static int currentLine;

    public static List<Token> analyze(String code) {
        sourceCode = code;
        clear();
        while (currentPosition < sourceCode.length()) {
            char currentChar = sourceCode.charAt(currentPosition);
            if (Character.isLetter(currentChar)) {
                processIdentifierOrKeyword();
            } else if (Character.isDigit(currentChar)) {
                processNumber();
            } else if (currentChar == '"') {
                processString();
            } else if (currentChar == '\'') {
                processCharacter();
            } else if (isOperator(currentChar)) {
                processOperator();
            } else if (currentChar == ';') {
                processSeparator(TokenType.SEPARATOR, String.valueOf(currentChar));
            } else if (isDelimiter(currentChar)) {
                processDelimiter();
            } else if (currentChar == '#') {
                processPreprocessorDirective();
            } else if (currentChar == '/') {
                if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '/') {
                    processSingleLineComment();
                } else if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '*') {
                    processMultiLineComment();
                } else {
                    currentPosition++;
                }
            } else if(currentChar == '\n'){
                currentPosition++;
                currentLine++;
            }else if (!Character.isWhitespace(currentChar)) {
                // Handle unrecognized characters or symbols
                currentPosition++;
            } else {
                currentPosition++;
            }
        }

        return tokens;
    }

    private static void clear() {
        tokens.clear();
        currentPosition = 0;
        currentLine = 1;
    }

    private static void processIdentifierOrKeyword() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && (Character.isLetterOrDigit(sourceCode.charAt(currentPosition)) || sourceCode.charAt(currentPosition) == '_')) {
            currentPosition++;
        }

        String identifier = sourceCode.substring(start, currentPosition);

        
        if (isKeyword(identifier)) {
            tokens.add(new Token(TokenType.KEYWORD, identifier,currentLine));
        } else {
            tokens.add(new Token(TokenType.IDENTIFIER, identifier,currentLine));
        }
    }

    private static void processNumber() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && (Character.isDigit(sourceCode.charAt(currentPosition)) || sourceCode.charAt(currentPosition) == '.')) {
            currentPosition++;
        }

        String number = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.NUMBER_LITERAL, number,currentLine));
    }

    private static void processString() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && sourceCode.charAt(currentPosition) != '"') {
            currentPosition++;
        }

        if (currentPosition < sourceCode.length()) {
            currentPosition++;
        }

        String str = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.STRING_LITERAL, str,currentLine));
    }

    private static void processCharacter() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && sourceCode.charAt(currentPosition) != '\'') {
            currentPosition++;
        }

        if (currentPosition < sourceCode.length()) {
            currentPosition++;
        }

        String character = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.CHARACTER_LITERAL, character,currentLine));
    }

    private static void processOperator() {
        char currentChar = sourceCode.charAt(currentPosition);

        if (currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/' || currentChar == '%') {
            tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar),currentLine));
            currentPosition++;
        } else if (currentChar == '=') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '=') {
                tokens.add(new Token(TokenType.OPERATOR, "==",currentLine));
                currentPosition += 2;
            } else {
                tokens.add(new Token(TokenType.OPERATOR, "=",currentLine));
                currentPosition++;
            }
        } else if (currentChar == '&') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '&') {
                tokens.add(new Token(TokenType.OPERATOR, "&&",currentLine));
                currentPosition += 2;
            } else {
                currentPosition++;
            }
        } else if (currentChar == '|') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '|') {
                tokens.add(new Token(TokenType.OPERATOR, "||",currentLine));
                currentPosition += 2;
            } else {
                currentPosition++;
            }
        } else if (currentChar == '!') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '=') {
                tokens.add(new Token(TokenType.OPERATOR, "!=",currentLine));
                currentPosition += 2;
            } else {
                tokens.add(new Token(TokenType.OPERATOR, "!",currentLine));
                currentPosition++;
            }
        } else if (currentChar == '<' || currentChar == '>') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '=') {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar) + "=",currentLine));
                currentPosition += 2;
            } else if(currentPosition + 1 < sourceCode.length() && currentChar == '<' && sourceCode.charAt(currentPosition+1) == '<'){
                tokens.add(new Token(TokenType.OPERATOR, "<<",currentLine));
                currentPosition += 2;
            }else if(currentPosition + 1 < sourceCode.length() && currentChar == '>' && sourceCode.charAt(currentPosition+1) == '>'){
                tokens.add(new Token(TokenType.OPERATOR, ">>",currentLine));
                currentPosition += 2;
            }else {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar),currentLine));
                currentPosition++;
            }
        } else {
            currentPosition++;
        }
    }

    private static void processSeparator(TokenType tokenType, String value) {
        tokens.add(new Token(tokenType, value,currentLine));
        currentPosition++;
    }

    private static void processDelimiter() {
        char currentChar = sourceCode.charAt(currentPosition);
        tokens.add(new Token(TokenType.DELIMITER, String.valueOf(currentChar),currentLine));
        currentPosition++;
    }

    private static void processPreprocessorDirective() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && !Character.isWhitespace(sourceCode.charAt(currentPosition))) {
            currentPosition++;
        }

        String directive = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.PREPROCESSOR_DIRECTIVE, directive,currentLine));
    }

    private static void processSingleLineComment() {
        int start = currentPosition;
        currentPosition += 2; // Skip the "//" characters

        while (currentPosition < sourceCode.length() && sourceCode.charAt(currentPosition) != '\n') {
            currentPosition++;
        }

        String comment = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.COMMENT, comment,currentLine));
    }

    private static void processMultiLineComment() {
        int start = currentPosition;
        currentPosition += 2; // Skip the "/*" characters

        while (currentPosition + 1 < sourceCode.length() && !(sourceCode.charAt(currentPosition) == '*' && sourceCode.charAt(currentPosition + 1) == '/')) {
            currentPosition++;
        }

        if (currentPosition + 1 < sourceCode.length()) {
            currentPosition += 2; // Skip the "*/" characters
        }

        String comment = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.COMMENT, comment,currentLine));
    }
    
    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '=' || c == '&' || c == '|' || c == '!' || c == '<' || c == '>';
    }

    private static boolean isDelimiter(char c) {
        return c == ';' || c == ',' || c == '{' || c == '}' || c == '[' || c == ']' || c == '(' || c == ')';
    }

    private static boolean isKeyword(String identifier) {
        List<String> keywords = Arrays.asList("auto", "break", "case", "const", "continue", "default", "delete", "do", "else", "for", "if", "int", "double", "string", "char", "return", "switch", "void", "while", "cout", "cin", "endl");
        return keywords.contains(identifier);
    }
}
