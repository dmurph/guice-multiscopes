/*******************************************************************************
 * Copyright (c) 2012, Daniel Murphy and Deanna Surma
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.protobee.guice.multiscopes.scopes;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.protobee.guice.multiscopes.Multiscope;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;

/**
 * Assisted multiscope facilitates lazy prescoped objects
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public class AssistedMultiscope extends AbstractMultiscope {

  public static class LazyScopedObject {
    private final Provider<?> provider;

    public LazyScopedObject(Provider<?> provider) {
      this.provider = provider;
    }

    public Provider<?> getProvider() {
      return provider;
    }
  }

  public AssistedMultiscope(Class<? extends Annotation> instanceAnnotation) {
    super(instanceAnnotation);
  }

  @Override
  public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
    final Object putLock = new Object();
    final Multiscope scope = this;
    return new Provider<T>() {
      @SuppressWarnings("unchecked")
      public T get() {

        Map<Key<?>, Object> scopeMap = scopeContext.get();

        if (scopeMap == null) {
          throw new OutOfScopeException("Cannot access scoped object '" + key
              + "'. This means we are not inside of a " + getName() + " scoped call.");
        }
        Object preT = scopeMap.get(key);

        if (preT == null || preT instanceof LazyScopedObject) {
          synchronized (putLock) {
            preT = scopeMap.get(key);
            if (preT == null || preT instanceof LazyScopedObject) {
              if (preT == null) {
                preT = creator.get();
              } else {
                preT = ((LazyScopedObject) preT).getProvider().get();
              }
              // TODO: for next guice release, add this check:
              // if (!Scopes.isCircularProxy(t)) {
              // Store a sentinel for provider-given null values.
              scopeMap.put(key, preT != null ? preT : NullObject.INSTANCE);
              // }
            }
          }
        }
        T t = (T) preT;

        // Accounts for @Nullable providers.
        if (NullObject.INSTANCE == t) {
          return null;
        }

        return t;
      }

      @Override
      public String toString() {
        return "{ key: " + key + ", unscopedProvider: " + creator + ", scope: " + scope + "}";
      }
    };
  }
}
