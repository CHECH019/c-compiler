import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticAnalyzer {
    private Map<String, VariableType> symbolTable;
    private List<Token> tokens;
    private List<String> analysisDetails;

    public SemanticAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        analysisDetails = new ArrayList<>();
        symbolTable = new HashMap<>();
    }

    public List<String> analyze() {
        // Perform semantic analysis on the parsed statements
        for (Token token : tokens) {
            if (!analyzeStatement(token)) {
                // Error found, stop analysis
                return analysisDetails;
            }
        }

        return analysisDetails;
    }

    private boolean analyzeStatement(Token token) {
        if (token.getType() == TokenType.IDENTIFIER) {
            String identifier = token.getValue();
            if (!symbolTable.containsKey(identifier)) {
                analysisDetails.add("Semantic Error at line " + token.getLine() + ": Variable '" + identifier + "' is not declared");
                return false;
            }
        } else if (isVariableDeclaration(token)) {
            return analyzeVariableDeclaration(token);
        } else if (isCoutStatement(token)) {
            return analyzeCoutStatement(token);
        } else if (isAssignmentStatement(token)) {
            return analyzeAssignmentStatement(token);
        } else if (isBinaryOperation(token)) {
            return analyzeBinaryOperation(token);
        } else if (isForLoop(token)) {
            return analyzeForLoop(token);
        }

        return true;
    }

    private boolean isVariableDeclaration(Token token) {
        return token.getType() == TokenType.KEYWORD && (token.getValue().equals("int")
                || token.getValue().equals("double") || token.getValue().equals("string"));
    }

    private boolean analyzeVariableDeclaration(Token token) {
        Token nextToken = getNextToken(token);
        if (nextToken != null && nextToken.getType() == TokenType.IDENTIFIER) {
            String identifier = nextToken.getValue();
            if (symbolTable.containsKey(identifier)) {
                analysisDetails.add("Semantic Error at line " + token.getLine() + ": Variable '" + identifier + "' is already declared");
                return false;
            } else {
                VariableType variableType = getVariableType(token);
                Token assignmentToken = getNextToken(nextToken);
                if (assignmentToken != null && assignmentToken.getType() == TokenType.OPERATOR && assignmentToken.getValue().equals("=")) {
                    Token expressionToken = getNextToken(assignmentToken);
                    if (!analyzeExpressionWithType(expressionToken, variableType)) {
                        analysisDetails.add("Semantic Error at line " + token.getLine() + ": Incompatible type in variable assignment");
                        return false;
                    }
                }
                symbolTable.put(identifier, variableType);
            }
        }
        return true;
    }
    

    private VariableType getVariableType(Token token) {
        if (token.getValue().equals("int")) {
            return VariableType.INT;
        } else if (token.getValue().equals("double")) {
            return VariableType.DOUBLE;
        } else if (token.getValue().equals("string")) {
            return VariableType.STRING;
        }
        return null; // Should not reach here if the token is a valid variable type
    }

    private boolean isCoutStatement(Token token) {
        return token.getType() == TokenType.IDENTIFIER && token.getValue().equals("cout");
    }

    private boolean analyzeCoutStatement(Token token) {
        Token nextToken = getNextToken(token);
        if (nextToken != null && nextToken.getType() == TokenType.OPERATOR && nextToken.getValue().equals("<<")) {
            Token expressionToken = getNextToken(nextToken);
            return analyzeExpression(expressionToken);
        }
        return true;
    }

    private boolean isAssignmentStatement(Token token) {
        return token.getType() == TokenType.IDENTIFIER && getNextToken(token).getType() == TokenType.OPERATOR
                && getNextToken(token).getValue().equals("=");
    }

    private boolean analyzeAssignmentStatement(Token token) {
        Token identifierToken = token;
        Token assignmentToken = getNextToken(token);
        Token expressionToken = getNextToken(assignmentToken);
    
        String identifier = identifierToken.getValue();
        if (!symbolTable.containsKey(identifier)) {
            analysisDetails.add("Semantic Error at line " + token.getLine() + ": Variable '" + identifier + "' is not declared");
            return false;
        }
    
        VariableType expectedType = symbolTable.get(identifier);
        if (!isLiteral(expressionToken)) {
            if (!analyzeExpressionWithType(expressionToken, expectedType)) {
                analysisDetails.add("Semantic Error at line " + token.getLine() + ": Incompatible type in variable assignment");
                return false;
            }
        } else {
            VariableType literalType = getExpressionType(expressionToken);
            if (expectedType != literalType) {
                analysisDetails.add("Semantic Error at line " + token.getLine() + ": Incompatible types in variable assignment: " + expectedType + " and " + literalType);
                return false;
            }
        }
    
        return true;
    }
    

    private boolean isBinaryOperation(Token token) {
        return token.getType() == TokenType.OPERATOR && (token.getValue().equals("+") || token.getValue().equals("-")
                || token.getValue().equals("*") || token.getValue().equals("/"));
    }

    private boolean analyzeBinaryOperation(Token token) {
        Token leftOperandToken = getPreviousToken(token);
        Token rightOperandToken = getNextToken(token);

        if (!analyzeExpression(leftOperandToken) || !analyzeExpression(rightOperandToken)) {
            return false;
        }

        VariableType leftOperandType = getExpressionType(leftOperandToken);
        VariableType rightOperandType = getExpressionType(rightOperandToken);

        if (leftOperandType != rightOperandType) {
            analysisDetails.add("Semantic Error at line " + token.getLine() + ": Incompatible types in binary operation: " + leftOperandType + " and "
                    + rightOperandType);
            return false;
        }

        return true;
    }

    private boolean analyzeExpression(Token token) {
        if (token.getType() == TokenType.IDENTIFIER) {
            String identifier = token.getValue();
            if (!symbolTable.containsKey(identifier)) {
                analysisDetails.add("Semantic Error at line " + token.getLine() + ": Variable '" + identifier + "' is not declared");
                return false;
            }
        }
        return true;
    }

    private boolean analyzeExpressionWithType(Token token, VariableType expectedType) {
        if (!analyzeExpression(token)) {
            return false;
        }

        VariableType actualType = getExpressionType(token);
        if (expectedType != actualType) {
            analysisDetails.add("Semantic Error at line " + token.getLine() + ": Incompatible types. Expected: " + expectedType + ", Actual: " + actualType);
            return false;
        }

        return true;
    }

    private boolean analyzeForLoop(Token token) {
        Token initializerToken = getNextToken(token);
        Token conditionToken = getNextToken(initializerToken);
        Token updateToken = getNextToken(conditionToken);
        Token blockToken = getNextToken(updateToken);
        
        // Analizar la declaración del bucle for
       // Analizar la declaración del bucle for
if (initializerToken != null && initializerToken.getType() == TokenType.DELIMITER && initializerToken.getValue().equals("(")) {
    Token declarationToken = getNextToken(initializerToken);
    if (declarationToken != null && declarationToken.getType() == TokenType.KEYWORD) {
        VariableType variableType = getVariableType(declarationToken);
        Token identifierToken = getNextToken(declarationToken);
        if (identifierToken != null && identifierToken.getType() == TokenType.IDENTIFIER) {
            String identifier = identifierToken.getValue();
            if (symbolTable.containsKey(identifier)) {
                analysisDetails.add("Semantic Error at line " + declarationToken.getLine() + ": Variable '" + identifier + "' is already declared");
                return false;
            }
            symbolTable.put(identifier, variableType);
        }
    }
}

    
        return analyzeExpression(conditionToken)
                && analyzeExpression(updateToken)
                && analyzeBlockStatement(blockToken);
    }
    
    

    private boolean analyzeBlockStatement(Token token) {
        // Assuming a block statement starts with an opening curly brace '{'
        if (token != null && token.getType() == TokenType.DELIMITER && token.getValue() == "{") {
            int openingBraceCount = 1;
            int closingBraceCount = 0;

            while (openingBraceCount > closingBraceCount) {
                token = getNextToken(token);
                if (token == null) {
                    analysisDetails.add("Semantic Error: Missing closing curly brace '}' for the block statement");
                    return false;
                }

                if (token.getType() == TokenType.DELIMITER && token.getValue() == "{") {
                    openingBraceCount++;
                } else if (token.getType() == TokenType.DELIMITER && token.getValue() == "}") {
                    closingBraceCount++;
                } else {
                    analyzeStatement(token);
                }
            }

            return true;
        }

        return false;
    }

    private boolean isForLoop(Token token) {
        return token.getType() == TokenType.KEYWORD && token.getValue().equals("for");
    }

    private boolean isLiteral(Token token) {
        TokenType type = token.getType();
        return type == TokenType.NUMBER_LITERAL || type == TokenType.STRING_LITERAL;
    }

    private VariableType getExpressionType(Token token) {
        if (token.getType() == TokenType.IDENTIFIER) {
            String identifier = token.getValue();
            return symbolTable.getOrDefault(identifier, null);
        } else if (token.getType() == TokenType.NUMBER_LITERAL) {
            return VariableType.INT;
        } else if (token.getType() == TokenType.STRING_LITERAL) {
            return VariableType.STRING;
        }
        return null; // Should not reach here if the token is a valid expression
    }

    private Token getNextToken(Token currentToken) {
        int currentIndex = tokens.indexOf(currentToken);
        if (currentIndex < tokens.size() - 1) {
            return tokens.get(currentIndex + 1);
        }
        return null;
    }

    private Token getPreviousToken(Token currentToken) {
        int currentIndex = tokens.indexOf(currentToken);
        if (currentIndex > 0) {
            return tokens.get(currentIndex - 1);
        }
        return null;
    }
}