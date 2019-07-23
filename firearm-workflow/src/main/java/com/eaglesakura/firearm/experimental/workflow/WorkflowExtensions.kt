package com.eaglesakura.firearm.experimental.workflow

/**
 * Require all module load.
 */
fun workflowRequiredModule(obj: Any, vararg objects: Any) {

    val checks = mutableSetOf(obj)
    checks.addAll(objects)

    require(checks.size == (objects.size + 1)) {
        "illegal load objects, $obj, extra='$objects'"
    }
}