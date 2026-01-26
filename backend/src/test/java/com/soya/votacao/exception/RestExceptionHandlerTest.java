package com.soya.votacao.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class RestExceptionHandlerTest {
    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void handleNotFoundBuildsResponse() {
        HttpServletRequest request = mockRequest("/pautas/1");
        ErrorResponse response = handler.handleNotFound(new NotFoundException("Nao achou"), request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Nao achou");
    }

    @Test
    void handleValidationBuildsMessage() {
        HttpServletRequest request = mockRequest("/pautas");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "titulo", "Titulo obrigatorio"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter(), bindingResult);

        ErrorResponse response = handler.handleValidation(ex, request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMessage()).contains("Titulo obrigatorio");
    }

    @Test
    void handleUnreadableUsesDefaultMessage() {
        HttpServletRequest request = mockRequest("/pautas");
        ErrorResponse response = handler.handleUnreadable(new HttpMessageNotReadableException("bad"), request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Corpo da requisição inválido");
    }

    @Test
    void handleNoResourceUsesNotFound() {
        HttpServletRequest request = mockRequest("/nao-existe");
        NoResourceFoundException ex = Mockito.mock(NoResourceFoundException.class);

        ErrorResponse response = handler.handleNoResource(ex, request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Recurso não encontrado");
    }

    @Test
    void handleGenericUsesFallbackMessage() {
        HttpServletRequest request = mockRequest("/erro");
        ErrorResponse response = handler.handleGeneric(new RuntimeException("boom"), request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMessage()).isEqualTo("Erro inesperado");
    }

    private HttpServletRequest mockRequest(String uri) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn(uri);
        Mockito.when(request.getMethod()).thenReturn("GET");
        return request;
    }

    private MethodParameter methodParameter() {
        try {
            return new MethodParameter(getClass().getDeclaredMethod("dummyMethod", String.class), 0);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Metodo de teste nao encontrado", ex);
        }
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String value) {
        // usado apenas para obter MethodParameter no teste.
    }
}


