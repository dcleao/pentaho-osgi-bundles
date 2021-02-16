/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2019-2021 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.service.csrf.jaxrs;

import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * This REST resource represents the CSRF token that should be used to request a specified URL, under a certain user
 * session.
 * <p>
 * This resource is designed to work with Sprint's {@link org.springframework.security.web.csrf.CsrfFilter} filter. The
 * filter must be applied to this resource. However, the resource itself must not be protected against CSRF.
 * Additionally, the resource should not require authentication, so that the login operation itself can be secured.
 * <p>
 * When {@code CsrfFilter} receives a request which is <em>not</em> subject to protection, and for which an applicable
 * CSRF token is not present in the filter's associated
 * {@link org.springframework.security.web.csrf.CsrfTokenRepository}
 * (typically, mapping to the user session), it creates and stores an applicable token.
 * <p>
 * For non-rejected requests, the filter also makes the currently applicable CSRF token available in a request attribute
 * named {@code "_csrf"}, which can be obtained by {@link javax.servlet.ServletRequest#getAttribute(String)}. From this
 * location, the token can be read for generating pages which embed the token.
 * <p>
 * This resource reads the CSRF token as placed by the filter in the {@code "_csrf"} request attribute and responds with
 * that information. The CSRF token must then be included in a following request, to the protected service specified to
 * this resource in the {@code url} query parameter, and in the same user session, so that the request to the protected
 * service is accepted.
 */
@Path( "/csrf/token" )
public class CsrfTokenService {
  /**
   * The name of the {@link javax.servlet.ServletRequest} attribute where
   * {@link org.springframework.security.web.csrf.CsrfFilter}
   * places the {@link CsrfToken} instance.
   */
  static final String REQUEST_ATTRIBUTE_NAME = "_csrf";

  /**
   * The name of the query parameter on which to specify the URL of the protected service which is to be called.
   */
  static final String QUERY_PARAM_URL = "url";

  /**
   * The name of the response header whose value is the name of the request header on which the value of the CSRF token
   * should be sent on requests to the protected service.
   */
  static final String RESPONSE_HEADER_HEADER = "X-CSRF-HEADER";

  /**
   * The name of the response header whose value is the name of the request parameter on which the value of the CSRF
   * token should be sent on requests to the protected service.
   * <p>
   * It is preferable to use a request header to send the CSRF token, if possible.
   */
  static final String RESPONSE_HEADER_PARAM = "X-CSRF-PARAM";

  /**
   * The name of the response header whose value is the CSRF token.
   */
  static final String RESPONSE_HEADER_TOKEN = "X-CSRF-TOKEN";

  @Context
  HttpServletRequest request;

  @GET
  @Path( "/" )
  public Response getToken( @QueryParam( QUERY_PARAM_URL ) String url ) {

    Response.ResponseBuilder responseBuilder = Response.noContent();

    // Spring's CsrfFilter should be setup to run before, and should have placed a token in this attribute.
    // When CSRF is disabled, the attribute will not have been set.
    CsrfToken token = (CsrfToken) request.getAttribute( REQUEST_ATTRIBUTE_NAME );
    if ( token != null ) {
      String tokenHeaderName = token.getHeaderName();
      responseBuilder.header( RESPONSE_HEADER_HEADER, tokenHeaderName );

      String tokenParameterName = token.getParameterName();
      responseBuilder.header( RESPONSE_HEADER_PARAM, tokenParameterName );

      String tokenValue = token.getToken();
      responseBuilder.header( RESPONSE_HEADER_TOKEN, tokenValue );
    }

    return responseBuilder.build();
  }
}
