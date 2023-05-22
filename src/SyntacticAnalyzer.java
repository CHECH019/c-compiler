import java.util.ArrayList;
import java.util.List;

public class SyntacticAnalyzer {
    private List<Token> tokens;
    private int currentPosition;
    private boolean syntaxError;
    private List<String> analysisResult;

    public SyntacticAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        this.currentPosition = 0;
        this.syntaxError = false;
        this.analysisResult = new ArrayList<>();
    }

    public List<String> analyze() {
        parseProgram();

        if (syntaxError || currentPosition < tokens.size()) {
            analysisResult.add("Syntax error");
        } else {
            analysisResult.add("Syntax analysis completed successfully");
        }

        return analysisResult;
    }

    private void parseProgram() {
        while (currentPosition < tokens.size()) {
            parseStatement();
        }
    }

    private void parseStatement() {
        if (match(TokenType.IDENTIFIER)) {
            match(TokenType.DELIMITER, ";");
        } else if (match(TokenType.KEYWORD, "if")) {
            match(TokenType.DELIMITER, "(");
            parseExpression();
            match(TokenType.DELIMITER, ")");
            parseStatement();
            if (match(TokenType.KEYWORD, "else")) {
                parseStatement();
            }
        } else if (match(TokenType.KEYWORD, "while")) {
            match(TokenType.DELIMITER, "(");
            parseExpression();
            match(TokenType.DELIMITER, ")");
            parseStatement();
        } else {
            syntaxError();
        }
    }

    private void parseExpression() {
        parseLogicalOrExpression();
    }

    private void parseLogicalOrExpression() {
        parseLogicalAndExpression();
        while (match(TokenType.OPERATOR, "||")) {
            parseLogicalAndExpression();
        }
    }

    private void parseLogicalAndExpression() {
        parseEqualityExpression();
        while (match(TokenType.OPERATOR, "&&")) {
            parseEqualityExpression();
        }
    }

    private void parseEqualityExpression() {
        parseRelationalExpression();
        while (match(TokenType.OPERATOR, "==") || match(TokenType.OPERATOR, "!=")) {
            parseRelationalExpression();
        }
    }

    private void parseRelationalExpression() {
        parseAdditiveExpression();
        while (match(TokenType.OPERATOR, "<") || match(TokenType.OPERATOR, ">") || match(TokenType.OPERATOR, "<=") || match(TokenType.OPERATOR, ">=")) {
            parseAdditiveExpression();
        }
    }

    private void parseAdditiveExpression() {
        parseMultiplicativeExpression();
        while (match(TokenType.OPERATOR, "+") || match(TokenType.OPERATOR, "-")) {
            parseMultiplicativeExpression();
        }
    }

    private void parseMultiplicativeExpression() {
        parseUnaryExpression();
        while (match(TokenType.OPERATOR, "*") || match(TokenType.OPERATOR, "/") || match(TokenType.OPERATOR, "%")) {
            parseUnaryExpression();
        }
    }

    private void parseUnaryExpression() {
        if (match(TokenType.OPERATOR, "+") || match(TokenType.OPERATOR, "-")) {
            parseUnaryExpression();
        } else {
            parsePrimaryExpression();
        }
    }

    private void parsePrimaryExpression() {
        if (match(TokenType.IDENTIFIER) || match(TokenType.NUMBER_LITERAL) || match(TokenType.STRING_LITERAL)) {
            // Terminal expression
        } else if (match(TokenType.DELIMITER, "(")) {
            parseExpression();
            match(TokenType.DELIMITER, ")");
        } else {
            syntaxError();
        }
    }

    private boolean match(TokenType type) {
        if (currentPosition < tokens.size() && tokens.get(currentPosition).getType() == type) {
            analysisResult.add("Matched token: " + tokens.get(currentPosition).getType() + ", Value: " + tokens.get(currentPosition).getValue());
            currentPosition++;
            return true;
        }
        syntaxError();
        return false;
    }

    private boolean match(TokenType type, String value) {
        if (currentPosition < tokens.size() && tokens.get(currentPosition).getType() == type && tokens.get(currentPosition).getValue().equals(value)) {
            analysisResult.add("Matched token: " + tokens.get(currentPosition).getType() + ", Value: " + tokens.get(currentPosition).getValue());
            currentPosition++;
            return true;
        }
        syntaxError();
        return false;
    }

    private void syntaxError() {
        syntaxError = true;
        if (currentPosition < tokens.size()) {
            analysisResult.add("Syntax error near token: " + tokens.get(currentPosition).getValue());
            currentPosition++;
        } else {
            analysisResult.add("Syntax error at the end of input");
        }
    }
}
