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

package org.hitachivantara.security.web.impl.model.csrf;

import org.hitachivantara.security.web.api.model.csrf.CsrfConfiguration;
import org.hitachivantara.security.web.api.model.csrf.CsrfRequestSetConfiguration;
import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;
import org.hitachivantara.security.web.impl.model.matcher.OrRequestMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements a {@link CsrfConfiguration} which determines the requests which are protected by aggregating
 * {@link CsrfRequestSetConfiguration} instances.
 * <p>
 * The implementation is thread-safe and is designed to be used in Spring or Blueprint containers.
 * <p>
 * Specifically, regarding
 * <a href="https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.blueprint.html">Blueprint
 * containers</a>, the {@link #requestSetConfigurationDidBind(CsrfRequestSetConfiguration)} and {@link
 * #requestSetConfigurationWillUnbind(CsrfRequestSetConfiguration)} methods can be used as a reference listener's {@code
 * bind-method} and {@code unbind-method}, respectively.
 *
 * @see <a href="https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.blueprint.html#i2985507">Blueprint
 * <code>reference-listener</code></a>.
 */
public final class AggregatedRequestSetCsrfConfiguration implements CsrfConfiguration {

  private static final CsrfConfiguration ALL_ENABLED_CONFIG =
    new SimpleCsrfConfiguration( true, RequestMatcher.ALL );

  @Nonnull
  private CsrfConfiguration compiledConfiguration;

  @Nullable
  private List<CsrfRequestSetConfiguration> protectedRequestSetConfigs;

  public AggregatedRequestSetCsrfConfiguration() {
    this.compiledConfiguration = ALL_ENABLED_CONFIG;
    this.protectedRequestSetConfigs = null;
  }

  @Override
  public boolean isEnabled() {
    return compiledConfiguration.isEnabled();
  }

  @Override
  public boolean isEnabled( @Nonnull HttpServletRequest request ) {
    return compiledConfiguration.isEnabled( request );
  }

  /**
   * Sets a value that indicates if protection is globally enabled.
   *
   * @param isEnabled Indicates if protection is enabled. {@code true} if enabled; {@code false}, otherwise.
   */
  public final synchronized void setEnabled( boolean isEnabled ) {
    if ( isEnabled != this.isEnabled() ) {
      this.compileConfiguration( isEnabled );
    }
  }

  /**
   * Sets the list of protected request set configurations.
   *
   * @param protectedRequestSetConfigs The list of protected request set configurations. If {@code null}, then all
   *                                   requests are protected. If an empty list, then no requests are protected.
   */
  public final synchronized void setRequestSetConfigurations(
    @Nullable List<CsrfRequestSetConfiguration> protectedRequestSetConfigs ) {

    this.protectedRequestSetConfigs = protectedRequestSetConfigs;

    this.compileConfiguration( copyProtectedRequestConfigs() );
  }

  /**
   * Called to inform that the current list of protected request configurations has changed, and, specifically, that the
   * specified request configuration has been added or changed.
   *
   * @param requestSetConfig The protected request configuration that was added or changed (mutated).
   */
  public final synchronized void requestSetConfigurationDidBind(
    @Nonnull CsrfRequestSetConfiguration requestSetConfig ) {
    compileConfiguration( copyProtectedRequestConfigs() );
  }

  /**
   * Called to inform that the current list of protected request configurations has changed, and, specifically, that the
   * specified request configuration <em>will</em> be removed from the list.
   *
   * @param requestSetConfig The protected request configuration that <em>will</em> be removed.
   */
  public final synchronized void requestSetConfigurationWillUnbind(
    @Nonnull CsrfRequestSetConfiguration requestSetConfig ) {

    List<CsrfRequestSetConfiguration> protectedRequestConfigsCopy = copyProtectedRequestConfigs();
    if ( protectedRequestConfigsCopy != null ) {
      protectedRequestConfigsCopy.remove( requestSetConfig );
    }

    compileConfiguration( protectedRequestConfigsCopy );
  }

  private void compileConfiguration( boolean isEnabled ) {
    compileConfiguration( isEnabled, copyProtectedRequestConfigs() );
  }

  private void compileConfiguration( @Nullable List<CsrfRequestSetConfiguration> protectedRequestSetConfigs ) {
    compileConfiguration( isEnabled(), protectedRequestSetConfigs );
  }

  private void compileConfiguration( boolean isEnabled,
                                     @Nullable List<CsrfRequestSetConfiguration> protectedRequestSetConfigs ) {

    compiledConfiguration = new SimpleCsrfConfiguration(
      isEnabled,
      createOrRequestMatcher( isEnabled, protectedRequestSetConfigs ) );
  }

  @Nullable
  private List<CsrfRequestSetConfiguration> copyProtectedRequestConfigs() {
    // Defensive variable.
    List<CsrfRequestSetConfiguration> protectedRequestConfigsLocal = protectedRequestSetConfigs;
    if ( protectedRequestConfigsLocal == null ) {
      return null;
    }

    // List is mutable, so create a defensive copy of the list.

    return Arrays.asList(
      protectedRequestConfigsLocal.toArray( new CsrfRequestSetConfiguration[ 0 ] ) );
  }

  @Nonnull
  private RequestMatcher createOrRequestMatcher( boolean isEnabled,
                                                 @Nullable List<CsrfRequestSetConfiguration> requestSetConfigs ) {

    if ( !isEnabled ) {
      return RequestMatcher.NONE;
    }

    if ( requestSetConfigs == null ) {
      return RequestMatcher.ALL;
    }

    // Collect the underlying RequestMatchers.
    List<RequestMatcher> requestMatchers = requestSetConfigs.stream()
      .map( CsrfRequestSetConfiguration::getRequestMatcher )
      .collect( Collectors.toList() );

    return OrRequestMatcher.create( requestMatchers );
  }

  /**
   * This class is an immutable implementation of the {@link CsrfConfiguration} interface, which evaluates the requests
   * that require CSRF protection based on a given request matcher.
   */
  private static class SimpleCsrfConfiguration implements CsrfConfiguration {

    private final boolean isEnabled;

    @Nonnull
    private final RequestMatcher requestMatcher;

    /**
     * Creates a CSRF configuration with a given enabled status and for a given request matcher.
     *
     * @param isEnabled      Indicates if CSRF protection is enabled for the requests identified by {@code
     *                       requestMatcher}.
     * @param requestMatcher The request matcher which identifies protected requests.
     */
    public SimpleCsrfConfiguration( boolean isEnabled, RequestMatcher requestMatcher ) {
      this.isEnabled = isEnabled;
      this.requestMatcher = requestMatcher != null ? requestMatcher : RequestMatcher.NONE;
    }

    /**
     * @inheritDoc
     */
    @Override
    public final boolean isEnabled() {
      return isEnabled;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isEnabled( @Nonnull HttpServletRequest request ) {
      return requestMatcher.test( request );
    }
  }
}
