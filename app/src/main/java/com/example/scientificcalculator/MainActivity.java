package com.example.scientificcalculator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "calc_history_prefs";
    private static final String KEY_LINES = "history_lines";
    private static final int MAX_HISTORY = 50;

    private TextView tvExpression;
    private TextView tvResult;
    private TextView tvAngleLabel;
    private TextView tvFmtLabel;

    private final StringBuilder expression = new StringBuilder();
    private MathEvaluator evaluator;
    private MathEvaluator.AngleMode angleMode = MathEvaluator.AngleMode.DEG;

    private int fmtIndex;
    private boolean shiftOn;
    private boolean hypOn;
    private double memory;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        evaluator = new MathEvaluator(angleMode);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            PopupMenu pm = new PopupMenu(MainActivity.this, v);
            pm.getMenuInflater().inflate(R.menu.popup_nav, pm.getMenu());
            pm.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.nav_history) {
                    showHistoryDialog();
                    return true;
                }
                return false;
            });
            pm.show();
        });

        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);
        tvAngleLabel = findViewById(R.id.tvAngleLabel);
        tvFmtLabel = findViewById(R.id.tvFmtLabel);
        updateAngleLabel();
        updateFmtLabel();

        findViewById(R.id.btnShift).setOnClickListener(v -> shiftOn = !shiftOn);
        findViewById(R.id.btnDrg).setOnClickListener(v -> cycleDrg());
        findViewById(R.id.btnFse).setOnClickListener(v -> cycleFse());
        findViewById(R.id.btnMR).setOnClickListener(v -> memoryRecall());
        findViewById(R.id.btnMS).setOnClickListener(v -> memoryStore());
        findViewById(R.id.btnMplus).setOnClickListener(v -> memoryAdd());

        findViewById(R.id.btnHyp).setOnClickListener(v -> hypOn = !hypOn);

        findViewById(R.id.btnSin).setOnClickListener(v -> appendTrig("sin"));
        findViewById(R.id.btnCos).setOnClickListener(v -> appendTrig("cos"));
        findViewById(R.id.btnTan).setOnClickListener(v -> appendTrig("tan"));
        findViewById(R.id.btnLn).setOnClickListener(v -> appendLn());
        findViewById(R.id.btnLog).setOnClickListener(v -> appendLog());

        findViewById(R.id.btnXrootY).setOnClickListener(v -> append("root("));
        findViewById(R.id.btnSqrt).setOnClickListener(v -> append("sqrt("));
        findViewById(R.id.btnSquare).setOnClickListener(v -> append("^2"));
        findViewById(R.id.btnPercent).setOnClickListener(v -> applyPercent());
        findViewById(R.id.btnOpen).setOnClickListener(v -> append("("));
        findViewById(R.id.btnClose).setOnClickListener(v -> append(")"));

        wireNumericPad();

        findViewById(R.id.btnOnAc).setOnClickListener(v -> clearAll());
        findViewById(R.id.btnDel).setOnClickListener(v -> deleteLast());
        findViewById(R.id.btnEquals).setOnClickListener(v -> onEquals());
        findViewById(R.id.btnPlusMinus).setOnClickListener(v -> negateLastNumber());
        findViewById(R.id.btnExp).setOnClickListener(v -> appendExp());

        refreshDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            showHistoryDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void wireNumericPad() {
        int[] ids = new int[]{
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv
        };
        String[] values = new String[]{
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                ".", "+", "-", "×", "÷"
        };
        for (int i = 0; i < ids.length; i++) {
            final String s = values[i];
            findViewById(ids[i]).setOnClickListener(v -> append(s));
        }
    }

    private void appendTrig(String base) {
        if (hypOn) {
            append(base + "h(");
            hypOn = false;
        } else if (shiftOn) {
            append("a" + base + "(");
            shiftOn = false;
        } else {
            append(base + "(");
        }
    }

    private void appendLn() {
        if (shiftOn) {
            append("exp(");
            shiftOn = false;
        } else {
            append("ln(");
        }
    }

    private void appendLog() {
        if (shiftOn) {
            append("pow10(");
            shiftOn = false;
        } else {
            append("log(");
        }
    }

    private void appendExp() {
        if (expression.length() == 0) {
            return;
        }
        char c = expression.charAt(expression.length() - 1);
        if (Character.isDigit(c) || c == ')') {
            append("E");
        } else {
            Toast.makeText(this, R.string.exp_hint, Toast.LENGTH_SHORT).show();
        }
    }

    private void cycleDrg() {
        switch (angleMode) {
            case DEG:
                angleMode = MathEvaluator.AngleMode.RAD;
                break;
            case RAD:
                angleMode = MathEvaluator.AngleMode.GRAD;
                break;
            default:
                angleMode = MathEvaluator.AngleMode.DEG;
                break;
        }
        evaluator = new MathEvaluator(angleMode);
        updateAngleLabel();
    }

    private void updateAngleLabel() {
        switch (angleMode) {
            case DEG:
                tvAngleLabel.setText(R.string.deg);
                break;
            case RAD:
                tvAngleLabel.setText(R.string.rad);
                break;
            case GRAD:
                tvAngleLabel.setText(R.string.grad);
                break;
        }
    }

    private void cycleFse() {
        fmtIndex = (fmtIndex + 1) % 4;
        updateFmtLabel();
    }

    private void updateFmtLabel() {
        switch (fmtIndex) {
            case 0:
                tvFmtLabel.setText(R.string.fmt_norm);
                break;
            case 1:
                tvFmtLabel.setText(R.string.fmt_fix);
                break;
            case 2:
                tvFmtLabel.setText(R.string.fmt_sci);
                break;
            case 3:
                tvFmtLabel.setText(R.string.fmt_eng);
                break;
        }
    }

    private void memoryRecall() {
        expression.setLength(0);
        expression.append(formatResult(memory));
        tvResult.setText("");
        refreshDisplay();
    }

    private void memoryStore() {
        try {
            if (expression.length() == 0) {
                return;
            }
            memory = evaluator.eval(expression.toString());
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error) + ": " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void memoryAdd() {
        try {
            if (expression.length() == 0) {
                return;
            }
            memory += evaluator.eval(expression.toString());
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error) + ": " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void applyPercent() {
        if (expression.length() == 0) {
            return;
        }
        try {
            double v = evaluator.eval(expression.toString());
            v /= 100.0;
            expression.setLength(0);
            expression.append(formatResult(v));
            tvResult.setText("");
            refreshDisplay();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void negateLastNumber() {
        String s = expression.toString();
        if (s.isEmpty()) {
            return;
        }
        int i = s.length() - 1;
        if (!Character.isDigit(s.charAt(i)) && s.charAt(i) != '.') {
            return;
        }
        while (i >= 0 && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
            i--;
        }
        int start = i + 1;
        String numStr = s.substring(start);
        try {
            double v = Double.parseDouble(numStr);
            v = -v;
            String rep;
            if (Math.abs(v - Math.rint(v)) < 1e-12 && Math.abs(v) < 1e15) {
                rep = String.valueOf((long) Math.rint(v));
            } else {
                rep = trimDouble(v);
            }
            expression.setLength(0);
            expression.append(s, 0, start);
            expression.append(rep);
            tvResult.setText("");
            refreshDisplay();
        } catch (NumberFormatException ignored) {
        }
    }

    private static String trimDouble(double v) {
        String t = Double.toString(v);
        if (t.endsWith(".0")) {
            return t.substring(0, t.length() - 2);
        }
        return t;
    }

    private void append(String s) {
        expression.append(s);
        tvResult.setText("");
        refreshDisplay();
    }

    private void clearAll() {
        expression.setLength(0);
        tvResult.setText("");
        refreshDisplay();
    }

    private void deleteLast() {
        if (expression.length() > 0) {
            expression.deleteCharAt(expression.length() - 1);
            tvResult.setText("");
            refreshDisplay();
        }
    }

    private void refreshDisplay() {
        String e = expression.length() == 0 ? "0" : expression.toString();
        tvExpression.setText(e);
    }

    private void onEquals() {
        if (expression.length() == 0) {
            return;
        }
        String raw = expression.toString();
        try {
            double value = evaluator.eval(raw);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                return;
            }
            String formatted = formatResult(value);
            tvResult.setText("= " + formatted);
            saveHistoryLine(raw + " = " + formatted);
        } catch (Exception ex) {
            Toast.makeText(this, getString(R.string.error) + ": " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private String formatResult(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "?";
        }
        if (Math.abs(value) < 1e-12) {
            value = 0;
        }
        switch (fmtIndex) {
            case 1:
                return new DecimalFormat("0.00").format(value);
            case 2:
                return new DecimalFormat("0.###E0").format(value);
            case 3:
                return formatEngineering(value);
            default:
                return new DecimalFormat("#.##########").format(value);
        }
    }

    private String formatEngineering(double value) {
        if (value == 0) {
            return "0";
        }
        int exp = (int) Math.floor(Math.log10(Math.abs(value)) / 3) * 3;
        double mant = value / Math.pow(10, exp);
        return String.format(Locale.US, "%.6gE%+d", mant, exp);
    }

    private void saveHistoryLine(String line) {
        String old = prefs.getString(KEY_LINES, "");
        List<String> lines = new ArrayList<>();
        if (!TextUtils.isEmpty(old)) {
            String[] parts = old.split("\n");
            Collections.addAll(lines, parts);
        }
        lines.add(0, line);
        while (lines.size() > MAX_HISTORY) {
            lines.remove(lines.size() - 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(lines.get(i));
        }
        prefs.edit().putString(KEY_LINES, sb.toString()).apply();
    }

    private void showHistoryDialog() {
        String content = prefs.getString(KEY_LINES, "");
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        TextView tv = new TextView(this);
        tv.setPadding(pad, pad, pad, pad);
        tv.setTextIsSelectable(true);
        if (TextUtils.isEmpty(content)) {
            tv.setText(R.string.history_empty);
        } else {
            tv.setText(content);
        }
        ScrollView scroll = new ScrollView(this);
        tv.setLayoutParams(new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        scroll.addView(tv);
        scroll.setPadding(0, 0, 0, pad);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.history_title)
                .setView(scroll)
                .setPositiveButton(R.string.close, (d, w) -> d.dismiss())
                .setNeutralButton(R.string.clear_history, (d, w) -> {
                    prefs.edit().remove(KEY_LINES).apply();
                    Toast.makeText(this, R.string.clear_history, Toast.LENGTH_SHORT).show();
                    d.dismiss();
                })
                .show();
    }
}
