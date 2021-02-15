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

import org.hitachivantara.security.web.api.model.cors.CorsConfiguration;
import org.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AggregatedRequestSetCorsConfiguration implements CorsConfiguration {

  private static final Logger logger = LoggerFactory.getLogger( AggregatedRequestSetCorsConfiguration.class );

  @Nonnull
  private CompiledCorsRequestSetConfigurationPojo compiledRequestSetConfig;

  @Nullable
  private List<CorsRequestSetConfiguration> requestSetConfigs;

  public AggregatedRequestSetCorsConfiguration() {
    compiledRequestSetConfig = CompiledCorsRequestSetConfigurationPojo.DISABLED;
    requestSetConfigs = null;
  }

  @Nonnull @Override
  public CorsRequestSetConfiguration getRootConfiguration() {
    return compiledRequestSetConfig;
  }

  @Nullable @Override
  public CorsRequestSetConfiguration getRequestConfiguration( @Nonnull HttpServletRequest request ) {
    return compiledRequestSetConfig.getRequestConfiguration( request );
  }

  /**
   * Sets the list of request set configurations.
   *
   * @param requestSetConfigs The list of request set configurations. If {@code null} or an empty list, then CORS is
   *                          disabled.
   */
  public final synchronized void setRequestSetConfigurations(
    @Nullable List<CorsRequestSetConfiguration> requestSetConfigs ) {

    this.requestSetConfigs = requestSetConfigs;

    this.compileConfiguration( copyProtectedRequestConfigs() );
  }

  /**
   * Called to inform that the current list of request configurations has changed, and, specifically, that the specified
   * request configuration has been added or changed.
   *
   * @param requestSetConfig The request configuration that was added or changed (mutated).
   */
  public final synchronized void requestSetConfigurationDidBind(
    @Nonnull CorsRequestSetConfiguration requestSetConfig ) {

    compileConfiguration( copyProtectedRequestConfigs() );
  }

  /**
   * Called to inform that the current list of request configurations has changed, and, specifically, that the specified
   * request configuration <em>will</em> be removed from the list.
   *
   * @param requestSetConfig The request configuration that <em>will</em> be removed.
   */
  public final synchronized void requestSetConfigurationWillUnbind(
    @Nonnull CorsRequestSetConfiguration requestSetConfig ) {

    List<CorsRequestSetConfiguration> protectedRequestConfigsCopy = copyProtectedRequestConfigs();
    if ( protectedRequestConfigsCopy != null ) {
      protectedRequestConfigsCopy.remove( requestSetConfig );
    }

    compileConfiguration( protectedRequestConfigsCopy );
  }

  private void compileConfiguration( @Nullable List<CorsRequestSetConfiguration> requestSetConfigsCopy ) {
    compiledRequestSetConfig = new CorsRequestSetCompilation( requestSetConfigsCopy ).compile();
  }

  @Nullable
  private List<CorsRequestSetConfiguration> copyProtectedRequestConfigs() {
    // Defensive variable.
    List<CorsRequestSetConfiguration> requestConfigsLocal = requestSetConfigs;
    if ( requestConfigsLocal == null ) {
      return null;
    }

    // List is mutable, so create a defensive copy of the list.

    return Arrays.asList( requestConfigsLocal.toArray( new CorsRequestSetConfiguration[ 0 ] ) );
  }

  private static class CorsRequestSetCompilation {
    @Nullable
    private final List<CorsRequestSetConfiguration> requestSetConfigs;

    @Nonnull
    private Map<String, CompiledCorsRequestSetConfigurationPojo> namedConfigsByName;

    @Nonnull
    private List<CompiledCorsRequestSetConfigurationPojo> configs;

    public CorsRequestSetCompilation( @Nullable List<CorsRequestSetConfiguration> requestSetConfigs ) {
      this.requestSetConfigs = requestSetConfigs;
      this.namedConfigsByName = Collections.emptyMap();
      this.configs = Collections.emptyList();
    }

    @Nonnull
    public CompiledCorsRequestSetConfigurationPojo compile() {

      if ( requestSetConfigs == null || requestSetConfigs.isEmpty() ) {
        return CompiledCorsRequestSetConfigurationPojo.DISABLED;
      }

      namedConfigsByName = new HashMap<>();
      configs = new ArrayList<>( requestSetConfigs.size() );

      // Phase 1.
      createCompiledCopiesAndIndexByName();

      // Phase 2.
      CompiledCorsRequestSetConfigurationPojo root = checkExistenceOfRootConfiguration();
      if ( root == null ) {
        return CompiledCorsRequestSetConfigurationPojo.DISABLED;
      }

      // Phase 3.
      buildTree();

      // Phase 4.
      // Special case in which all configs were invalid and removed (e.g. cycle including root).
      if ( configs.isEmpty() ) {
        return CompiledCorsRequestSetConfigurationPojo.DISABLED;
      }

      // Phase 5.
      root.propagateEffectiveConfig( null );

      return root;
    }


    private void createCompiledCopiesAndIndexByName() {

      assert requestSetConfigs != null;

      for ( CorsRequestSetConfiguration requestSetConfig : requestSetConfigs ) {
        CompiledCorsRequestSetConfigurationPojo config =
          new CompiledCorsRequestSetConfigurationPojo( requestSetConfig );
        String name = config.getName();
        if ( name != null ) {
          if ( namedConfigsByName.containsKey( name ) ) {
            logger.warn(
              "Found same named {}. Ignoring duplicate configuration {}.",
              namedConfigsByName.get( name ),
              config );
            continue;
          }

          namedConfigsByName.put( name, config );
        }

        configs.add( config );
      }
    }

    private CompiledCorsRequestSetConfigurationPojo checkExistenceOfRootConfiguration() {
      CompiledCorsRequestSetConfigurationPojo config = namedConfigsByName.get( CorsRequestSetConfiguration.ROOT_NAME );
      if ( config == null ) {
        logger.warn( "There is no root CORS request set configuration. Assuming CORS disabled." );
      }

      return config;
    }


    private void buildTree() {

      List<CompiledCorsRequestSetConfigurationPojo> invalidConfigs = new ArrayList<>();

      // Validate existence of parents.
      // Filter out orphaned or cyclic children.
      // Add to existing parents.
      configs = configs.stream().filter( config -> {
        String parentName = config.getParentName();
        if ( parentName != null ) {
          CompiledCorsRequestSetConfigurationPojo parent = namedConfigsByName.get( parentName );
          if ( parent == null ) {
            logger.warn(
              "{} references undefined parent {}. "
                + "Ignoring configuration and all descendant configurations.",
              config,
              parentName );

            invalidConfigs.add( config );
            return false;
          }

          // Add child to parent.
          if ( !parent.addChild( config ) ) {
            // Child C is already an **ancestor** of Parent P.
            // Adding C as child of P would create a cycle and a separate ring of configs:
            //  C > D > E > P -?-> C
            // All configs below C must be removed. This includes the Parent...
            // A special case is config === parent...
            logger.warn(
              "{} would create a cycle if added to parent {}."
                + "Ignoring configuration and all descendant configurations.",
              config,
              parent );

            invalidConfigs.add( config );
            return false;
          }
        }

        // valid
        return true;
      } )
        .collect( Collectors.toList() );

      // Remove invalid config sub-trees.
      if ( !invalidConfigs.isEmpty() ) {
        for ( CompiledCorsRequestSetConfigurationPojo invalidConfig : invalidConfigs ) {
          removeConfigSubtree( invalidConfig );
        }
      }
    }

    private void removeConfigSubtree( @Nonnull CompiledCorsRequestSetConfigurationPojo config ) {
      configs.remove( config );
      namedConfigsByName.remove( config.getName() );

      List<CompiledCorsRequestSetConfigurationPojo> children = config.children;
      if ( children != null ) {
        for ( CompiledCorsRequestSetConfigurationPojo child : children ) {
          removeConfigSubtree( child );
        }
      }
    }
  }

  private static class CompiledCorsRequestSetConfigurationPojo extends CorsRequestSetConfigurationPojo {

    public static final CompiledCorsRequestSetConfigurationPojo DISABLED =
      new CompiledCorsRequestSetConfigurationPojo( CorsRequestSetConfigurationPojo.DISABLED );

    @Nullable
    private List<CompiledCorsRequestSetConfigurationPojo> children;

    public CompiledCorsRequestSetConfigurationPojo( @Nonnull CorsRequestSetConfiguration other ) {

      super( other );

      if ( parentName == null ) {

        if ( !ROOT_NAME.equals( name ) ) {
          // Default parent name.
          setParentName( ROOT_NAME );
        }

      } else if ( ROOT_NAME.equals( name ) ) {
        // Root must have null parent.
        setParentName( null );
      }
    }

    public boolean addChild( @Nonnull CompiledCorsRequestSetConfigurationPojo child ) {

      if ( !checkNoCycle( child ) ) {
        return false;
      }

      if ( children == null ) {
        children = new ArrayList<>();
      }

      children.add( child );

      return true;
    }

    private boolean checkNoCycle( @Nonnull CompiledCorsRequestSetConfigurationPojo child ) {
      Set<CompiledCorsRequestSetConfigurationPojo> visited = new HashSet<>();
      visited.add( this );

      return checkNoCycle( child, visited );
    }

    private boolean checkNoCycle(
      @Nonnull CompiledCorsRequestSetConfigurationPojo config,
      @Nonnull Set<CompiledCorsRequestSetConfigurationPojo> visited ) {

      if ( visited.contains( config ) ) {
        return false;
      }

      List<CompiledCorsRequestSetConfigurationPojo> children = config.children;
      if ( children != null ) {
        visited.add( config );

        for ( CompiledCorsRequestSetConfigurationPojo child : children ) {
          if ( !checkNoCycle( child, visited ) ) {
            return false;
          }
        }

        visited.remove( config );
      }

      return true;
    }

    public void propagateEffectiveConfig( @Nullable CompiledCorsRequestSetConfigurationPojo parent ) {

      assert parent == null || parent.isEnabled;

      // No point in merging more, for our purposes.
      if ( !isEnabled ) {
        return;
      }

      if ( parent != null ) {
        this.combineWithParent( parent );
      }

      List<CompiledCorsRequestSetConfigurationPojo> children = this.children;
      if ( children != null ) {
        for ( CompiledCorsRequestSetConfigurationPojo child : children ) {
          child.propagateEffectiveConfig( this );
        }
      }
    }

    private void combineWithParent( @Nonnull CompiledCorsRequestSetConfigurationPojo parent ) {

      // isEnabled &= parent.isEnabled;

      // Request matcher is not combined.
      // These are run in sequence, every time, as part of the configuration selection process.
      // See #getRequestConfiguration(.).

      allowedOrigins = combineSetsMutateLeft( allowedOrigins, parent.allowedOrigins );

      allowedMethods = combineSetsMutateLeft( allowedMethods, parent.allowedMethods );

      allowedHeaders = combineSetsMutateLeft( allowedHeaders, parent.allowedHeaders );

      exposedHeaders = combineSetsMutateLeft( exposedHeaders, parent.exposedHeaders );

      allowCredentials = combineSinglePreferLeft( allowCredentials, parent.allowCredentials );

      maxAge = combineSinglePreferLeft( maxAge, parent.maxAge );
    }

    @Nullable
    private Set<String> combineSetsMutateLeft( @Nullable Set<String> setA, @Nullable Set<String> setB ) {
      if ( setA == null ) {
        return setB;
      }

      if ( setB == null || setA == setB ) {
        return setA;
      }

      setA.addAll( setB );
      return setA;
    }

    @Nullable
    private <T> T combineSinglePreferLeft( @Nullable T valueA, @Nullable T valueB ) {
      return valueA != null ? valueA : valueB;
    }

    @Nullable
    public CorsRequestSetConfiguration getRequestConfiguration( @Nonnull HttpServletRequest request ) {

      // N/A
      if ( !requestMatcher.test( request ) ) {
        return null;
      }

      if ( !isEnabled ) {
        return this;
      }

      List<CompiledCorsRequestSetConfigurationPojo> children = this.children;
      if ( children != null ) {
        for ( CompiledCorsRequestSetConfigurationPojo child : children ) {
          CorsRequestSetConfiguration result = child.getRequestConfiguration( request );
          if ( result != null ) {
            return result;
          }
        }
      }

      return this;
    }

    @Override
    public String toString() {
      return String.format( "CorsRequestSetConfiguration {name: %s, matcher: %s}", name, requestMatcher );
    }
  }
}
