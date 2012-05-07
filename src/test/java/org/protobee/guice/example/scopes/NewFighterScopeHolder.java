package org.protobee.guice.example.scopes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.protobee.guice.ScopeHolder;

import com.google.inject.BindingAnnotation;

/**
 * Specifies a new {@link ScopeHolder} for the {@link FighterScope} (basically a new
 * {@link FighterScope} is created). Also specifies a new scope map for the
 * {@link FighterScope}.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@BindingAnnotation
public @interface NewFighterScopeHolder {}