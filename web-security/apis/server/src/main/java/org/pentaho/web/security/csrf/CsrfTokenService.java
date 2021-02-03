/*!
 *
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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.web.security.csrf;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.annotation.Nonnull;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * This interface describes the CSRF token service.
 * <p>
 * The {@link #getToken(String)} endpoint responds with the CSRF token for requesting a given CSRF protected service,
 * given by its URL.
 * <p>
 * The CSRF token is returned in response headers, so that it can only be read using JavaScript, by either pages from
 * the same site or from sites trusted via CORS.
 */
@OpenAPIDefinition(
  info = @Info(
    title = "Pentaho CSRF Token Resource API",
    version = "1.0.0"
  )
)
public interface CsrfTokenService {
  /**
   * The name of the query parameter on which to specify the URL of the protected service which is to be called.
   */
  String QUERY_PARAM_URL = "url";

  /**
   * The name of the response header whose value is the name of the request header on which the value of the CSRF token
   * should be sent on requests to the protected service.
   */
  String RESPONSE_HEADER_HEADER = "X-CSRF-HEADER";

  /**
   * The name of the response header whose value is the name of the request parameter on which the value of the CSRF
   * token should be sent on requests to the protected service.
   * <p>
   * It is preferable to use a request header to send the CSRF token, if possible.
   */
  String RESPONSE_HEADER_PARAM = "X-CSRF-PARAM";

  /**
   * The name of the response header whose value is the CSRF token.
   */
  String RESPONSE_HEADER_TOKEN = "X-CSRF-TOKEN";

  @GET
  @Path( "/" )
  @Operation(
    summary = "Gets a CSRF token for calling the protected service exposed in the given URL",
    method = HttpMethod.GET,
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Successful Response",
        headers = {
          @Header(
            name = RESPONSE_HEADER_HEADER,
            schema = @Schema( type = "string" ),
            description =
              "The name of the request header on which the CSRF token "
                + "should be sent on requests to the protected service."
          ),
          @Header(
            name = RESPONSE_HEADER_PARAM,
            schema = @Schema( type = "string" ),
            description = "The name of the request parameter on which the CSRF token "
              + "should be sent on requests to the protected service. "
              + "It is preferable to use a request header to send the CSRF token, if possible."
          ),
          @Header(
            name = RESPONSE_HEADER_TOKEN,
            schema = @Schema( type = "string" ),
            description = "The CSRF token."
          )
        }
      ),
      @ApiResponse( responseCode = "400", description = "Bad Request: Invalid Parameters" ),
      @ApiResponse( responseCode = "500", description = "Server Error" )
    }
  )
  Response getToken(
    @Nonnull
    @Parameter(
      name = QUERY_PARAM_URL,
      description = "The url of the CSRF protected service for which the CSRF token will be used",
      in = ParameterIn.QUERY,
      required = true,
      schema = @Schema( type = "string" )
    )
    @QueryParam( "url" )
      String url );
}
