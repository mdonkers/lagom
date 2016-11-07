/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.client

import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.internal.scaladsl.client.{ ScaladslClientMacroImpl, ScaladslServiceClient, ScaladslServiceResolver, ScaladslWebSocketClient }
import com.lightbend.lagom.scaladsl.api.deser.{ DefaultExceptionSerializer, ExceptionSerializer }
import play.api.Environment
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.language.experimental.macros

/**
 * The Lagom service client implementor.
 *
 * Instances of this must also implement [[ServiceClientConstructor]], so that the `implementClient` macro can
 * generate code that constructs the service client.
 */
trait ServiceClient { self: ServiceClientConstructor =>

  /**
   * Implement a client for the given service descriptor.
   */
  def implement[S <: Service]: S = macro ScaladslClientMacroImpl.implementClient[S]
}

/**
 * Lagom service client constructor.
 *
 * This API should not be used directly, it will be invoked by the client generated by [[ServiceClient.implement]] in
 * order to construct the client and obtain the dependencies necessary for the client to operate.
 *
 * The reason for a separation between this interface and [[ServiceClient]] is so that the [[#construct]] method
 * doesn't appear on the user facing [[ServiceClient]] API. The macro it generates will cast the [[ServiceClient]] to
 * a [[ServiceClientConstructor]] in order to invoke it.
 *
 * Although this API should not directly be used by end users, the code generated by the [[ServiceClient]] macro does
 * cause end users to have a binary dependency on this class, which is why it's in the `scaladsl` package.
 */
trait ServiceClientConstructor extends ServiceClient {

  /**
   * Construct a service client, by invoking the passed in function that takes the implementation context.
   */
  def construct[S <: Service](constructor: ServiceClientImplementationContext => S): S
}

/**
 * The service client implementation context.
 *
 * This API should not be used directly, it will be invoked by the client generated by [[ServiceClient.implement]] in
 * order to resolve the service descriptor.
 *
 * The purpose of this API is to capture the dependencies required in order to implement a service client, such as the
 * HTTP and WebSocket clients.
 *
 * Although this API should not directly be used by end users, the code generated by the [[ServiceClient]] macro does
 * cause end users to have a binary dependency on this class, which is why it's in the `scaladsl` package.
 */
trait ServiceClientImplementationContext {

  /**
   * Resolve the given descriptor to a service client context.
   */
  def resolve(descriptor: Descriptor): ServiceClientContext
}

/**
 * The service client context.
 *
 * This API should not be used directly, it will be invoked by the client generated by [[ServiceClient.implement]] in
 * order to implement each service call and topic.
 *
 * The service client context is essentially a map of service calls and topics, constructed from a service descriptor,
 * that allows a [[ServiceCall]] to be easily constructed by the services methods.
 *
 * Although this API should not directly be used by end users, the code generated by the [[ServiceClient]] macro does
 * cause end users to have a binary dependency on this class, which is why it's in the `scaladsl` package.
 */
trait ServiceClientContext {
  /**
   * Create a service call for the given method name and passed in parameters.
   */
  def createServiceCall[Request, Response](methodName: String, params: immutable.Seq[Any]): ServiceCall[Request, Response]
}

trait ServiceResolver {
  def resolve(descriptor: Descriptor): Descriptor
}

trait LagomServiceClientComponents {
  def wsClient: WSClient
  def serviceInfo: ServiceInfo
  def serviceLocator: ServiceLocator
  def materializer: Materializer
  def executionContext: ExecutionContext
  def environment: Environment
  def applicationLifecycle: ApplicationLifecycle

  lazy val serviceResolver: ServiceResolver = new ScaladslServiceResolver(defaultExceptionSerializer)
  lazy val defaultExceptionSerializer: ExceptionSerializer = new DefaultExceptionSerializer(environment)
  lazy val scaladslWebSocketClient: ScaladslWebSocketClient = new ScaladslWebSocketClient(environment, applicationLifecycle)(executionContext)
  lazy val serviceClient: ServiceClient = new ScaladslServiceClient(wsClient, scaladslWebSocketClient, serviceInfo,
    serviceLocator: ServiceLocator, serviceResolver)(executionContext, materializer)
}