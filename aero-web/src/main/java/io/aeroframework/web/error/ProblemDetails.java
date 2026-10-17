package io.aeroframework.web.error;

import java.net.URI;

public record ProblemDetails(String title,
                             String detail,
                             String cause,
                             URI instance,
                             int status) {
}
