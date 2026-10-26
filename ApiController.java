package com.example.atm;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final Pattern PIN_PATTERN = Pattern.compile("\\d{4}");

    public ApiController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        if (request.accountNumber() == null || request.pin() == null ||
                !PIN_PATTERN.matcher(request.pin()).matches()) {
            return error(HttpStatus.BAD_REQUEST, "Enter an account number and a 4-digit PIN.");
        }

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT account_number, holder_name, pin_hash FROM accounts WHERE account_number = ?",
                request.accountNumber().trim());

        if (rows.isEmpty() || !encoder.matches(request.pin(), (String) rows.get(0).get("pin_hash"))) {
            return error(HttpStatus.UNAUTHORIZED, "Invalid account number or PIN.");
        }

        session.setAttribute("accountNumber", request.accountNumber().trim());
        return ResponseEntity.ok(Map.of("message", "Login successful",
                "holderName", rows.get(0).get("holder_name")));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logged out successfully.");
    }

    @GetMapping("/account")
    public ResponseEntity<?> account(HttpSession session) {
        String account = currentAccount(session);
        if (account == null) return unauthorized();
        return jdbc.query("SELECT account_number, holder_name, balance FROM accounts WHERE account_number = ?",
                rs -> rs.next()
                        ? ResponseEntity.ok(Map.of(
                            "accountNumber", rs.getString("account_number"),
                            "holderName", rs.getString("holder_name"),
                            "balance", rs.getBigDecimal("balance")))
                        : error(HttpStatus.NOT_FOUND, "Account not found."),
                account);
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody AmountRequest request, HttpSession session) {
        String account = currentAccount(session);
        if (account == null) return unauthorized();
        ResponseEntity<?> validation = validateAmount(request.amount());
        if (validation != null) return validation;

        jdbc.update("UPDATE accounts SET balance = balance + ? WHERE account_number = ?",
                request.amount(), account);
        jdbc.update("INSERT INTO transactions(account_number, transaction_type, amount) VALUES (?, 'DEPOSIT', ?)",
                account, request.amount());
        return ResponseEntity.ok(Map.of("message", "Deposit successful."));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody AmountRequest request, HttpSession session) {
        String account = currentAccount(session);
        if (account == null) return unauthorized();
        ResponseEntity<?> validation = validateAmount(request.amount());
        if (validation != null) return validation;

        // A single conditional UPDATE prevents a negative balance, including concurrent requests.
        int updated = jdbc.update(
                "UPDATE accounts SET balance = balance - ? WHERE account_number = ? AND balance >= ?",
                request.amount(), account, request.amount());
        if (updated == 0) {
            return error(HttpStatus.BAD_REQUEST, "Insufficient balance or account not found.");
        }
        jdbc.update("INSERT INTO transactions(account_number, transaction_type, amount) VALUES (?, 'WITHDRAWAL', ?)",
                account, request.amount());
        return ResponseEntity.ok(Map.of("message", "Withdrawal successful."));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> transactions(HttpSession session) {
        String account = currentAccount(session);
        if (account == null) return unauthorized();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT transaction_id, transaction_type, amount, transaction_time " +
                "FROM transactions WHERE account_number = ? ORDER BY transaction_time DESC LIMIT 20",
                account);
        return ResponseEntity.ok(rows);
    }

    @PostMapping("/change-pin")
    public ResponseEntity<?> changePin(@RequestBody ChangePinRequest request, HttpSession session) {
        String account = currentAccount(session);
        if (account == null) return unauthorized();
        if (request.currentPin() == null || request.newPin() == null ||
                !PIN_PATTERN.matcher(request.newPin()).matches()) {
            return error(HttpStatus.BAD_REQUEST, "PINs must be 4 digits.");
        }
        String hash = jdbc.query("SELECT pin_hash FROM accounts WHERE account_number = ?",
                rs -> rs.next() ? rs.getString("pin_hash") : null, account);
        if (hash == null || !encoder.matches(request.currentPin(), hash)) {
            return error(HttpStatus.BAD_REQUEST, "Current PIN is incorrect.");
        }
        jdbc.update("UPDATE accounts SET pin_hash = ? WHERE account_number = ?",
                encoder.encode(request.newPin()), account);
        return ResponseEntity.ok(Map.of("message", "PIN changed successfully."));
    }

    private String currentAccount(HttpSession session) {
        return (String) session.getAttribute("accountNumber");
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return error(HttpStatus.UNAUTHORIZED, "Please log in again.");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    private ResponseEntity<?> validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2 ||
                amount.compareTo(new BigDecimal("1000000.00")) > 0) {
            return error(HttpStatus.BAD_REQUEST, "Enter an amount greater than zero, up to ₹10,00,000, with at most 2 decimals.");
        }
        return null;
    }

    public record LoginRequest(String accountNumber, String pin) {}
    public record AmountRequest(BigDecimal amount) {}
    public record ChangePinRequest(String currentPin, String newPin) {}
}
