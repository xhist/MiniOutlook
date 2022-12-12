package bg.sofia.uni.fmi.mjt.mail;

import bg.sofia.uni.fmi.mjt.mail.exceptions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutlookTest {
    private Outlook outlook = new Outlook();

    @Test
    void testAddNewAccountInvalidParametersAccountName() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.addNewAccount(null, "test@email.com"));
    }

    @Test
    void testAddNewAccountInvalidParametersEmail() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.addNewAccount("xhist", null));
    }

    @Test
    void testAddNewAccountAlreadyExists() {
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        assertThrows(AccountAlreadyExistsException.class,
            () -> outlook.addNewAccount("xhist", "copa@gmail.com"));
    }

    @Test
    void testAddNewAccount() {
        Account account = new Account("grizki@gmail.com", "xhist");
        assertEquals(account, outlook.addNewAccount("xhist", "grizki@gmail.com"));
    }

    @Test
    void testCreateFolderInvalidParametersAccountName() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.createFolder(null, "/inbox/random"));
    }

    @Test
    void testCreateFolderInvalidParametersPath() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.createFolder("xhist", null));
    }

    @Test
    void testCreateFolderAccountNotFound() {
        assertThrows(AccountNotFoundException.class,
            () -> outlook.createFolder("xhist", "/inbox/random"));
    }

    @Test
    void testCreateFolderInvalidPath() {
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        assertThrows(InvalidPathException.class,
            () -> outlook.createFolder("xhist", "/inbox/random/clover"));
    }

    @Test
    void testCreateFolderValidPath() {
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        assertTrue(outlook.getMailsFromFolder("xhist", "/inbox/random").size() == 0);
    }

    @Test
    void testCreateFolderAlreadyExists() {
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        assertThrows(FolderAlreadyExistsException.class,
            () -> outlook.createFolder("xhist", "/inbox/random"));
    }

    @Test
    void testAddRuleInvalidParametersAccountName() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.addRule(null, "/inbox/random", "random", 10));
    }

    @Test
    void testAddRuleInvalidParametersFolderPath() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.addRule("xhist", null, "random", 5));
    }

    @Test
    void testAddRuleInvalidParametersRuleDefinition() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.addRule("xhist", "/inbox/random", null, 5));
    }

    @Test
    void testAddRuleInvalidParametersPriority() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.addRule("xhist", "/inbox/random", "random", -1));
    }

    @Test
    void testAddRuleAccountNotFound() {
        assertThrows(AccountNotFoundException.class,
            () -> outlook.addRule("xhist", "/inbox/random", "random", 5));
    }

    @Test
    void testAddRuleFolderNotFound() {
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        assertThrows(FolderNotFoundException.class,
            () -> outlook.addRule("xhist", "/inbox/random", "random", 5));
    }

    @Test
    void testAddRuleRuleAlreadyDefinedSubject() {
        String rule = "subject-includes: hello, world\n" +
            "subject-includes: hey";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        assertThrows(RuleAlreadyDefinedException.class,
            () -> outlook.addRule("xhist", "/inbox/random", rule, 5));
    }

    @Test
    void testAddRuleRuleAlreadyDefinedSubjectOrBody() {
        String rule = "subject-or-body-includes: hello, world\n" +
            "subject-or-body-includes: hey";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        assertThrows(RuleAlreadyDefinedException.class,
            () -> outlook.addRule("xhist", "/inbox/random", rule, 5));
    }

    @Test
    void testAddRuleRuleAlreadyDefinedFromEmail() {
        String rule = "from: stoyo@fmi.bg\n" +
            "from: stoyo@fmi.bg";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        assertThrows(RuleAlreadyDefinedException.class,
            () -> outlook.addRule("xhist", "/inbox/random", rule, 5));
    }

    @Test
    void testAddRuleRuleAlreadyDefinedRecipients() {
        String rule = "recipients-includes: stoyo@fmi.bg\n" +
            "recipients-includes: stoyo@fmi.bg";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        assertThrows(RuleAlreadyDefinedException.class,
            () -> outlook.addRule("xhist", "/inbox/random", rule, 5));
    }

    @Test
    void testAddRuleRuleAlreadyDefinedNoSubjectOrBody() {
        String rule = "from: stoyo@fmi.bg";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        assertThrows(RuleAlreadyDefinedException.class,
            () -> outlook.addRule("xhist", "/inbox/random", rule, 5));
    }

    @Test
    void testAddRuleConflictRule() {
        String rule = "subject-includes: hello, world\n" +
            "subject-or-body-includes: hey\n" +
            "from: stoyo@fmi.bg";
        String conflict = "subject-includes: hello\n" +
            "from: stoyo@fmi.bg";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.createFolder("xhist", "/inbox/conflict");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        assertThrows(RuleAlreadyDefinedException.class,
            () -> outlook.addRule("xhist", "/inbox/conflict", conflict, 5));
    }

    @Test
    void testAddExistingRule() {
        String rule = "subject-includes: hello, world\n" +
            "subject-or-body-includes: hey\n" +
            "from: stoyo@fmi.bg";
        String conflict = rule;
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.createFolder("xhist", "/inbox/conflict");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        assertThrows(RuleAlreadyDefinedException.class,
            () -> outlook.addRule("xhist", "/inbox/conflict", conflict, 5));
    }

    @Test
    void testAddRuleAndApply() {
        String rule = "subject-includes: Hello\n" +
            "subject-or-body-includes: MJT\n" +
            "from: stoyo@fmi.bg";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        String mailMetadata = "sender: stoyo@fmi.bg\n" +
            "subject: Hello, MJT\n" +
            "received: 2022-12-08 14:14";
        outlook.sendMail("stoyo", mailMetadata, "Welcome to MJT course!");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
    }

    @Test
    void testGetMailsFromFolderInvalidParametersAccountName() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.getMailsFromFolder(null, "/inbox/random"));
    }

    @Test
    void testGetMailsFromFolderInvalidParametersPath() {
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        assertThrows(IllegalArgumentException.class,
            () -> outlook.getMailsFromFolder("xhist", null));
    }

    @Test
    void testGetMailsFromFolderAccountNotFound() {
        assertThrows(AccountNotFoundException.class,
            () -> outlook.getMailsFromFolder("xhist", "/inbox/random"));
    }

    @Test
    void testGetMailsFromFolderFolderNotFound() {
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        assertThrows(FolderNotFoundException.class,
            () -> outlook.getMailsFromFolder("xhist", "/inbox/random"));
    }

    @Test
    void testGetMailsFromFolderCorrectSize() {
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testSendMailInvalidParametersAccountName() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.sendMail(null, "", ""));
    }

    @Test
    void testSendMailInvalidParametersMailMetadata() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.sendMail("xhist", null, ""));
    }

    @Test
    void testSendMailInvalidParametersMailContent() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.sendMail("xhist", "dasfas", null));
    }

    @Test
    void testSendMailAccountNotFound() {
        assertThrows(AccountNotFoundException.class,
            () -> outlook.sendMail("xhist", "dafas", "fsadafa"));
    }

    @Test
    void testSendMail() {
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        outlook.sendMail("stoyo", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testSendMailExisting() {
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        outlook.sendMail("stoyo", mailMetadata, mailContent);
        outlook.sendMail("stoyo", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testSendMailNonExistentRecipients() {
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: pesho@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        outlook.sendMail("stoyo", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testSendMailWithApplicableRule() {
        String rule = "subject-includes: MJT\n" +
            "subject-or-body-includes: MJT\n" +
            "from: stoyo@fmi.bg";
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        outlook.sendMail("stoyo", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testSendMailWithNotApplicableRuleFromEmail() {
        String rule = "subject-includes: MJT\n" +
            "subject-or-body-includes: MJT\n" +
            "from: stoyo@fmi.bg";
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.com");
        outlook.sendMail("stoyo", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testSendMailWithApplicableRuleToAll() {
        String rule = "subject-includes: MJT\n" +
            "subject-or-body-includes: MJT";
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.com");
        outlook.sendMail("stoyo", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testSendMailWithNotApplicableRuleFromText() {
        String rule = "subject-includes: MJT\n" +
            "subject-or-body-includes: MJT\n" +
            "from: stoyo@fmi.bg";
        String mailMetadata = "subject: yep\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to Yep course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.com");
        outlook.sendMail("stoyo", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testSendMailWithApplicableRuleTextBody() {
        String rule = "subject-or-body-includes: yes\n" +
            "from: stoyo@fmi.com";
        String mailMetadata = "subject: yes\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to Yep course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.com");
        outlook.sendMail("stoyo", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testReceiveMailInvalidParametersAccountName() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.receiveMail(null, "dasfas", "dsafas"));
    }

    @Test
    void testReceiveMailInvalidParametersMailMetadata() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.receiveMail("xhist", null, ""));
    }

    @Test
    void testReceiveMailInvalidParametersMailContent() {
        assertThrows(IllegalArgumentException.class,
            () -> outlook.receiveMail("xhist", "dsafas", null));
    }

    @Test
    void testReceiveMailAccountNotFound() {
        assertThrows(AccountNotFoundException.class,
            () -> outlook.receiveMail("xhist", "dasdas", "dsafa"));
    }

    @Test
    void testReceiveMail() {
        String mailMetadata = "subject: Hello, MJT\n" +
            "sender: stoyo@fmi.bg\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        outlook.receiveMail("xhist", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testReceiveMailInvalidSender() {
        String mailMetadata = "subject: Hello, MJT\n" +
            "sender: xhist@gmail.com\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        assertThrows(IllegalArgumentException.class,
            () -> outlook.receiveMail("xhist", mailMetadata, mailContent));
    }

    @Test
    void testReceiveMailNonExistentRecipients() {
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: pesho@gmail.com, gosho@gmail.com,\n" +
            "sender: stoyo@fmi.bg\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        outlook.receiveMail("xhist", mailMetadata, mailContent);
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testReceiveMailWithApplicableRule() {
        String rule = "subject-includes: MJT\n" +
            "subject-or-body-includes: MJT\n" +
            "from: stoyo@fmi.bg";
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "sender: stoyo@fmi.bg\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        outlook.receiveMail("xhist", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testReceiveMailWithNotApplicableRuleFromEmail() {
        String rule = "subject-includes: MJT\n" +
            "subject-or-body-includes: MJT\n" +
            "from: stoyo@fmi.bg";
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "sender: stoyo@fmi.com\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.com");
        outlook.receiveMail("xhist", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testReceiveMailWithApplicableRuleToAll() {
        String rule = "subject-includes: MJT\n" +
            "sender: stoyo@fmi.bg\n" +
            "subject-or-body-includes: MJT";
        String mailMetadata = "subject: Hello, MJT\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "sender: stoyo@fmi.com\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to MJT course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.com");
        outlook.receiveMail("xhist", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testReceiveMailWithNotApplicableRuleFromText() {
        String rule = "subject-includes: MJT\n" +
            "subject-or-body-includes: MJT\n" +
            "from: stoyo@fmi.bg";
        String mailMetadata = "subject: yep\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "sender: stoyo@fmi.bg\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to Yep course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.bg");
        outlook.receiveMail("xhist", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }

    @Test
    void testReceiveMailWithApplicableRuleTextBody() {
        String rule = "subject-or-body-includes: yes\n" +
            "from: stoyo@fmi.com";
        String mailMetadata = "subject: yes\n" +
            "recipients: xhist@gmail.com, gosho@gmail.com,\n" +
            "sender: stoyo@fmi.com\n" +
            "received: 2022-12-08 14:14";
        String mailContent = "Welcome to Yep course!";
        outlook.addNewAccount("xhist", "xhist@gmail.com");
        outlook.createFolder("xhist", "/inbox/random");
        outlook.addRule("xhist", "/inbox/random", rule, 5);
        outlook.addNewAccount("stoyo", "stoyo@fmi.com");
        outlook.receiveMail("xhist", mailMetadata, mailContent);
        assertEquals(1, outlook.getMailsFromFolder("stoyo", "/sent").size());
        assertEquals(1, outlook.getMailsFromFolder("xhist", "/inbox/random").size());
        assertEquals(0, outlook.getMailsFromFolder("xhist", "/inbox").size());
    }
}