package ma.youcode.CoRi7la.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        return creerErreur(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler(BusinessConflictException.class)
    public ProblemDetail handleConflict(
            BusinessConflictException exception,
            HttpServletRequest request) {

        return creerErreur(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ProblemDetail handleForbidden(
            ForbiddenActionException exception,
            HttpServletRequest request) {

        return creerErreur(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ProblemDetail handleInvalidRequest(
            InvalidRequestException exception,
            HttpServletRequest request) {

        return creerErreur(HttpStatus.BAD_REQUEST, exception, request);
    }

    private ProblemDetail creerErreur(
            HttpStatus statut,
            RuntimeException exception,
            HttpServletRequest request) {

        log.warn("Action refusée sur {} : {}",
                request.getRequestURI(), exception.getMessage());

        ProblemDetail erreur = ProblemDetail.forStatusAndDetail(
                statut, exception.getMessage()
        );

        erreur.setTitle(statut.getReasonPhrase());
        erreur.setType(URI.create(
                "urn:cori7la:error:" + statut.value()
        ));
        erreur.setInstance(URI.create(request.getRequestURI()));
        erreur.setProperty("timestamp", Instant.now());

        return erreur;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> erreurs = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .toList();

        String chemin = ((ServletWebRequest) request)
                .getRequest()
                .getRequestURI();

        log.warn("Validation refusée sur {} : {}", chemin, erreurs);

        ProblemDetail erreur = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Les données envoyées sont invalides."
        );

        erreur.setTitle("Erreur de validation");
        erreur.setType(URI.create("urn:cori7la:error:validation"));
        erreur.setInstance(URI.create(chemin));
        erreur.setProperty("timestamp", Instant.now());
        erreur.setProperty("errors", erreurs);

        return handleExceptionInternal(
                exception, erreur, headers, status, request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

        log.error("Erreur inattendue sur {}",
                request.getRequestURI(), exception);

        ProblemDetail erreur = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue."
        );

        erreur.setTitle("Erreur interne");
        erreur.setType(URI.create("urn:cori7la:error:500"));
        erreur.setInstance(URI.create(request.getRequestURI()));
        erreur.setProperty("timestamp", Instant.now());

        return erreur;
    }
}