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
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.model.cors;

import org.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration;
import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class is a POJO implementation of the
 * {@link org.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration}
 * interface.
 * <p>
 * Additionally, this implementation is proxy-safe.
 */
public class CorsRequestSetConfigurationPojo implements CorsRequestSetConfiguration {

  @Nonnull
  public static final CorsRequestSetConfiguration DISABLED = new CorsRequestSetConfigurationPojo( false );

  @Nullable
  protected String name;

  @Nullable
  protected String parentName;

  @Nonnull
  protected RequestMatcher requestMatcher;

  protected boolean isEnabled;

  protected boolean isAbstract;

  @Nullable
  protected Set<String> allowedOrigins;

  @Nullable
  protected Set<String> allowedMethods;

  @Nullable
  protected Set<String> allowedHeaders;

  @Nullable
  protected Set<String> exposedHeaders;

  @Nullable
  protected Boolean allowCredentials;

  @Nullable
  protected Long maxAge;

  /**
   * Creates a concrete and enabled CORS request set configuration, for an empty set of requests.
   */
  public CorsRequestSetConfigurationPojo() {
    this( true );
  }

  /**
   * Creates a concrete CORS request set configuration, for an empty set of requests and a given enabled status.
   *
   * @param isEnabled The enabled status.
   */
  public CorsRequestSetConfigurationPojo( boolean isEnabled ) {
    this.isEnabled = isEnabled;
    this.requestMatcher = RequestMatcher.NONE;
  }

  /**
   * Creates a CORS request set configuration based on another given configuration.
   *
   * @param other The other CORS request set configuration.
   */
  public CorsRequestSetConfigurationPojo( @Nonnull CorsRequestSetConfiguration other ) {

    Objects.requireNonNull( other );

    this.name = other.getName();
    this.parentName = other.getParentName();
    this.isEnabled = other.isEnabled();
    this.isAbstract = other.isAbstract();
    this.requestMatcher = other.getRequestMatcher();
    this.allowedOrigins = copySet( other.getAllowedOrigins() );
    this.allowedHeaders = copySet( other.getAllowedHeaders() );
    this.allowedMethods = copySet( other.getAllowedMethods() );
    this.exposedHeaders = copySet( other.getExposedHeaders() );

    // Boolean and Long are immutable.
    this.allowCredentials = other.getAllowCredentials();
    this.maxAge = other.getMaxAge();
  }

  @Nullable
  private Set<String> copySet( @Nullable Set<String> value ) {
    return value != null ? new HashSet<>( value ) : null;
  }


  /**
   * @inheritDoc
   */
  @Nullable @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the request set.
   *
   * @param name The name of the request set.
   */
  public void setName( @Nullable String name ) {
    this.name = name;
  }

  /**
   * @inheritDoc
   */
  @Nullable @Override
  public String getParentName() {
    return parentName;
  }

  /**
   * Sets the name of the parent request set.
   *
   * @param parentName The name of the parent request set.
   */
  public void setParentName( @Nullable String parentName ) {
    this.parentName = parentName;
  }

  /**
   * @inheritDoc
   */
  @Nonnull @Override
  public RequestMatcher getRequestMatcher() {
    return requestMatcher;
  }

