package com.axibase.tsd.driver.jdbc.content.json;

import java.util.List;

public interface AtsdExceptionRepresentation {
    String getState();

    List<ExceptionSection> getException();

    String getMessage();
}
