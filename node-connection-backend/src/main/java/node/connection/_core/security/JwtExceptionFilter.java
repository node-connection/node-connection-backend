package node.connection._core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import node.connection._core.exception.ExceptionResponseWriter;
import node.connection._core.exception.client.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ExceptionResponseWriter responseWriter;


    public JwtExceptionFilter(@Autowired ExceptionResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        }
        catch (UnauthorizedException exception) {
            responseWriter.write(response, exception);
        }
    }
}
