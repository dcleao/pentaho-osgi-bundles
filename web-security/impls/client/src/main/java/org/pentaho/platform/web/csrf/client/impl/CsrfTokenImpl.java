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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.csrf.client.impl;

import org.pentaho.platform.web.csrf.client.CsrfToken;

/**
 * The `CsrfToken` class is a basic implementation of the `ICsrfToken` interface.
 */
public class CsrfTokenImpl implements CsrfToken {

  private String header;
  private String parameter;
  private String token;

  CsrfTokenImpl( String header, String parameter, String token ) {
    this.header = header;
    this.parameter = parameter;
    this.token = token;
  }

  /** @inheritDoc */
  public String getHeader() {
    return this.header;
  }

  /** @inheritDoc */
  public String getParameter() {
    return this.parameter;
  }

  /** @inheritDoc */
  public String getToken() {
    return this.token;
  }
}
