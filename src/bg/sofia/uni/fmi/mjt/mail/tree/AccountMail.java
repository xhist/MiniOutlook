package bg.sofia.uni.fmi.mjt.mail.tree;

import bg.sofia.uni.fmi.mjt.mail.Account;
import bg.sofia.uni.fmi.mjt.mail.Mail;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.InvalidPathException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.RuleAlreadyDefinedException;
import bg.sofia.uni.fmi.mjt.mail.rules.RuleDefinition;

import java.util.*;

class MailFolder {
    private final String name;
    private final Set<Mail> mails;
    private final Map<String, MailFolder> folders;

    public MailFolder(String name) {
        this.name = name;
        this.mails = new HashSet<>();
        this.folders = new TreeMap<>();
    }

    public String getName() {
        return name;
    }

    public Set<Mail> getMails() {
        return Set.copyOf(mails);
    }

    public Map<String, MailFolder> getFolders() {
        return Collections.unmodifiableMap(folders);
    }

    public void addFolder(MailFolder folder) {
        this.folders.put(folder.getName(), folder);
    }

    public void addMail(Mail mail) {
        this.mails.add(mail);
    }

    public void removeMails(Collection<Mail> mails) {
        this.mails.removeAll(mails);
    }
}

public class AccountMail {
    private final MailFolder root = new MailFolder("");
    private final Account account;
    private final Map<RuleDefinition, String> rules;

    public Account getAccount() {
        return account;
    }

    public AccountMail(Account account) {
        this.account = account;
        this.rules = new TreeMap<>(Comparator.comparing(RuleDefinition::priority).reversed());
        final MailFolder inbox = new MailFolder("inbox");
        final MailFolder sent = new MailFolder("sent");
        root.addFolder(inbox);
        root.addFolder(sent);
    }

    public boolean pathExists(String path) {
        String[] folders = path.split("/");
        MailFolder currentFolder = root;
        for (int i = 1; i < folders.length; i++) {
            if (!currentFolder.getFolders().containsKey(folders[i].strip())) {
                return false;
            }
            currentFolder = currentFolder.getFolders().get(folders[i].strip());
        }
        return true;
    }

    private MailFolder getFolder(String path) {
        MailFolder current = root;
        String[] folders = path.split("/");
        for (int i = 1; i < folders.length; i++) {
            current = current.getFolders().get(folders[i]);
        }
        return current;
    }

    public void createFolder(String path) {
        int lastIndexOfSlash = path.lastIndexOf("/");
        String folderName = path.substring(lastIndexOfSlash + 1);
        String previousDirectories = path.substring(0, lastIndexOfSlash);
        if (!pathExists(previousDirectories)) {
            throw new InvalidPathException("Path is invalid!");
        }
        MailFolder currentFolder = getFolder(previousDirectories);
        if (currentFolder.getFolders().containsKey(folderName)) {
            throw new FolderAlreadyExistsException(String.format("Folder %s already exists!", folderName));
        }
        MailFolder newFolder = new MailFolder(folderName);
        currentFolder.addFolder(newFolder);
    }

    public void addRule(RuleDefinition rule) {
        if (rules.containsKey(rule)) {
            if (!rules.get(rule).equals(rule.path())) {
                throw new RuleAlreadyDefinedException("Rule is already defined!");
            }
            return;
        }
        Queue<Mail> mails = new LinkedList<>();
        MailFolder inbox = getFolder("/inbox");
        for (Mail mail : inbox.getMails()) {
            if (rule.matchesMail(mail, account.emailAddress())) {
                moveMail(mail, rule.path());
                mails.add(mail);
            }
        }
        if (mails.size() > 0) {
            inbox.removeMails(mails);
        }
        rules.put(rule, rule.path());
    }

    private boolean mailExists(String path, Mail mail) {
        MailFolder folder = getFolder(path);
        if (!folder.getMails().contains(mail)) {
            return false;
        }
        return true;
    }

    public Set<Mail> getMailsFromFolder(String path) {
        MailFolder currentFolder = root;
        String[] folders = path.split("/");
        for (int i = 1; i < folders.length; i++) {
            currentFolder = currentFolder.getFolders().get(folders[i]);
        }
        return currentFolder.getMails();
    }

    private void moveMail(Mail mail, String path) {
        MailFolder folder = getFolder(path);
        folder.addMail(mail);
    }

    public void receiveMail(Mail mail) {
        String path = "/inbox";
        for (RuleDefinition rule : rules.keySet()) {
            if (rule.matchesMail(mail, account.emailAddress())) {
                path = rule.path();
                break;
            }
        }
        if (mailExists(path, mail)) {
            return;
        }
        if (!path.equals("/inbox")) {
            moveMail(mail, path);
            return;
        }
        if (mail.recipients().size() > 0 && !mail.recipients().contains(account.emailAddress())) {
            return;
        }
        getFolder("/inbox").addMail(mail);
    }

    public void sendMail(Mail mail) {
        if (mailExists("/sent", mail)) {
            return;
        }
        getFolder("/sent").addMail(mail);
    }

}