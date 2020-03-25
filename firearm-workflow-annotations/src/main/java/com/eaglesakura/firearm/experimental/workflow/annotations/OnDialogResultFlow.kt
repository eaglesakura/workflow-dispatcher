package com.eaglesakura.firearm.experimental.workflow.annotations

/**
 *  Annotation to startActivityForResult:onActivityResult flow.
 *
 *  e.g.)
 *  @ WorkflowOwner
 *  class ExampleFlow {
 *      fun startFlow() {
 *          val dialogFactory = AlertDialogFactory.Builder() {
 *              // setup
 *          }.build()
 *          exampleDialogFlow(dialogFactory) // call generated entry-point.
 *      }
 *
 *      @UiThread
 *      @OnDialogResultFlow("exampleDialogFlow")
 *      fun onExampleDialogFlowResult(result: DialogResult, savedFlowState: Bundle?) {
 *          // do something
 *      }
 *  }
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class OnDialogResultFlow(
    /**
     *  Generate entry-point function name.
     */
    val entryPointName: String
)