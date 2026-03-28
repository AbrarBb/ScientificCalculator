package com.example.scientificcalculator;

/**
 * Evaluates expressions with + - * / ^, scientific notation (E), root(x,y), and trig.
 * Angle mode affects sin/cos/tan input and asin/acos/atan output.
 */
public class MathEvaluator {

    public enum AngleMode {
        DEG, RAD, GRAD
    }

    private String expr;
    private int pos;
    private final AngleMode angleMode;

    public MathEvaluator(AngleMode angleMode) {
        this.angleMode = angleMode;
    }

    public double eval(String input) throws Exception {
        expr = input
                .replace('×', '*')
                .replace('÷', '/')
                .replace("π", Double.toString(Math.PI))
                .replaceAll("\\s+", "");
        if (expr.isEmpty()) {
            throw new Exception("Empty expression");
        }
        pos = 0;
        double value = parseAddSub();
        if (pos < expr.length()) {
            throw new Exception("Unexpected character");
        }
        return value;
    }

    private double toRadians(double angle) {
        switch (angleMode) {
            case DEG:
                return Math.toRadians(angle);
            case GRAD:
                return angle * Math.PI / 200.0;
            case RAD:
            default:
                return angle;
        }
    }

    private double fromRadians(double rad) {
        switch (angleMode) {
            case DEG:
                return Math.toDegrees(rad);
            case GRAD:
                return rad * 200.0 / Math.PI;
            case RAD:
            default:
                return rad;
        }
    }

    private double parseAddSub() throws Exception {
        double v = parseMulDiv();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '+') {
                pos++;
                v += parseMulDiv();
            } else if (c == '-') {
                pos++;
                v -= parseMulDiv();
            } else {
                break;
            }
        }
        return v;
    }

    private double parseMulDiv() throws Exception {
        double v = parsePower();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '*') {
                pos++;
                v *= parsePower();
            } else if (c == '/') {
                pos++;
                double d = parsePower();
                if (d == 0.0) {
                    throw new Exception("Division by zero");
                }
                v /= d;
            } else {
                break;
            }
        }
        return v;
    }

    private double parsePower() throws Exception {
        double base = parseUnary();
        if (pos < expr.length() && expr.charAt(pos) == '^') {
            pos++;
            double exp = parsePower();
            return Math.pow(base, exp);
        }
        return base;
    }

    private double parseUnary() throws Exception {
        if (pos < expr.length() && expr.charAt(pos) == '+') {
            pos++;
            return parseUnary();
        }
        if (pos < expr.length() && expr.charAt(pos) == '-') {
            pos++;
            return -parseUnary();
        }
        return parsePrimary();
    }

    private double parsePrimary() throws Exception {
        if (pos >= expr.length()) {
            throw new Exception("Expression incomplete");
        }
        char c = expr.charAt(pos);
        if (c == '(') {
            pos++;
            double v = parseAddSub();
            if (pos >= expr.length() || expr.charAt(pos) != ')') {
                throw new Exception("Missing )");
            }
            pos++;
            return v;
        }
        if (Character.isDigit(c) || c == '.') {
            return parseNumber();
        }
        if (Character.isLetter(c)) {
            String name = parseName();
            if ("e".equals(name)) {
                return Math.E;
            }
            if ("root".equals(name)) {
                return parseRoot();
            }
            if (isOneArgFunc(name)) {
                return parseOneArgCall(name);
            }
            throw new Exception("Unknown: " + name);
        }
        throw new Exception("Invalid character");
    }

    private boolean isOneArgFunc(String name) {
        switch (name) {
            case "sin":
            case "cos":
            case "tan":
            case "asin":
            case "acos":
            case "atan":
            case "sinh":
            case "cosh":
            case "tanh":
            case "log":
            case "ln":
            case "sqrt":
            case "exp":
            case "pow10":
                return true;
            default:
                return false;
        }
    }

    private double parseRoot() throws Exception {
        if (pos >= expr.length() || expr.charAt(pos) != '(') {
            throw new Exception("Missing ( after root");
        }
        pos++;
        double x = parseAddSub();
        if (pos >= expr.length() || expr.charAt(pos) != ',') {
            throw new Exception("root needs x,y");
        }
        pos++;
        double y = parseAddSub();
        if (pos >= expr.length() || expr.charAt(pos) != ')') {
            throw new Exception("Missing )");
        }
        pos++;
        if (y == 0) {
            throw new Exception("Invalid root index");
        }
        return Math.pow(x, 1.0 / y);
    }

    private double parseOneArgCall(String name) throws Exception {
        if (pos >= expr.length() || expr.charAt(pos) != '(') {
            throw new Exception("Missing ( after " + name);
        }
        pos++;
        double arg = parseAddSub();
        if (pos >= expr.length() || expr.charAt(pos) != ')') {
            throw new Exception("Missing )");
        }
        pos++;
        return applyFunction(name, arg);
    }

    private String parseName() {
        int start = pos;
        while (pos < expr.length() && Character.isLetter(expr.charAt(pos))) {
            pos++;
        }
        return expr.substring(start, pos);
    }

    private double parseNumber() throws Exception {
        int start = pos;
        while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) {
            pos++;
        }
        if (pos < expr.length() && (expr.charAt(pos) == 'E' || expr.charAt(pos) == 'e')) {
            pos++;
            if (pos < expr.length() && (expr.charAt(pos) == '+' || expr.charAt(pos) == '-')) {
                pos++;
            }
            while (pos < expr.length() && Character.isDigit(expr.charAt(pos))) {
                pos++;
            }
        }
        if (start == pos) {
            throw new Exception("Invalid number");
        }
        return Double.parseDouble(expr.substring(start, pos));
    }

    private double applyFunction(String name, double arg) throws Exception {
        switch (name) {
            case "sin":
                return Math.sin(toRadians(arg));
            case "cos":
                return Math.cos(toRadians(arg));
            case "tan":
                return Math.tan(toRadians(arg));
            case "asin":
                return fromRadians(Math.asin(arg));
            case "acos":
                return fromRadians(Math.acos(arg));
            case "atan":
                return fromRadians(Math.atan(arg));
            case "sinh":
                return Math.sinh(arg);
            case "cosh":
                return Math.cosh(arg);
            case "tanh":
                return Math.tanh(arg);
            case "log":
                return Math.log10(arg);
            case "ln":
                return Math.log(arg);
            case "sqrt":
                if (arg < 0) {
                    throw new Exception("Invalid sqrt");
                }
                return Math.sqrt(arg);
            case "exp":
                return Math.exp(arg);
            case "pow10":
                return Math.pow(10, arg);
            default:
                return arg;
        }
    }
}
