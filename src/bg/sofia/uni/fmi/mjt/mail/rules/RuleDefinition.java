package bg.sofia.uni.fmi.mjt.mail.rules;

import bg.sofia.uni.fmi.mjt.mail.Mail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record RuleDefinition(String path, Set<String> subjectKeywords, Set<String> bodyKeywords,
                             Set<String> recipientEmails, String fromEmail, int priority) {
    public boolean matchesMail(Mail mail, String receiverEmail) {
        String mailBody = mail.body();
        String mailSubject = mail.subject();
        Set<String> bodyKeywords = new HashSet<>();
        bodyKeywords.addAll(List.of(mailBody.split("\\W+")));
        Set<String> subjectKeywords = new HashSet<>();
        subjectKeywords.addAll(List.of(mailSubject.split("\\W+")));
        return (fromEmail.isEmpty() || fromEmail.equals(mail.sender().emailAddress())) &&
            (recipientEmails.isEmpty() || recipientEmails.contains(receiverEmail)) &&
            (bodyKeywords.containsAll(this.bodyKeywords) || subjectKeywords.containsAll(this.subjectKeywords));
    }
}