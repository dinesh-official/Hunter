package com.devkng.Hunter.mail;

public class Template {

    public static String getSSHCybSubject(String ip) {
        return "Urgent: Secure Your node against Cyber Attacks - " + ip;
    }

    public static String getSSHCybBody(String ip, String vmName, String port) {
        return """
        <p>Dear Customer,</p>

        <p>We are writing to bring to your attention a critical security concern regarding your compute node.</p>

        <p><strong>Node IP:</strong> %s<br/>
        <strong>Node Name:</strong> %s<br/>
        <strong>Target Ports:</strong> %s</p>

        <p>Our monitoring systems have detected repeated login attempts and unauthorized access requests targeting your server. As a result, there is significantly high resource usage on the node, causing instability to the infrastructure hosting the compute node.</p>

        <p>To safeguard your system and the shared infrastructure, we strongly recommend taking the following actions immediately:</p>

        <p><strong>Recommended Actions:</strong></p>
        <ol>
            <li>
                <strong>Change Default Port:</strong>
                <ul>
                    <li>Change the default SSH/RDP port to a non-standard one to reduce exposure to automated attacks.</li>
                </ul>
            </li>
            <li>
                <strong>Enable IP Restrictions:</strong>
                <ul>
                    <li>Restrict SSH/RDP access to specific trusted IP addresses using Security Groups.</li>
                </ul>
            </li>
            <li>
                <strong>Enforce Strong Authentication:</strong>
                <ul>
                    <li>Disable password authentication and implement public key-based authentication for SSH.</li>
                    <li>Enable Multi-Factor Authentication (MFA) if applicable.</li>
                </ul>
            </li>
            <li>
                <strong>Monitor Login Attempts:</strong>
                <ul>
                    <li>Regularly monitor login attempts and configure alert systems to notify you of suspicious activity.</li>
                </ul>
            </li>
            <li>
                <strong>Install Security Updates:</strong>
                <ul>
                    <li>Keep your operating system and applications up to date to address known vulnerabilities.</li>
                </ul>
            </li>
            <li>
                <strong>Consider Security Tools:</strong>
                <ul>
                    <li>Use tools like Fail2Ban or RDP Defender to automatically block IPs with multiple failed login attempts.</li>
                    <li>Employ intrusion detection and prevention systems for enhanced protection.</li>
                </ul>
            </li>
            <li>
                <strong>Backup Data Regularly:</strong>
                <ul>
                    <li>Ensure that all critical data is backed up and can be restored in case of an incident.</li>
                </ul>
            </li>
        </ol>

        <p>We expect your immediate attention to resolve this issue. Please note that failure to take appropriate and timely action will compel us to initiate mitigation measures, which may include network suspension to protect the infrastructure.</p>

        <p>If you require any assistance or further clarification, do not hesitate to reach out to us.</p>

        <p>Regards,<br/>
        SOC Team<br/>
        E2E Networks<br/>
        Phone: +91-11-4117-1818 Ext: 3</p>
        """.formatted(ip, vmName, port);
    }

    public static String getPostgresSubject(String ip) {
        return "Urgent: Prevent Public Exposure of PostgreSQL Port on Your VM - " + ip;
    }

    public static String getPostgresBody(String ip, String vmName) {
        return """
    <p>Dear Customer,</p>

    <p>We are reaching out to inform you of a critical security concern regarding your compute node.</p>

    <p><strong>Node IP:</strong> %s<br/>
    <strong>Node Name:</strong> %s<br/>

    <p>Our system has detected that database port 5432 on your VM is currently publicly accessible.</p>
    
    <p>Exposing database ports to the public internet without proper security measures significantly increases the risk of unauthorized access, data breaches, and other cyber threats.</p>

    <p><strong>Recommendations to Secure Your Database:</strong></p>
    <ol>
        <li>
            <strong>Restrict Access to Database Ports:</strong>
            <ul>
                <li>Configure your firewall or security group rules to allow access only from specific, trusted IP addresses.</li>
            </ul>
        </li>
        <li>
            <strong>Disable Public Access:</strong>
            <ul>
                <li>If public access to the database is not required, bind the database service to the local interface (127.0.0.1) to restrict external connections.</li>
            </ul>
        </li>
        <li>
            <strong>Use a VPN or Bastion Host:</strong>
            <ul>
                <li>Require connections to the database to go through a secure VPN or bastion host for additional protection.</li>
            </ul>
        </li>
    </ol>

    <p>Please address this issue at the earliest to ensure the security of your VM and data. If needed, our support team is available to assist you in implementing the necessary changes.</p>

    <p>Please refer to this article for security <a href="https://docs.e2enetworks.com/security/bestpractice/index.html" target="_blank">Best Practices</a>.</p>

    <p>Regards,<br/>
    SOC Team<br/>
    E2E Networks<br/>
    Phone: +91-11-4117-1818 Ext: 3</p>
    """.formatted(ip, vmName);
    }


