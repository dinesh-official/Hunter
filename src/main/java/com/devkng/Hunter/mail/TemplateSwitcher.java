package com.devkng.Hunter.mail;

public class TemplateSwitcher {

    public static class MailTemplate {
        public final String subject;
        public final String body;

        public MailTemplate(String subject, String body) {
            this.subject = subject;
            this.body = body;
        }
    }

    public static MailTemplate getTemplateForPort(int port_case, String ip, String vmName, String port) {
        switch (port_case) {
            case 22: // For SSH brute-force
                return new MailTemplate(
                        Template.getSSHCybSubject(ip),
                        Template.getSSHCybBody(ip, vmName, port)
                );
            case 5432: // PostgreSQL
                return new MailTemplate(
                        Template.getPostgresSubject(ip),
                        Template.getPostgresBody(ip, vmName)
                );
            case 27017: // MongoDB
                return new MailTemplate(
                        Template.getMongoSubject(ip),
                        Template.getMongoBody(ip, vmName)
                );
            case 6379: // Redis
                return new MailTemplate(
                        Template.getRedisSubject(ip),
                        Template.getRedisBody(ip, vmName)
                );
            case 1433: // MS SQL
                return new MailTemplate(
                        Template.getMssqlSubject(ip),
                        Template.getMssqlBody(ip, vmName)
                );
            case 11211: // Memcached
                return new MailTemplate(
                        Template.getMemcachedSubject(ip),
                        Template.getMemcachedBody(ip, vmName)
                );
            default:
                return new MailTemplate(
                        Template.getDefaultSubject(ip),
                        Template.getDefaultBody(ip, vmName, port)
                );
        }
    }
}
