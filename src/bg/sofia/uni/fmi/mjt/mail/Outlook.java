package bg.sofia.uni.fmi.mjt.mail;

import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.RuleAlreadyDefinedException;
import bg.sofia.uni.fmi.mjt.mail.rules.RuleDefinition;
import bg.sofia.uni.fmi.mjt.mail.tree.AccountMail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Outlook implements MailClient {
    private final Map<String, AccountMail> accounts = new HashMap<>();
    private final Map<String, String> emails = new HashMap<>();
    private final static int MIN_PRIORITY = 1;
    private final static int MAX_PRIORITY = 10;

    private Mail convertToMail(String accountName, String mailMetadata, String mailContent) {
        String[] lines = mailMetadata.split(System.lineSeparator());
        Account sender = null;
        String subject = "";
        String body = mailContent;
        Set<String> recipients = new LinkedHashSet<>();
        LocalDateTime received = null;
        for (String line : lines) {
            String[] lineWords = line.split(":");
            if (lineWords[0].strip().equals("subject")) {
                subject = lineWords[1].strip();
            }
            if (lineWords[0].strip().equals("recipients")) {
                String[] recipientEmails = lineWords[1].strip().split(",");
                for (String recipientEmail : recipientEmails) {
                    recipients.add(recipientEmail.strip());
                }
            }
            if (lineWords[0].strip().equals("sender")) {
                sender = accounts.get(emails.get(lineWords[1].strip())).getAccount();
            }
            if (lineWords[0].strip().equals("received")) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                received = LocalDateTime.parse(
                    line.substring(line.indexOf(":") + 1).strip(), df
                );
            }
        }
        if (sender == null) {
            sender = accounts.get(accountName).getAccount();
        }
        Mail mail = new Mail(sender, recipients, subject, body, received);
        return mail;
    }

    private RuleDefinition convertToRule(String path, int priority, String ruleDefinition) {
        String[] lines = ruleDefinition.split(System.lineSeparator());
        Set<String> subjectKeywords = null;
        Set<String> bodyKeywords = null;
        Set<String> recipientEmails = null;
        String fromEmail = null;
        for (String line : lines) {
            String[] lineWords = line.strip().split(":");
            if (lineWords[0].strip().equals("subject-includes")) {
                if (subjectKeywords != null) {
                    return null;
                }
                subjectKeywords = new LinkedHashSet<>();
                String[] subjectWords = lineWords[1].strip().split(",");
                for (String subjectWord : subjectWords) {
                    subjectKeywords.add(subjectWord.strip());
                }
            }
            if (lineWords[0].strip().equals("subject-or-body-includes")) {
                if (bodyKeywords != null) {
                    return null;
                }
                if (subjectKeywords == null) {
                    subjectKeywords = new LinkedHashSet<>();
                }
                bodyKeywords = new LinkedHashSet<>();
                String[] subjectOrBodyWords = lineWords[1].strip().split(",");
                for (String subjectOrBodyWord : subjectOrBodyWords) {
                    bodyKeywords.add(subjectOrBodyWord.strip());
                    subjectKeywords.add(subjectOrBodyWord.strip());
                }
            }
            if (lineWords[0].strip().equals("recipients-includes")) {
                if (recipientEmails != null) {
                    return null;
                }
                recipientEmails = new LinkedHashSet<>();
                String[] recipients = lineWords[1].strip().split(",");
                for (String recipient : recipients) {
                    recipientEmails.add(recipient.strip());
                }
            }
            if (lineWords[0].strip().equals("from")) {
                if (fromEmail != null) {
                    return null;
                }
                fromEmail = lineWords[1].strip();
            }
        }
        if (fromEmail == null) {
            fromEmail = "";
        }
        if (subjectKeywords == null && bodyKeywords == null) {
            return null;
        }
        if (recipientEmails == null) {
            recipientEmails = new HashSet<>();
        }
        RuleDefinition rule = new RuleDefinition(path, subjectKeywords,
            bodyKeywords, recipientEmails, fromEmail, priority);
        return rule;
    }

    @Override
    public Account addNewAccount(String accountName, String email) {
        if (accountName == null || accountName.isEmpty() || accountName.isBlank()) {
            throw new IllegalArgumentException("Account name is invalid!");
        }
        if (email == null || email.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException("Email is invalid!");
        }
        final Account account = new Account(email, accountName);
        final AccountMail accountMail = new AccountMail(account);
        if (accounts.containsKey(accountName.toLowerCase())) {
            throw new AccountAlreadyExistsException("Account already exists!");
        }
        accounts.put(accountName.toLowerCase(), accountMail);
        emails.put(email, accountName);
        return account;
    }

    @Override
    public void createFolder(String accountName, String path) {
        if (accountName == null || accountName.isBlank() || accountName.isEmpty()) {
            throw new IllegalArgumentException("Account name is invalid!");
        }
        if (path == null || path.isEmpty() || path.isBlank()) {
            throw new IllegalArgumentException("Path is invalid!");
        }
        if (!accounts.containsKey(accountName)) {
            throw new AccountNotFoundException("Account is not found!");
        }
        accounts.get(accountName).createFolder(path);
    }

    @Override
    public void addRule(String accountName, String folderPath, String ruleDefinition, int priority) {
        if (accountName == null || accountName.isEmpty() || accountName.isBlank()) {
            throw new IllegalArgumentException("Account name is invalid!");
        }
        if (folderPath == null || folderPath.isBlank() || folderPath.isEmpty()) {
            throw new IllegalArgumentException("Folder path is invalid!");
        }
        if (ruleDefinition == null || ruleDefinition.isBlank() || ruleDefinition.isEmpty()) {
            throw new IllegalArgumentException("Rule definition text is invalid!");
        }
        if (!(priority >= MIN_PRIORITY && priority <= MAX_PRIORITY)) {
            throw new IllegalArgumentException("Priority is not in range [1, 10]!");
        }
        if (!accounts.containsKey(accountName)) {
            throw new AccountNotFoundException("Account is not found!");
        }
        AccountMail account = accounts.get(accountName);
        if (!account.pathExists(folderPath)) {
            throw new FolderNotFoundException("Folder is not found!");
        }
        RuleDefinition rule = convertToRule(folderPath, priority, ruleDefinition);
        if (rule == null) {
            throw new RuleAlreadyDefinedException("Rule metadata is invalid!");
        }
        account.addRule(rule);
    }

    @Override
    public void receiveMail(String accountName, String mailMetadata, String mailContent) {
        if (accountName == null || accountName.isBlank() || accountName.isEmpty()) {
            throw new IllegalArgumentException("Account is invalid!");
        }
        if (mailMetadata == null || mailMetadata.isEmpty() || mailMetadata.isBlank()) {
            throw new IllegalArgumentException("Mail metadata is invalid!");
        }
        if (mailContent == null || mailContent.isEmpty() || mailContent.isBlank()) {
            throw new IllegalArgumentException("Mail content is invalid!");
        }
        if (!accounts.containsKey(accountName)) {
            throw new AccountNotFoundException("Account is not found!");
        }
        Mail mail = convertToMail(accountName, mailMetadata, mailContent);
        AccountMail receiver = accounts.get(accountName);
        AccountMail sender = accounts.get(mail.sender().name());
        if (sender.getAccount().name().equals(accountName)) {
            throw new IllegalArgumentException("Sender is invalid!");
        }
        receiver.receiveMail(mail);
        sender.sendMail(mail);
    }

    @Override
    public Collection<Mail> getMailsFromFolder(String account, String folderPath) {
        if (account == null || account.isEmpty() || account.isBlank()) {
            throw new IllegalArgumentException("Account is invalid!");
        }
        if (folderPath == null || folderPath.isEmpty() || folderPath.isBlank()) {
            throw new IllegalArgumentException("Folder path is invalid!");
        }
        if (!accounts.containsKey(account)) {
            throw new AccountNotFoundException("Account is not found!");
        }
        AccountMail accountMail = accounts.get(account);
        if (!accountMail.pathExists(folderPath)) {
            throw new FolderNotFoundException("Folder is not found!");
        }
        return accountMail.getMailsFromFolder(folderPath);
    }

    @Override
    public void sendMail(String accountName, String mailMetadata, String mailContent) {
        if (accountName == null || accountName.isBlank() || accountName.isEmpty()) {
            throw new IllegalArgumentException("Account is invalid!");
        }
        if (mailMetadata == null || mailMetadata.isEmpty() || mailMetadata.isBlank()) {
            throw new IllegalArgumentException("Mail metadata is invalid!");
        }
        if (mailContent == null || mailContent.isEmpty() || mailContent.isBlank()) {
            throw new IllegalArgumentException("Mail content is invalid!");
        }
        if (!accounts.containsKey(accountName)) {
            throw new AccountNotFoundException("Account is not found!");
        }
        AccountMail accountMail = accounts.get(accountName);
        Mail mail = convertToMail(accountName, mailMetadata, mailContent);
        accountMail.sendMail(mail);
        for (Map.Entry<String, AccountMail> accountMailEntry : accounts.entrySet()) {
            if (!accountMailEntry.getKey().equals(accountName) &&
                (mail.recipients().contains(accountMailEntry.getValue().getAccount().emailAddress())
                    || mail.recipients().size() == 0)) {
                accountMailEntry.getValue().receiveMail(mail);
            }
        }
    }
}