    public static String getMongoSubject(String ip) {
        return "Urgent: Prevent Public Exposure of MongoDB Port on Your VM - " + ip;
    }

    public static String getMongoBody(String ip, String vmName) {
        return """
        <p>Dear Customer,</p>

        <p>We are reaching out to inform you of a critical security concern regarding your compute node.</p>

        <p><strong>Node IP:</strong> %s<br/>
        <strong>Node Name:</strong> %s</p>

        <p>Our system has detected that database port 27017 on your VM is currently publicly accessible.</p>

        <p>Exposing database ports to the public internet without proper security measures significantly increases the risk of unauthorized access, data breaches, and other cyber threats.</p>

        <p><strong>Recommendations to Secure Your Database:</strong></p>
        <ol>
            <li>
                <strong>Restrict Access to Database Ports:</strong>
                <ul>
                    <li>Configure your firewall or security group rules to allow access only from specific, trusted IP addresses.</li>
                </ul>
            </li>
            <li>
                <strong>Disable Public Access:</strong>
                <ul>
                    <li>If public access to the database is not required, bind the database service to the local interface (127.0.0.1) to restrict external connections.</li>
                </ul>
            </li>
            <li>
                <strong>Use a VPN or Bastion Host:</strong>
                <ul>
                    <li>Require connections to the database to go through a secure VPN or bastion host for additional protection.</li>
                </ul>
            </li>
        </ol>

        <p>Please address this issue at the earliest to ensure the security of your VM and data. If needed, our support team is available to assist you in implementing the necessary changes.</p>

        <p>Please refer to this article for security <a href="https://docs.e2enetworks.com/security/bestpractice/index.html" target="_blank">Best Practices</a>.</p>

        <p>Regards,<br/>
        SOC Team<br/>
        E2E Networks<br/>
        Phone: +91-11-4117-1818 Ext: 3</p>
        """.formatted(ip, vmName);
    }


    public static String getRedisSubject(String ip) {
        return "Urgent: Prevent Public Exposure of Redis Port on Your VM - " + ip;
    }

    public static String getRedisBody(String ip, String vmName) {
        return """
        <p>Dear Customer,</p>

        <p>We are reaching out to inform you of a critical security concern regarding your compute node.</p>

        <p><strong>Node IP:</strong> %s<br/>
        <strong>Node Name:</strong> %s</p>

        <p>Our system has detected that database port 6379 on your VM is currently publicly accessible.</p>

        <p>Exposing database ports to the public internet without proper security measures significantly increases the risk of unauthorized access, data breaches, and other cyber threats.</p>

        <p><strong>Recommendations to Secure Your Database:</strong></p>
        <ol>
            <li>
                <strong>Restrict Access to Database Ports:</strong>
                <ul>
                    <li>Configure your firewall or security group rules to allow access only from specific, trusted IP addresses.</li>
                </ul>
            </li>
            <li>
                <strong>Disable Public Access:</strong>
                <ul>
                    <li>If public access to the database is not required, bind the database service to the local interface (127.0.0.1) to restrict external connections.</li>
                </ul>
            </li>
            <li>
                <strong>Use a VPN or Bastion Host:</strong>
                <ul>
                    <li>Require connections to the database to go through a secure VPN or bastion host for additional protection.</li>
                </ul>
            </li>
        </ol>

        <p>Please address this issue at the earliest to ensure the security of your VM and data. If needed, our support team is available to assist you in implementing the necessary changes.</p>

        <p>Please refer to this article for security <a href="https://docs.e2enetworks.com/security/bestpractice/index.html" target="_blank">Best Practices</a>.</p>

        <p>Regards,<br/>
        SOC Team<br/>
        E2E Networks<br/>
        Phone: +91-11-4117-1818 Ext: 3</p>
        """.formatted(ip, vmName);
    }


    public static String getMssqlSubject(String ip) {
        return "Urgent: Prevent Public Exposure of MS-SQL Port on Your VM - " + ip;
    }

