package tum.i2.cma

import tum.i2.common.VirtualMachine

class CMa(
    private val instructions: List<CMaInstruction>
) : VirtualMachine {

    private companion object {
        private const val MEMORY_SIZE = 200
    }

    val mem = IntArray(MEMORY_SIZE)

    var pc = 0
    var sp = -1
    var np = 0

    override fun step(): Boolean {
        val ir = instructions[pc]
        pc++

        if (ir.type == CMaMiscInstruction.HALT) return false
        execute(ir)
        return pc < instructions.size
    }

    override fun run(): Int {
        while (true) {
            val cont = step()
            if (!cont) {
                return if (sp >= 0 && sp < MEMORY_SIZE) {
                    stackPeek()
                } else {
                    -1
                }
            }
        }
    }

    fun execute(instruction: CMaInstruction) {
        // CMaInstructionType enum contains comments,
        // describing where the operations are defined
        when (val type = instruction.type) {
            CMaMiscInstruction.LOADC -> {
                loadC(instruction.firstArg)
            }

            CMaMiscInstruction.LOAD -> {
                load()
            }

            CMaMiscInstruction.STORE -> {
                store()
            }

            CMaMiscInstruction.LOADA -> {
                loadC(instruction.firstArg)
                load()
            }

            CMaMiscInstruction.STOREA -> {
                loadC(instruction.firstArg)
                store()
            }

            CMaMiscInstruction.POP -> {
                stackPop()
            }

            CMaMiscInstruction.JUMP -> {
                jump(instruction.firstArg)
            }

            CMaMiscInstruction.JUMPZ -> {
                jumpz(instruction.firstArg)
            }

            CMaMiscInstruction.JUMPI -> {
                jumpi(instruction.firstArg)
            }

            CMaMiscInstruction.DUP -> {
                dup()
            }

            CMaMiscInstruction.ALLOC -> {
                stackAlloc(instruction.firstArg)
            }

            CMaMiscInstruction.NEW -> {
                new()
            }

            is CmaBinaryInstruction -> {
                binary(type)
            }

            is CmaUnaryInstruction -> {
                unary(type)
            }

            else -> throw UnsupportedOperationException("Unknown instruction type: " + instruction.type)
        }
    }

    private fun loadC(arg: Int) {
        stackPush(arg)
    }

    private fun load() {
        val address = stackPeek()
        val value = mem[address]
        stackHeadReplace(value)
    }

    private fun store() {
        val address = stackPop()
        val value = stackPeek()

        mem[address] = value
    }

    private fun dup() {
        val value = stackPeek()
        stackPush(value)
    }

    private fun binary(type: CmaBinaryInstruction) {
        val second = stackPop()
        val first = stackPeek()
        stackHeadReplace(type.op(first, second))
    }

    private fun unary(type: CmaUnaryInstruction) {
        val value = stackPeek()
        stackHeadReplace(type.op(value))
    }

    private fun jump(arg: Int) {
        pc = arg
    }

    private fun jumpz(arg: Int) {
        if (stackPop() == 0) {
            jump(arg)
        }
    }

    private fun jumpi(arg: Int) {
        val offset = stackPop()
        jump(arg + offset)
    }

    private fun stackPush(value: Int) {
        sp++
        mem[sp] = value
    }

    private fun stackHeadReplace(value: Int) {
        mem[sp] = value
    }

    private fun stackPop(): Int {
        val value = mem[sp]
        sp--
        return value
    }

    private fun stackPeek(): Int {
        return mem[sp]
    }

    private fun stackAlloc(size: Int) {
        sp += size
    }

    // HEAP

    private fun new() {
        val size = stackPeek()
        np -= size
        stackHeadReplace(np)
    }
}
