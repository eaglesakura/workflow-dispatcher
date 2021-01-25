package com.eaglesakura.workflowdispatcher

/**
 *  Annotation to startActivityForResult:onActivityResult flow.
 *
 *  e.g.)
 *  class ExampleFlow : Fragment() {
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
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class OnActivityResultFlow(
    /**
     *  Generate entry-point function name.
     */
    val entryPointName: String
)