    public static String getMssqlBody(String ip, String vmName) {
        return """
        <p>Dear Customer,</p>

        <p>We are reaching out to inform you of a critical security concern regarding your compute node.</p>

        <p><strong>Node IP:</strong> %s<br/>
        <strong>Node Name:</strong> %s</p>

        <p>Our system has detected that database port 1433 on your VM is currently publicly accessible.</p>

        <p>Exposing database ports to the public internet without proper security measures significantly increases the risk of unauthorized access, data breaches, and other cyber threats.</p>

        <p><strong>Recommendations to Secure Your Database:</strong></p>
        <ol>
            <li>
                <strong>Restrict Access to Database Ports:</strong>
                <ul>
                    <li>Configure your firewall or security group rules to allow access only from specific, trusted IP addresses.</li>
                </ul>
            </li>
            <li>
                <strong>Disable Public Access:</strong>
                <ul>
                    <li>If public access to the database is not required, bind the database service to the local interface (127.0.0.1) to restrict external connections.</li>
                </ul>
            </li>
            <li>
                <strong>Use a VPN or Bastion Host:</strong>
                <ul>
                    <li>Require connections to the database to go through a secure VPN or bastion host for additional protection.</li>
                </ul>
            </li>
        </ol>

        <p>Please address this issue at the earliest to ensure the security of your VM and data. If needed, our support team is available to assist you in implementing the necessary changes.</p>

        <p>Please refer to this article for security <a href="https://docs.e2enetworks.com/security/bestpractice/index.html" target="_blank">Best Practices</a>.</p>

        <p>Regards,<br/>
        SOC Team<br/>
        E2E Networks<br/>
        Phone: +91-11-4117-1818 Ext: 3</p>
        """.formatted(ip, vmName);
    }


    public static String getMemcachedSubject(String ip) {
        return "Urgent: Prevent Public Exposure of Memcache Port on Your VM - " + ip;
    }

    public static String getMemcachedBody(String ip, String vmName) {
        return """
        <p>Dear Customer,</p>

        <p>We are reaching out to inform you of a critical security concern regarding your compute node.</p>

        <p><strong>Node IP:</strong> %s<br/>
        <strong>Node Name:</strong> %s</p>

        <p>Our system has detected that database port 11211 on your VM is currently publicly accessible.</p>

        <p>Exposing database ports to the public internet without proper security measures significantly increases the risk of unauthorized access, data breaches, and other cyber threats.</p>

        <p><strong>Recommendations to Secure Your Database:</strong></p>
        <ol>
            <li>
                <strong>Restrict Access to Database Ports:</strong>
                <ul>
                    <li>Configure your firewall or security group rules to allow access only from specific, trusted IP addresses.</li>
                </ul>
            </li>
            <li>
                <strong>Disable Public Access:</strong>
                <ul>
                    <li>If public access to the database is not required, bind the database service to the local interface (127.0.0.1) to restrict external connections.</li>
                </ul>
            </li>
            <li>
                <strong>Use a VPN or Bastion Host:</strong>
                <ul>
                    <li>Require connections to the database to go through a secure VPN or bastion host for additional protection.</li>
                </ul>
            </li>
        </ol>

        <p>Please address this issue at the earliest to ensure the security of your VM and data. If needed, our support team is available to assist you in implementing the necessary changes.</p>

        <p>Please refer to this article for security <a href="https://docs.e2enetworks.com/security/bestpractice/index.html" target="_blank">Best Practices</a>.</p>

        <p>Regards,<br/>
        SOC Team<br/>
        E2E Networks<br/>
        Phone: +91-11-4117-1818 Ext: 3</p>
        """.formatted(ip, vmName);
    }


    public static String getDefaultSubject(String ip) {
        return "Urgent: Prevent Public Exposure of Database Port on Your VM - " + ip ;
    }

    public static String getDefaultBody(String ip, String vmName, String port) {
        return """
        <p>Dear Customer,</p>

        <p>We are reaching out to inform you of a critical security concern regarding your compute node.</p>

        <p><strong>Node IP:</strong> %s<br/>
        <strong>Node Name:</strong> %s</p>

        <p>Our system has detected that database port <strong>%s</strong> on your VM is currently publicly accessible.</p>

        <p>Exposing database ports to the public internet without proper security measures significantly increases the risk of unauthorized access, data breaches, and other cyber threats.</p>

        <p><strong>Recommendations to Secure Your Database:</strong></p>
        <ol>
            <li><strong>Restrict Access to Database Ports:</strong>
                <ul>
                    <li>Configure your firewall or security group rules to allow access only from specific, trusted IP addresses.</li>
                </ul>
            </li>
            <li><strong>Disable Public Access:</strong>
                <ul>
                    <li>If public access to the database is not required, bind the database service to the local interface (127.0.0.1) to restrict external connections.</li>
                </ul>
            </li>
            <li><strong>Use a VPN or Bastion Host:</strong>
                <ul>
                    <li>Require connections to the database to go through a secure VPN or bastion host for additional protection.</li>
                </ul>
            </li>
        </ol>

        <p>Please address this issue at the earliest to ensure the security of your VM and data. If needed, our support team is available to assist you in implementing the necessary changes.</p>

        <p>Please refer to this article for security <a href="https://docs.e2enetworks.com/security/bestpractice/index.html" target="_blank">Best Practices</a>.</p>

        <p>Regards,<br/>
        SOC Team<br/>
        E2E Networks<br/>
        Phone: +91-11-4117-1818 Ext: 3</p>
        """.formatted(ip, vmName, port);
    }
}
