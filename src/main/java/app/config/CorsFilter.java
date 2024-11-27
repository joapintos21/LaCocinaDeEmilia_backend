package app.config;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CorsFilter implements Filter {

    private String baseUrl;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        baseUrl = System.getenv("BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:3000"; // Fallback
        }
    }

    @Override
    public void doFilter(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Configurar los encabezados de CORS
        response.setHeader("Access-Control-Allow-Origin", baseUrl);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

        // Si es una solicitud OPTIONS, responder con 200 OK y detener la cadena de
        // filtros
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Continua con la cadena de filtros
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Método destroy opcional, puedes dejarlo vacío si no necesitas limpieza
        // adicional
    }
}
