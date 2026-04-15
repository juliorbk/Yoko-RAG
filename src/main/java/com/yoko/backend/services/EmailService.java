package com.yoko.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String senderEmail;

  // Spring inyecta automáticamente el JavaMailSender gracias a la dependencia
  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendWelcomeEmail(String toEmail, String userName) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(senderEmail);
      helper.setTo(toEmail);
      // 1. Formateamos el asunto por separado
      helper.setSubject(String.format("¡Bienvenido a Yoko, %s! 🤖", userName));

      // 2. Formateamos el HTML (Solo hay dos variables: %s para el nombre y %d para el año)
      String htmlBody = String.format(
        """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin: 0; padding: 0; background-color: #f4f4f5; font-family: 'Segoe UI', Arial, sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f5; padding: 40px 0;">
                <tr>
                    <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1);">

                            <tr>
                                <td style="background-color: #3366FF; padding: 0;">
                                    <table width="100%%" cellpadding="0" cellspacing="0" style="padding: 30px 40px;">
                                        <tr>
                                            <td style="text-align: left;">
                                                <img src="https://i.imgur.com/UjFsvrW.png" alt="UNEG" height="45" style="display: block; border: 0;">
                                            </td>
                                            <td style="text-align: right; color: #ffffff; font-size: 12px; font-weight: bold; letter-spacing: 1px;">
                                                INICIO &nbsp; &bull; &nbsp; DOCS &nbsp; &bull; &nbsp; SOPORTE
                                            </td>
                                        </tr>
                                    </table>

                                    <table width="100%%" cellpadding="0" cellspacing="0" style="padding: 0 40px 40px 40px;">
                                        <tr>
                                            <td align="center">
                                                <h2 style="color: #ffffff; margin: 0; font-size: 16px; font-weight: 500; letter-spacing: 2px; text-transform: uppercase; opacity: 0.9;">
                                                    TU NUEVO ASISTENTE DE IA
                                                </h2>
                                                <img src="https://i.imgur.com/6LVnMKt.png" alt="Yoko AI" width="280" style="margin-top: 20px; display: block; clickable: false;">
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>

                            <tr>
                                <td style="padding: 50px 50px; text-align: center;">
                                    <h1 style="color: #1e293b; font-size: 28px; font-weight: 800; margin: 0 0 15px 0; letter-spacing: -0.5px;">
                                        EMPIEZA A CHATEAR
                                    </h1>
                                    <p style="color: #64748b; font-size: 17px; line-height: 1.6; margin: 0 0 35px 0;">
                                        ¡Hola <strong>%s</strong>! 👋 Estamos listos para ayudarte a resolver dudas y potenciar tus estudios en la UNEG con inteligencia artificial.
                                    </p>

                                    <table width="100%%" cellpadding="0" cellspacing="0">
                                        <tr>
                                            <td align="center">
                                                <a href="https://yoko-frontend-rho.vercel.app" target="_blank" style="background-color: #1e293b; color: #ffffff; padding: 16px 32px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block; font-size: 16px;">
                                                    INICIAR CONVERSACIÓN
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>

                            <tr>
                                <td style="background-color: #f8fafc; padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                                    <p style="margin: 0; color: #94a3b8; font-size: 13px; font-weight: 500;">
                                        yoko.uneg.edu.ve | Universidad Nacional Experimental de Guayana
                                    </p>
                                    <p style="margin: 8px 0 0 0; color: #cbd5e1; font-size: 11px; text-transform: uppercase; letter-spacing: 1px;">
                                        © %d Yoko AI. Todos los derechos reservados.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """,
        userName,
        java.time.Year.now().getValue()
      );

      helper.setText(htmlBody, true);

      mailSender.send(message);
      log.info("📧 Correo de bienvenida enviado exitosamente a: {}", toEmail);
    } catch (MessagingException e) {
      log.error(
        "❌ Error al enviar el correo de bienvenida a {}: {}",
        toEmail,
        e.getMessage()
      );
    }
  }
}
