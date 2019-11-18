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

package org.pentaho.csrf;

/**
 * This interface represents an HTTP GET service which,
 * given a URL for a protected service in the query parameter {@code "url"},
 * responds with a {@code 204} HTTP status code (empty body)
 * and with three headers, containing the CSRF token value itself
 * and the names of the request header or request parameter on which to send the token
 * for requests to the protected service.
 */
public interface ICsrfService {
  /**
   * The name of the query parameter on which to send the URL of the protected service
   * which is to be called.
   */
  String QUERY_PARAM_URL = "url";

  /**
   * The name of the response header whose value is the name of
   * the request header on which the value of the CSRF token should be sent
   * on requests to the protected service.
   */
  String RESPONSE_HEADER_HEADER = "X-CSRF-HEADER";

  /**
   * The name of the response header whose value is the name of
   * the request parameter on which the value of the CSRF token should be sent
   * on requests to the protected service.
   *
   * It is preferable to use a request header to send the CSRF token, if possible.
   */
  String RESPONSE_HEADER_PARAM = "X-CSRF-PARAM";

  /**
   * The name of the response header whose value is the CSRF token.
   */
  String RESPONSE_HEADER_TOKEN = "X-CSRF-TOKEN";
}
