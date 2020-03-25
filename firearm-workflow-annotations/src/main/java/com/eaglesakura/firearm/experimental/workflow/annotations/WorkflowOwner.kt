package com.eaglesakura.firearm.experimental.workflow.annotations

/**
 *  Annotation to startActivityForResult:onActivityResult flow.
 *
 *  e.g.)
 *  @ WorkflowOwner
 *  class ExampleFlow {
 *      fun startFlow() {
 *          val intent: Intent = ...
 *          exampleActivityFlow(intent) // call generated entry-point.
 *      }
 *
 *      @UiThread
 *      @OnActivityResultFlow("exampleActivityFlow")
 *      fun onExampleActivityFlowResult(result: Int, data: Intent?, savedFlowState: Bundle?) {
 *          // do something
 *      }
 *  }
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class WorkflowOwner