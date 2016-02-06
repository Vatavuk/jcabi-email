/**
 * Copyright (c) 2014-2015, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.email.wire;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.email.Protocol;
import com.jcabi.email.enclosure.EnHTML;
import com.jcabi.email.enclosure.EnPlain;
import com.jcabi.email.stamp.StBCC;
import com.jcabi.email.stamp.StCC;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.email.stamp.StSubject;
import com.jcabi.email.token.Sign;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import javax.mail.internet.InternetAddress;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SMTP}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 1.0
 */
public final class SMTPTest {

    /**
     * SMTP can send errors by email through SMTP.
     * @throws Exception If fails
     */
    @Test
    public void sendsErrorsBySmtp() throws Exception {
        final int port = SMTPTest.port();
        final SimpleSmtpServer server = SimpleSmtpServer.start(port);
        try {
            new Postman.Default(
                new SMTP(
                    new Sign("", "")
                        .access(new Protocol.SMTP("localhost", port))
                )
            ).send(
                new Envelope.Safe(
                    new Envelope.MIME()
                        .with(new StSender("from <test-from@jcabi.com>"))
                        .with(new StRecipient("to", "test-to@jcabi.com"))
                        .with(new StCC(new InternetAddress("cc <c@jcabi.com>")))
                        .with(new StBCC("bcc <bcc@jcabi.com>"))
                        .with(new StSubject("test subject: test me"))
                        .with(new EnPlain("hello"))
                        .with(new EnHTML("<p>how are you?</p>"))
                )
            );
        } finally {
            server.stop();
        }
        MatcherAssert.assertThat(server.getReceivedEmailSize(), Matchers.is(1));
        final Iterator<?> emails = server.getReceivedEmail();
        final SmtpMessage email = SmtpMessage.class.cast(emails.next());
        MatcherAssert.assertThat(
            email.getHeaderValue("Subject"),
            Matchers.containsString("test me")
        );
    }

    /**
     * Allocate free port.
     * @return Found port.
     * @throws IOException In case of error.
     */
    private static int port() throws IOException {
        final ServerSocket socket = new ServerSocket(0);
        try {
            return socket.getLocalPort();
        } finally {
            socket.close();
        }
    }

}