  /**
   * Sets the request matcher that identifies the requests included in this set.
   *
   * @param requestMatcher The request matcher; if {@code null}, defaults to {@link RequestMatcher#ALL}.
   */
  public void setRequestMatcher( @Nullable RequestMatcher requestMatcher ) {
    this.requestMatcher = requestMatcher != null ? requestMatcher : RequestMatcher.NONE;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean isEnabled() {
    return isEnabled;
  }

  /**
   * Sets the enabled status of this CORS configuration.
   *
   * @param isEnabled Indicates if the configuration is enabled.
   */
  public void setEnabled( boolean isEnabled ) {
    this.isEnabled = isEnabled;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean isAbstract() {
    return isAbstract;
  }

  /**
   * Sets the abstract nature of this CORS configuration.
   *
   * @param isAbstract Indicates if the configuration is abstract.
   */
  public void setAbstract( boolean isAbstract ) {
    this.isAbstract = isAbstract;
  }

  /**
   * @inheritDoc
   */
  @Nullable @Override
  public Set<String> getAllowedOrigins() {
    return allowedOrigins;
  }

  /**
   * Sets the additional origins allowed by the requests of this set.
   *
   * @param allowedOrigins The local set of allowed origins.
   */
  public void setAllowedOrigins( @Nullable Set<String> allowedOrigins ) {
    this.allowedOrigins = allowedOrigins;
  }

  /**
   * @inheritDoc
   */
  @Nullable @Override
  public Set<String> getAllowedMethods() {
    return allowedMethods;
  }

  /**
   * Sets the additional methods allowed by the requests of this set.
   *
   * @param allowedMethods The local set of allowed methods.
   */
  public void setAllowedMethods( @Nullable Set<String> allowedMethods ) {
    this.allowedMethods = allowedMethods;
  }

  /**
   * @inheritDoc
   */
  @Nullable @Override
  public Set<String> getAllowedHeaders() {
    return allowedHeaders;
  }

  /**
   * Sets the additional headers allowed by requests of this set.
   *
   * @param allowedHeaders The local set of allowed request headers.
   */
  public void setAllowedHeaders( @Nullable Set<String> allowedHeaders ) {
    this.allowedHeaders = allowedHeaders;
  }

  /**
   * @inheritDoc
   */
  @Nullable @Override
  public Boolean getAllowCredentials() {
    return allowCredentials;
  }

  /**
   * Sets whether CORS requests of this set are allowed to include credentials.
   *
   * @param allowCredentials {@code true} if allowed; {@code false}, if not allowed; {@code null} if not set.
   */
  public void setAllowCredentials( @Nullable Boolean allowCredentials ) {
    this.allowCredentials = allowCredentials;
  }

  /**
   * @inheritDoc
   */
  @Nullable @Override
  public Long getMaxAge() {
    return maxAge;
  }

  /**
   * Sets the maximum time, in seconds, that the response of a pre-flight request can be cached by the browser.
   *
   * @param maxAge The maximum age.
   */
  public void setMaxAge( @Nullable Long maxAge ) {
    this.maxAge = maxAge;
  }

  /**
   * @inheritDoc
   */
  @Nullable @Override
  public Set<String> getExposedHeaders() {
    return exposedHeaders;
  }

  /**
   * Sets the additional set of headers of an actual response that can be read by the browser scripting environment.
   *
   * @param exposedHeaders The local set of exposed headers.
   */
  public void setExposedHeaders( @Nullable Set<String> exposedHeaders ) {
    this.exposedHeaders = exposedHeaders;
  }

  // Proxy-safe implementation of #equals. Uses instanceof and getters, instead of fields.
  // This is important for the implementation of AggregatedRequestSetCorsConfiguration#requestSetConfigurationWillUnbind,
  // to be able to remove a proxy from a list...
  @Override
  public boolean equals( Object other ) {
    if ( this == other ) {
      return true;
    }
    if ( !( other instanceof CorsRequestSetConfiguration ) ) {
      return false;
    }

    CorsRequestSetConfiguration that = (CorsRequestSetConfiguration) other;
    return isEnabled() == that.isEnabled()
      && isAbstract() == that.isAbstract()
      && Objects.equals( getName(), that.getName() )
      && Objects.equals( getParentName(), that.getParentName() )
      && getRequestMatcher().equals( that.getRequestMatcher() )
      && Objects.equals( getAllowedOrigins(), that.getAllowedOrigins() )
      && Objects.equals( getAllowedMethods(), that.getAllowedMethods() )
      && Objects.equals( getAllowedHeaders(), that.getAllowedHeaders() )
      && Objects.equals( getExposedHeaders(), that.getExposedHeaders() )
      && Objects.equals( getAllowCredentials(), that.getAllowCredentials() )
      && Objects.equals( getMaxAge(), that.getMaxAge() );
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      getName(),
      getParentName(),
      getRequestMatcher(),
      isEnabled(),
      isAbstract(),
      getAllowedOrigins(),
      getAllowedMethods(),
      getAllowedHeaders(),
      getExposedHeaders(),
      getAllowCredentials(),
      getMaxAge() );
  }
}
