package com.safedata.sdr.solicitudes.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")      private String remitente;
    @Value("${app.mail.from-name}") private String nombreRemitente;
    @Value("${app.2fa.issuer}")     private String nombrePortal;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** Envío genérico HTML. Devuelve true si salió bien. */
    public boolean enviarHtml(String destinatario, String asunto, String htmlBody) {
        return enviarHtml(destinatario, null, null, asunto, htmlBody, null);
    }

    public boolean enviarHtml(String destinatario, String conCopia, String copiaOculta,
                              String asunto, String htmlBody, List<File> adjuntos) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Remitente con nombre visible
            helper.setFrom(new InternetAddress(remitente, nombreRemitente));
            helper.setTo(parseDirecciones(destinatario));
            if (conCopia != null && !conCopia.isBlank()) {
                helper.setCc(parseDirecciones(conCopia));
            }
            if (copiaOculta != null && !copiaOculta.isBlank()) {
                helper.setBcc(parseDirecciones(copiaOculta));
            }

            helper.setSubject(asunto);
            helper.setText(htmlBody, true);  // true = HTML

            if (adjuntos != null) {
                for (File f : adjuntos) {
                    helper.addAttachment(f.getName(), new FileSystemResource(f));
                }
            }

            mailSender.send(message);
            log.info("Correo enviado a {} (asunto: {})", destinatario, asunto);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error enviando correo a {}: {}", destinatario, e.getMessage(), e);
            return false;
        }
    }

    /** Correo específico para contraseña temporal. */
    public void enviarContrasenaTemporal(String destino, String nombreCompleto, String passwordTemporal) {
        String html = """
            <div style="font-family:Segoe UI,Roboto,Arial,sans-serif;color:#2b2f36;max-width:520px;">
              <h2 style="color:#14477a;margin:0 0 12px;">Bienvenido al %s</h2>
              <p>Hola <b>%s</b>,</p>
              <p>Se creó una cuenta para ti. Estos son tus accesos temporales:</p>
              <table style="border-collapse:collapse;margin:16px 0;">
                <tr><td style="padding:6px 12px;background:#eaf1fa;border:1px solid #dfe4ea;"><b>Usuario</b></td>
                    <td style="padding:6px 12px;border:1px solid #dfe4ea;">%s</td></tr>
                <tr><td style="padding:6px 12px;background:#eaf1fa;border:1px solid #dfe4ea;"><b>Contraseña temporal</b></td>
                    <td style="padding:6px 12px;border:1px solid #dfe4ea;font-family:Consolas,monospace;">%s</td></tr>
              </table>
              <p style="color:#c0392b;"><b>Por seguridad, al iniciar sesión el sistema te pedirá cambiar la contraseña.</b></p>
              <p style="font-size:0.85em;color:#6b7280;">Si no esperabas este correo, ignóralo.</p>
            </div>
            """.formatted(nombrePortal, nombreCompleto, destino, passwordTemporal);

        enviarHtml(destino, null, null,
                nombrePortal + " - Tu acceso temporal", html, null);
    }
    
    /** Correo para cuando un admin restablece la contraseña de un usuario existente. */
    public void enviarContrasenaRestablecida(String destino, String nombreCompleto, String passwordTemporal) {
        String html = """
            <div style="font-family:Segoe UI,Roboto,Arial,sans-serif;color:#2b2f36;max-width:520px;">
              <h2 style="color:#14477a;margin:0 0 12px;">%s</h2>
              <p>Hola <b>%s</b>,</p>
              <p>Un administrador restableció la contraseña de tu cuenta.
                 Estos son tus nuevos accesos temporales:</p>
              <table style="border-collapse:collapse;margin:16px 0;">
                <tr><td style="padding:6px 12px;background:#eaf1fa;border:1px solid #dfe4ea;"><b>Usuario</b></td>
                    <td style="padding:6px 12px;border:1px solid #dfe4ea;">%s</td></tr>
                <tr><td style="padding:6px 12px;background:#eaf1fa;border:1px solid #dfe4ea;"><b>Contraseña temporal</b></td>
                    <td style="padding:6px 12px;border:1px solid #dfe4ea;font-family:Consolas,monospace;">%s</td></tr>
              </table>
              <p style="color:#c0392b;"><b>Por seguridad, al iniciar sesión el sistema te pedirá cambiar la contraseña.</b></p>
              <p style="font-size:0.85em;color:#6b7280;">
                Si no esperabas este correo, contacta al administrador del portal.
              </p>
            </div>
            """.formatted(nombrePortal, nombreCompleto, destino, passwordTemporal);

        enviarHtml(destino, null, null,
                nombrePortal + " - Contraseña restablecida", html, null);
    }
    
    /** Correo para restablecer contraseña olvidada. */
    public void enviarRecuperacionContrasena(String destino, String nombreCompleto, String enlace) {
        String html = """
            <div style="font-family:Segoe UI,Roboto,Arial,sans-serif;color:#2b2f36;max-width:520px;">
              <h2 style="color:#14477a;margin:0 0 12px;">%s</h2>
              <p>Hola <b>%s</b>,</p>
              <p>Recibimos una solicitud para restablecer tu contraseña.</p>
              <p style="margin:24px 0;">
                <a href="%s"
                   style="background:#14477a;color:#fff;padding:11px 22px;border-radius:22px;
                          text-decoration:none;font-weight:600;">
                  Restablecer contraseña
                </a>
              </p>
              <p style="color:#6b7280;font-size:0.85em;">
                O copia este enlace en tu navegador:<br>
                <span style="word-break:break-all;">%s</span>
              </p>
              <p style="color:#c0392b;font-size:0.85em;">
                El enlace expira en 30 minutos. Si no solicitaste esto, ignora este correo.
              </p>
            </div>
            """.formatted(nombrePortal, nombreCompleto, enlace, enlace);

        enviarHtml(destino, null, null,
                nombrePortal + " - Restablecer contraseña", html, null);
    }

    /** Convierte "a@x.com;b@y.com" en array de InternetAddress. */
    private InternetAddress[] parseDirecciones(String raw) throws MessagingException {
        String[] partes = raw.split(";");
        InternetAddress[] dirs = new InternetAddress[partes.length];
        for (int i = 0; i < partes.length; i++) {
            dirs[i] = new InternetAddress(partes[i].trim());
        }
        return dirs;
    }
}