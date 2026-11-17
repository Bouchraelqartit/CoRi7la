package ma.youcode.CoRi7la.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
}