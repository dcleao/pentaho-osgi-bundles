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

package org.hitachivantara.security.web.impl.model.csrf;

import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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
@Path( "/csrf/token" )
public class CsrfTokenService {
  /**
   * The name of the {@link javax.ws.rs.core.Request} attribute where {@link CsrfGateFilter} places the {@link
   * CsrfToken} instance.
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

  public CsrfTokenService() {
  }

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
