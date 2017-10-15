/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.generallib.math.expression;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.PatternSyntaxException;

public class ParsableCalculator {
    public static void main(String[] ar) {
        ParsableCalculator calc = new ParsableCalculator();

        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("Calc? >> ");
                String input = sc.nextLine();
                System.out.println("Result: " + calc.parse(input));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static enum Operators {
        PLUS('+'), MINUS('-'), MULTIPLY('*'), DIVIDE('/'), POWER('^'), REMAINDER('%'), UNARYMINUS('-');
        private char c;

        private Operators(char c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return String.valueOf(c);
        }

        public static Operators getOperator(char c) {
            switch (c) {
            case '+':
                return PLUS;
            case '-':
                return MINUS;
            case '*':
                return MULTIPLY;
            case '/':
                return DIVIDE;
            case '^':
                return POWER;
            case '%':
                return REMAINDER;
            default:
                return null;
            }
        }
    }

    /*
     * public static final Map<Operators, Operation> operations = new
     * EnumMap<Operators, Operation>(Operators.class){{ put(Operators.PLUS, new
     * Operation() {@Override public double calc(double num1, double num2)
     * {return num1 + num2;}}); put(Operators.MINUS, new Operation() {@Override
     * public double calc(double num1, double num2) {return num1 - num2;}});
     * put(Operators.MULTIPLY, new Operation() {@Override public double
     * calc(double num1, double num2) {return num1 * num2;}});
     * put(Operators.DIVIDE, new Operation() {@Override public double
     * calc(double num1, double num2) {return num1 / num2;}});
     * put(Operators.POWER, new Operation() {@Override public double calc(double
     * num1, double num2) {return Math.pow(num1, num2);}});
     * put(Operators.REMAINDER, new Operation() {@Override public double
     * calc(double num1, double num2) {return num1 % num2;}});
     * put(Operators.UNARYMINUS, new Operation() {@Override public double
     * calc(double num1, double num2) {return -num1;}}); }}; private static
     * final Map<Operators, Integer> precedence = new EnumMap<Operators,
     * Integer>(Operators.class){{ put(Operators.PLUS, 2); put(Operators.MINUS,
     * 2); put(Operators.MULTIPLY, 3); put(Operators.DIVIDE, 3);
     * put(Operators.POWER, 4); put(Operators.REMAINDER, 3);
     * put(Operators.UNARYMINUS, 4); }};
     */

    public static final Map<Character, Operation> operations = new HashMap<Character, Operation>() {
        {
            put('+', new Operation() {
                @Override
                public double calc(double num1, double num2) {
                    return num1 + num2;
                }
            });
            put('-', new Operation() {
                @Override
                public double calc(double num1, double num2) {
                    return num1 - num2;
                }
            });
            put('*', new Operation() {
                @Override
                public double calc(double num1, double num2) {
                    return num1 * num2;
                }
            });
            put('/', new Operation() {
                @Override
                public double calc(double num1, double num2) {
                    return num1 / num2;
                }
            });
            put('^', new Operation() {
                @Override
                public double calc(double num1, double num2) {
                    return Math.pow(num1, num2);
                }
            });
            put('%', new Operation() {
                @Override
                public double calc(double num1, double num2) {
                    return num1 % num2;
                }
            });
            // put(Operators.UNARYMINUS, new Operation() {@Override public
            // double calc(double num1, double num2) {return -num1;}});
        }
    };
    private static final Map<Character, Integer> precedence = new HashMap<Character, Integer>() {
        {
            put('+', 2);
            put('-', 2);
            put('*', 3);
            put('/', 3);
            put('^', 4);
            put('%', 3);
            // put(Operators.UNARYMINUS, 4);
        }
    };

    private final Map<String, DecimalReplacer> replaces = new HashMap<String, DecimalReplacer>();

    public void registerPlaceHolder(String name, DecimalReplacer rep) {
        replaces.put(name, rep);
    }

    private static Stack<String> stack = new Stack<String>();

    /**
     * 
     * @param input
     *            String to parse
     * @return result
     * @throws PatternSyntaxException
     *             throw if syntax error found.
     * @throws NumberFormatException
     *             throw if invalid number in the string.
     */
    public double parse(String input) {
        stack.clear();

        Queue<String> output = eval(input);

        while (!output.isEmpty()) {
            String out = output.poll();

            if (out.length() == 1 && operations.containsKey(out.charAt(0))) {
                char op = out.charAt(0);
                double num2 = Double.parseDouble(stack.pop());
                double num1 = Double.parseDouble(stack.pop());
                stack.push(String.valueOf(operations.get(op).calc(num1, num2)));
            } else {
                stack.push(out);
            }
        }

        return Double.parseDouble(stack.pop());
    }

    private char[] text;
    private int index;

    private Stack<Character> op = new Stack<Character>();
    private Queue<String> output = new LinkedList<String>();

    private Queue<String> eval(String input) {
        // clean up
        op.clear();
        output.clear();

        // initialize
        text = input.toCharArray();
        index = 0;

        // remove white spaces
        input = input.toLowerCase().replaceAll("[ \\t]", "");

        // replace place holders
        for (Entry<String, DecimalReplacer> entry : replaces.entrySet()) {
            if (entry.getValue().replace() >= 0.0D) {
                input = input.replaceAll(entry.getKey(), String.valueOf(entry.getValue().replace()));
            } else {
                input = input.replaceAll(entry.getKey(), "(0" + String.valueOf(entry.getValue().replace() + ")"));
            }
        }
        // char array
        char[] chars = input.toCharArray();

        String numStr = "";
        for (int index = 0; index < chars.length; index++) {
            char c = chars[index];

            // digit or num
            if (Character.isDigit(c) || c == '.') {
                numStr += c;
                continue;
            }

            // push previously built number
            if (!numStr.equals("")) {
                output.add(numStr);
                numStr = "";
            }

            if (c == '(') {
                op.push(c);
            } else if (c == ')') {
                char operator;
                while ((operator = op.pop()) != '(') {
                    output.add(String.valueOf(operator));
                }
            } else if (operations.containsKey(c)) {
                if (output.isEmpty())
                    throw new PatternSyntaxException("Cannot use sign infront of number. Use parenthesis ex) (0 - 5)",
                            input, index);

                if (op.isEmpty()) {
                    op.push(c);
                } else if (op.peek() == '(') {
                    op.push(c);
                } else if (op.peek() == '^') {
                    // right to left
                    op.push(c);
                } else {
                    int precBefore = precedence.get(op.peek());
                    int precAfter = precedence.get(c);

                    // pop operator and push to output
                    if (precAfter <= precBefore) {
                        output.add(String.valueOf(op.pop()));
                    }

                    op.push(c);
                }
            } else {
                // syntax
                throw new PatternSyntaxException("Unrecognized character", input, index);
            }
        }

        // push previously built number
        if (!numStr.equals("")) {
            output.add(numStr);
            numStr = "";
        }

        while (!op.isEmpty()) {
            output.add(String.valueOf(op.pop()));
        }

        return output;
    }

    private void ignoreWhiteSpaces() {
        while (text[index] != ' ' && text[index] != '\t')
            index++;
    }

    private double readNumber() {
        StringBuilder builder = new StringBuilder();
        while (Character.isDigit(text[index]))
            builder.append(text[index++]);
        if (text[index++] == '.')
            builder.append(".");
        while (Character.isDigit(text[index]))
            builder.append(text[index++]);

        return Double.parseDouble(builder.toString());
    }

    public interface Operation {
        double calc(double num1, double num2);
    }

    public interface DecimalReplacer {
        double replace();
    }
}
