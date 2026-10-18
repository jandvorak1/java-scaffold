package cz.cdcargo.javascaffold.shell.ui.webapp.platform.account;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryUserAccountStore implements UserAccountStore {

    private final Map<String, UserAccount> accounts;

    public InMemoryUserAccountStore() {
        this(java.util.List.of());
    }

    public InMemoryUserAccountStore(Collection<UserAccount> accounts) {
        Objects.requireNonNull(accounts, "Accounts must not be null");

        var map = new HashMap<String, UserAccount>();
        for (var account : accounts) {
            Objects.requireNonNull(account, "Account must not be null");
            var email = normalizeEmail(account.email());
            if (map.putIfAbsent(email, account) != null) {
                throw new IllegalArgumentException("Duplicate account email: " + account.email());
            }
        }
        this.accounts = Map.copyOf(map);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        Objects.requireNonNull(email, "Email must not be null");
        if (email.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(accounts.get(normalizeEmail(email)));
    }

    @Override
    public Optional<UserAccount> findBySubject(String subject) {
        Objects.requireNonNull(subject, "Subject must not be null");
        if (subject.isBlank()) {
            return Optional.empty();
        }
        return accounts.values().stream()
                .filter(account -> account.subject().equals(subject))
                .findFirst();
    }

    private static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
