/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.internal.javadsl.registry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;

import java.net.URI;

import org.immutables.value.Value;


//NOTE: The `RegisteredService.java` is generated by Immutables, but since Scaladoc is
//not running the annotation processor we had to embed the generated code.
//Do not edit `RegisteredService.java` manually, but re-generate it and copy the 
//generated source. Remove the comment @Value.Immutable at this class to
//re-generate.

//@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = RegisteredService.class)
public interface AbstractRegisteredService {

  @Value.Parameter
  String name();
  
  @Value.Parameter
  URI url();

}
