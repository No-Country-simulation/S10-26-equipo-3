package com.nautiflow.service;

final class CorreoHtmlTemplate {
    private CorreoHtmlTemplate() {
    }

    static String escapar(String valor) {
        return valor == null ? "" : valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    static String renderizar(String titulo, String emoji, String contenidoHtml) {
        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:Arial,Helvetica,sans-serif;color:#16324f">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                         style="width:100%%;background-color:#f1f5f9">
                    <tr>
                      <td align="center" style="padding:24px 12px">
                        <table role="presentation" width="620" cellspacing="0" cellpadding="0" border="0"
                               style="width:100%%;max-width:620px;background-color:#ffffff;border-radius:16px;overflow:hidden">
                          <tr>
                            <td style="padding:28px;background-color:#075985;color:#ffffff">
                              <div style="font-size:13px;font-weight:bold;letter-spacing:2px">NAUTIFLOW</div>
                              <h1 style="margin:12px 0 0;font-size:24px;line-height:1.35;color:#ffffff">%s %s</h1>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 28px">
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                                     style="width:100%%;background-color:#f8fafc;border-radius:10px">
                                <tr>
                                  <td style="padding:20px;font-size:16px;line-height:1.65;color:#16324f">
                                    %s
                                  </td>
                                </tr>
                              </table>
                              <p style="margin:24px 0 0;font-size:14px;line-height:1.6;color:#475569">
                                El equipo de NautiFlow
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(escapar(titulo), emoji, escapar(titulo), contenidoHtml);
    }
}
