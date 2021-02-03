package org.pentaho.platform.web.csrf.filter;

import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * This REST resource represents the CSRF token that should be used to request a given user session and URL.
 * <p>
 * This resource is designed to work with the {@link CsrfGateFilter}, which is assumed to be applied to this resource.
 * It is very important that this resource's URL is not, itself, configured to be protected by CSRF. Additionally, it
 * should be possible to call this resource anonymously, so that login operations themselves can be secured.
 * <p>
 * The {@code CsrfGateFilter} places a secret CSRF token in the {@link javax.ws.rs.core.Request} attribute named {@code
 * _crsf}. This exact token must be specified, in the current user session, in the next request to the URL specified in
 * the {@code url} query parameter.
 * <p>
 * This resource simply returns the CSRF token information in the corresponding response headers.
 */
@Path( "/system/csrf" )
public class CsrfTokenService {
  /**
   * The name of the {@link javax.ws.rs.core.Request} attribute where {@link CsrfGateFilter} places the {@link
   * CsrfToken} instance.
   */
  private static final String REQUEST_ATTRIBUTE_NAME = "_csrf";

  @Context
  private HttpServletRequest request;

  @Context
  private HttpServletResponse response;

  private final CsrfConfigurationProvider csrfProtectionDefinitionProvider;

  public CsrfTokenService( CsrfConfigurationProvider csrfProtectionDefinitionProvider ) {
    if ( csrfProtectionDefinitionProvider == null ) {
      throw new IllegalArgumentException( "csrfProtectionDefinitionProvider" );
    }

    this.csrfProtectionDefinitionProvider = csrfProtectionDefinitionProvider;
  }

  @GET
  @Path( "/" )
  public Response getCsrfHeaders() {
    Response.ResponseBuilder responseBuilder = Response.noContent();

    // Spring's CsrfFilter should be setup to run before,
    // and should have placed a token in this attribute.
    // When CSRF is disabled, the attribute will not have been set.
    final CsrfToken token = (CsrfToken) request.getAttribute( REQUEST_ATTRIBUTE_NAME );
    if ( token != null ) {
      final String tokenHeaderName = token.getHeaderName();
      responseBuilder.header( org.pentaho.web.security.csrf.CsrfTokenService.RESPONSE_HEADER_HEADER, tokenHeaderName );

      final String tokenParameterName = token.getParameterName();
      responseBuilder.header( org.pentaho.web.security.csrf.CsrfTokenService.RESPONSE_HEADER_PARAM, tokenParameterName );

      final String tokenValue = token.getToken();
      responseBuilder.header( org.pentaho.web.security.csrf.CsrfTokenService.RESPONSE_HEADER_TOKEN, tokenValue );
    }

    // Maybe add CORS headers, depending on the origin being trusted (local or external) or not (an attacker).
    CsrfUtil.setCorsResponseHeaders( request, response, csrfProtectionDefinitionProvider.getCorsAllowOrigins() );

    return responseBuilder.build();
  }
}
