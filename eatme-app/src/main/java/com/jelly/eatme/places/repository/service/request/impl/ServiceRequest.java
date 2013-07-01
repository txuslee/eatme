package com.jelly.eatme.places.repository.service.request.impl;

import com.jelly.eatme.places.repository.service.ServiceException;
import com.jelly.eatme.places.repository.service.request.IServiceRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceRequest implements IServiceRequest {

    private static final Pattern PATTERN = Pattern.compile("\\$\\{[-a-zA-Z0-9._]+\\}");

    public static final String DEFAULT_CONTENT_TYPE = "application/xml;charset=utf-8";
    public static final String DEFAULT_ACCEPT = "application/xml";

    private String contentType = DEFAULT_CONTENT_TYPE;
    private String accept = DEFAULT_ACCEPT;

    private Method method;
    private String uri;

    public ServiceRequest(Method method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public String getAccept() {
        return this.accept;
    }

    @Override
    public String getUri() {
        return this.uri;
    }

    @Override
    public String build(String... values) throws ServiceException {
        Matcher matcher = PATTERN.matcher(this.uri);
        StringBuffer buffer = new StringBuffer();
        int count = 0;
        while (matcher.find()) {
            if (count + 1 <= values.length) {
                // replace key from braced parameters
                matcher.appendReplacement(buffer, values[count++]);
            } else {
                throw new ServiceException("Index out of bounds");
            }
        }
        if (values.length != count) {
            throw new ServiceException("Malformed URI parameters");
        }
        matcher.appendTail(buffer);
        this.uri = buffer.toString();
        return this.uri;
    }

}
