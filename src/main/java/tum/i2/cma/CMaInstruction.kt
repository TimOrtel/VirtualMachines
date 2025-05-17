package tum.i2.cma

import tum.i2.cma.CMaInstructionType.Companion.expectedNumberOfArguments

class CMaInstruction(val type: CMaInstructionType, val args: IntArray) {
    val firstArg: Int
        get() = args[0]

    fun hasRightNumberOfArguments(): Boolean {
        return when (type) {
            CMaMiscInstruction.POP -> args.size <= 1
            else -> args.size == expectedNumberOfArguments(this.type)
        }
    }
}
