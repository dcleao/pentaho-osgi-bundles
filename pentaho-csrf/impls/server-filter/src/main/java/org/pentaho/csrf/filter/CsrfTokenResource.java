package org.pentaho.csrf.filter;

import org.pentaho.csrf.ICsrfService;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path( "/system/csrf" )
public class CsrfTokenResource {
  private static final String REQUEST_ATTRIBUTE_NAME = "_csrf";

  @Context
  private HttpServletRequest request;

  @Context
  private HttpServletResponse response;

  @GET
  @Path( "/" )
  public Response getCsrfHeaders() {
    Response.ResponseBuilder responseBuilder = Response.noContent();

    final CsrfToken token = (CsrfToken) request.getAttribute( REQUEST_ATTRIBUTE_NAME );
    if ( token != null ) {
      final String tokenHeaderName = token.getHeaderName();
      responseBuilder.header( ICsrfService.RESPONSE_HEADER_HEADER, tokenHeaderName );

      final String tokenParameterName = token.getParameterName();
      responseBuilder.header( ICsrfService.RESPONSE_HEADER_PARAM, tokenParameterName );

      final String tokenValue = token.getToken();
      responseBuilder.header( ICsrfService.RESPONSE_HEADER_TOKEN, tokenValue );
    }

    // Add CORS headers.
    CsrfUtil.setCorsResponseHeaders( request, response );

    return responseBuilder.build();
  }
}
