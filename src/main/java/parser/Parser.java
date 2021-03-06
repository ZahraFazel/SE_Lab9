package parser;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import codegenerator.CodeGenerator;
import errorhandler.ErrorHandler;
import scanner.lexicalAnalyzer;
import scanner.token.Token;


public class Parser {
  private List<Rule> rules;
  private Stack<Integer> parsStack;
  private ParseTable parseTable;
  private CodeGenerator cg;

  public Parser() {
    parsStack = new Stack<Integer>();
    parsStack.push(0);
    try {
      parseTable = ParseTable.create(Files.readAllLines(Paths.get("src/main/resources/parseTable")).get(0));
    } catch (Exception e) {
      e.printStackTrace();
    }
    rules = new ArrayList<Rule>();
    try {
      for (String stringRule : Files.readAllLines(Paths.get("src/main/resources/Rules"))) {
        rules.add(new Rule(stringRule));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    cg = new CodeGenerator();
  }

  public void startParse(java.util.Scanner sc) {
    lexicalAnalyzer lexicalAnalyzer = new lexicalAnalyzer(sc);
    Token lookAhead = lexicalAnalyzer.getNextToken();
    Action currentAction;
    loop:while (true) {
      try {
//                Log.print("state : "+ parsStack.peek());
        currentAction = parseTable.getActionTable(parsStack.peek(), lookAhead);
        //Log.print("");

          if (currentAction.action == act.shift)
          {
            parsStack.push(currentAction.number);
            lookAhead = lexicalAnalyzer.getNextToken();
          }
          if (currentAction.action == act.reduce) {
            Rule rule = rules.get(currentAction.number);
            for (int i = 0; i < rule.RHS.size(); i++) {
              parsStack.pop();
            }
//                        Log.print("LHS : "+rule.LHS);
            parsStack.push(parseTable.getGotoTable(parsStack.peek(), rule.LHS));
//                        Log.print("");
            try {
              cg.semanticFunction(rule.semanticAction, lookAhead);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        if (currentAction.action == act.accept)
            break loop;

      } catch (Exception ignored) {

        ignored.printStackTrace();
//                boolean find = false;
//                for (NonTerminal t : NonTerminal.values()) {
//                    if (parseTable.getGotoTable(parsStack.peek(), t) != -1) {
//                        find = true;
//                        parsStack.push(parseTable.getGotoTable(parsStack.peek(), t));
//                        StringBuilder tokenFollow = new StringBuilder();
//                        tokenFollow.append(String.format("|(?<%s>%s)", t.name(), t.pattern));
//                        Matcher matcher = Pattern.compile(tokenFollow.substring(1)).matcher(lookAhead.toString());
//                        while (!matcher.find()) {
//                            lookAhead = lexicalAnalyzer.getNextToken();
//                        }
//                    }
//                }
//                if (!find)
//                    parsStack.pop();
      }


    }
    if (!ErrorHandler.hasError)
      cg.printMemory();


  }


}
