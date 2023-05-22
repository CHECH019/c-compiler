import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LexicalAnalyzer {
    private String sourceCode;
    private List<Token> tokens;
    private int currentPosition;

    public LexicalAnalyzer(String sourceCode) {
        this.sourceCode = sourceCode;
        this.tokens = new ArrayList<>();
        this.currentPosition = 0;
    }

    public List<Token> analyze() {
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
            } else if (!Character.isWhitespace(currentChar)) {
                // Handle unrecognized characters or symbols
                currentPosition++;
            } else {
                currentPosition++;
            }
        }

        return tokens;
    }

    private void processIdentifierOrKeyword() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && (Character.isLetterOrDigit(sourceCode.charAt(currentPosition)) || sourceCode.charAt(currentPosition) == '_')) {
            currentPosition++;
        }

        String identifier = sourceCode.substring(start, currentPosition);

        // Check if the identifier is a keyword
        if (isKeyword(identifier)) {
            tokens.add(new Token(TokenType.KEYWORD, identifier));
        } else {
            tokens.add(new Token(TokenType.IDENTIFIER, identifier));
        }
    }

    private void processNumber() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && (Character.isDigit(sourceCode.charAt(currentPosition)) || sourceCode.charAt(currentPosition) == '.')) {
            currentPosition++;
        }

        String number = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.NUMBER_LITERAL, number));
    }

    private void processString() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && sourceCode.charAt(currentPosition) != '"') {
            currentPosition++;
        }

        if (currentPosition < sourceCode.length()) {
            currentPosition++;
        }

        String str = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.STRING_LITERAL, str));
    }

    private void processCharacter() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && sourceCode.charAt(currentPosition) != '\'') {
            currentPosition++;
        }

        if (currentPosition < sourceCode.length()) {
            currentPosition++;
        }

        String character = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.CHARACTER_LITERAL, character));
    }

    private void processOperator() {
        char currentChar = sourceCode.charAt(currentPosition);

        if (currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/' || currentChar == '%') {
            tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar)));
            currentPosition++;
        } else if (currentChar == '=') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '=') {
                tokens.add(new Token(TokenType.OPERATOR, "=="));
                currentPosition += 2;
            } else {
                tokens.add(new Token(TokenType.OPERATOR, "="));
                currentPosition++;
            }
        } else if (currentChar == '&') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '&') {
                tokens.add(new Token(TokenType.OPERATOR, "&&"));
                currentPosition += 2;
            } else {
                currentPosition++;
            }
        } else if (currentChar == '|') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '|') {
                tokens.add(new Token(TokenType.OPERATOR, "||"));
                currentPosition += 2;
            } else {
                currentPosition++;
            }
        } else if (currentChar == '!') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '=') {
                tokens.add(new Token(TokenType.OPERATOR, "!="));
                currentPosition += 2;
            } else {
                tokens.add(new Token(TokenType.OPERATOR, "!"));
                currentPosition++;
            }
        } else if (currentChar == '<' || currentChar == '>') {
            if (currentPosition + 1 < sourceCode.length() && sourceCode.charAt(currentPosition + 1) == '=') {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar) + "="));
                currentPosition += 2;
            } else {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar)));
                currentPosition++;
            }
        } else {
            currentPosition++;
        }
    }

    private void processSeparator(TokenType tokenType, String value) {
        tokens.add(new Token(tokenType, value));
        currentPosition++;
    }

    private void processDelimiter() {
        char currentChar = sourceCode.charAt(currentPosition);
        tokens.add(new Token(TokenType.DELIMITER, String.valueOf(currentChar)));
        currentPosition++;
    }

    private void processPreprocessorDirective() {
        int start = currentPosition;
        currentPosition++;

        while (currentPosition < sourceCode.length() && !Character.isWhitespace(sourceCode.charAt(currentPosition))) {
            currentPosition++;
        }

        String directive = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.PREPROCESSOR_DIRECTIVE, directive));
    }

    private void processSingleLineComment() {
        int start = currentPosition;
        currentPosition += 2; // Skip the "//" characters

        while (currentPosition < sourceCode.length() && sourceCode.charAt(currentPosition) != '\n') {
            currentPosition++;
        }

        String comment = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.COMMENT, comment));
    }

    private void processMultiLineComment() {
        int start = currentPosition;
        currentPosition += 2; // Skip the "/*" characters

        while (currentPosition + 1 < sourceCode.length() && !(sourceCode.charAt(currentPosition) == '*' && sourceCode.charAt(currentPosition + 1) == '/')) {
            currentPosition++;
        }

        if (currentPosition + 1 < sourceCode.length()) {
            currentPosition += 2; // Skip the "*/" characters
        }

        String comment = sourceCode.substring(start, currentPosition);
        tokens.add(new Token(TokenType.COMMENT, comment));
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '=' || c == '&' || c == '|' || c == '!' || c == '<' || c == '>';
    }

    private boolean isDelimiter(char c) {
        return c == ';' || c == ',' || c == '{' || c == '}' || c == '[' || c == ']';
    }

    private boolean isKeyword(String identifier) {
        List<String> keywords = Arrays.asList("auto", "break", "case", "const", "continue", "default", "delete", "do", "else", "for", "if", "int", "double", "string", "char", "return", "switch", "void", "while");
        return keywords.contains(identifier);
    }
}
