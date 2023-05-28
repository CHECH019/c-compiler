import java.util.ArrayList;
import java.util.List;

public class SyntacticAnalyzer {
    
    private List<Token> tokens;
    private int currentPosition;
    private Token currentToken;
    private List<String> analysisDetails;

    public SyntacticAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        this.currentPosition = 0;
        this.currentToken = tokens.get(0);
        this.analysisDetails = new ArrayList<>();
    }

    public List<String> analyze() {        
        while (currentPosition < tokens.size() && parseStatement()) ;
        
        if (currentPosition >= tokens.size())
            log("Syntax analysis completed without errors");
        
        return analysisDetails;
    }

    private boolean parseStatement() {
        if (compareToken(TokenType.KEYWORD)) {
            String tokenValue = currentToken.getValue();
            if (tokenValue.equals("if")) {
                return ifStatement();
            }else if(tokenValue.equals("else")){
                return elseStatement();
            } else if (tokenValue.equals("while")) {
                // return whileStatement();                
                return true;
            } else if (tokenValue.equals("for")) {
                return forStatement();
            } else if (tokenValue.equals("int") || tokenValue.equals("double") || tokenValue.equals("string")) {
                return declarationStatement();
            }else if(tokenValue.equals("cout")){
                return outputStatement();
            }else if(tokenValue.equals("return")){
                return returnStatement();
            }else{
                return true;
            }
        } else if (compareToken(TokenType.IDENTIFIER)) {
            return assignmentStatement();
        } else if(compareToken(TokenType.PREPROCESSOR_DIRECTIVE)){
            return preprocessorDirective();
        }else {
            advance();
            return true;
        }
    }
    
    private boolean elseStatement() {
        advance();
        if(!match(TokenType.DELIMITER, "{")) return false;
        do{
            if(!parseStatement()) return false;
        }while(!compareToken(TokenType.DELIMITER, "}") && currentPosition < tokens.size());
        if(!match(TokenType.DELIMITER, "}")) return false;
        return true;
    }

    private boolean preprocessorDirective() {
        advance();
        if(!match(TokenType.OPERATOR, "<")) return false;
        if(!match(TokenType.IDENTIFIER)) return false;
        if(!match(TokenType.OPERATOR, ">")) return false;
        return true;
    }

    private boolean ifStatement() {
        advance();
        if(!match(TokenType.DELIMITER, "(")) return false;
        if(!parseLogicalExpression()) return false;;
        
        if(!match(TokenType.DELIMITER, ")")) return false;
        if(!match(TokenType.DELIMITER, "{")) return false;
        do{
            if(!parseStatement()) return false;
        }while(!compareToken(TokenType.DELIMITER, "}") && currentPosition < tokens.size());
        if(!match(TokenType.DELIMITER, "}")) return false;
        return true;
    }
    private boolean outputStatement(){
        advance();
        if(compareToken(TokenType.OPERATOR, "<<")){
            do {
                match(TokenType.OPERATOR, "<<");
                if(compareToken(TokenType.STRING_LITERAL)){
                    match(TokenType.STRING_LITERAL);
                }else if(compareToken(TokenType.NUMBER_LITERAL)){
                    match(TokenType.NUMBER_LITERAL);
                }else if(compareToken(TokenType.IDENTIFIER)){
                    match(TokenType.IDENTIFIER);
                }
                
            }while(compareToken(TokenType.OPERATOR, "<<"));

            if(compareToken(TokenType.KEYWORD, "endl")){            
                match(TokenType.KEYWORD, "endl");
            }
            if(!match(TokenType.SEPARATOR, ";")) return false;
        }else{
            if(!match(TokenType.OPERATOR, "<<")) return false;
        }
        return true;
    }
    // private void whileStatement() {
    //     advance();
    //     match(TokenType.KEYWORD, "while");
    //     match(TokenType.DELIMITER, "(");
    //     parseExpression();
    //     match(TokenType.DELIMITER, ")");
    //     parseStatement();
    // }

    private boolean forStatement() {
        advance();
        if(!match(TokenType.DELIMITER, "(")) return false;
        if(!declarationStatement()) return false;
        if(!parseLogicalExpression()) return false;
        advance();
        if(!match(TokenType.IDENTIFIER)) return false;
        if(!match(TokenType.OPERATOR,"+")) return false;
        if(!match(TokenType.OPERATOR,"+")) return false;
        
        if(!match(TokenType.DELIMITER, ")")) return false;
        if(!match(TokenType.DELIMITER,"{")) return false;
        do{
            if(!parseStatement()) return false;
        }while(!compareToken(TokenType.DELIMITER, "}") && currentPosition < tokens.size());
        if(!match(TokenType.DELIMITER,"}")) return false;
        return true;
    }
    private boolean returnStatement(){
        advance();
        if(match(TokenType.NUMBER_LITERAL) || match(TokenType.STRING_LITERAL) || match(TokenType.IDENTIFIER))
        ;
        if(!match(TokenType.SEPARATOR, ";")) return false;
        return true;

    }
    private boolean assignmentStatement (){
        advance();
        if(!match(TokenType.OPERATOR,"="))return false;
        if(!parseExpression()) return false;
        if(!match(TokenType.SEPARATOR, ";"))return false;
        return true;
    }

    private boolean declarationStatement() {
        advance();
        if(!match(TokenType.IDENTIFIER)) return false;

        if(compareToken(TokenType.SEPARATOR,";")){ // int x;
            match(TokenType.SEPARATOR, ";");
        }else if(compareToken(TokenType.OPERATOR,"=")){ //int x = (expr);
            match(TokenType.OPERATOR,"=");
            if(!parseExpression()) return false;
            if(!match(TokenType.SEPARATOR, ";")) return false;
            
        }else if(compareToken(TokenType.DELIMITER, "(")){ // int main(){statement}
            match(TokenType.DELIMITER, "(");
            if(!match(TokenType.DELIMITER, ")")) return false;
            if(!match(TokenType.DELIMITER, "{")) return false;
            do{
                if(!parseStatement()) return false;
            }while(!compareToken(TokenType.DELIMITER, "}"));
            if(!match(TokenType.DELIMITER, "}")) return false;
        }else{
            error("invalid assignment statement");
            advance();
            return false;
        }
        return true;
    }
    private boolean parseLogicalExpression() {
        // Analizar el primer factor de la expresión
        if(!parseFactor()) return false;
    
        // Analizar los posibles operadores y factores adicionales
        while (currentToken.getType() == TokenType.OPERATOR) {
            if (isLogicalOperator(currentToken.getValue())) {
                advance();
                if(!parseFactor()) return false;
            } else {
                error("Invalid operator in expression");
                advance();
                return false;
            }
        }
        return true;
    }

    private boolean parseExpression() {
        // Analizar el primer factor de la expresión
        if(!parseFactor()) return false;
    
        // Analizar los posibles operadores y factores adicionales
        while (currentToken.getType() == TokenType.OPERATOR) {
            if (isBinaryOperator(currentToken.getValue())) {
                advance();
                if(!parseFactor()) return false;
            } else {
                error("Invalid operator in expression");
                advance();
                return false;
            }
        }
        return true;
    }
    
    private boolean parseFactor() {
        // Analizar el primer elemento del factor
        if (compareToken(TokenType.IDENTIFIER) || compareToken(TokenType.NUMBER_LITERAL) || compareToken(TokenType.STRING_LITERAL)) {
            // Coincidencia exitosa, avanzar al siguiente token
            advance();
        } else if (match(TokenType.DELIMITER, "(")) {
            // Factor es una expresión entre paréntesis, analizar la expresión
            if(!parseExpression()) return false;
            if(!match(TokenType.DELIMITER, ")")) return false;
        } else {
            return false;
        }
        return true;
    }
    
    private boolean isBinaryOperator(String operator) {
        // Verificar si el operador es válido en el lenguaje que estás analizando
        return operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/");
    }
    private boolean isLogicalOperator(String operator) {
        // Verificar si el operador es válido en el lenguaje que estás analizando
        return operator.equals(">") || operator.equals("<") 
            || operator.equals("==") || operator.equals("!=") 
            || operator.equals("<=") || operator.equals(">=")
            || operator.equals("&&") || operator.equals("||");
    }
    // Métodos auxiliares para realizar coincidencias y manejar errores

    private boolean compareToken(TokenType expectedType){
        return currentToken.getType() == expectedType;
    }
    private boolean compareToken(TokenType expectedType, String expectedValue) {
        return currentToken.getType() == expectedType && currentToken.getValue().equals(expectedValue);
    }
    private boolean match(TokenType expectedType) {
        if (currentToken.getType() == expectedType) {
            advance();
            return true;
        } else {
            error("Expected token of type " + expectedType);
            advance();
            return false;
        }
    }

    private boolean match(TokenType expectedType, String expectedValue) {
        if (currentToken.getType() == expectedType && currentToken.getValue().equals(expectedValue)) {
            advance();
            return true;
        } else {
            error("Expected token of type " + expectedType + " with value '" + expectedValue + "'");
            advance();
            return false;
        }
    }

    private void advance() {
        currentPosition++;
        if (currentPosition < tokens.size()) {
            currentToken = tokens.get(currentPosition);
        }
    }

    private void error(String message) {
        System.out.println("Syntax Error: "+ message);
        log("Syntax Error at line: "+ currentToken.getLine()+": "+ message);
    }
    private void log(String message) {
        analysisDetails.add(message);
    }    
}