package bti.pds.dinner.auth.application.service;

import bti.pds.dinner.auth.domain.User;
import bti.pds.dinner.auth.infrastructure.mail.EmailSender;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final EmailSender emailSender;
    private final Logger logger = LoggerFactory.getLogger(MailService.class);

    public MailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendVerificationEmail(@NonNull User user, String token) {
        String verifyLink = "http://localhost:3000/verificar?token=" + token;
        logger.info(verifyLink);
        String subject = "Confirme seu e-mail | Orderly";
        String content = buildEmailTemplate(
                "Confirmação de Cadastro",
                "Bem-vindo(a) à Orderly!",
                user.getName(),
                "Estamos muito felizes em ter você conosco! Para começar a aproveitar nosso cardápio e gerenciar seus pedidos com total facilidade, confirme o seu endereço de e-mail clicando no botão abaixo:",
                "Verificar Minha Conta",
                verifyLink,
                "Se você não criou uma conta na Orderly, nenhuma ação é necessária e você pode desconsiderar esta mensagem com total segurança."
        );

        emailSender.sendEmail(user.getEmail(), subject, content);
    }

    public void sendResetPasswordEmail(@NonNull User user, String token) {
        String resetLink = "http://localhost:3000/password-reset/validate?token=" + token;
        logger.info(resetLink);
        String subject = "Redefinição de senha | Orderly";
        String content = buildEmailTemplate(
                "Recuperação de Acesso",
                "Redefinir sua senha",
                user.getName(),
                "Recebemos uma solicitação para redefinir a senha de acesso à sua conta na Orderly. Para criar uma nova senha e continuar aproveitando a plataforma, basta clicar no botão abaixo:",
                "Redefinir Minha Senha",
                resetLink,
                "Se você não solicitou a redefinição da sua senha, nenhuma ação é necessária. Sua senha permanecerá a mesma e sua conta continuará protegida."
        );

        emailSender.sendEmail(user.getEmail(), subject, content);
    }

    private String buildEmailTemplate(
            String badgeText,
            String heading,
            String userName,
            String introText,
            String buttonText,
            String actionUrl,
            String securityNotice
    ) {
        String template = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>{{title}} | Orderly</title>
                </head>
                <body style="margin: 0; padding: 0; background-color: #fbfaf9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased; color: #292524;">
                  <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #fbfaf9; padding: 40px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 520px; background-color: #ffffff; border-radius: 16px; border: 1px solid #eee7e1; box-shadow: 0 4px 16px rgba(103, 59, 38, 0.04); overflow: hidden; text-align: left;">
                          
                          <!-- Top Brand Color Stripe -->
                          <tr>
                            <td style="height: 4px; background-color: #673b26; line-height: 4px; font-size: 4px;">&nbsp;</td>
                          </tr>

                          <!-- Main Card Body -->
                          <tr>
                            <td style="padding: 36px 36px 32px 36px;">
                              
                              <!-- Brand Logo Header -->
                              <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin-bottom: 28px;">
                                <tr>
                                  <td style="background-color: #673b26; width: 34px; height: 34px; border-radius: 8px; text-align: center; vertical-align: middle;">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: inline-block; vertical-align: middle;">
                                      <path d="M6 13.87A4 4 0 0 1 7.41 6a5.11 5.11 0 0 1 1.05-1.54 5 5 0 0 1 7.08 0A5.11 5.11 0 0 1 16.59 6 4 4 0 0 1 18 13.87V21H6Z"/>
                                      <line x1="6" x2="18" y1="17" y2="17"/>
                                    </svg>
                                  </td>
                                  <td style="padding-left: 12px; font-size: 20px; font-weight: 700; color: #1c1917; letter-spacing: -0.4px;">
                                    Orderly
                                  </td>
                                </tr>
                              </table>

                              <!-- Badge -->
                              <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin-bottom: 16px;">
                                <tr>
                                  <td style="background-color: #faebe2; border-radius: 20px; padding: 4px 12px; font-size: 12px; font-weight: 600; color: #673b26;">
                                    {{badgeText}}
                                  </td>
                                </tr>
                              </table>

                              <!-- Heading -->
                              <h1 style="margin: 0 0 12px 0; font-size: 24px; font-weight: 700; color: #1c1917; line-height: 1.25; letter-spacing: -0.5px;">
                                {{heading}}
                              </h1>

                              <!-- Greeting & Intro -->
                              <p style="margin: 0 0 16px 0; font-size: 15px; line-height: 1.6; color: #57534e;">
                                Olá, <strong>{{userName}}</strong>!
                              </p>
                              <p style="margin: 0 0 28px 0; font-size: 15px; line-height: 1.6; color: #57534e;">
                                {{introText}}
                              </p>

                              <!-- Action CTA Button Box -->
                              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-bottom: 28px;">
                                <tr>
                                  <td align="center" style="background-color: #fbfaf9; border-radius: 12px; padding: 20px;">
                                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                                      <tr>
                                        <td align="center" style="border-radius: 8px; background-color: #673b26;">
                                          <a href="{{actionUrl}}" target="_blank" style="display: inline-block; padding: 13px 32px; font-size: 15px; font-weight: 600; color: #ffffff; text-decoration: none; border-radius: 8px; background-color: #673b26;">
                                            {{buttonText}}
                                          </a>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>

                              <!-- Direct Link Fallback -->
                              <p style="margin: 0 0 8px 0; font-size: 13px; color: #78716c; line-height: 1.5;">
                                Se o botão acima não funcionar, copie e cole o link abaixo no seu navegador:
                              </p>
                              <p style="margin: 0 0 24px 0; font-size: 12px; line-height: 1.4; word-break: break-all;">
                                <a href="{{actionUrl}}" style="color: #673b26; text-decoration: underline;">
                                  {{actionUrl}}
                                </a>
                              </p>

                              <!-- Security Notice -->
                              <div style="border-top: 1px solid #f0eae4; padding-top: 20px;">
                                <p style="margin: 0; font-size: 12px; line-height: 1.5; color: #a8a29e;">
                                  {{securityNotice}}
                                </p>
                              </div>

                            </td>
                          </tr>

                          <!-- Card Footer -->
                          <tr>
                            <td style="background-color: #fbfaf9; border-top: 1px solid #f0eae4; padding: 18px 36px; text-align: center;">
                              <p style="margin: 0; font-size: 12px; color: #a8a29e;">
                                © 2026 <strong>Orderly</strong> • Gestão e pedidos inteligentes
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;

        return template
                .replace("{{title}}", heading)
                .replace("{{badgeText}}", badgeText)
                .replace("{{heading}}", heading)
                .replace("{{userName}}", userName != null ? userName : "Cliente")
                .replace("{{introText}}", introText)
                .replace("{{actionUrl}}", actionUrl)
                .replace("{{buttonText}}", buttonText)
                .replace("{{securityNotice}}", securityNotice);
    }
}
