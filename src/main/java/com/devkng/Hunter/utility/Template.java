package com.devkng.Hunter.utility;

public class Template {

    public static String getSSHCybSubject(String ip) {
        return "Urgent: Secure Your node against Cyber Attacks - " + ip;
    }

    public static String getSSHCybBody(String ip, String vmName, int port) {
        return """
        <p>Dear Customer,</p>

        <p>We are writing to bring to your attention a critical security concern regarding your compute node.</p>

        <p><strong>Node IP:</strong> %s<br/>
        <strong>Node Name:</strong> %s<br/>
        <strong>Target Ports:</strong> %d</p>

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

}
