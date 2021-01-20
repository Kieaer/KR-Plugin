package data.auth

import data.Config
import org.simplejavamail.api.email.Email
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder

object Email {
    fun send(){
        val email: Email = EmailBuilder.startingBlank()
            .to("lollypop", "lolly.pop@somemail.com")
            .bcc("Mr Sweetnose <snose@candyshop.org>")
            .withSubject("hey")
            .withHTMLText("<img src='cid:wink1'><b>We should meet up!</b><img src='cid:wink2'>")
            .withPlainText("Please view this email in a modern email client!")
            .buildEmail()

        val mailer: Mailer = MailerBuilder
            .withSMTPServer(Config.smtpServer, Config.smtpPort, Config.emailID, Config.emailPassword)
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .withSessionTimeout(10 * 1000)
            .async()
            .buildMailer();

        mailer.sendMail(email)
    }
}